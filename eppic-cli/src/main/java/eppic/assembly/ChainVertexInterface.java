package eppic.assembly;

/**
 * An interface for vertices representing chains in lattice graphs.
 * 
 * @author Jose Duarte
 *
 */
public interface ChainVertexInterface {

	/**
	 * Return the chain identifier, uniquely identifying the chain within the asymmetric unit
	 * @return the chain identifier
	 */
	String getChainId();
	
	/**
	 * Return the operator identifier, uniquely identifying each space group operator
	 * @return
	 */
	int getOpId();
	
	/**
	 * Return the entity identifier, uniquely identifying sequence-identical chains.
	 * @return
	 */
	int getEntityId();
	
	
}
