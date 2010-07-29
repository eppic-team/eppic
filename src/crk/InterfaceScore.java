package crk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class InterfaceScore implements Serializable {
	
	private static final long serialVersionUID = -6635580043425902373L;
	
	private List<InterfaceMemberScore> memberScores; // at the moment strictly 2 members only
	private int minCoreSize; // the cut-off of minimum number of core residues used for this score
	
	public InterfaceScore(InterfaceMemberScore ims1, InterfaceMemberScore ims2, int minCoreSize) {
		this.memberScores = new ArrayList<InterfaceMemberScore>();
		this.memberScores.add(ims1);
		this.memberScores.add(ims2);
		this.minCoreSize = minCoreSize;
	}
	
	/**
	 * Gets the average core to rim ratio for the list of given indices of members.
	 * @param a list of indices of the memberScores List
	 * @return
	 */
	private double getAvrgRatio(List<Integer> indices) {
		double sum = 0.0;
		for (int i:indices) {
			sum+=memberScores.get(i).getRatio();				
		}
		return sum/(double)indices.size();
	}
	
	/**
	 * Gets the sum of the core sizes of given indices of members 
	 * @param indices
	 * @return
	 */
	private int getSumCoreSize(List<Integer> indices) {
		int size = 0;
		for (int i:indices) {
			size+=memberScores.get(i).getRimCore().getCoreSize();
		}
		return size;
	}
	
	/**
	 * Tells whether the sum of core sizes of given indices of members is above the 
	 * minCoreSize cut-off.
	 * @param indices
	 * @return
	 */
	private boolean hasEnoughCore(List<Integer> indices) {
		int size = getSumCoreSize(indices);
		if (size<minCoreSize) {
			return false;
		}
		return true;
	}
		
	/**
	 * Returns true if all members of this InterfaceScore are protein.
	 * @return
	 */
	public boolean isProtein() {
		for (InterfaceMemberScore memberScore:memberScores) {
			if (!memberScore.isProtein()) {
				return false;
			}
		} 
		return true;
	}
		
	/**
	 * Gets the prediction call for this InterfaceScore. See the {@link CallType} enum for 
	 * the possible prediction calls.
	 * @param bioCutoff
	 * @param xtalCutoff
	 * @return
	 */
	public InterfaceCall getCall(double bioCutoff, double xtalCutoff) {
		// the votes with voters (no anonymous vote here!)
		List<Integer> bioCalls = new ArrayList<Integer>();
		List<Integer> xtalCalls = new ArrayList<Integer>();
		List<Integer> grayCalls = new ArrayList<Integer>();
		List<Integer> noPredictCalls = new ArrayList<Integer>();
		Map<Integer,List<Integer>> allCalls = new TreeMap<Integer,List<Integer>>();
		allCalls.put(CallType.BIO.getIndex(), bioCalls);
		allCalls.put(CallType.CRYSTAL.getIndex(), xtalCalls);
		allCalls.put(CallType.GRAY.getIndex(), grayCalls);
		allCalls.put(CallType.NO_PREDICTION.getIndex(), noPredictCalls);
		
		for (int i=0;i<memberScores.size();i++) {
			// cast your votes!
			if      (memberScores.get(i).getCall(bioCutoff, xtalCutoff).equals(CallType.BIO)) {
				bioCalls.add(i);
			}
			else if (memberScores.get(i).getCall(bioCutoff, xtalCutoff).equals(CallType.CRYSTAL)) {
				xtalCalls.add(i);
			}
			else if (memberScores.get(i).getCall(bioCutoff, xtalCutoff).equals(CallType.GRAY)) {
				grayCalls.add(i);
			}
			else if (memberScores.get(i).getCall(bioCutoff, xtalCutoff).equals(CallType.NO_PREDICTION)) {
				noPredictCalls.add(i);
			}
		}
		int countBio = bioCalls.size();
		int countXtal = xtalCalls.size();
		int countGray = grayCalls.size();
		int countNoPredict = noPredictCalls.size();

		
		// decision time!
		//int validVotes = countBio+countXtal+countGray;

		if (countNoPredict==memberScores.size()) {
			return new InterfaceCall(CallType.NO_PREDICTION,this,Double.NaN,allCalls);
		}
		
		if (countBio>countXtal) {
			//TODO check the discrepancies among the different voters. The variance could be a measure of the confidence of the call
			//TODO need to do a study about the correlation of scores in members of the same interface
			//TODO it might be the case that there is good agreement and bad agreement would indicate things like a bio-mimicking crystal interface
			double score = getAvrgRatio(bioCalls);
			return new InterfaceCall(CallType.BIO,this,score,allCalls);
		} else if (countXtal>countBio) {
			double score = getAvrgRatio(xtalCalls);
			return new InterfaceCall(CallType.CRYSTAL,this,score,allCalls);
		} else if (countGray>countBio+countXtal) {
			// we use as final score the average of all gray member scores
			double score = getAvrgRatio(grayCalls);
			return new InterfaceCall(CallType.GRAY,this,score,allCalls);
		} else if (countBio==countXtal) {
			//TODO we are taking simply the average, is this the best solution?
			// weighting is not done here, scores are calculated either weighted/non-weighted before
			List<Integer> indices = new ArrayList<Integer>();
			indices.addAll(bioCalls);
			indices.addAll(xtalCalls);
			double score = getAvrgRatio(indices);
			
			// first we check that the sum of core sizes is above the cutoff (if not we call xtal directly)
			if (!hasEnoughCore(indices)) {
				return new InterfaceCall(CallType.CRYSTAL, this, score, allCalls);
			}

			if (score<bioCutoff) {
				return new InterfaceCall(CallType.BIO,this,score,allCalls);
			} else if (score>xtalCutoff) {
				return new InterfaceCall(CallType.CRYSTAL,this,score,allCalls);
			} else {
				return new InterfaceCall(CallType.GRAY,this,score,allCalls);
			}
		}
		return null;
	}	
	
	/**
	 * 
	 * @return the threshold of minimum number of core residues used to calculate this score
	 */
	public int getMinCoreSize() {
		return minCoreSize;
	}
	
	public InterfaceMemberScore getMemberScore(int i) {
		return memberScores.get(i);
	}
	
	public void serialize(File serializedFile) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(serializedFile);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
	}

	public static InterfaceScore readFromFile(File serialized) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(serialized);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		InterfaceScore interfSc = (InterfaceScore) in.readObject();
		in.close();
		fileIn.close();		
		return interfSc;
	}
	
}
