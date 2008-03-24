package se.kth.nada.kmr.collaborilla.rest.resource;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import se.kth.nada.kmr.collaborilla.ldap.CollaborillaObject;
import se.kth.nada.kmr.collaborilla.rest.LDAPCommunicator;
import se.kth.nada.kmr.collaborilla.util.URIHelper;

import com.novell.ldap.LDAPException;

public class ContainerResource extends Resource {

	Log log = LogFactory.getLog(ContainerResource.class);

	public ContainerResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		URI uri = null;
		try {
			uri = URIHelper.extractURI(request);
		} catch (ResourceException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		
		String location = getLocation(uri);
		if (location == null) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} else {
			response.redirectTemporary(location);
		}
	}

	private String getLocation(URI uri) {
		LDAPCommunicator ldapC = new LDAPCommunicator();
		CollaborillaObject co;
		try {
			co = ldapC.getCollaborillaObject(uri, false);
		} catch (ResourceException e) {
			log.info(e.getMessage());
			return null;
		}
		
		String[] loc = null;
		if (co != null) {
			try {
				loc = co.getLocation();
			} catch (LDAPException e) {
				log.info(e.getMessage());
			}
		}
		
		if ((loc != null) && (loc.length > 0)) {
			return loc[0];
		}

		return null;
	}
	
}