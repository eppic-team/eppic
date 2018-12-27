package eppic.analysis.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Matrix4d;

import eppic.DataModelAdaptor;
import org.biojava.nbio.structure.quaternary.BioAssemblyInfo;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.SpaceGroup;

import eppic.commons.pisa.PisaInterface;
import eppic.commons.pisa.PisaInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleInterface {

	private static final Logger logger = LoggerFactory.getLogger(SimpleInterface.class);

	/**
	 * PDB biounits that have more than this number of transformations won't be matched
	 * against EPPIC's transforms to find the matching assemblies. Current algorithm is O(n2) and
	 * will take forever on such large assemblies.
	 */
	public static final int MAX_NUM_TRANSFORMS_IN_PDB_BIOUNIT = 10000;
	
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
	
	@Override
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
		
		List<SimpleInterface> list = new ArrayList<>();
		for (PisaInterface pisaI:pisaInterfaces) {
			if (pisaI.getInterfaceArea()>minArea && pisaI.isProtein()) {
				list.add(createSimpleInterfaceFromPisaInterface(pisaI));
			}
		}
		
		return list;
	}

	/**
	 * Create a list of SimpleInterfaces (operators+chain ids) via enumerating all pairwise
	 * combinations of operators-chainIds.
	 * @param bioUnit
	 * @param cell
	 * @param asymIds2chainIds
	 * @return the list of null if {@value #MAX_NUM_TRANSFORMS_IN_PDB_BIOUNIT} is exceeded, where calculation would take too long
	 */
	public static List<SimpleInterface> createSimpleInterfaceListFromPdbBioUnit(
			BioAssemblyInfo bioUnit, CrystalCell cell, HashMap<String, String> asymIds2chainIds) {
	
		List<SimpleInterface> list = new ArrayList<>();
		
		int id = 1;
		
		for (int i = 0; i<bioUnit.getTransforms().size(); i++) {
			for (int j = 0; j<bioUnit.getTransforms().size(); j++) {
				if (j>i) {
					// note: in BioJava the BioAssemblyInfo have asym_ids as the chain ids, 
					// we need to convert the asym ids to chain ids or we'd have problems (see issues https://github.com/eppic-team/eppic/issues/98 and https://github.com/eppic-team/eppic/issues/74)
					String iAsymId = bioUnit.getTransforms().get(i).getChainId();
					String jAsymId = bioUnit.getTransforms().get(j).getChainId();
					
					String iChainId = asymIds2chainIds.get(iAsymId);
					String jChainId = asymIds2chainIds.get(jAsymId);
					
					if (iChainId==null || jChainId==null) {
						// either asym id didn't have a corresponding chain id, most likely the chain was a ligand-only or water-only chain that we can ignore
						continue;
					}
					
					Matrix4d iOp = bioUnit.getTransforms().get(i).getTransformationMatrix();
					Matrix4d jOp = bioUnit.getTransforms().get(j).getTransformationMatrix();
					if (cell!=null) {
						// note: the Biojava provided transf matrix is in orthonormal frame (as it is in PDB/mmCIF files)
						iOp = cell.transfToCrystal(bioUnit.getTransforms().get(i).getTransformationMatrix());
						jOp = cell.transfToCrystal(bioUnit.getTransforms().get(j).getTransformationMatrix());
					}
					SimpleInterface si = new SimpleInterface();
					si.setChain1(iChainId);
					si.setChain2(jChainId);
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

					if (list.size()> MAX_NUM_TRANSFORMS_IN_PDB_BIOUNIT) {
						// Cases like 1m4x_1 (5040 operators) or 1m4x_3 (420 operators) take forever to run
						// Because algorithm is O(n2) currently
						logger.warn("Exceeded the max allowed number of SimpleInterfaces for PDB biounit matching ({}). Will not do PDB biounit matching.", MAX_NUM_TRANSFORMS_IN_PDB_BIOUNIT);
						return null;
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

}
