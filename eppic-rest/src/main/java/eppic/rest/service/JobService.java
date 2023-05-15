package eppic.rest.service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import eppic.db.adaptors.ViewsAdaptor;
import eppic.db.dao.mongo.ContactDAOMongo;
import eppic.db.dao.mongo.InterfaceResidueFeaturesDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.mongoutils.MongoDbStore;
import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.ContactDB;
import eppic.model.db.GraphEdgeDB;
import eppic.model.db.HomologDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.ResidueBurialDB;
import eppic.model.db.ResidueInfoDB;
import eppic.model.dto.views.*;
import eppic.rest.commons.CoordFilesAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.db.dao.*;

import javax.persistence.NoResultException;

/**
 * The service implementation to retrieve data as needed by the REST endpoints.
 * @author Nikhil Biyani
 * @author Jose Duarte
 */
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private static final Pattern PDBID_REGEX = Pattern.compile("^\\d\\w\\w\\w$");

    private final PDBInfoDAO pdbInfoDAO;
    private final PDBInfoDAO pdbInfoDAOUserJobs;
    private final InterfaceResidueFeaturesDAO featuresDAO;
    private final InterfaceResidueFeaturesDAO featuresDAOUserJobs;

    public JobService() {
        pdbInfoDAO = new PDBInfoDAOMongo(MongoDbStore.getMongoDb());
        featuresDAO = new InterfaceResidueFeaturesDAOMongo(MongoDbStore.getMongoDb());

        pdbInfoDAOUserJobs = new PDBInfoDAOMongo(MongoDbStore.getMongoDbUserJobs());
        featuresDAOUserJobs = new InterfaceResidueFeaturesDAOMongo(MongoDbStore.getMongoDbUserJobs());
    }

    /**
     * Retrieves pdbInfo item for entry.
     * @param entryId identifier of the entry
     * @param getInterfaceInfo whether to retrieve interface info or not
     * @param getAssemblyInfo whether to retrieve assembly info or not
     * @param getSeqInfo whether to retrieve sequence info or not
     * @param getResInfo whether to retrieve residue info or not
     * @return pdb info item
     * @throws DaoException when can not retrieve result of the entry
     */
    public PdbInfoDB getResultData(String entryId,
                                          boolean getInterfaceInfo,
                                          boolean getAssemblyInfo,
                                          boolean getSeqInfo,
                                          boolean getResInfo) throws DaoException
    {

        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        if (pdbInfo == null) {
            throw new NoResultException("Could not find id '" + entryId + "' in database");
        }

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
     * Retrieves assembly data for entry.
     * @param entryId identifier of the entry
     * @return assembly data corresponding to entry id
     * @throws DaoException when can not retrieve result of the entry
     */
    public List<AssemblyDB> getAssemblyDataByPdbAssemblyId(String entryId) throws DaoException {

        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        return pdbInfo.getAssemblies();
    }

    /**
     * Retrieves interface cluster data for entry.
     * @param entryId identifier of the entry
     * @return interface cluster data corresponding to entry id
     * @throws DaoException when can not retrieve result of the entry
     */
    public List<InterfaceClusterDB> getInterfaceClusterData(String entryId) throws DaoException {

        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        return pdbInfo.getInterfaceClusters();
    }

    /**
     * Retrieves interface data for entry.
     * @param entryId identifier of the entry
     * @return interface data corresponding to entry id
     * @throws DaoException when can not retrieve result of the entry
     */
    public List<InterfaceDB> getInterfaceData(String entryId) throws DaoException {

        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        return pdbInfo.getInterfaces();
    }

    /**
     * Retrieves sequence data for entry.
     * @param entryId identifier of the entry
     * @return sequence data corresponding to entry id
     * @throws DaoException when can not retrieve result of the entry
     */
    public List<ChainClusterDB> getSequenceData(String entryId) throws DaoException {

        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        return pdbInfo.getChainClusters();
    }

    public Map<String, String> getAlignment(String entryId, String repChainId) throws DaoException {
        List<ChainClusterDB> chainClusterDBS = getSequenceData(entryId);
        ChainClusterDB chainClusterDB = null;
        for (ChainClusterDB ccDb : chainClusterDBS) {
            if (ccDb.getRepChain().equals(repChainId)) {
                chainClusterDB = ccDb;
                break;
            }
        }
        if (chainClusterDB == null) {
            logger.warn("No ChainClusterDB object found for entry {} and repChain {}", entryId, repChainId);
            return null;
        }
        Map<String, String> seqs = new LinkedHashMap<>();
        String queryId = constructSeqId(chainClusterDB.getRefUniProtId(), chainClusterDB.getRefUniProtStart(), chainClusterDB.getRefUniProtEnd());
        seqs.put(queryId, chainClusterDB.getMsaAlignedSeq());
        for (HomologDB hom : chainClusterDB.getHomologs()) {
            String upId = hom.getUniProtId();
            String seq = hom.getAlignedSeq();
            String id = constructSeqId(upId, hom.getSubjectStart(), hom.getSubjectEnd());
            seqs.put(id, seq);
        }
        return seqs;
    }

    private String constructSeqId(String uniprotId, int start, int end) {
        return uniprotId + "_" + start + "-" + end;
    }

    public String serializeToFasta(Map<String, String> sequences) {
        StringBuilder sb = new StringBuilder();

        int len = 80;
        for (Map.Entry<String, String> entry : sequences.entrySet()) {
            sb.append("> ").append(entry.getKey()).append("\n");
            String seq = entry.getValue();
            for (int i = 0; i < seq.length(); i += len) {
                sb.append(seq, i, Math.min(i + len, seq.length())).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves residue data for entry and interface id.
     * @param entryId identifier of the entry
     * @param interfId the interface id
     * @return residue data corresponding to entry id and interface id
     * @throws DaoException when can not retrieve result of the entry
     */
    public List<Residue> getResidueData(String entryId, int interfId) throws DaoException {

        // 1st get burial info
        InterfaceResidueFeaturesDB features = getInterfaceFeaturesDAO(entryId).getInterfResFeatures(entryId, interfId);

        if (features == null) {
            throw new NoResultException("Could not find id '" + entryId + "' in database");
        }

        List<ResidueBurialDB> burials1 = features.getResBurials1();
        List<ResidueBurialDB> burials2 = features.getResBurials2();

        // 2nd get entropy scores
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);
        InterfaceDB interf = pdbInfo.getInterface(interfId);
        ChainClusterDB chain1 = pdbInfo.getChainCluster(interf.getChain1());
        ChainClusterDB chain2 = pdbInfo.getChainCluster(interf.getChain2());

        // 3rd create Residue objects for output
        List<Residue> residues = new ArrayList<>();

        fillResiduesData(residues, burials1, chain1, 1);
        fillResiduesData(residues, burials2, chain2, 2);

        return residues;
    }

    private void fillResiduesData(List<Residue> residues, List<ResidueBurialDB> burials, ChainClusterDB chainCluster, int molecId) {

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
     * Retrieves contact data for entry and interface id
     * @param entryId identifier of the entry
     * @param interfId the interface id
     * @return contact data
     * @throws DaoException when can not retrieve result of the entry
     */
    public List<ContactDB> getContactData(String entryId, int interfId) throws DaoException {
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        ContactDAO contactDAO = new ContactDAOMongo();
        List<ContactDB> list = contactDAO.getContactsForInterface(pdbInfo.getUid(), interfId);

        if (list==null) {
            throw new DaoException("Could not find contact data for entry "+entryId+" and interface id "+interfId);
        }

        return list;
    }

    /**
     * Retrieves assembly data for entry and Pdb assembly id
     * @param entryId entry identifier
     * @param pdbAssemblyId the PDB assembly id
     * @return assembly data
     * @throws DaoException
     */
    public AssemblyDB getAssemblyDataByPdbAssemblyId(String entryId, int pdbAssemblyId) throws DaoException {
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        AssemblyDB assembly = pdbInfo.getAssemblyByPdbAssemblyId(pdbAssemblyId);

        if (assembly==null) {
            throw new DaoException("Could not find assembly data for entry "+entryId+" and PDB assembly id "+pdbAssemblyId);
        }
        return assembly;
    }

    /**
     * Retrieves assembly data for entry and eppic assembly id
     * @param entryId entry identifier
     * @param assemblyId the eppic assembly id
     * @return assembly data
     * @throws DaoException
     */
    public AssemblyDB getAssemblyData(String entryId, int assemblyId) throws DaoException {
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        AssemblyDB assembly = pdbInfo.getAssemblyById(assemblyId);

        if (assembly==null) {
            throw new DaoException("Could not find assembly data for entry "+entryId+" and PDB assembly id "+assemblyId);
        }
        return assembly;
    }

    /**
     * Retrieves lattice graph data for entry and eppic assembly id
     * @param entryId entry identifier
     * @param assemblyId the eppic assembly id
     * @return lattice graph data
     * @throws DaoException
     */
    public LatticeGraph getLatticeGraphData(String entryId, int assemblyId) throws DaoException {
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        AssemblyDB assembly = pdbInfo.getAssemblyById(assemblyId);
        AssemblyDB unitcellAssembly = pdbInfo.getAssemblyById(0);

        if (assembly==null || unitcellAssembly == null) {
            throw new DaoException("Could not find assembly data for entry "+entryId+" and PDB assembly id "+assemblyId);
        }

        return ViewsAdaptor.getLatticeGraphView(assembly, unitcellAssembly);
    }

    /**
     * Retrieves lattice graph data for entry and eppic interface ids
     * string (comma separated and possibly with hyphens).
     * @param entryId entry identifier
     * @param interfaceIds the eppic interface ids, if null all interfaces are assumed
     * @return lattice graph data
     * @throws DaoException
     */
    public LatticeGraph getLatticeGraphDataByInterfaceIds(String entryId, Set<Integer> interfaceIds) throws DaoException {
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        List<GraphEdgeDB> graphEdges = new ArrayList<>();

        AssemblyDB unitcellAssembly = pdbInfo.getAssemblyById(0);

        if (unitcellAssembly == null) {
            throw new DaoException("Could not find unitcell assembly data for entry "+entryId+" and PDB assembly id 0");
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
     * Retrieves lattice graph data for entry and eppic interface cluster ids
     * string (comma separated and possibly with hyphens).
     * @param entryId entry identifier
     * @param interfaceClusterIds the eppic interface cluster ids, if null all interface clusters are assumed
     * @return lattice graph data
     * @throws DaoException
     */
    public LatticeGraph getLatticeGraphDataByInterfaceClusterIds(String entryId, Set<Integer> interfaceClusterIds) throws DaoException {
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        List<GraphEdgeDB> graphEdges = new ArrayList<>();

        AssemblyDB unitcellAssembly = pdbInfo.getAssemblyById(0);

        if (unitcellAssembly == null) {
            throw new DaoException("Could not find unitcell assembly data for entry "+entryId+" and PDB assembly id 0");
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
     * Retrieces assembly diagram data for entry id and eppic assembly id.
     * @param entryId entry identifier
     * @param assemblyId the eppic assembly id
     * @return
     * @throws DaoException
     */
    public AssemblyDiagram getAssemblyDiagram(String entryId, int assemblyId) throws DaoException {
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        AssemblyDB assembly = pdbInfo.getAssemblyById(assemblyId);

        if (assembly == null) {
            throw new DaoException("Could not find assembly data for entry "+entryId+" and PDB assembly id "+assemblyId);
        }

        return ViewsAdaptor.getAssemblyDiagram(assembly);
    }

    public byte[] getCoordinateFile(String entryId, String interfId, String assemblyId, Map<String, Object> props) throws DaoException, IOException {

        if (assemblyId != null && interfId != null) {
            throw new IllegalArgumentException("Only one of interfId or assemblyId can be not null");
        }

        // get data and produce file
        PdbInfoDB pdbInfo = getPdbInfoDAO(entryId).getPDBInfo(entryId);

        File baseOutDir = new File((String)props.get("base.out.dir"));
        // for user jobs (this works because we create a symlink, see SubmitService)
        File auFile = new File(baseOutDir, entryId);
        if (!auFile.exists()) {
            // for precomputed jobs
            auFile = new File(baseOutDir, entryId + ".cif.gz");
        }

        CoordFilesAdaptor adaptor = new CoordFilesAdaptor();

        byte[] data;
        if (assemblyId!=null) {
            data = adaptor.getAssemblyCoordsMmcif(entryId, auFile, pdbInfo, Integer.parseInt(assemblyId), true);
        } else if (interfId!=null) {
            data = adaptor.getInterfaceCoordsMmcif(entryId, auFile, pdbInfo, Integer.parseInt(interfId), true);
        } else {
            // should not happen, the validation took care of this
            throw new RuntimeException("Unsupported file type ");
        }
        return data;
    }

    private PDBInfoDAO getPdbInfoDAO(String entryId) {
        if (PDBID_REGEX.matcher(entryId).matches()) {
            return pdbInfoDAO;
        }
        return pdbInfoDAOUserJobs;
    }

    private InterfaceResidueFeaturesDAO getInterfaceFeaturesDAO(String entryId) {
        if (PDBID_REGEX.matcher(entryId).matches()) {
            return featuresDAO;
        }
        return featuresDAOUserJobs;
    }
}
