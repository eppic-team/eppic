package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.PdbInfoDB;

import javax.persistence.NoResultException;
import javax.persistence.Table;
import java.util.*;

public class PDBInfoDAOMongo implements PDBInfoDAO {

    private final MongoDatabase mongoDb;
    private final String pdbInfoCollectionName;
    private final String sequencesCollectionName;

    public PDBInfoDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table pdbInfoTableMetadata = MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class);
        this.pdbInfoCollectionName = pdbInfoTableMetadata.name();
        Table sequencesTableMetadata = MongoUtils.getTableMetadataFromJpa(ChainClusterDB.class);
        this.sequencesCollectionName = sequencesTableMetadata.name();
    }

    @Override
    public PdbInfoDB getPDBInfo(String entryId, boolean withSequenceData) throws DaoException {
        PdbInfoDB pdbInfoDB;
        try {
            pdbInfoDB = MongoUtils.findOne(mongoDb, pdbInfoCollectionName,
                    Collections.singletonList("entryId"),
                    Collections.singletonList(entryId),
                    PdbInfoDB.class);
        } catch (Exception e) {
            throw new DaoException(e);
        }

        if (pdbInfoDB == null) {
            throw new NoResultException("Could not find PDBInfo data for id " + entryId);
        }

        if (withSequenceData) {
            List<ChainClusterDB> sequences =  new ArrayList<>();
            pdbInfoDB.getPolyEntityIds().forEach(entityId -> {
                sequences.addAll(MongoUtils.findAll(mongoDb, sequencesCollectionName, List.of("entryId", "entityId"), List.of(entryId, entityId), ChainClusterDB.class));
            });
            pdbInfoDB.setChainClusters(sequences);
        }

        return pdbInfoDB;
    }

    @Override
    public void insertPDBInfo(PdbInfoDB pdbInfo) throws DaoException {
        try {
            List<ChainClusterDB> sequences = pdbInfo.getChainClusters();
            // remove chain clusters before persisting
            pdbInfo.setChainClusters(null);
            MongoUtils.writeObject(mongoDb, pdbInfoCollectionName, pdbInfo);

            // put the original pdbInfo as it was
            pdbInfo.setChainClusters(sequences);

            // write sequences data to separate collection
            sequences.forEach(chainClusterDB -> {
                MongoUtils.writeObject(mongoDb, sequencesCollectionName, chainClusterDB);
            });

        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void insertPDBInfos(List<PdbInfoDB> pdbInfos) throws DaoException {
        try {
            Map<String, List<ChainClusterDB>> entriesToSequences = new HashMap<>();
            List<ChainClusterDB> allSequences = new ArrayList<>();
            pdbInfos.forEach(pdbInfo -> {
                entriesToSequences.put(pdbInfo.getEntryId(), pdbInfo.getChainClusters());
                pdbInfo.setChainClusters(null);
            });
            entriesToSequences.values().forEach(allSequences::addAll);
            MongoUtils.writeObjects(mongoDb, pdbInfoCollectionName, pdbInfos);

            // put the original pdbInfos as they were
            pdbInfos.forEach(pdbInfo -> pdbInfo.setChainClusters(entriesToSequences.get(pdbInfo.getEntryId())));

            // write to sequences collection
            MongoUtils.writeObjects(mongoDb, sequencesCollectionName, allSequences);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public List<PdbInfoDB> getAll() throws DaoException {
       List<PdbInfoDB> list;
       try {
           list = MongoUtils.findAll(mongoDb, pdbInfoCollectionName, PdbInfoDB.class);
       } catch (Exception e) {
           throw new DaoException(e);
       }
       return list;
    }

    @Override
    public long remove(String entryId) throws DaoException {
        return MongoUtils.remove(mongoDb, pdbInfoCollectionName, "entryId", List.of(entryId));
    }
}
