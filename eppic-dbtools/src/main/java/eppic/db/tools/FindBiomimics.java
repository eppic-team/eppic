package eppic.db.tools;

import static eppic.db.Interface.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.vecmath.GMatrix;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.core.alignment.SimpleProfile;
import org.biojava.nbio.core.alignment.SimpleSequencePair;
import org.biojava.nbio.core.alignment.template.AlignedSequence;
import org.biojava.nbio.core.alignment.template.Profile;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.structure.contact.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.db.ChainCluster;
import eppic.db.Interface;
import eppic.db.InterfaceComparator;
import eppic.db.PdbInfo;
import eppic.db.SeqClusterLevel;
import eppic.model.ChainClusterDB;
import eppic.model.InterfaceDB;
import gnu.getopt.Getopt;

public class FindBiomimics {
	private static final Logger logger = LoggerFactory.getLogger(FindBiomimics.class);
	
	private static final SeqClusterLevel DEFAULT_SEQ_CLUSTER_LEVEL = SeqClusterLevel.C100;

	private static class DirectedInterface {
		private Interface iface;
		private int direction;// Interface.FIRST or Interface.SECOND
		public DirectedInterface(Interface iface, int dir) {
			if( dir != FIRST && dir != SECOND) {
				throw new IllegalArgumentException("Illegal direction");
			}
			this.iface = iface;
			this.direction = dir;
		}
		/**
		 * @return the iface
		 */
		public Interface getInterface() {
			return iface;
		}
		/**
		 * @return the direction
		 */
		public int getDirection() {
			return direction;
		}
	}
	private static class InterfaceComparison {
		public DirectedInterface a;
		public DirectedInterface b;
	}
	
	private final List<ChainClusterDB> clusterMembers;
	private final Profile<ProteinSequence, AminoAcidCompound> profile; //Row for each chain
	

	private final List<List<DirectedInterface>> interfaces;
	
	private final GMatrix overlapMat;
	
	public FindBiomimics(String pdbCode, String chainId, DBHandler dbh, SeqClusterLevel seqClusterLevel) throws CompoundNotFoundException {
		// Sequence cluster that the query belongs to
		int repCluster = dbh.getClusterIdForPdbCodeAndChain(pdbCode, chainId, seqClusterLevel.getLevel());
		logger.info("{}.{} belongs to {}% SeqCluster {}",pdbCode, chainId, seqClusterLevel.getLevel(), repCluster);
		
		// Get list of all members of that cluster
		clusterMembers = dbh.getClusterMembers(seqClusterLevel.getLevel(), repCluster);
		
		ListIterator<ChainClusterDB> it = clusterMembers.listIterator();
		Pattern xray = Pattern.compile("x-?ray diffraction", Pattern.CASE_INSENSITIVE);
		while(it.hasNext()) {
			ChainClusterDB clust = it.next();
			// filter to proteins from xray crystals
			String expMethod = clust.getPdbInfo().getExpMethod();
			if(!clust.isProtein() || !xray.matcher(expMethod).matches())
				it.remove();
		}
		logger.info("Found {} other members",clusterMembers.size()-1);

		// build alignment of all chains
		profile = multipleAlignment(clusterMembers);
		Map<ChainClusterDB,AlignedSequence<ProteinSequence, AminoAcidCompound>> alignedSeq = new HashMap<>();
		for(int i=0;i<clusterMembers.size();i++) {
			alignedSeq.put(clusterMembers.get(i), profile.getAlignedSequence(i+1));
		}
		logger.info("Alignment:\n{}",profile);

		// Get list of all interfaces in that cluster
		interfaces = new ArrayList<>(clusterMembers.size());
		for(ChainClusterDB member : clusterMembers) {
			PdbInfo pdbInfo = new PdbInfo(member.getPdbInfo());

			// get all interfaces containing this chain
			Map<InterfaceDB, Integer> memberInterfaces = dbh.getInterfacesForChainCluster(member);
			List<DirectedInterface> clustInterfaces = new ArrayList<>(memberInterfaces.size());
			for(Entry<InterfaceDB, Integer> entry : memberInterfaces.entrySet()) {
				InterfaceDB ifaceDb = entry.getKey();
				int direction = entry.getValue();

				Interface iface = new Interface(ifaceDb, pdbInfo);
				
				// only use protein-protein interfaces
				if(! iface.getChainCluster(FIRST).getChainCluster().isProtein() ||
						! iface.getChainCluster(SECOND).getChainCluster().isProtein() ) {
					continue;
				}
				
				// Add directions. Could be both directions (3)
				if( (direction & 1) > 0) {
					clustInterfaces.add(new DirectedInterface(iface,FIRST));
				}
				if( (direction & 2) > 0) {
					clustInterfaces.add(new DirectedInterface(iface,SECOND));
				}

			}
			interfaces.add(clustInterfaces);
		}
		
		// Do pairwise comparisons
		List<DirectedInterface> flatInterfaces = getInterfaces();
		overlapMat = new GMatrix(flatInterfaces.size(),flatInterfaces.size());
		overlapMat.setZero();
		
		for(int i=1;i<interfaces.size();i++) {
			DirectedInterface di = flatInterfaces.get(i);
			ChainCluster clusti = di.getInterface().getChainCluster(di.getDirection());
			AlignedSequence<ProteinSequence, AminoAcidCompound> seqi = alignedSeq.get(clusti.getChainCluster());
			String chaini;
			if(di.direction == FIRST) {
				chaini = di.getInterface().getInterface().getChain1();
			} else {
				chaini = di.getInterface().getInterface().getChain2();
			}

			for(int j=0;j<=i;j++) {
				DirectedInterface dj = flatInterfaces.get(j);
				
				ChainCluster clustj = dj.getInterface().getChainCluster(dj.getDirection());
				AlignedSequence<ProteinSequence, AminoAcidCompound> seqj = alignedSeq.get(clustj.getChainCluster());
				String chainj;
				if(dj.direction == FIRST) {
					chainj = dj.getInterface().getInterface().getChain1();
				} else {
					chainj = dj.getInterface().getInterface().getChain2();
				}
				
				
				Pair<String> chainIds = new Pair<>(chaini,chainj);
				SequencePair<ProteinSequence, AminoAcidCompound> seqs = new SimpleSequencePair<>(seqi,seqj);
				
				Map<Pair<String>, SequencePair<ProteinSequence, AminoAcidCompound>> alnPool = Collections.singletonMap(chainIds, seqs);
				
				final boolean debug = false;
				InterfaceComparator comp = new InterfaceComparator(di.getInterface(),dj.getInterface(), alnPool, seqClusterLevel);
				comp.setDebug(debug);
				double overlap = comp.calcOverlap();
				
				overlapMat.setElement(i, j, overlap);
				overlapMat.setElement(j, i, overlap);
			}
		}
	}
	
	public List<DirectedInterface> getInterfaces() {
		List<DirectedInterface> oriented = new ArrayList<>();
		for(List<DirectedInterface> l : interfaces) {
			for(DirectedInterface i : l) {
				oriented.add(i);
			}
		}
		return oriented;
	}

	public GMatrix getOverlap() {
		return overlapMat;
	}
	public void printOverlap(PrintWriter out) {
		List<DirectedInterface> flatInterfaces = getInterfaces();
		String[] headers = new String[flatInterfaces.size()];
		
		int i=0;
		for(DirectedInterface interf : flatInterfaces) {
			InterfaceDB interfDB = interf.getInterface().getInterface();
			String name = String.format("%4s-%s%s",interfDB.getPdbCode(),interfDB.getInterfaceId(),
					interf.direction==FIRST ? ">" : "<");
			headers[i++] = name;
		}
		
		printMatrix(out,getOverlap(),headers,headers);
	}
	private static void printMatrix(PrintWriter out, GMatrix m, String[] rowheaders, String[] colheaders) {
		if( m.getNumRow() != rowheaders.length || m.getNumCol() != colheaders.length) {
			throw new IllegalArgumentException("Mismatched dimensions");
		}
		
		// determinie column widths
		int[] colwidths = new int[colheaders.length+1];
		for( int i=0;i<rowheaders.length;i++) {
			colwidths[0] = Math.max(colwidths[0], rowheaders[i].length());
		}
		for(int i=0;i<colheaders.length;i++) {
			colwidths[i+1] = colheaders[i].length();
		}
		
		// print header line
		out.format("%"+colwidths[0]+"s ","");
		for(int j=0;j<colheaders.length;j++) {
			out.format("%"+colwidths[j+1]+"s ", colheaders[j]);
		}
		out.format("%n");
		
		for(int i=0;i<rowheaders.length;i++) {
			out.format("%"+colwidths[0]+"s ",rowheaders[i]);
			for(int j=0;j<colheaders.length;j++) {
				out.format("%"+colwidths[j+1]+"f ", m.getElement(i, j));
			}
			out.format("%n");
		}
		out.flush();
	}
	
	private static Profile<ProteinSequence, AminoAcidCompound> multipleAlignment(List<ChainClusterDB> clusterMembers) throws CompoundNotFoundException {
		// degenerate case with one member
		if(clusterMembers.size() == 1) {
			ChainClusterDB clust = clusterMembers.get(0);
			String seq = clust.getPdbAlignedSeq();
			seq = stripDashes(seq);
			ProteinSequence prot = new ProteinSequence(seq);
			return new SimpleProfile<ProteinSequence, AminoAcidCompound>(prot);
		} else if(clusterMembers.isEmpty()) {
			throw new IllegalArgumentException("Empty cluster");
		}
		// Align all members
		List<ProteinSequence> lst = new ArrayList<>();
		for (ChainClusterDB clust : clusterMembers) {
			String seq = clust.getPdbAlignedSeq();
			seq = stripDashes(seq);
			ProteinSequence prot = new ProteinSequence(seq);
			lst.add(prot);
		}
		Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);
		return profile;
	}

	private static String stripDashes(String seq) {
		return seq.replaceAll("-", "");
	}

	public static void main(String[] args) {
		String help = 
				"Usage: FindBiomimics \n" +		
				"  -D         : the database name to use\n"+
				"  -p         : input pdbCode or filename\n"+
				"  -c         : chainId\n"+
				"  -l         : sequence cluster level to be used (default "+DEFAULT_SEQ_CLUSTER_LEVEL.getLevel()+"\n"+
				"The database access parameters must be set in file "+DBHandler.CONFIG_FILE_NAME+" in home dir\n";
		
		
		String dbName = null;
		String pdbCode = null;
		String chainId = null;
		SeqClusterLevel seqClusterLevel = DEFAULT_SEQ_CLUSTER_LEVEL;

		
		Getopt g = new Getopt("ClusterSequences", args, "D:p:c:l:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'D':
				dbName = g.getOptarg();
				break;
			case 'p':
				pdbCode = g.getOptarg();
				break;
			case 'c':
				chainId = g.getOptarg();
				break;
			case 'l':
				seqClusterLevel = SeqClusterLevel.getByLevel(Integer.parseInt(g.getOptarg())); 
				break;
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break; // getopt() already printed an error
			}
		}
		
		if (dbName == null) {
			System.err.println("A database name must be provided with -D");
			System.exit(1);
		}
		if (pdbCode == null || chainId == null) {
			System.err.println("At least options -p and -c are needed");
			System.exit(1);
		}
		
		
		DBHandler dbh = new DBHandler(dbName);
		
		try {
			FindBiomimics mimics = new FindBiomimics(pdbCode, chainId, dbh, seqClusterLevel);
			mimics.printOverlap(new PrintWriter(System.out));
		} catch (CompoundNotFoundException e) {
			logger.error("Invalid sequence",e);
			System.exit(1); return;
		}

	}

}
