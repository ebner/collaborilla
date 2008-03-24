package se.kth.nada.kmr.collaborilla.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public class URIHelper {
	
	public static String decodeURI(String encodedURI) {
    	String url = null;
		try {
			url = URLDecoder.decode(encodedURI, "UTF-8");
		} catch (UnsupportedEncodingException ignored) {}
		return url;
	}
	
	public static String encodeURI(String uri) {
    	String encodedURL = null;
		try {
			encodedURL = URLEncoder.encode(uri, "UTF-8");
		} catch (UnsupportedEncodingException ignored) {}
		return encodedURL;
	}
	
	public static URI extractURI(Request request) throws ResourceException {
		return extractURI("uri", request);
	}
	
	public static URI extractURI(String parameter, Request request) throws ResourceException {
    	String mapURI = decodeURI(request.getAttributes().get(parameter).toString());
		URI uri = null;
    	try {
			uri = new URI(mapURI);
		} catch (URISyntaxException urise) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, urise.getMessage());
		}
		return uri;
	}

}