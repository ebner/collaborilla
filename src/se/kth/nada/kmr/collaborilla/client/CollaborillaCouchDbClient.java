package se.kth.nada.kmr.collaborilla.client;

import java.net.URI;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.http.StdHttpClient.Builder;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import com.hp.hpl.jena.reasoner.IllegalParameterException;

public class CollaborillaCouchDbClient implements CollaborillaStatelessClient {
	
	private CouchDbConnector con;
	
	private CollaborillaCouchDbRepository repo;
	
	public CollaborillaCouchDbClient(String host, int port) {
		if (host == null) {
			throw new IllegalParameterException("CouchDB host must not be null");
		}
		Builder httpClientBuilder = new StdHttpClient.Builder().host(host);
		if (port > 0) {
			httpClientBuilder.port(port);
		} else {
			httpClientBuilder.port(80);
		}
		HttpClient httpClient = httpClientBuilder.build();
		CouchDbInstance couchDbInstance = new StdCouchDbInstance(httpClient);
		con = new StdCouchDbConnector("collaborilla", couchDbInstance);
		con.createDatabaseIfNotExists();
		repo = new CollaborillaCouchDbRepository(con);
	}

	public CollaborillaDataSet get(URI uri) {
		String id = uriToCouchDbId(uri);
		if (repo.contains(id)) {
			return repo.get(id);
		} else {
			return null;
		}
	}

	public CollaborillaDataSet get(URI uri, int revision) {
		throw new UnsupportedOperationException();
	}

	public boolean put(URI uri, CollaborillaDataSet dataSet) {
		String id = uriToCouchDbId(uri);
		if (repo.contains(id)) {
			repo.update(dataSet);
		} else {
			repo.add(dataSet);
		}
		return true;
	}

	public void delete(URI uri) {
		throw new UnsupportedOperationException();
	}
	
	public String uriToCouchDbId(URI uri) {
		return uri.toASCIIString();
//		String id = null;
//		try {
//			id = URLEncoder.encode(uri.toASCIIString(), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		if (id == null) {
//			id = uri.toASCIIString();
//		}
//		return id;
	}
	
}