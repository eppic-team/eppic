package ch.systemsx.sybit.server.db.entitylisteners;

//import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

/**
 * ContactDB listener to avoid NaNs in burial values
 * TODO we should not need this once it is fixed in owl
 */
import eppic.model.ContactDB;

public class ContactListener {

	@PrePersist
	public void prePersist(ContactDB contactDB)
	{
		if(Double.isNaN(contactDB.getFirstBurial()))
		{
			contactDB.setFirstBurial(0);
		}


		if(Double.isNaN(contactDB.getSecondBurial()))
		{
			contactDB.setSecondBurial(0);
		}
		
	}

	
}
