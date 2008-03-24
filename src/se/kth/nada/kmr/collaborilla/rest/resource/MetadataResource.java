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
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import se.kth.nada.kmr.collaborilla.ldap.CollaborillaObject;
import se.kth.nada.kmr.collaborilla.rest.LDAPCommunicator;
import se.kth.nada.kmr.collaborilla.util.URIHelper;

import com.novell.ldap.LDAPException;

public class MetadataResource extends Resource {

	Log log = LogFactory.getLog(MetadataResource.class);

	private URI uri;

	public MetadataResource(Context context, Request request, Response response) {
		super(context, request, response);
		try {
			uri = URIHelper.extractURI(request);
		} catch (ResourceException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		getVariants().add(new Variant(MediaType.APPLICATION_RDF_XML));
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		Representation result = null;
		String rdfMetadata = getMetadata();
		if (rdfMetadata == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		if (variant.getMediaType().equals(MediaType.APPLICATION_RDF_XML)) {
			result = new StringRepresentation(rdfMetadata, MediaType.APPLICATION_RDF_XML);
		} else {
			// TODO JSON here
			//result = Representation.createEmpty();
			result = new StringRepresentation("{\"json\":\"not supported yet\"}", MediaType.APPLICATION_JSON);
		}
		
		return result;
	}
	
	@Override
	public boolean allowPut() {
		return true;
	}
	
	@Override
	public void storeRepresentation(Representation representation) throws ResourceException {
		if (representation.getMediaType().equals(MediaType.APPLICATION_RDF_XML)) {
			String md = null;
			try {
				md = representation.getText();
			} catch (IOException e) {
				log.error(e.getMessage());
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			}
			if (md != null) {
				setMetadata(md);
			}
		} else {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		}
	}

	private String getMetadata() throws ResourceException {
		LDAPCommunicator ldapC = new LDAPCommunicator();
		CollaborillaObject co = ldapC.getCollaborillaObject(uri, false);

		if (co != null) {
			try {
				return co.getMetaData();
			} catch (LDAPException e) {
				log.error(e.getMessage());
			}
		}

		return null;
	}
	
	private void setMetadata(String rdfMetadata) throws ResourceException {
		LDAPCommunicator ldapC = new LDAPCommunicator();
		CollaborillaObject co = ldapC.getCollaborillaObject(uri, true);

		if (co != null) {
			try {
				co.setMetaData(rdfMetadata);
			} catch (LDAPException e) {
				log.error(e.getMessage());
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			}
		}
	}
	
}