/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;

import se.kth.nada.kmr.collaborilla.ldap.LDAPStringHelper;
import se.kth.nada.kmr.collaborilla.service.ServiceCommands;
import se.kth.nada.kmr.collaborilla.service.ResponseMessage;
import se.kth.nada.kmr.collaborilla.service.Status;

/**
 * Client class to communicate with CollaborillaService.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public final class CollaborillaServiceClient implements CollaborillaAccessible {
	
	private String serverHost = null;
	
	private int serverPort = -1;

	private String identifier = null;
	
	private int responseTimeOut = -1;

	private Socket socket = null;

	private PrintWriter out = null;

	private BufferedReader in = null;

	/**
	 * Initializes the object. No timeout for responses from the server.
	 * 
	 * @param host
	 *            Host to connect to
	 * @param port
	 *            Port on which the service is listening
	 */
	public CollaborillaServiceClient(String host, int port) {
		this.serverHost = host;
		this.serverPort = port;
	}

	/**
	 * Initializes the object and set a timeout for the responses from the
	 * server.
	 * 
	 * @param host
	 *            Host to connect to
	 * @param port
	 *            Port on which the service is listening
	 * @param timeout
	 *            Response timeout in seconds
	 */
	public CollaborillaServiceClient(String host, int port, int timeout) {
		this.serverHost = host;
		this.serverPort = port;
		this.responseTimeOut = timeout;
	}

	private ResponseMessage sendRequest(String request) throws CollaborillaException {
		String result = new String();
		String tmp = null;
		ResponseMessage answer = null;

		try {
			this.out.println(request);

			while ((tmp = this.in.readLine()) != null) {
				if (result.length() > 0) {
					result += "\n";
				}

				result += tmp;

				if (this.isStatusLine(tmp)) {
					break;
				}
			}

			answer = this.parseResponse(result);
		} catch (SocketTimeoutException ste) {
			throw new CollaborillaException(Status.SC_SERVER_TIMEOUT);
		} catch (IOException ioe) {
			throw new CollaborillaException(ioe);
		}

		this.checkResponse(answer);

		return answer;
	}

	private ResponseMessage parseResponse(String response) {
		ResponseMessage result = new ResponseMessage();
		String statusMessage = null;

		StringTokenizer responseTokens = new StringTokenizer(response, "\n");
		int responseLines = responseTokens.countTokens();

		if (responseLines == 0) {
			return null;
		}

		result.responseData = new String[responseLines - 1];

		int i = 0;
		while (responseTokens.hasMoreTokens()) {
			String nextLine = responseTokens.nextToken();
			if (this.isStatusLine(nextLine)) {
				statusMessage = nextLine;
			} else {
				result.responseData[i++] = nextLine;
			}
		}

		// try to parse the status code out of the returned message
		if (statusMessage != null) {
			String strHelper = statusMessage.substring(statusMessage.indexOf(" ") + 1);
			String statusCode = strHelper.substring(0, strHelper.indexOf(" "));
			result.statusCode = Integer.parseInt(statusCode);
		}

		return result;
	}

	private boolean isStatusLine(String line) {
		return line.toUpperCase().startsWith(Status.PROTOCOL_FOOTPRINT.toUpperCase());
	}

	private void checkResponse(ResponseMessage resp) throws CollaborillaException {
		if (resp != null) {
			if ((resp.statusCode != Status.SC_OK)
					&& (resp.statusCode != Status.SC_CLIENT_DISCONNECT)) {
				throw new CollaborillaException(resp.statusCode);
			}
		} else {
			throw new CollaborillaException(Status.SC_UNKNOWN);
		}
	}

	/*
	 * Interface implementation
	 * 
	 */

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#connect()
	 */
	public void connect() throws CollaborillaException {
		try {
			this.socket = new Socket(this.serverHost, this.serverPort);

			if (this.responseTimeOut != -1) {
				this.socket.setSoTimeout(this.responseTimeOut * 1000);
			}

			this.out = new PrintWriter(new BufferedOutputStream(this.socket.getOutputStream()), true);
			this.in = new BufferedReader(new InputStreamReader(new BufferedInputStream(this.socket.getInputStream())));
		} catch (UnknownHostException e) {
			throw new CollaborillaException(e);
		} catch (ConnectException ce) {
			throw new CollaborillaException(CollaborillaException.ErrorCode.SC_CONNECTION_FAILED, ce);
		} catch (IOException ioe) {
			this.disconnect();
			throw new CollaborillaException(ioe);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#disconnect()
	 */
	public void disconnect() throws CollaborillaException {
		try {
			if (this.isConnected()) {
				this.sendRequest(ServiceCommands.CMD_QUIT);
			}

			if (this.out != null) {
				this.out.close();
			}

			if (this.in != null) {
				this.in.close();
			}

			if (this.socket != null) {
				this.socket.close();
			}
		} catch (Exception e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#isConnected()
	 */
	public boolean isConnected() {
		if (this.socket == null) {
			return false;
		}

		return this.socket.isConnected();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setIdentifier(String,
	 *      boolean)
	 */
	public void setIdentifier(String uri, boolean create) throws CollaborillaException {
		if (create) {
			this.sendRequest(ServiceCommands.CMD_URI + " " + ServiceCommands.CMD_URI_NEW + " "
					+ uri);
		} else {
			this.sendRequest(ServiceCommands.CMD_URI + " " + uri);
		}
		
		this.identifier = uri;
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getDataSet()
	 */
	public CollaborillaDataSet getDataSet() throws CollaborillaException {
		return new CollaborillaDataSet(this);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getIdentifier()
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionNumber()
	 */
	public int getRevisionNumber() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION);

		return Integer.parseInt(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setRevisionNumber(int)
	 */
	public void setRevisionNumber(int rev) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_REVISION + " "
				+ rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionCount()
	 */
	public int getRevisionCount() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION_COUNT);

		return Integer.parseInt(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionInfo()
	 */
	public String getRevisionInfo() throws CollaborillaException {
		return this.getRevisionInfo(0);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionInfo(int)
	 */
	public String getRevisionInfo(int rev) throws CollaborillaException {
		String revisionInfo = new String();

		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION_INFO + " " + rev);

		for (int i = 0; i < resp.responseData.length; i++) {
			revisionInfo += resp.responseData[i];
		}

		return revisionInfo;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#createRevision()
	 */
	public void createRevision() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_REVISION);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#restoreRevision(int)
	 */
	public void restoreRevision(int rev) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_RESTORE + " " + ServiceCommands.ATTR_REVISION
				+ " " + rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getAlignedLocation()
	 */
	public Set getAlignedLocation() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_ALIGNEDLOCATION);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getLocation()
	 */
	public Set getLocation() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_LOCATION);
		
		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addLocation(java.lang.String)
	 */
	public void addLocation(String url) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_LOCATION + " "
				+ url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyLocation(java.lang.String,
	 *      java.lang.String)
	 */
	public void modifyLocation(String oldUrl, String newUrl) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_MOD + " " + ServiceCommands.ATTR_LOCATION + " "
				+ oldUrl + " " + newUrl);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeLocation(java.lang.String)
	 */
	public void removeLocation(String url) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_LOCATION + " "
				+ url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRequiredContainers()
	 */
	public Set getRequiredContainers() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_URI_ORIG);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addRequiredContainer(java.lang.String)
	 */
	public void addRequiredContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_URI_ORIG + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyRequiredContainer(java.lang.String,
	 *      java.lang.String)
	 */
	public void modifyRequiredContainer(String oldUri, String newUri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_MOD + " " + ServiceCommands.ATTR_URI_ORIG + " "
				+ oldUri + " " + newUri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeRequiredContainer(java.lang.String)
	 */
	public void removeRequiredContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_URI_ORIG + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getOptionalContainers()
	 */
	public Set getOptionalContainers() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_URI_OTHER);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addOptionalContainer(java.lang.String)
	 */
	public void addOptionalContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_URI_OTHER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyOptionalContainer(java.lang.String,
	 *      java.lang.String)
	 */
	public void modifyOptionalContainer(String oldUri, String newUri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_MOD + " " + ServiceCommands.ATTR_URI_OTHER + " "
				+ oldUri + " " + newUri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeOptionalContainer(java.lang.String)
	 */
	public void removeOptionalContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_URI_OTHER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContextRdfInfo()
	 */
	public String getContextRdfInfo() throws CollaborillaException {
		String rdfInfo = new String();

		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_CONTEXT_RDFINFO);

		for (int i = 0; i < resp.responseData.length; i++) {
			rdfInfo += resp.responseData[i];
		}

		return LDAPStringHelper.decode(rdfInfo);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContextRdfInfo(java.lang.String)
	 */
	public void setContextRdfInfo(String rdfInfo) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_CONTEXT_RDFINFO
				+ " " + LDAPStringHelper.encode(rdfInfo));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeContextRdfInfo()
	 */
	public void removeContextRdfInfo() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_CONTEXT_RDFINFO);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContainerRdfInfo()
	 */
	public String getContainerRdfInfo() throws CollaborillaException {
		String rdfInfo = new String();

		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_CONTAINER_RDFINFO);

		for (int i = 0; i < resp.responseData.length; i++) {
			rdfInfo += resp.responseData[i];
		}

		return LDAPStringHelper.decode(rdfInfo);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContainerRdfInfo(java.lang.String)
	 */
	public void setContainerRdfInfo(String rdfInfo) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_CONTAINER_RDFINFO
				+ " " + LDAPStringHelper.encode(rdfInfo));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeContainerRdfInfo()
	 */
	public void removeContainerRdfInfo() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " "
						+ ServiceCommands.ATTR_CONTAINER_RDFINFO);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContainerRevision()
	 */
	public String getContainerRevision() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_CONTAINER_REVISION);

		return resp.responseData[0];
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContainerRevision(java.lang.String)
	 */
	public void setContainerRevision(String containerRevision) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_CONTAINER_REVISION
				+ " " + containerRevision);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getDescription()
	 */
	public String getDescription() throws CollaborillaException {
		String desc = new String();

		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_DESCRIPTION);

		for (int i = 0; i < resp.responseData.length; i++) {
			desc += resp.responseData[i];
		}

		return LDAPStringHelper.decode(desc);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setDescription(java.lang.String)
	 */
	public void setDescription(String desc) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_DESCRIPTION + " "
				+ LDAPStringHelper.encode(desc));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeDescription()
	 */
	public void removeDescription() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_DESCRIPTION);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getLdif()
	 */
	public String getLdif() throws CollaborillaException {
		String ldif = new String();

		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_LDIF);

		for (int i = 0; i < resp.responseData.length; i++) {
			ldif += resp.responseData[i];

			if (i < (resp.responseData.length - 1)) {
				ldif += "\n";
			}
		}

		return ldif;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getTimestampCreated()
	 */
	public Date getTimestampCreated() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_INTERNAL_TIMESTAMP_CREATED);

		return LDAPStringHelper.parseTimestamp(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getTimestampCreated()
	 */
	public Date getTimestampModified() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_INTERNAL_TIMESTAMP_MODIFIED);

		return LDAPStringHelper.parseTimestamp(resp.responseData[0]);
	}

}
