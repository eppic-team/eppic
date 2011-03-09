package crk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbLoadException;
import owl.core.util.FileFormatException;

public class InterfaceEvolContextList implements Iterable<InterfaceEvolContext>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String IDENTIFIER_HEADER       = "# PDB identifier:";
	private static final String SCORE_METHOD_HEADER 	= "# Score method:";
	private static final String SCORE_TYPE_HEADER   	= "# Score type:";
	private static final String NUM_HOMS_CUTOFF_HEADER  = "# Min number homologs required:";
	private static final String SEQUENCE_ID_HEADER  	= "# Sequence identity cutoff:";
	private static final String QUERY_COV_HEADER    	= "# Query coverage cutoff:";
	private static final String MAX_NUM_SEQS_HEADER     = "# Max num sequences used:";
	private static final String BIO_CALL_HEADER         = "# Bio-call cutoff:";
	private static final String XTAL_CALL_HEADER        = "# Xtal-call cutoff:";
	private static final String CA_CUTOFF_HEADER        = "# Total core size xtal-call cutoff:";
	private static final String CA_MEMBER_CUTOFF_HEADER = "# Per-member core size xtal-call cutoff:";
	private static final String BSA_TO_ASA_CUTOFFS_HEADER = "# Core assignment cutoffs:";
	
	private static final String  DOUBLE_REGEX = "NaN|Infinity|(?:[+-]?[0-9]+\\.[0-9]+)";
	
	private static final Pattern IDENTIFIER_PAT = Pattern.compile("^"+Pattern.quote(IDENTIFIER_HEADER)+"\\s+(.*)$");
	private static final Pattern SCORE_METHOD_PAT = Pattern.compile("^"+Pattern.quote(SCORE_METHOD_HEADER)+"\\s+(.*)$");
	private static final Pattern SCORE_TYPE_PAT = Pattern.compile("^"+Pattern.quote(SCORE_TYPE_HEADER)+"\\s+(.*)$");
	private static final Pattern NUM_HOMS_CUTOFF_PAT = Pattern.compile("^"+Pattern.quote(NUM_HOMS_CUTOFF_HEADER)+"\\s+(.*)$");
	private static final Pattern SEQUENCE_ID_PAT = Pattern.compile("^"+Pattern.quote(SEQUENCE_ID_HEADER)+"\\s+(.*)$");
	private static final Pattern QUERY_COV_PAT = Pattern.compile("^"+Pattern.quote(QUERY_COV_HEADER)+"\\s+(.*)$");
	private static final Pattern MAX_NUM_SEQS_PAT = Pattern.compile("^"+Pattern.quote(MAX_NUM_SEQS_HEADER)+"\\s+(.*)$");
	private static final Pattern BIO_CALL_PAT = Pattern.compile("^"+Pattern.quote(BIO_CALL_HEADER)+"\\s+(.*)$");
	private static final Pattern XTAL_CALL_PAT = Pattern.compile("^"+Pattern.quote(XTAL_CALL_HEADER)+"\\s+(.*)$");
	private static final Pattern CA_CUTOFF_PAT = Pattern.compile("^"+Pattern.quote(CA_CUTOFF_HEADER)+"\\s+(.*)$");
	private static final Pattern CA_MEMBER_CUTOFF_PAT = Pattern.compile("^"+Pattern.quote(CA_MEMBER_CUTOFF_HEADER)+"\\s+(.*)$");
	private static final Pattern BSA_TO_ASA_CUTOFFS_PAT = Pattern.compile("^"+Pattern.quote(BSA_TO_ASA_CUTOFFS_HEADER)+"\\s+(.*)$");
	private static final Pattern ZOOMED_PAT = Pattern.compile("^zoomed\\s\\(("+DOUBLE_REGEX+"),("+DOUBLE_REGEX+"),("+DOUBLE_REGEX+").*$");
	
	private static final Pattern TITLES_LINE_PAT = Pattern.compile("^\\s+interface.*");
	//                                                     size1   size2        CA             n1        n2         
	private static final String  INTERF_PAT_STRING = "\\s+(\\d+)\\s+(\\d+)\\s+(\\d\\.\\d+)\\s+(\\d+)\\s+(\\d+)\\s+("
		//    core1                   rim1                rat1                   core2                 rim2                   rat2           call          score
		+DOUBLE_REGEX+")\\s+("+DOUBLE_REGEX+")\\s+("+DOUBLE_REGEX+")\\s+("+DOUBLE_REGEX+")\\s+("+DOUBLE_REGEX+")\\s+("+DOUBLE_REGEX+")\\s+(\\w+)\\s+("+DOUBLE_REGEX+").*$";
	//                                                                          id    chain1  chain2       area
	private static final Pattern FIRST_INTERF_LINE_PAT = Pattern.compile("^\\s*(\\d+)\\((.*)\\+(.*)\\)\\s+(\\d+\\.\\d+)"+INTERF_PAT_STRING);
	private static final Pattern INTERF_LINE_PAT = Pattern.compile("^"+INTERF_PAT_STRING);
	
	private List<InterfaceEvolContext> list;

	private String pdbName;
	private ScoringType scoType;
	private boolean isScoreWeighted;
	private double bioCutoff;
	private double xtalCutoff;
	private int homologsCutoff;
	private int minCoreSize;
	private int minMemberCoreSize;
	private double idCutoff;
	private double queryCovCutoff;
	private int maxNumSeqsCutoff;
	private double minInterfAreaReporting;
	
	public InterfaceEvolContextList(){
		list = new ArrayList<InterfaceEvolContext>();		
	}
	
	public InterfaceEvolContextList(String pdbName, int homologsCutoff, int minCoreSize, int minMemberCoreSize, 
			double idCutoff, double queryCovCutoff, int maxNumSeqsCutoff, double minInterfAreaReporting) {
		this.pdbName = pdbName;
		this.homologsCutoff = homologsCutoff;
		this.minCoreSize = minCoreSize;
		this.minMemberCoreSize = minMemberCoreSize;
		this.idCutoff = idCutoff;
		this.queryCovCutoff = queryCovCutoff;
		this.maxNumSeqsCutoff = maxNumSeqsCutoff;
		this.minInterfAreaReporting = minInterfAreaReporting;
		
		list = new ArrayList<InterfaceEvolContext>();
	}
	
	public void add(InterfaceEvolContext iec) {
		list.add(iec);
	}
	
	/**
	 * Given a ChainInterfaceList with all interfaces of a given PDB and a ChainEvolContextList with
	 * all evolutionary contexts of chains of that same PDB adds an InterfaceEvolContext (containing a pair
	 * of ChainEvolContext and a ChainInterface) to this list for each protein-portein interface. 
	 * @param interfaces
	 * @param cecs
	 */
	public void addAll(ChainInterfaceList interfaces, ChainEvolContextList cecs) {
		for (ChainInterface pi:interfaces) {
			if (pi.isProtein()) {
				ArrayList<ChainEvolContext> chainsEvCs = new ArrayList<ChainEvolContext>();
				chainsEvCs.add(cecs.getChainEvolContext(pi.getFirstMolecule().getPdbChainCode()));
				chainsEvCs.add(cecs.getChainEvolContext(pi.getSecondMolecule().getPdbChainCode()));
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, chainsEvCs);
				this.add(iec);
			}
		}
	}
	
	@Override
	public Iterator<InterfaceEvolContext> iterator() {
		return list.iterator();
	}

	
	
	public void scoreEntropy(boolean weighted) {
		this.scoType = ScoringType.ENTROPY;
		this.isScoreWeighted = weighted;
		for (InterfaceEvolContext iec:this) {
			iec.scoreEntropy(weighted);
		}
	}
	
	public void scoreKaKs(boolean weighted) {
		this.scoType = ScoringType.KAKS;
		this.isScoreWeighted = weighted;
		for (InterfaceEvolContext iec:this) {
			if (iec.canDoCRK()) {
				iec.scoreKaKs(weighted);
			}
		}		
	}
	
	public boolean isScoreWeighted() {
		return isScoreWeighted;
	}
	
	public void printScoresTable(PrintStream ps, double bioCutoff, double xtalCutoff) {
		this.bioCutoff = bioCutoff;
		this.xtalCutoff = xtalCutoff;
		
		printScoringParams(ps);
		printScoringHeaders(ps);
		
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.printScoresTable(ps, bioCutoff, xtalCutoff, homologsCutoff, minCoreSize, minMemberCoreSize);
			}
		}
	}
	
	public void writeScoresPDBFiles(CRKParams params, String suffix) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.writePdbFile(params.getOutputFile("."+iec.getInterface().getId()+suffix), InterfaceEvolContext.SCORES);
			}
		}
	}
	
	public void writeRimCorePDBFiles(CRKParams params, String suffix) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.writePdbFile(params.getOutputFile("."+iec.getInterface().getId()+suffix), InterfaceEvolContext.RIMCORE);
			}
		}
	}	
	
	public void generateThumbnails(File pymolExe, CRKParams params, String suffix) throws IOException, InterruptedException, PdbLoadException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				File pdbFile = params.getOutputFile("."+iec.getInterface().getId()+suffix);
				iec.generateThumbnails(pymolExe,pdbFile);
			}
		}		
	}
	
	public void writeResidueDetailsFiles(CRKParams params, String suffix) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.writeResidueDetailsFile(params.getOutputFile("."+iec.getInterface().getId()+"."+suffix),params.isDoScoreCRK());
			}
		}
	}
	
	public double getBioCutoff() {
		return bioCutoff;
	}

	public double getXtalCutoff() {
		return xtalCutoff;
	}

	public int getHomologsCutoff() {
		return homologsCutoff;
	}

	public int getMinCoreSize() {
		return minCoreSize;
	}

	public int getMinMemberCoreSize() {
		return minMemberCoreSize;
	}
	
	public ScoringType getScoringType() {
		return scoType;
	}

	private void printScoringParams(PrintStream ps) {
		ps.println(IDENTIFIER_HEADER+" "+pdbName);
		ps.println(SCORE_METHOD_HEADER+" "+scoType.getName());
		ps.println(SCORE_TYPE_HEADER+"   "+(isScoreWeighted?"weighted":"unweighted"));
		ps.println(NUM_HOMS_CUTOFF_HEADER+" "+homologsCutoff);
		ps.printf (SEQUENCE_ID_HEADER+" %4.2f\n",idCutoff);
		ps.printf (QUERY_COV_HEADER+" %4.2f\n",queryCovCutoff);
		ps.println(MAX_NUM_SEQS_HEADER+" "+maxNumSeqsCutoff);
		ps.printf (BIO_CALL_HEADER+"  %4.2f\n",bioCutoff);
		ps.printf (XTAL_CALL_HEADER+" %4.2f\n",xtalCutoff);
		ps.println(CA_CUTOFF_HEADER+" "+minCoreSize);
		ps.println(CA_MEMBER_CUTOFF_HEADER+" "+minMemberCoreSize);
		ps.print(BSA_TO_ASA_CUTOFFS_HEADER+" ");
		if (list.get(0).getInterface().isRimAndCoreZoomed()){
			ps.printf("zoomed (%4.2f,%4.2f,%4.2f)\n",
					list.get(0).getInterface().getBsaToAsaSoftCutoff(),
					list.get(0).getInterface().getBsaToAsaCutoffs()[0],
					list.get(0).getInterface().getBsaToAsaRelaxStep());
		} else {
			for (double bsaToAsaCutoff:list.get(0).getInterface().getBsaToAsaCutoffs()) {
				ps.printf("%4.2f ",bsaToAsaCutoff);
			}
			ps.println();
		}
	}
	
	private static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s","size1", "size2","CA");
		ps.print("\t");
		ps.printf("%2s\t%2s\t","n1","n2");
		ps.printf("%5s\t%5s\t%5s","core1","rim1","rat1");
		ps.print("\t");
		ps.printf("%5s\t%5s\t%5s","core2","rim2","rat2");
		ps.print("\t");
		ps.printf("%6s\t%5s\t%6s\t%6s\t%6s\t%6s",
				"call","score",CallType.BIO.getName(),CallType.CRYSTAL.getName(),CallType.GRAY.getName(),CallType.NO_PREDICTION.getName());
		ps.println();
	}
	
	public static PdbScore[] parseScoresFile(File scoresFile) throws IOException, FileFormatException {
		PdbScore[] pdbScs = new PdbScore[2];
		pdbScs[0] = new PdbScore();
		pdbScs[1] = new PdbScore();
		
		BufferedReader br = new BufferedReader(new FileReader(scoresFile));
		String line;
		int i = 0;
		int lineCount=0;
		InterfaceScore isc = null;
		int[] sizes1 = null;
		int[] sizes2 = null;
		double[] core1Scs = null;
		double[] rim1Scs = null;
		double[] rat1Scs = null;
		double[] core2Scs = null;
		double[] rim2Scs = null;
		double[] rat2Scs = null;
		double[] finalScs = null;
		CallType[] calls = null;
		int bsaToAsaCoInd = 0;
		while ((line=br.readLine())!=null){
			lineCount++;
			if (line.startsWith("#")) {
				Matcher m = IDENTIFIER_PAT.matcher(line);
				if (m.matches()){
					pdbScs[i].setPdbName(m.group(1).trim());
				}
				m = SCORE_METHOD_PAT.matcher(line);
				if (m.matches()) {
					pdbScs[i].setScoType(ScoringType.getByName(m.group(1).trim()));
				}
				m = SCORE_TYPE_PAT.matcher(line);
				if (m.matches()) {
					String val = m.group(1).trim();
					if (val.equals("unweighted")) {
						if (i!=0) throw new FileFormatException("CRK scores file "+scoresFile+" must have unweighted scores in first position"); 
						pdbScs[i].setScoreWeighted(false);
					} else if (val.equals("weighted")){
						if (i!=1) throw new FileFormatException("CRK scores file "+scoresFile+" must have unweighted scores in first position");
						pdbScs[i].setScoreWeighted(true);
					}
				}
				m = NUM_HOMS_CUTOFF_PAT.matcher(line);
				if (m.matches()) {
					pdbScs[i].setHomologsCutoff(Integer.parseInt(m.group(1).trim()));
				}
				m = SEQUENCE_ID_PAT.matcher(line);
				if (m.matches()) {
					pdbScs[i].setIdCutoff(Double.parseDouble(m.group(1).trim()));
				}
				m = QUERY_COV_PAT.matcher(line);
				if (m.matches()){
					pdbScs[i].setQueryCovCutoff(Double.parseDouble(m.group(1).trim()));
				}
				m = MAX_NUM_SEQS_PAT.matcher(line);
				if (m.matches()){
					pdbScs[i].setMaxNumSeqsCutoff(Integer.parseInt(m.group(1).trim()));
				}
				m = BIO_CALL_PAT.matcher(line);
				if (m.matches()){
					pdbScs[i].setBioCutoff(Double.parseDouble(m.group(1).trim()));
				}
				m = XTAL_CALL_PAT.matcher(line);
				if (m.matches()){
					pdbScs[i].setXtalCutoff(Double.parseDouble(m.group(1).trim()));
				}
				m = CA_CUTOFF_PAT.matcher(line);
				if (m.matches()){
					pdbScs[i].setMinCoreSize(Integer.parseInt(m.group(1).trim()));
				}
				m = CA_MEMBER_CUTOFF_PAT.matcher(line);
				if (m.matches()){
					pdbScs[i].setMinMemberCoreSize(Integer.parseInt(m.group(1).trim()));
				}
				m = BSA_TO_ASA_CUTOFFS_PAT.matcher(line);
				if (m.matches()){
					String caStr = m.group(1).trim();
					Matcher zm = ZOOMED_PAT.matcher(caStr);
					if (zm.matches()) {
						double[] bsaToAsaCutoffs = new double[1];
						double bsaToAsaSoftCutoff = Double.parseDouble(zm.group(1));
						bsaToAsaCutoffs[0] = Double.parseDouble(zm.group(2));
						double bsaToAsaRelaxStep = Double.parseDouble(zm.group(3));
						pdbScs[i].setBsaToAsaCutoffs(bsaToAsaCutoffs);
						pdbScs[i].setBsaToAsaSoftCutoff(bsaToAsaSoftCutoff);
						pdbScs[i].setBsaToAsaRelaxStep(bsaToAsaRelaxStep);
						pdbScs[i].setZoomUsed(true);
					} else {
						String[] tokens = m.group(1).trim().split("\\s+");
						double[] bsaToAsaCutoffs = new double[tokens.length];
						for (int c=0;c<tokens.length;c++) {
							bsaToAsaCutoffs[c] = Double.parseDouble(tokens[c]);
						}
						pdbScs[i].setZoomUsed(false);
						pdbScs[i].setBsaToAsaCutoffs(bsaToAsaCutoffs);
					}
					i++; // last field before scores table, we increment for next one
				}
					
 			} else {
 				Matcher m = TITLES_LINE_PAT.matcher(line);
				if (m.matches()) continue;
				m = FIRST_INTERF_LINE_PAT.matcher(line);
				if (m.matches()) {
					sizes1 = new int[pdbScs[i-1].getBsaToAsaCutoffs().length];
					sizes2 = new int[pdbScs[i-1].getBsaToAsaCutoffs().length];
					core1Scs = new double[pdbScs[i-1].getBsaToAsaCutoffs().length];
					rim1Scs = new double[pdbScs[i-1].getBsaToAsaCutoffs().length];
					rat1Scs = new double[pdbScs[i-1].getBsaToAsaCutoffs().length];
					core2Scs = new double[pdbScs[i-1].getBsaToAsaCutoffs().length];
					rim2Scs = new double[pdbScs[i-1].getBsaToAsaCutoffs().length];
					rat2Scs = new double[pdbScs[i-1].getBsaToAsaCutoffs().length];
					calls = new CallType[pdbScs[i-1].getBsaToAsaCutoffs().length];
					finalScs = new double[pdbScs[i-1].getBsaToAsaCutoffs().length];
					bsaToAsaCoInd = 0;
					
					isc = new InterfaceScore(pdbScs[i-1]);
					// get the fields
					int id = Integer.parseInt(m.group(1).trim());
					String chain1 = m.group(2).trim();
					String chain2 = m.group(3).trim();
					double area = Double.parseDouble(m.group(4).trim());
					isc.setId(id);
					isc.setFirstChainId(chain1);
					isc.setSecondChainId(chain2);
					isc.setInterfArea(area);
					
					pdbScs[i-1].addInterfScore(isc);
					
					sizes1[bsaToAsaCoInd] = Integer.parseInt(m.group(5).trim());
					sizes2[bsaToAsaCoInd] = Integer.parseInt(m.group(6).trim());
					// skip 7 (bsaToAsaCutoff)
					int numHoms1 = Integer.parseInt(m.group(8).trim());
					int numHoms2 = Integer.parseInt(m.group(9).trim());
					core1Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(10).trim());
					rim1Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(11).trim());
					rat1Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(12).trim());
					core2Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(13).trim());
					rim2Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(14).trim());
					rat2Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(15).trim());
					calls[bsaToAsaCoInd] = CallType.getByName(m.group(16).trim());
					finalScs[bsaToAsaCoInd] = Double.parseDouble(m.group(17).trim());
					
					isc.setNumHomologs1(numHoms1);
					isc.setNumHomologs2(numHoms2);
					
					isc.setCoreSize1(sizes1);
					isc.setCoreSize2(sizes2);
					isc.setCore1Scores(core1Scs);
					isc.setRim1Scores(rim1Scs);
					isc.setRatio1Scores(rat1Scs);
					isc.setCore2Scores(core2Scs);
					isc.setRim2Scores(rim2Scs);
					isc.setRatio2Scores(rat2Scs);
					isc.setFinalScores(finalScs);
					isc.setCalls(calls);

				}
				m = INTERF_LINE_PAT.matcher(line);
				if (m.matches()) {
					bsaToAsaCoInd++;
					sizes1[bsaToAsaCoInd] = Integer.parseInt(m.group(1).trim());
					sizes2[bsaToAsaCoInd] = Integer.parseInt(m.group(2).trim());
					// skip 3 (bsaToAsaCutoff)
					// skip 4,5 (num homologues, already parsed with FIRST_INTERF_LINE_PAT)
					core1Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(6).trim());
					rim1Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(7).trim());
					// we skip 8 (ratio1)
					core2Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(9).trim());
					rim2Scs[bsaToAsaCoInd] = Double.parseDouble(m.group(10).trim());
					// we skip 11 (ratio2)
					calls[bsaToAsaCoInd] = CallType.getByName(m.group(12));
					finalScs[bsaToAsaCoInd] = Double.parseDouble(m.group(13).trim());
				}
				
			}
		}
		br.close();
		
		return pdbScs;
	}
	
}
