/*
 $Id: $
 
 This file is part of the project Collaborilla (http://collaborilla.sf.net)
 Copyright (c) 2006 Hannes Ebner
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package se.kth.nada.kmr.collaborilla.service;

import java.util.StringTokenizer;

import se.kth.nada.kmr.collaborilla.ldap.*;
import se.kth.nada.kmr.collaborilla.util.InfoMessage;

import com.novell.ldap.LDAPException;

public class CollaborillaServiceProtocol {
	private CollaborillaObject collabObject;

	private LDAPAccess ldapConnection;

	private String serverDN;

	private InfoMessage log = InfoMessage.getInstance();

	private String availableCommands = "HLP                                \n"
			+ "URI <uri>                          \n" + "URI NEW <uri>                      \n\n"
			+ "GET REVISIONCOUNT                  \n" + "GET REVISION                       \n"
			+ "SET REVISION <rev nr>              \n" + "GET REVISIONINFO <rev nr>          \n"
			+ "ADD REVISION                       \n" + "RST REVISION <rev nr>              \n\n"
			+ "GET ALIGNEDURL                     \n" + "GET URL                            \n"
			+ "ADD URL <url>                      \n" + "MOD URL <old url> <new url>        \n"
			+ "DEL URL <url>                      \n\n" + "GET URIORIG                        \n"
			+ "ADD URIORIG <uri>                  \n" + "MOD URIORIG <old uri> <new uri>    \n"
			+ "DEL URIORIG <uri>                  \n\n" + "GET URIOTHER                       \n"
			+ "ADD URIOTHER <uri>                 \n" + "MOD URIOTHER <old uri> <new uri>   \n"
			+ "DEL URIOTHER <uri>                 \n\n" + "GET DESC                           \n"
			+ "SET DESC <description>             \n" + "DEL DESC                           \n\n"
			+ "GET CONTEXTRDFINFO                 \n" + "SET CONTEXTRDFINFO <rdf data>      \n"
			+ "DEL CONTEXTRDFINFO                 \n\n" + "GET CONTAINERRDFINFO               \n"
			+ "SET CONTAINERRDFINFO <rdf data>    \n" + "DEL CONTAINERRDFINFO               \n\n"
			+ "GET CONTAINERREVISION              \n" + "SET CONTAINERREVISION <rev nr>     \n\n"
			+ "GET LDIF                           \n\n" + "GET TIMESTAMPCREATED               \n"
			+ "GET TIMESTAMPMODIFIED              \n";

	CollaborillaServiceProtocol(LDAPAccess ldapConn, String serverDN) {
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
	public CollaborillaServiceResponse requestDistributor(String request) {
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
			if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_HELP)) {
				return this.handleHelp();
			}
		}

		/* all other commands need at least to words */
		if (paramCount < 2) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_BAD_REQUEST);
		}

		/* commands which start with URI */
		if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_URI)) {
			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.CMD_URI_NEW)) {
				return this.handleNewUri(command[2]);
			}

			if (paramCount >= 2) {
				return this.handleUri(command[1]);
			}
		}

		/*
		 * if the command was not URI and the LDAP object does not exist we
		 * return an error
		 */
		if (collabObject == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_BAD_REQUEST);
		}

		/* GET commands */
		if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_GET)) {
			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_ORIG)) {
				return this.handleGetUriOriginal();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_OTHER)) {
				return this.handleGetUriOther();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_LOCATION)) {
				return this.handleGetLocation();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_ALIGNEDLOCATION)) {
				return this.handleGetAlignedLocation();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_LDIF)) {
				return this.handleGetLdif();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_DESCRIPTION)) {
				return this.handleGetDescription();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTEXT_RDFINFO)) {
				return this.handleGetContextRdfInfo();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTAINER_RDFINFO)) {
				return this.handleGetContainerRdfInfo();
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_REVISION_INFO)) {
				return this.handleGetRevisionInfo(command[2]);
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_REVISION)) {
				return this.handleGetRevision();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTAINER_REVISION)) {
				return this.handleGetContainerRevision();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_REVISION_COUNT)) {
				return this.handleGetRevisionCount();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_INTERNAL_TIMESTAMP_CREATED)) {
				return this.handleGetTimestampCreated();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_INTERNAL_TIMESTAMP_MODIFIED)) {
				return this.handleGetTimestampModified();
			}
		}

		/* SET commands */
		if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_SET)) {
			/*
			 * we need more than just word, so we concat again ...not the most
			 * intelligent solution though...
			 */
			String setParam = new String();
			for (int j = 2; j < command.length; j++) {
				setParam += command[j];
				if (j < (command.length - 1)) {
					setParam += " ";
				}
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_REVISION)) {
				return this.handleSetRevision(setParam);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_DESCRIPTION)) {
				return this.handleSetDescription(setParam);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTEXT_RDFINFO)) {
				return this.handleSetContextRdfInfo(setParam);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTAINER_RDFINFO)) {
				return this.handleSetContainerRdfInfo(setParam);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTAINER_REVISION)) {
				return this.handleSetContainerRevision(setParam);
			}
		}

		/* MOD commands */
		if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_MOD)) {
			if ((paramCount >= 4) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_ORIG)) {
				return this.handleModUriOrig(command[2], command[3]);
			}

			if ((paramCount >= 4) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_OTHER)) {
				return this.handleModUriOther(command[2], command[3]);
			}

			if ((paramCount >= 4) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_LOCATION)) {
				return this.handleModLocation(command[2], command[3]);
			}
		}

		/* ADD commands */
		if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_ADD)) {
			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_REVISION)) {
				return this.handleAddRevision();
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_ORIG)) {
				return this.handleAddUriOrig(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_OTHER)) {
				return this.handleAddUriOther(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_LOCATION)) {
				return this.handleAddLocation(command[2]);
			}
		}

		/* DEL commands */
		if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_DEL)) {
			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTEXT_RDFINFO)) {
				return this.handleDelContextRdfInfo();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_CONTAINER_RDFINFO)) {
				return this.handleDelContainerRdfInfo();
			}

			if (command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_DESCRIPTION)) {
				return this.handleDelDescription();
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_ORIG)) {
				return this.handleDelUriOrig(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_URI_OTHER)) {
				return this.handleDelUriOther(command[2]);
			}

			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_LOCATION)) {
				return this.handleDelLocation(command[2]);
			}
		}

		/* RESTORE REVISION */
		if (command[0].equalsIgnoreCase(CollaborillaServiceCommands.CMD_RESTORE)) {
			if ((paramCount >= 3) && command[1].equalsIgnoreCase(CollaborillaServiceCommands.ATTR_REVISION)) {
				return this.handleRestoreRevision(command[2]);
			}
		}

		/* if nothing matches we got a bad request */
		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_BAD_REQUEST);
	}

	/*
	 * command handling follows, method names are self-explaining
	 */

	private CollaborillaServiceResponse handleUri(String uri) {
		try {
			this.collabObject = new CollaborillaObject(this.ldapConnection, this.serverDN, uri, false);

			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_OBJECT);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}
	}

	private CollaborillaServiceResponse handleNewUri(String uri) {
		try {
			this.collabObject = new CollaborillaObject(this.ldapConnection, this.serverDN, uri, true);

			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}
	}

	private CollaborillaServiceResponse handleHelp() {
		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, this.availableCommands);
	}

	private CollaborillaServiceResponse handleGetUriOriginal() {
		String[] uris;

		try {
			uris = this.collabObject.getUriOriginal();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (uris == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < uris.length; i++) {
			if (i == (uris.length - 1)) {
				result += uris[i];
			} else {
				result += uris[i] + "\n";
			}
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetUriOther() {
		String[] uris;

		try {
			uris = this.collabObject.getUriOther();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (uris == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < uris.length; i++) {
			if (i == (uris.length - 1)) {
				result += uris[i];
			} else {
				result += uris[i] + "\n";
			}
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetLocation() {
		String[] urls;

		try {
			urls = this.collabObject.getLocation();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (urls == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < urls.length; i++) {
			if (i == (urls.length - 1)) {
				result += urls[i];
			} else {
				result += urls[i] + "\n";
			}
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetAlignedLocation() {
		String[] urls;

		try {
			urls = this.collabObject.getAlignedLocation();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
			} else {
				this.log.write(e.toString());
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
			}
		}

		if (urls == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		String result = new String();
		for (int i = 0; i < urls.length; i++) {
			if (i == (urls.length - 1)) {
				result += urls[i];
			} else {
				result += urls[i] + "\n";
			}
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetDescription() {
		String result;

		try {
			result = this.collabObject.getDescription();
		} catch (Exception e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetLdif() {
		String result;

		try {
			result = this.collabObject.getLdif();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetContextRdfInfo() {
		String result;

		try {
			result = this.collabObject.getContextRdfInfo();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetContainerRdfInfo() {
		String result;

		try {
			result = this.collabObject.getContainerRdfInfo();
		} catch (Exception e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetContainerRevision() {
		String result;

		try {
			result = this.collabObject.getContainerRevision();
		} catch (Exception e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetRevision() {
		String result;

		try {
			result = String.valueOf(this.collabObject.getRevision());
		} catch (Exception e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetRevisionCount() {
		String result;

		try {
			result = String.valueOf(this.collabObject.getRevisionCount());
		} catch (Exception e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetRevisionInfo(String rev) {
		String result;

		try {
			result = this.collabObject.getRevisionInfo(Integer.parseInt(rev));
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_OBJECT);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleSetRevision(String strRevision) {
		try {
			int rev = Integer.parseInt(strRevision);
			this.collabObject.setRevision(rev);
		} catch (NumberFormatException e) {
			/* we didn't get an integer */
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_BAD_REQUEST);
		} catch (LDAPException le) {
			if (le.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_OBJECT);
			}
			
			if (le.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(le.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleSetDescription(String description) {
		try {
			this.collabObject.setDescription(description);
		} catch (LDAPException le) {
			if (le.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(le.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleSetContextRdfInfo(String rdfInfo) {
		try {
			this.collabObject.setContextRdfInfo(rdfInfo);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleSetContainerRdfInfo(String rdfLocationInfo) {
		try {
			this.collabObject.setContainerRdfInfo(rdfLocationInfo);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleSetContainerRevision(String containerRevision) {
		try {
			this.collabObject.setContainerRevision(containerRevision);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleModUriOrig(String oldUri, String newUri) {
		try {
			this.collabObject.modifyUriOriginal(oldUri, newUri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_VALUE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleModUriOther(String oldUri, String newUri) {
		try {
			this.collabObject.modifyUriOther(oldUri, newUri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_VALUE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleModLocation(String oldUrl, String newUrl) {
		try {
			this.collabObject.modifyLocation(oldUrl, newUrl);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_VALUE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}


			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleAddRevision() {
		try {
			this.collabObject.createRevision();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleAddUriOrig(String uri) {
		try {
			this.collabObject.addUriOriginal(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_ATTRIBUTE_OR_VALUE_EXISTS);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleAddUriOther(String uri) {
		try {
			this.collabObject.addUriOther(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_ATTRIBUTE_OR_VALUE_EXISTS);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleAddLocation(String url) {
		try {
			this.collabObject.addLocation(url);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_ATTRIBUTE_OR_VALUE_EXISTS);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleRestoreRevision(String strRevision) {
		try {
			int rev = Integer.parseInt(strRevision);
			this.collabObject.restoreRevision(rev);
		} catch (NumberFormatException e) {
			/* we didn't get an integer */
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_BAD_REQUEST);
		} catch (LDAPException le) {
			if (le.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_OBJECT);
			}

			this.log.write(le.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleDelLocation(String url) {
		try {
			this.collabObject.removeLocation(url);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleDelUriOrig(String uri) {
		try {
			this.collabObject.removeUriOriginal(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleDelUriOther(String uri) {
		try {
			this.collabObject.removeUriOther(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleDelContextRdfInfo() {
		try {
			this.collabObject.removeContextRdfInfo();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleDelContainerRdfInfo() {
		try {
			this.collabObject.removeContainerRdfInfo();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleDelDescription() {
		try {
			this.collabObject.removeDescription();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_NO_SUCH_ATTRIBUTE);
			}
			
			if (e.getResultCode() == LDAPException.UNWILLING_TO_PERFORM) {
				return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_REVISION_NOT_EDITABLE);
			}

			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK);
	}

	private CollaborillaServiceResponse handleGetTimestampCreated() {
		String result = null;

		try {
			result = this.collabObject.getTimestampCreatedAsString();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

	private CollaborillaServiceResponse handleGetTimestampModified() {
		String result = null;

		try {
			result = this.collabObject.getTimestampModifiedAsString();
		} catch (LDAPException e) {
			this.log.write(e.toString());
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		if (result == null) {
			return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_INTERNAL_ERROR);
		}

		return new CollaborillaServiceResponse(CollaborillaServiceStatus.SC_OK, result);
	}

}
