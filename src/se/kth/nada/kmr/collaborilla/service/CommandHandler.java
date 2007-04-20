/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import se.kth.nada.kmr.collaborilla.client.CollaborillaDataSet;
import se.kth.nada.kmr.collaborilla.ldap.CollaborillaObject;
import se.kth.nada.kmr.collaborilla.ldap.LDAPAccess;
import se.kth.nada.kmr.collaborilla.ldap.LDAPStringHelper;
import se.kth.nada.kmr.collaborilla.util.Configuration;
import se.kth.nada.kmr.collaborilla.util.InfoMessage;

import com.novell.ldap.LDAPException;

/**
 * Takes client requests and parses them. Requests and transfers data from and
 * to the LDAP service. Creates a response message to be sent to client.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class CommandHandler {
	
	private CollaborillaObject collabObject;

	private LDAPAccess ldapConnection;

	private String serverDN;

	private InfoMessage log = InfoMessage.getInstance();

	private String availableCommands = "HLP                                \n"
			+ "GET <uri> <rev nr> \n"
			+ "URI <uri>                          \n" + "URI NEW <uri>                      \n\n"
			+ "GET REVISIONCOUNT                  \n" + "GET REVISION                       \n"
			+ "SET REVISION <rev nr>              \n" + "GET REVISIONINFO <rev nr>          \n"
			+ "ADD REVISION                       \n" + "RST REVISION <rev nr>              \n\n"
			+ "GET ALIGNEDURL                     \n" + "GET URL                            \n"
			+ "ADD URL <url>                      \n" + "DEL URL <url>                      \n\n"
			+ "GET REQUIREDCONTAINER              \n" + "ADD REQUIREDCONTAINER <uri>        \n"
			+ "DEL REQUIREDCONTAINER <uri>        \n\n" + "GET OPTIONALCONTAINER            \n"
			+ "ADD OPTIONALCONTAINER <uri>        \n" + "DEL OPTIONALCONTAINER <uri>        \n\n"
			+ "GET DESC                           \n" + "SET DESC <description>             \n"
			+ "DEL DESC                           \n\n" + "GET METADATA                     \n"
			+ "SET METADATA <rdf data>      	  \n" + "DEL METADATA                       \n\n"
			+ "GET TYPE                           \n" + "SET TYPE <type>                    \n"
			+ "DEL TYPE                           \n\n"
			+ "GET CONTAINERREVISION              \n" + "SET CONTAINERREVISION <rev nr>     \n\n"
			+ "GET LDIF                           \n\n"	+ "GET TIMESTAMPCREATED             \n"
			+ "GET TIMESTAMPMODIFIED              \n\n" + "GET DATASET";

	/**
	 * @param ldapConn LDAPAccess object, contains the connection to specific LDAP server.
	 * @param serverDN The context on the LDAP server (server DN).
	 */
	public CommandHandler(LDAPAccess ldapConn, String serverDN) {
		this.ldapConnection = ldapConn;
		this.serverDN = serverDN;
	}

	/**
	 * Takes the received string from the client, parses it and delegates the
	 * command and its parameters to the responsible methods.
	 * 
	 * @param request
	 *            Received request from the client
	 * @return Response to the client
	 */
	public ResponseMessage processRequest(String request) {
		/* split the string */
		StringTokenizer requestTokens = new StringTokenizer(request, " ");
		int paramCount = requestTokens.countTokens();

		String[] command = new String[paramCount];
		int i = 0;

		while (requestTokens.hasMoreTokens()) {
			command[i++] = requestTokens.nextToken();
		}

		/* if we got just one word there is only one legal command */
		if (paramCount == 1) {
			if (command[0].equalsIgnoreCase(ServiceCommands.CMD_HELP)) {
				return this.handleHelp();
			}
		}

		/* all other commands need at least to words */
		if (paramCount < 2) {
			return new ResponseMessage(Status.SC_BAD_REQUEST);
		}

		/* commands which start with URI */
		if (command[0].equalsIgnoreCase(ServiceCommands.CMD_URI)) {
			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.CMD_URI_NEW)) {
				return this.handleNewUri(command[2]);
			}

			if (paramCount >= 2) {
				return this.handleUri(command[1]);
			}
		}
		
		if ((collabObject == null) && command[0].equalsIgnoreCase(ServiceCommands.CMD_GET)) {
			if (paramCount == 3) {
				return handelGetUriRevision(command[1], command[2]);
			}
		}

		/*
		 * if the command was not URI or a GET (with URI and revision) and the
		 * LDAP object does not exist we return an error
		 */
		if (collabObject == null) {
			return new ResponseMessage(Status.SC_BAD_REQUEST);
		}

		/* GET commands */
		if (command[0].equalsIgnoreCase(ServiceCommands.CMD_GET)) {
			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_REQUIRED_CONTAINER)) {
				return this.handleGetRequiredContainers();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_OPTIONAL_CONTAINER)) {
				return this.handleGetOptionalContainers();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_LOCATION)) {
				return this.handleGetLocation();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_ALIGNEDLOCATION)) {
				return this.handleGetAlignedLocation();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_LDIF)) {
				return this.handleGetLdif();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_DESCRIPTION)) {
				return this.handleGetDescription();
			}
			
			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_TYPE)) {
				return this.handleGetType();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_METADATA)) {
				return this.handleGetMetaData();
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_REVISION_INFO)) {
				return this.handleGetRevisionInfo(command[2]);
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_REVISION)) {
				return this.handleGetRevision();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_CONTAINER_REVISION)) {
				return this.handleGetContainerRevision();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_REVISION_COUNT)) {
				return this.handleGetRevisionCount();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_INTERNAL_TIMESTAMP_CREATED)) {
				return this.handleGetTimestampCreated();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_INTERNAL_TIMESTAMP_MODIFIED)) {
				return this.handleGetTimestampModified();
			}
			
			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_DATASET)) {
				return this.handleGetDataSet();
			}
		}

		/* SET commands */
		if (command[0].equalsIgnoreCase(ServiceCommands.CMD_SET)) {
			/* we need more than just word, so we concat again ...not the most
			 * intelligent solution though...
			 */
			String setParam = new String();
			for (int j = 2; j < command.length; j++) {
				setParam += command[j];
				if (j < (command.length - 1)) {
					setParam += " ";
				}
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_REVISION)) {
				return this.handleSetRevision(setParam);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_DESCRIPTION)) {
				return this.handleSetDescription(setParam);
			}
			
			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_TYPE)) {
				return this.handleSetType(setParam);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_METADATA)) {
				return this.handleSetMetaData(setParam);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_CONTAINER_REVISION)) {
				return this.handleSetContainerRevision(setParam);
			}
		}

		/* ADD commands */
		if (command[0].equalsIgnoreCase(ServiceCommands.CMD_ADD)) {
			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_REVISION)) {
				return this.handleAddRevision();
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_REQUIRED_CONTAINER)) {
				return this.handleAddRequiredContainer(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_OPTIONAL_CONTAINER)) {
				return this.handleAddOptionalContainer(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_LOCATION)) {
				return this.handleAddLocation(command[2]);
			}
		}

		/* DEL commands */
		if (command[0].equalsIgnoreCase(ServiceCommands.CMD_DEL)) {
			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_METADATA)) {
				return this.handleDelMetaData();
			}

			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_DESCRIPTION)) {
				return this.handleDelDescription();
			}
			
			if (command[1].equalsIgnoreCase(ServiceCommands.ATTR_TYPE)) {
				return this.handleDelType();
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_REQUIRED_CONTAINER)) {
				return this.handleDelRequiredContainer(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_OPTIONAL_CONTAINER)) {
				return this.handleDelOptionalContainer(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_LOCATION)) {
				return this.handleDelLocation(command[2]);
			}
		}

		/* RESTORE REVISION */
		if (command[0].equalsIgnoreCase(ServiceCommands.CMD_RESTORE)) {
			if ((paramCount >= 3) && command[1].equalsIgnoreCase(ServiceCommands.ATTR_REVISION)) {
				return this.handleRestoreRevision(command[2]);
			}
		}

		/* if nothing matches we got a bad request */
		return new ResponseMessage(Status.SC_BAD_REQUEST);
	}
	
	public boolean hasCollaborillaObject() {
		return (collabObject != null);
	}
	
	private String decodeURL(String uri) {
		try {
			return URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return uri;
		}
	}

	/*
	 * command handling follows, method names are self-explaining
	 */
	
	private ResponseMessage handelGetUriRevision(String uri, String revision) {
		String result;
		
		try {
			int rev = Integer.parseInt(revision);
			collabObject = new CollaborillaObject(ldapConnection, serverDN, decodeURL(uri), false);
			if (rev > 0) {
				collabObject.setRevision(rev);
			}
			CollaborillaDataSet dataSet = collabObject.getDataSet();
			collabObject = null; // we don't want this object to persist
			result = dataSet.toXML();
		} catch (NumberFormatException nfe) {
			return new ResponseMessage(Status.SC_BAD_REQUEST);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new ResponseMessage(Status.SC_NO_SUCH_OBJECT);
			}

			this.log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}
		
		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleUri(String uri) {
		try {
			collabObject = new CollaborillaObject(ldapConnection, serverDN, decodeURL(uri), false);

			return new ResponseMessage(Status.SC_OK);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new ResponseMessage(Status.SC_NO_SUCH_OBJECT);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}
	}

	private ResponseMessage handleNewUri(String uri) {
		try {
			collabObject = new CollaborillaObject(ldapConnection, serverDN, decodeURL(uri), true);

			return new ResponseMessage(Status.SC_OK);
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}
	}

	private ResponseMessage handleHelp() {
		return new ResponseMessage(Status.SC_OK, availableCommands);
	}

	private ResponseMessage handleGetRequiredContainers() {
		String[] uris;

		try {
			uris = collabObject.getRequiredContainers();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (uris == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < uris.length; i++) {
			if (i == (uris.length - 1)) {
				result += uris[i];
			} else {
				result += uris[i] + Configuration.LINEFEED;
			}
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetOptionalContainers() {
		String[] uris;

		try {
			uris = collabObject.getOptionalContainers();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (uris == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < uris.length; i++) {
			if (i == (uris.length - 1)) {
				result += uris[i];
			} else {
				result += uris[i] + Configuration.LINEFEED;
			}
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetLocation() {
		String[] urls;

		try {
			urls = collabObject.getLocation();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (urls == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < urls.length; i++) {
			if (i == (urls.length - 1)) {
				result += urls[i];
			} else {
				result += urls[i] + Configuration.LINEFEED;
			}
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetAlignedLocation() {
		String[] urls;

		try {
			urls = collabObject.getAlignedLocation();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
			} else {
				this.log.write(e.toString());
				return new ResponseMessage(Status.SC_INTERNAL_ERROR);
			}
		}

		if (urls == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < urls.length; i++) {
			if (i == (urls.length - 1)) {
				result += urls[i];
			} else {
				result += urls[i] + Configuration.LINEFEED;
			}
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetDescription() {
		String result;

		try {
			result = collabObject.getDescription();
		} catch (Exception e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}
	
	private ResponseMessage handleGetType() {
		String result;

		try {
			result = collabObject.getType();
		} catch (Exception e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetLdif() {
		String result;

		try {
			result = collabObject.getLdif();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}
	
	private ResponseMessage handleGetDataSet() {
		String result;
		
		try {
			CollaborillaDataSet dataSet = collabObject.getDataSet();
			result = dataSet.toXML();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}
		
		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetMetaData() {
		String result;

		try {
			result = collabObject.getMetaData();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetContainerRevision() {
		String result;

		try {
			result = collabObject.getContainerRevision();
		} catch (Exception e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetRevision() {
		String result;

		try {
			result = String.valueOf(collabObject.getRevision());
		} catch (Exception e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetRevisionCount() {
		String result;

		try {
			result = String.valueOf(collabObject.getRevisionCount());
		} catch (Exception e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetRevisionInfo(String rev) {
		String result;

		try {
			result = collabObject.getRevisionInfo(Integer.parseInt(rev));
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new ResponseMessage(Status.SC_NO_SUCH_OBJECT);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleSetRevision(String strRevision) {
		try {
			int rev = Integer.parseInt(strRevision);
			collabObject.setRevision(rev);
		} catch (NumberFormatException e) {
			// We didn't get an integer
			return new ResponseMessage(Status.SC_BAD_REQUEST);
		} catch (LDAPException le) {
			if (le.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new ResponseMessage(Status.SC_NO_SUCH_OBJECT);
			}
			
			if (le.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(le.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleSetDescription(String description) {
		try {
			collabObject.setDescription(LDAPStringHelper.decode(description));
		} catch (LDAPException le) {
			if (le.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(le.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}
	
	private ResponseMessage handleSetType(String type) {
		try {
			collabObject.setType(LDAPStringHelper.decode(type));
		} catch (LDAPException le) {
			if (le.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(le.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleSetMetaData(String rdfInfo) {
		try {
			collabObject.setMetaData(LDAPStringHelper.decode(rdfInfo));
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleSetContainerRevision(String containerRevision) {
		try {
			collabObject.setContainerRevision(containerRevision);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleAddRevision() {
		try {
			collabObject.createRevision();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleAddRequiredContainer(String uri) {
		try {
			collabObject.addRequiredContainer(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				return new ResponseMessage(Status.SC_ATTRIBUTE_OR_VALUE_EXISTS);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleAddOptionalContainer(String uri) {
		try {
			collabObject.addOptionalContainer(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				return new ResponseMessage(Status.SC_ATTRIBUTE_OR_VALUE_EXISTS);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleAddLocation(String url) {
		try {
			collabObject.addLocation(url);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				return new ResponseMessage(Status.SC_ATTRIBUTE_OR_VALUE_EXISTS);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleRestoreRevision(String strRevision) {
		try {
			int rev = Integer.parseInt(strRevision);
			collabObject.restoreRevision(rev);
		} catch (NumberFormatException e) {
			/* we didn't get an integer */
			return new ResponseMessage(Status.SC_BAD_REQUEST);
		} catch (LDAPException le) {
			if (le.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new ResponseMessage(Status.SC_NO_SUCH_OBJECT);
			}

			log.write(le.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleDelLocation(String url) {
		try {
			collabObject.removeLocation(url);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleDelRequiredContainer(String uri) {
		try {
			collabObject.removeRequiredContainer(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleDelOptionalContainer(String uri) {
		try {
			collabObject.removeOptionalContainer(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleDelMetaData() {
		try {
			collabObject.removeMetaData();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleDelDescription() {
		try {
			collabObject.removeDescription();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}
			
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}
	
	private ResponseMessage handleDelType() {
		try {
			collabObject.removeType();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new ResponseMessage(Status.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new ResponseMessage(Status.SC_REVISION_NOT_EDITABLE);
			}

			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK);
	}

	private ResponseMessage handleGetTimestampCreated() {
		String result = null;

		try {
			result = collabObject.getTimestampCreatedAsString();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

	private ResponseMessage handleGetTimestampModified() {
		String result = null;

		try {
			result = collabObject.getTimestampModifiedAsString();
		} catch (LDAPException e) {
			log.write(e.toString());
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new ResponseMessage(Status.SC_INTERNAL_ERROR);
		}

		return new ResponseMessage(Status.SC_OK, result);
	}

}