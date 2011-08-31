package martin;


import owl.core.connections.JPredConnection;

public class Jprediction {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		 if(args.length == 0) {
	            System.out.println("Usage: TestJpred <sequence>");
	            System.exit(1);
	     }
		 
		 String seq = args[0];
		 
		 JPredConnection jpredcon = new JPredConnection();
		 jpredcon.setDebugMode(true);
		 jpredcon.setTimeout(1000);
		 jpredcon.submitQuery(seq);

		 
		 for (String keysInMap:jpredcon.getMapKeys()){
			 if (keysInMap.startsWith("align1;")){
				 jpredcon.printResultLine(keysInMap);
			 }
		 }
		 
		 System.out.println(jpredcon.getSecondaryStructurePrediction());
	}

}
