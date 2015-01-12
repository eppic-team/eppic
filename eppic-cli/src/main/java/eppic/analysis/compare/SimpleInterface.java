package eppic.analysis.compare;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4d;

import org.biojava.bio.structure.quaternary.BiologicalAssemblyTransformation;
import org.biojava.bio.structure.xtal.CrystalCell;
import org.biojava.bio.structure.xtal.SpaceGroup;

import eppic.commons.pisa.PisaInterface;
import eppic.commons.pisa.PisaInterfaceList;


public class SimpleInterface {

	
	
	private int id;
	
	private double area;
	
	private String chain1;
	private String chain2;
	
	private Matrix4d operator1;
	private Matrix4d operator2;
	
	public SimpleInterface() {
		
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public double getArea() {
		return area;
	}
	
	public void setArea(double area) {
		this.area = area;
	}
	
	public String getChain1() {
		return chain1;
	}
	
	public void setChain1(String chain1) {
		this.chain1 = chain1;
	}
	
	public String getChain2() {
		return chain2;
	}
	
	public void setChain2(String chain2) {
		this.chain2 = chain2;
	}
	
	public Matrix4d getOperator1() {
		return operator1;
	}
	
	public void setOperator1(Matrix4d operator1) {
		this.operator1 = operator1;
	}
	
	public Matrix4d getOperator2() {
		return operator2;
	}
	
	public void setOperator2(Matrix4d operator2) {
		this.operator2 = operator2;
	}
	
	public String toString() {
		return id+":"+chain1+"|"+chain2+"("+SpaceGroup.getAlgebraicFromMatrix(operator1)+"|"+SpaceGroup.getAlgebraicFromMatrix(operator2)+")"; 
	}

	
	public static SimpleInterface createSimpleInterfaceFromPisaInterface(PisaInterface pisaI) {
		SimpleInterface i = new SimpleInterface();
		
		i.setArea(pisaI.getInterfaceArea());
		i.setChain1(pisaI.getFirstMolecule().getChainId()); 
		i.setChain2(pisaI.getSecondMolecule().getChainId());
		i.setId(pisaI.getId());
		i.setOperator1(pisaI.getFirstMolecule().getTransf().getMatTransform());
		i.setOperator2(pisaI.getSecondMolecule().getTransf().getMatTransform());
		
		return i;
	}
	
	public static List<SimpleInterface> createSimpleInterfaceListFromPisaInterfaceList(PisaInterfaceList pisaInterfaces, double minArea) {
		
		List<SimpleInterface> list = new ArrayList<SimpleInterface>();
		for (PisaInterface pisaI:pisaInterfaces) {
			if (pisaI.getInterfaceArea()>minArea && pisaI.isProtein()) {
				list.add(createSimpleInterfaceFromPisaInterface(pisaI));
			}
		}
		
		return list;
	}
	
	public static List<SimpleInterface> createSimpleInterfaceListFromPdbBioUnit(
			List<BiologicalAssemblyTransformation> bioUnit, CrystalCell cell) {
	
		List<SimpleInterface> list = new ArrayList<SimpleInterface>();
		
		int id = 1;
		
		for (int i = 0; i<bioUnit.size(); i++) {
			for (int j = 0; j<bioUnit.size(); j++) {
				if (j>i) {
					String iChain = bioUnit.get(i).getChainId();
					String jChain = bioUnit.get(j).getChainId();
					Matrix4d iOp = bioUnit.get(i).getTransformationMatrix();
					Matrix4d jOp = bioUnit.get(j).getTransformationMatrix();
					if (cell!=null) {
						// note: the Biojava provided transf matrix is in orthonormal frame (as it is in PDB/mmCIF files)
						iOp = cell.transfToCrystal(bioUnit.get(i).getTransformationMatrix());
						jOp = cell.transfToCrystal(bioUnit.get(j).getTransformationMatrix());
					}
					SimpleInterface si = new SimpleInterface();
					si.setChain1(iChain);
					si.setChain2(jChain);
					si.setOperator1(iOp);
					si.setOperator2(jOp);
					si.setId(id);
					
					// we take care of not adding duplicates arising from transforming the two chains with the same operator
					// e.g. 11bg with pisa biounit of size 4, A0-B0 (both identity) is first added, then A1-B1 (which is equivalent to A0-B0) 
					boolean alreadyPresent = false;
					for (SimpleInterface member:list) {
						if (areMatching(member,si)) {
							alreadyPresent = true;
							break;
						}
					}
					if (!alreadyPresent) {
						list.add(si);
						id++;
					}

					//System.out.println(i+" "+j+" "+si);
				}
			}
		}

		return list;
	}
	
	private static boolean areMatching(SimpleInterface firstI, SimpleInterface secondI) {
		
		if (! (firstI.getChain1().equals(secondI.getChain1()) && firstI.getChain2().equals(secondI.getChain2()))) {
			return false;
		}
		
		Matrix4d secondTransf12 = InterfaceMatcher.findTransf12(secondI.getOperator1(), secondI.getOperator2());
		Matrix4d firstTransf12 = InterfaceMatcher.findTransf12(firstI.getOperator1(), firstI.getOperator2()); 
		
		// now we can compare the two transf12
		if (secondTransf12.epsilonEquals(firstTransf12, 0.00001)) {
			return true;
		}
		return false;
	}

//	private class ChainOperator {
//
//		private String chainId;
//		private Matrix4d operator;
//		
//		public ChainOperator(String chainId, Matrix4d operator) {
//			this.chainId = chainId;
//			this.operator = operator;
//		}
//
//		public String getChainId() {
//			return chainId;
//		}
//
//		public Matrix4d getOperator() {
//			return operator;
//		}
//		
//	}
}
