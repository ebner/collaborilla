package se.kth.nada.kmr.collaborilla.client;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

public class CollaborillaCouchDbRepository extends CouchDbRepositorySupport<CollaborillaDataSet> {

	protected CollaborillaCouchDbRepository(CouchDbConnector connector) {
		super(CollaborillaDataSet.class, connector);
	}

}