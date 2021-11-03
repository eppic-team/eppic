package eppic.rest.service;

import java.util.*;

import com.mongodb.client.MongoDatabase;
import eppic.db.adaptors.ViewsAdaptor;
import eppic.db.dao.mongo.ContactDAOMongo;
import eppic.db.dao.mongo.InterfaceResidueFeaturesDAOMongo;
import eppic.db.dao.mongo.JobDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.ContactDB;
import eppic.model.db.GraphEdgeDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.ResidueBurialDB;
import eppic.model.db.ResidueInfoDB;
import eppic.model.dto.InputWithType;
import eppic.model.dto.views.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.db.dao.*;

/**
 * The service implementation to retrieve data as needed by the REST endpoints.
 * @author Nikhil Biyani
 * @author Jose Duarte
 */
public class JobService {
    
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    // TODO init!!!
    // TODO check if we can make it non-static
    private static MongoDatabase mongoDb;

    /**
     * Retrieves pdbInfo item for job.
     * @param jobId identifier of the job
     * @param getInterfaceInfo whether to retrieve interface info or not
     * @param getAssemblyInfo whether to retrieve assembly info or not
     * @param getSeqInfo whether to retrieve sequence info or not
     * @param getResInfo whether to retrieve residue info or not
     * @return pdb info item
     * @throws DaoException when can not retrieve result of the job
     */
    public static PdbInfoDB getResultData(String jobId,
                                          boolean getInterfaceInfo,
                                          boolean getAssemblyInfo,
                                          boolean getSeqInfo,
                                          boolean getResInfo) throws DaoException
    {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
        // TODO what to do with this after rewrite??
        JobDAO jobDAO = new JobDAOMongo();
        InputWithType input = jobDAO.getInputWithTypeForJob(jobId);
        //pdbInfo.setInputType(input.getInputType());
        //pdbInfo.setInputName(input.getInputName());

        if (!getInterfaceInfo) {
            pdbInfo.setInterfaceClusters(null);
        }

        if(!getSeqInfo){
            pdbInfo.setChainClusters(null);
        }

        if (!getAssemblyInfo) {
            pdbInfo.setAssemblies(null);
        }

        return pdbInfo;
    }

    /**
     * Retrieves assembly data for job.
     * @param jobId identifier of the job
     * @return assembly data corresponding to job id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<AssemblyDB> getAssemblyDataByPdbAssemblyId(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        return pdbInfo.getAssemblies();
    }

    /**
     * Retrieves interface cluster data for job.
     * @param jobId identifier of the job
     * @return interface cluster data corresponding to job id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<InterfaceClusterDB> getInterfaceClusterData(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        return pdbInfo.getInterfaceClusters();
    }

    /**
     * Retrieves interface data for job.
     * @param jobId identifier of the job
     * @return interface data corresponding to job id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<InterfaceDB> getInterfaceData(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        return pdbInfo.getInterfaces();
    }

    /**
     * Retrieves sequence data for job.
     * @param jobId identifier of the job
     * @return sequence data corresponding to job id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<ChainClusterDB> getSequenceData(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        return pdbInfo.getChainClusters();
    }

    /**
     * Retrieves residue data for job and interface id.
     * @param jobId identifier of the job
     * @param interfId the interface id
     * @return residue data corresponding to job id and interface id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<Residue> getResidueData(String jobId, int interfId) throws DaoException {

        // 1st get burial info
        InterfaceResidueFeaturesDAO featuresDAO = new InterfaceResidueFeaturesDAOMongo(mongoDb);
        InterfaceResidueFeaturesDB features = featuresDAO.getInterfResFeatures(jobId, interfId);
        List<ResidueBurialDB> burials1 = features.getResBurials1();
        List<ResidueBurialDB> burials2 = features.getResBurials2();

        // 2nd get entropy scores
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
        InterfaceDB interf = pdbInfo.getInterface(interfId);
        ChainClusterDB chain1 = pdbInfo.getChainCluster(interf.getChain1());
        ChainClusterDB chain2 = pdbInfo.getChainCluster(interf.getChain2());

        // 3rd create Residue objects for output
        List<Residue> residues = new ArrayList<>();

        fillResiduesData(residues, burials1, chain1, 1);
        fillResiduesData(residues, burials2, chain2, 2);

        return residues;
    }

    private static void fillResiduesData(List<Residue> residues, List<ResidueBurialDB> burials, ChainClusterDB chainCluster, int molecId) {

        for (ResidueBurialDB burial : burials) {

            int resser = burial.getResSerial();

            Residue res = new Residue();
            residues.add(res);

            res.setResidueNumber(resser);
            res.setAsa(burial.getAsa());
            res.setBsa(burial.getBsa());
            res.setRegion(burial.getRegion());
            // TODO consider if we should define burialRatio:=0 when asa==0
            res.setBurialFraction(burial.getBsa()/burial.getAsa());
            res.setSide(molecId == 2);

            if (resser == -1) {
                logger.warn("Residue burial object has resser==-1, will skip");
                continue;
            }

            // THIS NOTES CAME FROM DataModelAdaptor . Keeping here for reference. Possibly the issue is still there
            // this is a difficult operation: we are in a single chain and we connect to the equivalent
            // residue in the representative chain (the one we store in the residueInfos in chainCluster).
            // Thus the issues with residue serials in SEQRES/no SEQRES case will hit here!
            //
            // See the comment in createChainCluster
            // Here getResidue(resser) matches the residue serials via the residue serials we added earlier
            // to ResidueInfo. Those were coming from getAlignedResIndex, thus they should match correctly
            // TODO in no-SEQRES case if the residues are not consistently named across different chains of
            //      same entity, this will fail and return a null! e.g. 3ddo without seqres
            //      The ideal solution to this would be to go through our UniProt alignments and get a proper
            //      mapping from there, but at this point we don't have an alignment yet... so it is complicated

            ResidueInfoDB residueInfo = chainCluster.getResidue(resser);
            if (residueInfo == null) { // && !noseqres) {
                // we only warn if we have seqres and find a null, case noseqres==true emits only 1 warning above
                logger.warn("Could not find the ResidueInfo corresponding to ResidueBurial object with serial {}", resser);
                continue;
            }

            res.setResidueType(residueInfo.getResidueType());
            res.setEntropyScore(residueInfo.getEntropyScore());
        }
    }

    /**
     * Retrieves contact data for job and interface id
     * @param jobId identifier of the job
     * @param interfId the interface id
     * @return contact data
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<ContactDB> getContactData(String jobId, int interfId) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        ContactDAO contactDAO = new ContactDAOMongo();
        List<ContactDB> list = contactDAO.getContactsForInterface(pdbInfo.getUid(), interfId);

        if (list==null) {
            throw new DaoException("Could not find contact data for job "+jobId+" and interface id "+interfId);
        }

        return list;
    }

    /**
     * Retrieves assembly data for job and Pdb assembly id
     * @param jobId job identifier
     * @param pdbAssemblyId the PDB assembly id
     * @return assembly data
     * @throws DaoException
     */
    public static AssemblyDB getAssemblyDataByPdbAssemblyId(String jobId, int pdbAssemblyId) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        AssemblyDB assembly = pdbInfo.getAssemblyByPdbAssemblyId(pdbAssemblyId);

        if (assembly==null) {
            throw new DaoException("Could not find assembly data for job "+jobId+" and PDB assembly id "+pdbAssemblyId);
        }
        return assembly;
    }

    /**
     * Retrieves assembly data for job and eppic assembly id
     * @param jobId job identifier
     * @param assemblyId the eppic assembly id
     * @return assembly data
     * @throws DaoException
     */
    public static AssemblyDB getAssemblyData(String jobId, int assemblyId) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        AssemblyDB assembly = pdbInfo.getAssemblyById(assemblyId);

        if (assembly==null) {
            throw new DaoException("Could not find assembly data for job "+jobId+" and PDB assembly id "+assemblyId);
        }
        return assembly;
    }

    /**
     * Retrieves lattice graph data for job and eppic assembly id
     * @param jobId job identifier
     * @param assemblyId the eppic assembly id
     * @return lattice graph data
     * @throws DaoException
     */
    public static LatticeGraph getLatticeGraphData(String jobId, int assemblyId) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        AssemblyDB assembly = pdbInfo.getAssemblyById(assemblyId);
        AssemblyDB unitcellAssembly = pdbInfo.getAssemblyById(0);

        if (assembly==null || unitcellAssembly == null) {
            throw new DaoException("Could not find assembly data for job "+jobId+" and PDB assembly id "+assemblyId);
        }

        return ViewsAdaptor.getLatticeGraphView(assembly, unitcellAssembly);
    }

    /**
     * Retrieves lattice graph data for job and eppic interface ids
     * string (comma separated and possibly with hyphens).
     * @param jobId job identifier
     * @param interfaceIds the eppic interface ids, if null all interfaces are assumed
     * @return lattice graph data
     * @throws DaoException
     */
    public static LatticeGraph getLatticeGraphDataByInterfaceIds(String jobId, Set<Integer> interfaceIds) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        List<GraphEdgeDB> graphEdges = new ArrayList<>();

        AssemblyDB unitcellAssembly = pdbInfo.getAssemblyById(0);

        if (unitcellAssembly == null) {
            throw new DaoException("Could not find unitcell assembly data for job "+jobId+" and PDB assembly id 0");
        }

        if (interfaceIds == null) {
            graphEdges = unitcellAssembly.getGraphEdges();
        } else {
            for (GraphEdgeDB edge : unitcellAssembly.getGraphEdges()) {
                int interfaceId = edge.getInterfaceId();
                if (interfaceIds.contains(interfaceId)) {
                    graphEdges.add(edge);
                }
            }
        }

        return ViewsAdaptor.getLatticeGraphView(graphEdges, unitcellAssembly);
    }

    /**
     * Retrieves lattice graph data for job and eppic interface cluster ids
     * string (comma separated and possibly with hyphens).
     * @param jobId job identifier
     * @param interfaceClusterIds the eppic interface cluster ids, if null all interface clusters are assumed
     * @return lattice graph data
     * @throws DaoException
     */
    public static LatticeGraph getLatticeGraphDataByInterfaceClusterIds(String jobId, Set<Integer> interfaceClusterIds) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        List<GraphEdgeDB> graphEdges = new ArrayList<>();

        AssemblyDB unitcellAssembly = pdbInfo.getAssemblyById(0);

        if (unitcellAssembly == null) {
            throw new DaoException("Could not find unitcell assembly data for job "+jobId+" and PDB assembly id 0");
        }

        if (interfaceClusterIds == null) {
            graphEdges = unitcellAssembly.getGraphEdges();
        } else {
            for (GraphEdgeDB edge : unitcellAssembly.getGraphEdges()) {
                int interfaceClusterId = edge.getInterfaceClusterId();
                if (interfaceClusterIds.contains(interfaceClusterId)) {
                    graphEdges.add(edge);
                }
            }
        }
        return ViewsAdaptor.getLatticeGraphView(graphEdges, unitcellAssembly);
    }

    /**
     * Retrieces assembly diagram data for job id and eppic assembly id.
     * @param jobId job identifier
     * @param assemblyId the eppic assembly id
     * @return
     * @throws DaoException
     */
    public static AssemblyDiagram getAssemblyDiagram(String jobId, int assemblyId) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo(mongoDb);
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        AssemblyDB assembly = pdbInfo.getAssemblyById(assemblyId);

        if (assembly==null) {
            throw new DaoException("Could not find assembly data for job "+jobId+" and PDB assembly id "+assemblyId);
        }

        return ViewsAdaptor.getAssemblyDiagram(assembly);
    }

}
