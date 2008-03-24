/**
 * 
 */
package se.kth.nada.kmr.collaborilla.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Client;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;

import se.kth.nada.kmr.collaborilla.util.Configuration;
import se.kth.nada.kmr.collaborilla.util.URIHelper;

/**
 * @author Hannes Ebner
 */
public class CollaborillaRestClient implements CollaborillaStatelessClient {
	
	Logger log = Logger.getLogger(CollaborillaRestClient.class.toString());
	
	private String root;
	
	public CollaborillaRestClient(String serviceRoot) {
		this.root = serviceRoot;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#get(java.net.URI)
	 */
	public CollaborillaDataSet get(URI uri) {
		Request request = new Request(Method.GET, createResourceURI("element", uri));
		ClientInfo clientInfo = new ClientInfo();
		clientInfo.setAgent("Collaborilla Client " + Configuration.APPVERSION);
		List<Preference<MediaType>> preferences = new ArrayList<Preference<MediaType>>();
		preferences.add(new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT));
		clientInfo.setAcceptedMediaTypes(preferences);
		request.setClientInfo(clientInfo);
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		Representation rep = response.getEntity();
		CollaborillaDataSet result = null;

		if (rep != null) {
			try {
				result = new ObjectRepresentation<CollaborillaDataSet>(rep).getObject();
			} catch (IllegalArgumentException e) {
				log.log(Level.FINER, e.getMessage());
				log.info("No published data available for " + uri);
			} catch (IOException e) {
				log.log(Level.WARNING, e.getMessage());
			} catch (ClassNotFoundException e) {
				log.log(Level.SEVERE, e.getMessage());
			}
		}
		return result;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#put(java.net.URI, se.kth.nada.kmr.collaborilla.client.CollaborillaDataSet)
	 */
	public boolean put(URI uri, CollaborillaDataSet dataSet) {
		Request request = new Request(Method.PUT, createResourceURI("element", uri));
		ClientInfo clientInfo = new ClientInfo();
		clientInfo.setAgent("Collaborilla Client " + Configuration.APPVERSION);
		request.setClientInfo(clientInfo);
		Representation rep = new ObjectRepresentation<CollaborillaDataSet>(dataSet);
		rep.setMediaType(MediaType.APPLICATION_JAVA_OBJECT);
		request.setEntity(rep);
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		return response.getStatus().isSuccess();
	}
	
	public String createResourceURI(String resource, URI uri) {
		String result = root;
		if (!root.endsWith("/")) {
			result += "/";
		}
		result += resource + "/";
		result += URIHelper.encodeURI(uri.toASCIIString());	
		return result;
	}
	
	/* Unimplemented methods */
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#delete(java.net.URI)
	 */
	public void delete(URI uri) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#get(java.net.URI, int)
	 */
	public CollaborillaDataSet get(URI uri, int revision) {
		throw new UnsupportedOperationException();
	}

}