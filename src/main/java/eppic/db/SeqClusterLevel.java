package eppic.db;

public enum SeqClusterLevel {

	C100(100), C95(95), C90(90), C80(80), C70(70), C60(60), C50(50);
	
	private int level;
	
	private SeqClusterLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
}
