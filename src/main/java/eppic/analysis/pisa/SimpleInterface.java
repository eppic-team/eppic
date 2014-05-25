package eppic.analysis.pisa;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.vecmath.Matrix4d;

import owl.core.connections.pisa.PisaInterface;
import owl.core.connections.pisa.PisaInterfaceList;
import owl.core.structure.PdbBioUnit;

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
	
	public static List<SimpleInterface> createSimpleInterfaceListFromPdbBioUnit(PdbBioUnit bioUnit) {
	
		List<SimpleInterface> list = new ArrayList<SimpleInterface>();
		TreeMap<String,List<Matrix4d>> ops = bioUnit.getOperators();
		int id = 1;
		for (String iChain:ops.keySet()) {
			for (Matrix4d iOp:ops.get(iChain)) {

				for (String jChain:ops.keySet()) {
					for (Matrix4d jOp:ops.get(jChain)) {
						SimpleInterface si = new SimpleInterface();
						si.setChain1(iChain);
						si.setChain2(jChain);
						si.setOperator1(iOp);
						si.setOperator2(jOp);
						si.setId(id);
						list.add(si);
						id++;
					}
				}
			}
		}
		return list;
	}
}
