package eppic.rest.service;

import java.util.*;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import eppic.db.adaptors.ViewsAdaptor;
import eppic.db.dao.mongo.AssemblyDAOMongo;
import eppic.db.dao.mongo.ChainClusterDAOMongo;
import eppic.db.dao.mongo.ContactDAOMongo;
import eppic.db.dao.mongo.InterfaceClusterDAOMongo;
import eppic.db.dao.mongo.InterfaceDAOMongo;
import eppic.db.dao.mongo.JobDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.dao.mongo.ResidueDAOMongo;
import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.ContactDB;
import eppic.model.db.GraphEdgeDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.ResidueInfoDB;
import eppic.model.dto.InputWithType;
import eppic.model.dto.views.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.db.dao.*;

/**
 * Servlet used to download results in xml/json format.
 * Adapted to both json or xml by using eclipselink JAXB implementation.
 * @author Nikhil Biyani
 * @author Jose Duarte
 *
 */
@PersistenceContext(name="eppicjpa", unitName="eppicjpa")
public class JobService {
    
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);



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
        JobDAO jobDAO = new JobDAOMongo();
        InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
        // TODO what to do with this after rewrite??
        //pdbInfo.setInputType(input.getInputType());
        //pdbInfo.setInputName(input.getInputName());


        // retrieving interface clusters data only if requested
        if (getInterfaceInfo) {
            InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOMongo();
            List<InterfaceClusterDB> clusters = clusterDAO.getInterfaceClusters(pdbInfo.getUid(), true, false);

            InterfaceDAO interfaceDAO = new InterfaceDAOMongo();

            for (InterfaceClusterDB cluster : clusters) {

                logger.debug("Getting data for interface cluster uid {}", cluster.getUid());
                List<InterfaceDB> interfaceItems;
                if (getResInfo)
                    interfaceItems = interfaceDAO.getInterfacesForCluster(cluster.getUid(), true, true);
                else
                    interfaceItems = interfaceDAO.getInterfacesForCluster(cluster.getUid(), true, false);
                cluster.setInterfaces(interfaceItems);
            }

            pdbInfo.setInterfaceClusters(clusters);
        } else {
            pdbInfo.setInterfaceClusters(null);
        }

        if(getSeqInfo){
            ChainClusterDAO chainClusterDAO = new ChainClusterDAOMongo();
            List<ChainClusterDB> chainClusters = chainClusterDAO.getChainClusters(pdbInfo.getUid());
            pdbInfo.setChainClusters(chainClusters);
        } else {
            pdbInfo.setChainClusters(null);
        }

        if (getAssemblyInfo) {
            // assemblies info
            AssemblyDAO assemblyDAO = new AssemblyDAOMongo();

            List<AssemblyDB> assemblies = assemblyDAO.getAssemblies(pdbInfo.getUid(), true, false);

            pdbInfo.setAssemblies(assemblies);
        } else {
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

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOMongo();

        return assemblyDAO.getAssemblies(pdbInfo.getUid(), true, true);
    }

    /**
     * Retrieves interface cluster data for job.
     * @param jobId identifier of the job
     * @return interface cluster data corresponding to job id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<InterfaceClusterDB> getInterfaceClusterData(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOMongo();
        return clusterDAO.getInterfaceClusters(pdbInfo.getUid(), true, false);
    }

    /**
     * Retrieves interface data for job.
     * @param jobId identifier of the job
     * @return interface data corresponding to job id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<InterfaceDB> getInterfaceData(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        InterfaceDAO interfaceDAO = new InterfaceDAOMongo();
        return interfaceDAO.getInterfacesByPdbUid(pdbInfo.getUid(), true, false);
    }

    /**
     * Retrieves sequence data for job.
     * @param jobId identifier of the job
     * @return sequence data corresponding to job id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<ChainClusterDB> getSequenceData(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        ChainClusterDAO chainClusterDAO = new ChainClusterDAOMongo();
        return chainClusterDAO.getChainClusters(pdbInfo.getUid());
    }

    /**
     * Retrieves residue data for job and interface id.
     * @param jobId identifier of the job
     * @param interfId the interface id
     * @return residue data corresponding to job id and interface id
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<ResidueInfoDB> getResidueData(String jobId, int interfId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        InterfaceDAO interfaceDAO = new InterfaceDAOMongo();
        InterfaceDB interf = interfaceDAO.getInterface(pdbInfo.getUid(), interfId, false, false);
        ResidueDAO rdao = new ResidueDAOMongo();
        return rdao.getResiduesForInterface(interf.getUid());
    }

    /**
     * Retrieves contact data for job and interface id
     * @param jobId identifier of the job
     * @param interfId the interface id
     * @return contact data
     * @throws DaoException when can not retrieve result of the job
     */
    public static List<ContactDB> getContactData(String jobId, int interfId) throws DaoException {
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        ContactDAO contactDAO = new ContactDAOMongo();
        List<ContactDB> list = contactDAO.getContactsForInterface(pdbInfo.getUid(), interfId);

        if (list==null) {
            throw new NoResultException("Could not find contact data for job "+jobId+" and interface id "+interfId);
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
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOMongo();
        AssemblyDB assembly = assemblyDAO.getAssemblyByPdbAssemblyId(pdbInfo.getUid(), pdbAssemblyId, true);

        if (assembly==null) {
            throw new NoResultException("Could not find assembly data for job "+jobId+" and PDB assembly id "+pdbAssemblyId);
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
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOMongo();
        AssemblyDB assembly = assemblyDAO.getAssembly(pdbInfo.getUid(), assemblyId, true);

        if (assembly==null) {
            throw new NoResultException("Could not find assembly data for job "+jobId+" and PDB assembly id "+assemblyId);
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
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOMongo();
        AssemblyDB assembly = assemblyDAO.getAssembly(pdbInfo.getUid(), assemblyId, true);
        AssemblyDB unitcellAssembly = assemblyDAO.getAssembly(pdbInfo.getUid(), 0, true);

        if (assembly==null || unitcellAssembly == null) {
            throw new NoResultException("Could not find assembly data for job "+jobId+" and PDB assembly id "+assemblyId);
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
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOMongo();

        List<GraphEdgeDB> graphEdges = new ArrayList<>();

        AssemblyDB unitcellAssembly = assemblyDAO.getAssembly(pdbInfo.getUid(), 0, true);

        if (unitcellAssembly == null) {
            throw new NoResultException("Could not find unitcell assembly data for job "+jobId+" and PDB assembly id 0");
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
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOMongo();

        List<GraphEdgeDB> graphEdges = new ArrayList<>();

        AssemblyDB unitcellAssembly = assemblyDAO.getAssembly(pdbInfo.getUid(), 0, true);

        if (unitcellAssembly == null) {
            throw new NoResultException("Could not find unitcell assembly data for job "+jobId+" and PDB assembly id 0");
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
        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
        PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOMongo();
        AssemblyDB assembly = assemblyDAO.getAssembly(pdbInfo.getUid(), assemblyId, true);

        if (assembly==null) {
            throw new NoResultException("Could not find assembly data for job "+jobId+" and PDB assembly id "+assemblyId);
        }

        return ViewsAdaptor.getAssemblyDiagram(assembly);
    }

}
