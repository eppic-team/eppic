package crk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;


public class ChainEvolContextList implements Serializable {

	private static final long serialVersionUID = 1L;

	private TreeMap<String, ChainEvolContext> cecs; // one per representative chain
	private HashMap<String,String> allchains2representative; // pdb chain codes to pdb chain codes of representative chain
	
	public ChainEvolContextList() {
		this.cecs = new TreeMap<String, ChainEvolContext>();
		this.allchains2representative = new HashMap<String, String>();
	}
	
	public void addChainEvolContext(String representativeChain, List<String> chains, ChainEvolContext cec) {
		this.cecs.put(representativeChain, cec);
		this.allchains2representative.put(representativeChain,representativeChain);
		for (String chain:chains) {
			this.allchains2representative.put(chain,representativeChain);
		}
	}
	
	/**
	 * Gets the ChainEvolContext corresponding to the given PDB chain code (can be 
	 * any chain code, representative or not)
	 * @param pdbChainCode
	 * @return
	 */
	public ChainEvolContext getChainEvolContext(String pdbChainCode) {
		return cecs.get(allchains2representative.get(pdbChainCode));
	}
	
	/**
	 * Gets the Collection of all ChainEvolContext (sorted alphabetically by representative pdb chain code) 
	 * @return
	 */
	public Collection<ChainEvolContext> getAllChainEvolContext() {
		return cecs.values();
	}
	
    public void serialize(File serializedFile) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(serializedFile);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this);
        out.close();
        fileOut.close();
    }

    public static ChainEvolContextList readFromFile(File serialized) throws IOException, ClassNotFoundException {
    	FileInputStream fileIn = new FileInputStream(serialized);
    	ObjectInputStream in = new ObjectInputStream(fileIn);
    	ChainEvolContextList interfSc = (ChainEvolContextList) in.readObject();
    	in.close();
    	fileIn.close();
    	return interfSc;
    }
}
