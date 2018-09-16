package eppic.db.tools;

import static eppic.db.tools.helpers.Interface.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.vecmath.GMatrix;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.core.alignment.SimpleProfile;
import org.biojava.nbio.core.alignment.template.AlignedSequence;
import org.biojava.nbio.core.alignment.template.Profile;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.AccessionID;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.util.ConcurrencyTools;
import org.biojava.nbio.core.util.SingleLinkageClusterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.util.CallType;
import eppic.db.tools.helpers.DirectedInterface;
import eppic.db.tools.helpers.Interface;
import eppic.db.tools.helpers.InterfaceCluster;
import eppic.db.tools.helpers.MultipleInterfaceComparator;
import eppic.db.tools.helpers.PdbInfo;
import eppic.db.tools.helpers.SeqClusterLevel;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.ContactDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.InterfaceScoreDB;
import eppic.model.db.ScoringMethod;
import gnu.getopt.Getopt;

public class FindBiomimics {
	private static final Logger logger = LoggerFactory.getLogger(FindBiomimics.class);

	private static final SeqClusterLevel DEFAULT_SEQ_CLUSTER_LEVEL = SeqClusterLevel.C100;
	private static final double DEFAULT_JACCARD_OVERLAP_THRESHOLD = 0.2;

	private final List<ChainClusterDB> clusterMembers;
	private final Profile<ProteinSequence, AminoAcidCompound> profile; //Row  for each chain


	private final List<List<DirectedInterface>> interfaces;

	private final MultipleInterfaceComparator comparisons;
	private final List<CallType> predictions;
	
	public FindBiomimics(String pdbCode, String chainId, DBHandler dbh, SeqClusterLevel seqClusterLevel) throws CompoundNotFoundException {
		// Sequence cluster that the query belongs to
		this(dbh.getClusterIdForPdbCodeAndChain(pdbCode, chainId, seqClusterLevel.getLevel()), dbh, seqClusterLevel);
	}
	public FindBiomimics(int repCluster, DBHandler dbh, SeqClusterLevel seqClusterLevel) throws CompoundNotFoundException {
		// Get list of all members of that cluster
		clusterMembers = initClusterMembers(dbh,seqClusterLevel,repCluster);
		logger.info("Found {} other members",clusterMembers.size()-1);

		// build alignment of all chains
		profile = multipleAlignment(clusterMembers);
		logger.info("Alignment:\n{}",profile.toString(120));

		// Get list of all interfaces in that cluster
		interfaces = initInterfaces(dbh,clusterMembers);

		// flatten lists
		List<DirectedInterface> flatInterfaces = getInterfaces();
		Profile<ProteinSequence, AminoAcidCompound> alignment = getAlignment();

		// Get contacts
		List<List<ContactDB>> contacts = new ArrayList<>(flatInterfaces.size());
		for(DirectedInterface iface : flatInterfaces) {
			contacts.add(iface.getInterface().getInterface().getContacts());
		}
		// Do pairwise comparisons
		comparisons = new MultipleInterfaceComparator(flatInterfaces, alignment, contacts);
		
		// Assign predictions
		predictions = new ArrayList<>(flatInterfaces.size());
		for(DirectedInterface iface : flatInterfaces) {
			InterfaceScoreDB score = iface.getInterface().getInterface().getInterfaceScore(ScoringMethod.EPPIC_FINAL);
			predictions.add( CallType.getByName(score.getCallName()) );
		}
	}
	

	/**
	 * Gets all members of a sequence cluster, with some filters
	 * @param dbh
	 * @param seqClusterLevel
	 * @param repCluster
	 * @return
	 */
	private static List<ChainClusterDB> initClusterMembers(DBHandler dbh, SeqClusterLevel seqClusterLevel, int repCluster) {
		// Get list of all members of that cluster
		List<ChainClusterDB> clusterMembers = dbh.getClusterMembers(seqClusterLevel.getLevel(), repCluster);
		
		
		ListIterator<ChainClusterDB> it = clusterMembers.listIterator();
		Pattern xray = Pattern.compile("x-?ray diffraction", Pattern.CASE_INSENSITIVE);
		while(it.hasNext()) {
			ChainClusterDB clust = it.next();
			// filter to proteins from xray crystals
			String expMethod = clust.getPdbInfo().getExpMethod();
			if(!clust.isProtein() || !xray.matcher(expMethod).matches() || clust.getPdbCode()==null)
				it.remove();
			
//			//TODO debugging
//			else if( !( clust.getPdbCode().equals("1faa") ||
//					clust.getPdbCode().equals("2pvo") ||
//							clust.getPdbCode().equals("1f9m") ))
//				it.remove();

		}
		return clusterMembers;
	}
	
	private static List<List<DirectedInterface>> initInterfaces(DBHandler dbh, List<ChainClusterDB> clusterMembers) {
		// Get list of all interfaces in that cluster
		List<List<DirectedInterface>> interfaces = new ArrayList<>(clusterMembers.size());
		for(ChainClusterDB member : clusterMembers) {
			PdbInfo pdbInfo = new PdbInfo(member.getPdbInfo());

			// get all interfaces containing this chain
			Map<InterfaceDB, Integer> memberInterfaces = dbh.getInterfacesForChainCluster(member);
			List<DirectedInterface> clustInterfaces = new ArrayList<>(memberInterfaces.size());
			for(Entry<InterfaceDB, Integer> entry : memberInterfaces.entrySet()) {
				InterfaceDB ifaceDb = entry.getKey();
				int direction = entry.getValue();

//				//TODO for debugging
//				if( !( ifaceDb.getPdbCode().equals("1faa") && ifaceDb.getInterfaceId()==1 ||
//						ifaceDb.getPdbCode().equals("2pvo") && ifaceDb.getInterfaceId()==2 ||
//						ifaceDb.getPdbCode().equals("1f9m") && ifaceDb.getInterfaceId()==2 ))
//					continue;
				
				Interface iface = new Interface(ifaceDb, new InterfaceCluster(ifaceDb.getInterfaceCluster(), pdbInfo));
				
				// only use protein-protein interfaces
				if(! iface.getChainCluster(FIRST).getChainCluster().isProtein() ||
						! iface.getChainCluster(SECOND).getChainCluster().isProtein() ) {
					continue;
				}
				
				// Add directions. Could be both directions (3)
				if( (direction & 1) > 0) {
					clustInterfaces.add(new DirectedInterface(iface,FIRST));
				}
				if( (direction & 2) > 0 && !ifaceDb.isIsologous()) {
					clustInterfaces.add(new DirectedInterface(iface,SECOND));
				}

			}
			interfaces.add(clustInterfaces);
		}
		return interfaces;
	}
	
	public Map<Integer, Set<DirectedInterface>> getClusters(double cutoff) {
		GMatrix overlaps = getOverlap();
		
		// first we convert the it into a double matrix
		double[][] matrix = new double[overlaps.getNumRow()][overlaps.getNumCol()];
		for (int i=0;i<overlaps.getNumRow();i++) {
			for (int j=i+1;j<overlaps.getNumCol();j++) {
				matrix[i][j] = overlaps.getElement(i, j);
			}
		}
		
		// note that the clusterer alters the matrix, keep that in mind if we wanted to use the matrix down the line
		SingleLinkageClusterer cl = new SingleLinkageClusterer(matrix, true);
		//cl.setDebug();
		Map<Integer,Set<Integer>> cls = cl.getClusters(cutoff);
		
		// 
		Map<Integer,Set<DirectedInterface>> clusters = new HashMap<>(cls.size());
		List<DirectedInterface> flatInterfaces = getInterfaces();
		for(Entry<Integer, Set<Integer>> entry:cls.entrySet()) {
			Set<Integer> indices = entry.getValue();
			Set<DirectedInterface> members = new HashSet<>(indices.size());
			for(Integer i:indices) {
				members.add(flatInterfaces.get(i));
			}
			clusters.put(entry.getKey(),members);
		}
		return clusters;
	}

	
	/**
	 * Return a list of all interfaces to be considered
	 * @return
	 */
	public List<DirectedInterface> getInterfaces() {
		// flatten interfaces
		List<DirectedInterface> oriented = new ArrayList<>();
		for(List<DirectedInterface> l : interfaces) {
			for(DirectedInterface i : l) {
				oriented.add(i);
			}
		}
		return oriented;
	}
	
	/**
	 * Return an alignment of the active side of each interface. The Profile
	 * will have one row for each interface returned by {@link #getInterfaces()}.
	 * The active side of an interface is given by
	 * {@link DirectedInterface#getDirection()}, and is either {@link Interface#FIRST}
	 * (chain1) or  {@link Interface#SECOND} (chain2).
	 * @return
	 */
	public Profile<ProteinSequence,AminoAcidCompound> getAlignment() {
		// Duplicate sequences to match getInterfaces()
		List<AlignedSequence<ProteinSequence,AminoAcidCompound>> alignedSequences =
				new ArrayList<>();
		for(int i=0;i<interfaces.size();i++) {
			List<DirectedInterface> l = interfaces.get(i);
			AlignedSequence<ProteinSequence, AminoAcidCompound> seq = profile.getAlignedSequence(i+1);
			for(int j=0;j<l.size();j++) {
				alignedSequences.add(seq);
			}
		}
		return new SimpleProfile<ProteinSequence,AminoAcidCompound>(alignedSequences );
	}

	public GMatrix getOverlap() {
		GMatrix overlap = new GMatrix(comparisons.size(),comparisons.size());
		for(int i=0;i<comparisons.size();i++) {
			for(int j=0;j<=i;j++) {
				double o = comparisons.getJaccard(i, j);
//				double o = comparisons.getOverlap(i, j);
				overlap.setElement(i, j, o);
				overlap.setElement(j, i, o);
			}
		}
		return overlap;
	}
	
	public List<CallType> getPredictions() {
		return predictions;
	}
	
	public void printOverlap(PrintWriter out) {
		List<DirectedInterface> flatInterfaces = getInterfaces();
		String[] headers = new String[flatInterfaces.size()];
		
		int i=0;
		for(DirectedInterface interf : flatInterfaces) {
			InterfaceDB interfDB = interf.getInterface().getInterface();
			String name = String.format("%4s-%s%s",interfDB.getPdbCode(),interfDB.getInterfaceId(),
					interf.getDirection()==FIRST ? ">" : "<");
			headers[i++] = name;
		}
		
		
		printMatrix(out,getOverlap(),headers,headers);
		printSparseMatrix(out,getOverlap(),headers,headers);
	}
	private static void printSparseMatrix(PrintWriter out, GMatrix m, String[] rowheaders, String[] colheaders) {
		for(int i=0;i<rowheaders.length;i++) {
			for(int j=0;j<colheaders.length;j++) {
				if(Math.abs(m.getElement(i, j)) > 1e-15) {
					out.format("%s\t%s\t%f%n", rowheaders[i],colheaders[j], m.getElement(i, j));
				}
			}
		}
		out.flush();

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
			AccessionID a = new AccessionID(String.format("%s.%s", clust.getPdbCode(), clust.getRepChain()) );
			a.setIdentifier("1"); // Use alternate identifier to store index
			prot.setAccession(a);
			return new SimpleProfile<ProteinSequence, AminoAcidCompound>(prot);
		} else if(clusterMembers.isEmpty()) {
			throw new IllegalArgumentException("Empty cluster");
		}
		// Align all members
		List<ProteinSequence> lst = new ArrayList<>();
		int i=1;
		for (ChainClusterDB clust : clusterMembers) {
			String seq = clust.getPdbAlignedSeq();
			seq = stripDashes(seq);
			ProteinSequence prot = new ProteinSequence(seq);
			AccessionID a = new AccessionID(String.format("%s.%s", clust.getPdbCode(), clust.getRepChain()) );
			a.setIdentifier(Integer.toString(i)); // Use alternate identifier to store index
			prot.setAccession(a);
			i++;
			lst.add(prot);
		}
		Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);
		// Reorder based on accessions
		SortedMap<Integer, AlignedSequence<ProteinSequence, AminoAcidCompound>> sorted = new TreeMap<>();
		for(AlignedSequence<ProteinSequence, AminoAcidCompound> seq : profile.getAlignedSequences()) {
			int index = Integer.parseInt(seq.getAccession().getIdentifier());
			sorted.put(index,seq);
		}
		Profile<ProteinSequence, AminoAcidCompound> sortedProfile = new SimpleProfile<ProteinSequence, AminoAcidCompound>(sorted.values());
		return sortedProfile;
	}

	private static String stripDashes(String seq) {
		return seq.replaceAll("-", "");
	}

	public static void main(String[] args) {
		String help = 
				"Usage: FindBiomimics \n" +		
				"  -D <string> : the database name to use\n"+
				"  -p <string> : input pdbCode \n"+
				"  -c <string> : chainId\n"+
				" [-l <int>  ] : sequence cluster level to be used (default "+DEFAULT_SEQ_CLUSTER_LEVEL.getLevel()+"\n"+
				" [-o <float>] : minimum Jaccard overlap required to cluster interfaces (default "+DEFAULT_JACCARD_OVERLAP_THRESHOLD+"\n"+
				" [-g <file> ] : a configuration file containing the database access parameters, if not provided\n" +
				"                the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n";
		
		
		String dbName = null;
		String pdbCode = null;
		String chainId = null;
		SeqClusterLevel seqClusterLevel = DEFAULT_SEQ_CLUSTER_LEVEL;
		double overlapThreshold = DEFAULT_JACCARD_OVERLAP_THRESHOLD;
		File configFile = null;
		
		Getopt g = new Getopt("FindBiomimics", args, "D:p:c:l:o:g:h?");
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
			case 'o':
				overlapThreshold = Double.parseDouble(g.getOptarg());
				break;
			case 'g':
				configFile = new File(g.getOptarg());
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
		
		
		DBHandler dbh = new DBHandler(dbName, configFile);
		
		PrintWriter out = new PrintWriter(System.out);
		try {
			FindBiomimics mimics = new FindBiomimics(pdbCode, chainId, dbh, seqClusterLevel);
			//mimics.printOverlap(out);
			out.println("Key: > compare chain1; < compare chain2; * biological interface; ~ no prediction for interface");
			Map<Integer, Set<DirectedInterface>> clusters = mimics.getClusters(overlapThreshold);
			for(Integer clustNum : new TreeSet<>(clusters.keySet())) {
				out.format("Cluster %d: ",clustNum);
				for(DirectedInterface i:clusters.get(clustNum)) {
					InterfaceScoreDB score = i.getInterface().getInterface().getInterfaceScore(ScoringMethod.EPPIC_FINAL);
					CallType call = CallType.getByName(score.getCallName());
					
					out.print(i);
					switch(call) {
					case BIO:           out.print('*'); break;
					case CRYSTAL:       break;
					case NO_PREDICTION: out.print('~'); break;
					default: throw new IllegalStateException("Unknown call: "+call);
					}
					out.print(' ');
				}
				out.println();
			}
		} catch (CompoundNotFoundException e) {
			logger.error("Invalid sequence",e);
			System.exit(1); return;
		}
		out.close();
		ConcurrencyTools.shutdown();
	}

}
