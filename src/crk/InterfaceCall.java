package crk;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class InterfaceCall {
	
	private CallType type;
	private InterfaceScore interfScore;
	private double score;
	private Map<Integer,List<Integer>> memberCalls; // map of CallType indices (getIndex()) to lists of interface member indices
	
	public InterfaceCall(CallType type, InterfaceScore interfScore, double score, Map<Integer, List<Integer>> memberCalls) {
		this.type = type;
		this.interfScore = interfScore;
		this.score = score;
		this.memberCalls = memberCalls;
	}

	/**
	 * @return the type
	 */
	public CallType getType() {
		return type;
	}

	public InterfaceScore getInterfScore() {
		return interfScore;
	}
	
	public double getScore() {
		return score;
	}
	
	public void printTabular(PrintStream ps) {
		// call type, score, voters
		ps.printf("%6s\t%5.2f",type.getName(),score);

		for (CallType callType: CallType.values()) {
			List<Integer> calls = memberCalls.get(callType.getIndex());
			String callsStr = "";
			for (int i=0;i<calls.size();i++) {
				callsStr+=interfMemberIndexToSerial(calls.get(i));
				if (i!=calls.size()-1) callsStr+=","; // skip the comma for the last member
			}
			ps.printf("\t%6s",callsStr);
		}
		// more possible things to output: number of homologues (?)
	}
	
	public static void printHeader(PrintStream ps) {
		ps.printf("%6s\t%5s\t%6s\t%6s\t%6s\t%6s",
				"call","score",CallType.BIO.getName(),CallType.CRYSTAL.getName(),CallType.GRAY.getName(),CallType.NO_PREDICTION.getName());
	}
	
	private int interfMemberIndexToSerial(int index) {
		return index + 1;
	}
}
