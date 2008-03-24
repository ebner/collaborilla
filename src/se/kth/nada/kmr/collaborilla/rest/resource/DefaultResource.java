package se.kth.nada.kmr.collaborilla.rest.resource;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import se.kth.nada.kmr.collaborilla.util.Configuration;

public class DefaultResource extends Resource {

	public DefaultResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		String about = "Collaborilla " + Configuration.APPVERSION + ", http://collaborilla.conzilla.org";	
		Representation representation = new StringRepresentation(about, MediaType.TEXT_PLAIN);
		return representation;
	}
	
}