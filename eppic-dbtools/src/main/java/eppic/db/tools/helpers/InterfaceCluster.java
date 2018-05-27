package eppic.db.tools.helpers;

import java.util.ArrayList;
import java.util.List;

import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;

/**
 * A class to wrap an interface cluster within a PDB entry as extracted from the EPPIC database.
 * It maps 1:1 to an InterfaceClusterDB
 * 
 * 
 * @author Jose Duarte
 *
 */
public class InterfaceCluster {

	private PdbInfo pdb;
	
	private InterfaceClusterDB icDB;
	private List<Interface> members;
	
	public InterfaceCluster(InterfaceClusterDB icDB, PdbInfo pdb) {
		this.icDB = icDB;
		this.pdb = pdb;
		
		this.members = new ArrayList<>();
		
		for (InterfaceDB iDB:icDB.getInterfaces()) {
			members.add(new Interface(iDB, this));
			
		}
	}
	
	public Interface getRepresentative() {
		return members.get(0);
	}
	
	public PdbInfo getPdbInfo() {
		return pdb;
	}
	
	public InterfaceClusterDB getInterfaceClusterDB() {
		return icDB;
	}
}
