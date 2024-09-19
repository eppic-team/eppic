package eppic.db.loaders;

import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;

import java.util.List;

/**
 * Class that encapsulates the data for one entry
 */
public class EntryData {
    private PdbInfoDB pdbInfoDB;
    private List<InterfaceResidueFeaturesDB> interfResFeaturesDB;
    public EntryData(PdbInfoDB pdbInfoDB, List<InterfaceResidueFeaturesDB> interfResFeaturesDB) {
        this.pdbInfoDB = pdbInfoDB;
        this.interfResFeaturesDB = interfResFeaturesDB;
    }

    public PdbInfoDB getPdbInfoDB() {
        return pdbInfoDB;
    }

    public void setPdbInfoDB(PdbInfoDB pdbInfoDB) {
        this.pdbInfoDB = pdbInfoDB;
    }

    public List<InterfaceResidueFeaturesDB> getInterfResFeaturesDB() {
        return interfResFeaturesDB;
    }

    public void setInterfResFeaturesDB(List<InterfaceResidueFeaturesDB> interfResFeaturesDB) {
        this.interfResFeaturesDB = interfResFeaturesDB;
    }
}
