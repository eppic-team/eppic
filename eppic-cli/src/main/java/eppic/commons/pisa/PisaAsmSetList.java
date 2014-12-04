package eppic.commons.pisa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eppic.commons.pisa.PisaAssembly.PredictionType;


public class PisaAsmSetList implements Iterable<PisaAsmSet> {

	private String pdbCode;
	private List<PisaAsmSet> list;
	private String status;
	
	public PisaAsmSetList() {
		list = new ArrayList<PisaAsmSet>();
	}
	
	public PisaAsmSet get(int i) {
		return list.get(i);
	}
	
	public int size() {
		return list.size();
	}
	
	public boolean add(PisaAsmSet pisaAsmSet) {
		return list.add(pisaAsmSet);
	}
	
	public String getPdbCode() {
		return pdbCode;
	}
	
	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Iterator<PisaAsmSet> iterator() {
		return list.iterator();
	}
	
	public OligomericPrediction getOligomericPred(PisaInterfaceList pil) {
		
		// no AsmSet at all, PISA didn't find any possible assemblies (stable or not) and so we call monomer 
		if (size()==0) {
			return new OligomericPrediction(1);
		} 

		// we then take first AsmSet present as the prediction
		PisaAsmSet pas = get(0);
		
		// NOTE that in principle PISA uses deltaGdiss to classify between stable, unstable and gray:
		//  stable >0
		//  unstable ~<-2
		//  gray ~>-2 && <0
		// But the rule is not always so clear, see for instance 1ibr where first assembly is deltaGdiss>0 but gray
		// or 1eer where deltaGdiss=-0.9 and it's considered stable, while for 1bam deltaGdiss=-1.6 is gray
		
		// check whether all assemblies of the set are unstable or all gray
		boolean allGray = true;
		boolean allUnstable = true;
		
		for (PisaAssembly pa:pas) {

			if (pa.isProteinProtein(pil)) {
				// isProteinProtein will return false also for assemblies with mmSize==1, 
				// that doesn't matter here as those can't be gray or unstable
				PredictionType pt = pa.getPredictionType();
				if (pt==null) 
					System.err.println("ERROR: could not understand the prediction type (stable, gray or unstable) for "+pdbCode+" assembly "+pa.getId());
				
				if (pt!=PisaAssembly.PredictionType.GRAY) allGray = false;
				if (pt!=PisaAssembly.PredictionType.UNSTABLE) allUnstable = false;
			}
		}

		if (allGray) {
			return new OligomericPrediction(-1);
		}
		if (allUnstable) {
			// then it's a monomer
			return new OligomericPrediction(1);				
		}

			

		// all other cases: one or more stable assemblies in the AsmSet
		OligomericPrediction op = new OligomericPrediction(1); 
		//boolean sizeSet = false;
		int mmSize = 0;
		for (PisaAssembly pa:pas) {
			
			if (mmSize>1 && pa.getMmsize()==1) {
				// i.e. mmSize has been initialised (not 0) and its value is more than 1 (monomeric) and this pa is monomeric
				// we then are in a mixed >1 with ==1 assemblies in same set, we warn
				System.err.println("WARNING! Mixed assembly sizes in same assembly group for "+pdbCode+". Assembly group is of size "+mmSize+", new assembly (id "+pa.getId()+") is of size "+pa.getMmsize()+", ignoring it");								
			}
			// note following condition will also return false for assemblies of mmSize==1
			// anyway those by definition have deltaGdiss==0, so they don't matter here
			// if all assemblies in this set are monomeric then 'op' will stay monomeric as initialized above
			if (!pa.isProteinProtein(pil)) continue;

			// we set the size to the first deltaGdiss>0 mm assembly present
			if (mmSize==0 && pa.getDissEnergy()>0) {
				op.setMmSize(pa.getMmsize());
				mmSize = pa.getMmsize();
			}
			
			if (mmSize>0 && pa.getMmsize()!=mmSize) {
				System.err.println("WARNING! Mixed assembly sizes in same assembly group for "+pdbCode+". Assembly group is of size "+mmSize+", new assembly (id "+pa.getId()+") is of size "+pa.getMmsize()+", ignoring it");				
			} else {
				op.addAssembly(pa);
			}

			//System.out.printf("\t%2d\t%2d\t%5.1f\t%20s\t%s\n",
			//		mmsizePred,pa.getMmsize(),
			//		pa.getDissEnergy(),
			//		pa.getFormula(),
			//		pa.getInterfaceIdsString());
			
		}
		return op;
	}
	
}
