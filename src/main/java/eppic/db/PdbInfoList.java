package eppic.db;

import java.util.ArrayList;
import java.util.List;

import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
import eppic.model.PdbInfoDB;

public class PdbInfoList {
	
	private List<PdbInfo> pdbList;
	private boolean debug;

	public PdbInfoList(List<PdbInfoDB> pdbInfoList) {
		pdbList = new ArrayList<PdbInfo>();
		for (PdbInfoDB pdbInfo:pdbInfoList) {
			pdbList.add(new PdbInfo(pdbInfo)); 
		}		
	}
	
	public CFCompareMatrix calcLatticeOverlapMatrix(SeqClusterLevel seqClusterLevel, double coCutoff, double minArea) throws PairwiseSequenceAlignmentException {
		
		LatticeOverlapScore[][] matrix = new LatticeOverlapScore[pdbList.size()][pdbList.size()];
		for (int i=0;i<pdbList.size();i++) {
			for (int j=0;j<pdbList.size();j++) {
				if (j<=i) continue;
				PdbInfo ipdb = pdbList.get(i);
				PdbInfo jpdb = pdbList.get(j);
				
				if (!ipdb.haveSameContent(jpdb, seqClusterLevel)) {
					matrix[i][j] = new LatticeOverlapScore(0, 0);
				} else {
					LatticeMatchMatrix llm = ipdb.calcLatticeOverlapMatrix(jpdb, seqClusterLevel, minArea, debug);
					matrix[i][j] = llm.getLatticeOverlapScore(coCutoff);
				}
			}

		}
		return new CFCompareMatrix(matrix);
	}
	
	public int size() {
		return pdbList.size();
	}
	
	public PdbInfo get(int i) {
		return pdbList.get(i);
	}
}
