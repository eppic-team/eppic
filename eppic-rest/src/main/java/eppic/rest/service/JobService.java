package eppic.rest.service;

import java.util.*;

import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.InputWithType;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.db.dao.*;
import eppic.db.dao.jpa.*;

/**
 * Servlet used to download results in xml/json format.
 * Adapted to both json or xml by using eclipselink JAXB implementation.
 * @author Nikhil Biyani
 * @author Jose Duarte
 *
 */
@PersistenceContext(name="eppicjpa", unitName="eppicjpa")
public class JobService {

    /**
     * The servlet name, note that the name is defined in the web.xml file.
     */
    public static final String SERVLET_NAME = "dataDownload";

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
    public static PdbInfo getResultData(String jobId,
                                        boolean getInterfaceInfo,
                                        boolean getAssemblyInfo,
                                        boolean getSeqInfo,
                                        boolean getResInfo) throws DaoException
    {
        JobDAO jobDAO = new JobDAOJpa();
        InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
        PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
        pdbInfo.setInputType(input.getInputType());
        pdbInfo.setInputName(input.getInputName());


        // retrieving interface clusters data only if requested
        if (getInterfaceInfo) {
            InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOJpa();
            List<InterfaceCluster> clusters = clusterDAO.getInterfaceClustersWithoutInterfaces(pdbInfo.getUid());

            InterfaceDAO interfaceDAO = new InterfaceDAOJpa();

            for (InterfaceCluster cluster : clusters) {

                logger.debug("Getting data for interface cluster uid {}", cluster.getUid());
                List<Interface> interfaceItems;
                if (getResInfo)
                    interfaceItems = interfaceDAO.getInterfacesWithResidues(cluster.getUid());
                else
                    interfaceItems = interfaceDAO.getInterfacesWithScores(cluster.getUid());
                cluster.setInterfaces(interfaceItems);
            }

            pdbInfo.setInterfaceClusters(clusters);
        } else {
            pdbInfo.setInterfaceClusters(null);
        }

        if(getSeqInfo){
            ChainClusterDAO chainClusterDAO = new ChainClusterDAOJpa();
            List<ChainCluster> chainClusters = chainClusterDAO.getChainClusters(pdbInfo.getUid());
            pdbInfo.setChainClusters(chainClusters);
        } else {
            pdbInfo.setChainClusters(null);
        }

        if (getAssemblyInfo) {
            // assemblies info
            AssemblyDAO assemblyDAO = new AssemblyDAOJpa();

            List<Assembly> assemblies = assemblyDAO.getAssemblies(pdbInfo.getUid());

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
    public static List<Assembly> getAssemblyData(String jobId) throws DaoException {

        PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
        PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

        // assemblies info
        AssemblyDAO assemblyDAO = new AssemblyDAOJpa();

        return assemblyDAO.getAssemblies(pdbInfo.getUid());
    }


}
