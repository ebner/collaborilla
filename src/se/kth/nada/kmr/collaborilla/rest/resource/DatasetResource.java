package se.kth.nada.kmr.collaborilla.rest.resource;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import se.kth.nada.kmr.collaborilla.client.CollaborillaDataSet;
import se.kth.nada.kmr.collaborilla.ldap.CollaborillaObject;
import se.kth.nada.kmr.collaborilla.rest.LDAPCommunicator;
import se.kth.nada.kmr.collaborilla.util.URIHelper;

import com.novell.ldap.LDAPException;

public class DatasetResource extends Resource {
	
	Log log = LogFactory.getLog(DatasetResource.class);

	private URI uri;

	public DatasetResource(Context context, Request request, Response response) {
		super(context, request, response);
		try {
			uri = URIHelper.extractURI(request);
		} catch (ResourceException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
	}

	/**
	 * Returns a full representation for a given variant.
	 */
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		Representation result = null;
		CollaborillaDataSet dataset = getCollaborillaDataSet();
		
		if (variant.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
			result = new ObjectRepresentation<CollaborillaDataSet>(dataset);
			result.setMediaType(MediaType.APPLICATION_JAVA_OBJECT);
		} else {
			result = new JsonRepresentation(dataset);
			result.setMediaType(MediaType.APPLICATION_JSON);
		}

		return result;
	}
	
	@Override
	public boolean allowPut() {
		return true;
	}
	
	@Override
	public void storeRepresentation(Representation representation) throws ResourceException {
		if (representation.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
			ObjectRepresentation or = null;
			try {
				or = new ObjectRepresentation<CollaborillaDataSet>(representation);
			} catch (IOException e) {
				log.info(e.getMessage());
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			} catch (IllegalArgumentException e) {
				log.info(e.getMessage());
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage());
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
			if (or != null) {
				CollaborillaDataSet dataset = null;
				try {
					dataset = (CollaborillaDataSet) or.getObject();
				} catch (IOException e) {
					log.error(e.getMessage());
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
				}
				if (dataset != null) {
					setCollaborillaDataSet(dataset);
				} else {
					log.error("Dataset is null");
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
				}
			}
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		}
	}

	private CollaborillaDataSet getCollaborillaDataSet() throws ResourceException {
		LDAPCommunicator ldapC = new LDAPCommunicator();
		CollaborillaObject co = ldapC.getCollaborillaObject(uri, false);
		CollaborillaDataSet ds = null;
		if (co != null) {
			try {
				ds = co.getDataSet();
			} catch (LDAPException e) {
				log.error(e.getMessage());
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			}
		}

		return ds;
	}
	
	private void setCollaborillaDataSet(CollaborillaDataSet dataset) throws ResourceException {
		LDAPCommunicator ldapC = new LDAPCommunicator();
		CollaborillaObject co = ldapC.getCollaborillaObject(uri, true);
		if (co != null) {
			try {
				co.setDataSet(dataset);
			} catch (LDAPException e) {
				log.error(e.getMessage());
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			} catch (IllegalArgumentException iae) {
				log.info(iae.getMessage());
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, iae.getMessage());
			}
		}
	}

}