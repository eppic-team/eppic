package eppic.db;

public enum SeqClusterLevel {

	C100(100), C95(95), C90(90), C80(80), C70(70), C60(60), C50(50), C40(40), C30(30);
	
	private int level;
	
	private SeqClusterLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
	public static SeqClusterLevel getByLevel(int level) {
		
		for (SeqClusterLevel seqClusterLevel:SeqClusterLevel.values()) {
			if (seqClusterLevel.getLevel()==level) return seqClusterLevel;
		}
		
		return null;
	}
}
