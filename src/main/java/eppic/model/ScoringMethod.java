package eppic.model;

/**
 * 
 * The possible values for the method field in the InterfaceScoreDB and InterfaceClusterScoreDB model classes
 * 
 * See also owl.core.structure.BioUnitAssignmentType for the possible values for PDB-parsed biounit methods
 * 
 * @author duarte_j
 *
 */
public class ScoringMethod {

	public static final String EPPIC_GEOMETRY 		= "eppic-geometry";		// was "Geometry"
	public static final String EPPIC_CORERIM 		= "eppic-corerim";		// was "Entropy"
	public static final String EPPIC_CORESURFACE 	= "eppic-coresurface";	// was "Z-scores"
	public static final String EPPIC_FINAL 			= "eppic"; 
	
	public static final String PISA 				= "pisa";
	public static final String AUTHORS 				= "authors";
	public static final String PQS 					= "pqs";
	
}
