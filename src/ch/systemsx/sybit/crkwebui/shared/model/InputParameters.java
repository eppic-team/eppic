package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

/**
 * This class is used to transfer parameters selected by the user
 * @author srebniak_a
 *
 */
public class InputParameters implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String[] methods;

	private boolean useTCoffee;
	private boolean usePISA;
	private boolean useNACCESS;

	private int asaCalc;
	private int maxNrOfSequences;
	private int reducedAlphabet;

	private float identityCutoff;
	private float selecton;

	public InputParameters() {

	}

	public String[] getMethods() {
		return methods;
	}

	public void setMethods(String[] methods) {
		this.methods = methods;
	}

	public boolean isUseTCoffee() {
		return useTCoffee;
	}

	public void setUseTCoffee(boolean useTCoffee) {
		this.useTCoffee = useTCoffee;
	}

	public boolean isUsePISA() {
		return usePISA;
	}

	public void setUsePISA(boolean usePISA) {
		this.usePISA = usePISA;
	}

	public boolean isUseNACCESS() {
		return useNACCESS;
	}

	public void setUseNACCESS(boolean useNACCESS) {
		this.useNACCESS = useNACCESS;
	}

	public int getAsaCalc() {
		return asaCalc;
	}

	public void setAsaCalc(int asaCalc) {
		this.asaCalc = asaCalc;
	}

	public int getMaxNrOfSequences() {
		return maxNrOfSequences;
	}

	public void setMaxNrOfSequences(int maxNrOfSequences) {
		this.maxNrOfSequences = maxNrOfSequences;
	}

	public int getReducedAlphabet() {
		return reducedAlphabet;
	}

	public void setReducedAlphabet(int reducedAlphabet) {
		this.reducedAlphabet = reducedAlphabet;
	}

	public float getIdentityCutoff() {
		return identityCutoff;
	}

	public void setIdentityCutoff(float identityCutoff) {
		this.identityCutoff = identityCutoff;
	}

	public float getSelecton() {
		return selecton;
	}

	public void setSelecton(float selecton) {
		this.selecton = selecton;
	}
}
