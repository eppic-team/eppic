package eppic.db.mongoutils;

import eppic.model.db.AssemblyDB;
import eppic.model.db.BlobDB;
import eppic.model.db.HitHspDB;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.JobDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.UniProtInfoDB;
import eppic.model.db.UniProtMetadataDB;
import jakarta.persistence.Table;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMongoUtils {

	/**
	 * The collections and their indexes are created from the Table annotations of the model
	 * classes. If MongoUtils looked for an annotation type other than the one the model classes
	 * use (e.g. javax vs jakarta persistence) it would find none, and no index would be created.
	 */
	@Test
	public void testTableMetadataIsFound() {
		Class<?>[] modelClasses = {
				AssemblyDB.class, BlobDB.class, HitHspDB.class, InterfaceResidueFeaturesDB.class,
				JobDB.class, PdbInfoDB.class, UniProtInfoDB.class, UniProtMetadataDB.class };
		for (Class<?> modelClass : modelClasses) {
			Table table = MongoUtils.getTableMetadataFromJpa(modelClass);
			assertNotNull("No Table annotation found for " + modelClass.getSimpleName(), table);
			assertFalse(table.name().isEmpty());
		}
		Table pdbInfo = MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class);
		assertEquals("PdbInfo", pdbInfo.name());
		assertTrue(pdbInfo.indexes().length > 0);
	}
}
