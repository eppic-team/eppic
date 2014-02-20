package eppic;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;
import eppic.model.ResidueDB;

public class CsvOutputWriter {
	
	private PdbInfoDB pdbInfo;
	private EppicParams params;
	
	public CsvOutputWriter (PdbInfoDB pdbInfo, EppicParams params) {
		this.pdbInfo = pdbInfo;
		this.params = params;
	}

	
	
	public void writeInterfacesInfoFile() throws IOException {
		PrintStream ps = new PrintStream(params.getOutputFile(EppicParams.INTERFACES_FILE_SUFFIX));
		ps.println("Interfaces for input structure "+(params.isInputAFile()?params.getInFile().getName():params.getPdbCode()));
		ps.println("ASAs values calculated with "+params.getnSpherePointsASAcalc()+" sphere sampling points");
		
		
		printInterfacesInfo(ps, params.isUsePdbResSer());
		ps.close();
	}
	
	private void printInterfacesInfo(PrintStream ps, boolean usePdbResSer) {
		
		for (InterfaceClusterDB interfaceCluster: pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interfaceItem:interfaceCluster.getInterfaces()) {

				ps.print("# ");
				ps.printf("%d\t%9.2f\t%s\t%s\n",interfaceItem.getInterfaceId(),interfaceItem.getArea(), 
						interfaceItem.getChain1()+"+"+interfaceItem.getChain2(),interfaceItem.getOperator());
				
				ps.print("## ");
				this.printInterfacesMolInfo(ps, interfaceItem, 1, usePdbResSer);				
				
				ps.print("## ");
				this.printInterfacesMolInfo(ps, interfaceItem, 2, usePdbResSer);
				
			}
		}
	}
	
	private void printInterfacesMolInfo(PrintStream ps, InterfaceDB interfaceItem, int side, boolean usePdbResSer) {
		
		List<ResidueDB> cores = new ArrayList<ResidueDB>();
		
		for (ResidueDB residue:interfaceItem.getResidues()) {
			if (residue.getSide()==side) {
				
				if (residue.getRegion()==ResidueDB.CORE_GEOMETRY) cores.add(residue);
			}
		}
		
		String pdbChainCode = null;
		if (side==1) pdbChainCode = interfaceItem.getChain1();
		if (side==2) pdbChainCode = interfaceItem.getChain2();
		
		ps.println(side+"\t"+pdbChainCode+"\tprotein");

		if (cores.size()>0) {
			ps.printf("## core (%4.2f): %s\n", params.getCAcutoffForGeom(), getResString(cores, usePdbResSer));
		}
		ps.println("## seqres pdb res asa bsa burial(percent)");

		for (ResidueDB residue:interfaceItem.getResidues()) {	
			if (residue.getSide()==side) {
				ps.printf("%d\t%s\t%s\t%6.2f\t%6.2f",residue.getResidueNumber(),residue.getPdbResidueNumber(),residue.getResidueType(),residue.getAsa(),residue.getBsa());
				double percentBurial = 100.0*residue.getBsa()/residue.getAsa();
				if (percentBurial>0.1) {
					ps.printf("\t%5.1f\n",percentBurial);
				} else {
					ps.println();
				}
			}
		}
	}
	
	private String getResString(List<ResidueDB> residues, boolean usePdbResSer) {
		String str = "";
		for (int i=0;i<residues.size();i++) {
			String serial = null;
			if (usePdbResSer) {
				serial = residues.get(i).getPdbResidueNumber();
			} else {
				serial = ""+residues.get(i).getResidueNumber();
			}
			if (i!=residues.size()-1)
				str+=serial+",";
			else
				str+=serial;
		}
		return str;
	}

}
