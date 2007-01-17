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
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import se.kth.nada.kmr.collaborilla.ldap.LDAPStringHelper;
import se.kth.nada.kmr.collaborilla.service.ResponseMessage;
import se.kth.nada.kmr.collaborilla.service.ServiceCommands;
import se.kth.nada.kmr.collaborilla.service.Status;

/**
 * Client class to communicate with CollaborillaService.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public final class CollaborillaServiceClient implements CollaborillaStatefulClient {
	
	private String serverHost;
	
	private int serverPort = -1;

	private String identifier;
	
	private int responseTimeOut = -1;

	private Socket socket;

	private PrintWriter out;

	private BufferedReader in;

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
		this(host, port);
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#connect()
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#disconnect()
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#isConnected()
	 */
	public boolean isConnected() {
		if (this.socket == null) {
			return false;
		}
		return this.socket.isConnected();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setIdentifier(String,
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getDataSet()
	 */
	public CollaborillaDataSet getDataSet() throws CollaborillaException {
		return new CollaborillaDataSet(this);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getIdentifier()
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionNumber()
	 */
	public int getRevisionNumber() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION);

		return Integer.parseInt(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setRevisionNumber(int)
	 */
	public void setRevisionNumber(int rev) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_REVISION + " "
				+ rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionCount()
	 */
	public int getRevisionCount() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION_COUNT);

		return Integer.parseInt(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionInfo()
	 */
	public String getRevisionInfo() throws CollaborillaException {
		return this.getRevisionInfo(0);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionInfo(int)
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#createRevision()
	 */
	public void createRevision() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_REVISION);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#restoreRevision(int)
	 */
	public void restoreRevision(int rev) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_RESTORE + " " + ServiceCommands.ATTR_REVISION
				+ " " + rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getAlignedLocations()
	 */
	public Set getAlignedLocations() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_ALIGNEDLOCATION);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getLocations()
	 */
	public Set getLocations() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_LOCATION);
		
		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setLocations(java.util.Set)
	 */
	public void setLocations(Set locations) throws CollaborillaException {
		this.clearLocations();
		Iterator locIt = locations.iterator();
		while (locIt.hasNext()) {
			this.addLocation((String)locIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearLocations()
	 */
	public void clearLocations() throws CollaborillaException {
		Set oldLocations = this.getLocations();
		Iterator oldLocIt = oldLocations.iterator();
		while (oldLocIt.hasNext()) {
			this.removeLocation((String)oldLocIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addLocation(java.lang.String)
	 */
	public void addLocation(String url) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_LOCATION + " "
				+ url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeLocation(java.lang.String)
	 */
	public void removeLocation(String url) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_LOCATION + " "
				+ url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRequiredContainers()
	 */
	public Set getRequiredContainers() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REQUIRED_CONTAINER);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setRequiredContainers(java.util.Set)
	 */
	public void setRequiredContainers(Set containers) throws CollaborillaException {
		this.clearRequiredContainers();
		Iterator contIt = containers.iterator();
		while (contIt.hasNext()) {
			this.addRequiredContainer((String)contIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearRequiredContainers()
	 */
	public void clearRequiredContainers() throws CollaborillaException {
		Set oldContainers = this.getRequiredContainers();
		Iterator oldContIt = oldContainers.iterator();
		while (oldContIt.hasNext()) {
			this.removeRequiredContainer((String)oldContIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addRequiredContainer(java.lang.String)
	 */
	public void addRequiredContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_REQUIRED_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeRequiredContainer(java.lang.String)
	 */
	public void removeRequiredContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_REQUIRED_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getOptionalContainers()
	 */
	public Set getOptionalContainers() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_OPTIONAL_CONTAINER);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setOptionalContainers(java.util.Set)
	 */
	public void setOptionalContainers(Set containers) throws CollaborillaException {
		this.clearOptionalContainers();
		Iterator contIt = containers.iterator();
		while (contIt.hasNext()) {
			this.addOptionalContainer((String)contIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearOptionalContainers()
	 */
	public void clearOptionalContainers() throws CollaborillaException {
		Set oldContainers = this.getOptionalContainers();
		Iterator oldContIt = oldContainers.iterator();
		while (oldContIt.hasNext()) {
			this.removeOptionalContainer((String)oldContIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addOptionalContainer(java.lang.String)
	 */
	public void addOptionalContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_OPTIONAL_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeOptionalContainer(java.lang.String)
	 */
	public void removeOptionalContainer(String uri) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_OPTIONAL_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getContextRdfInfo()
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setContextRdfInfo(java.lang.String)
	 */
	public void setContextRdfInfo(String rdfInfo) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_CONTEXT_RDFINFO
				+ " " + LDAPStringHelper.encode(rdfInfo));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeContextRdfInfo()
	 */
	public void removeContextRdfInfo() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_CONTEXT_RDFINFO);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getContainerRdfInfo()
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setContainerRdfInfo(java.lang.String)
	 */
	public void setContainerRdfInfo(String rdfInfo) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_CONTAINER_RDFINFO
				+ " " + LDAPStringHelper.encode(rdfInfo));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeContainerRdfInfo()
	 */
	public void removeContainerRdfInfo() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " "
						+ ServiceCommands.ATTR_CONTAINER_RDFINFO);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getContainerRevision()
	 */
	public String getContainerRevision() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_CONTAINER_REVISION);

		return resp.responseData[0];
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setContainerRevision(java.lang.String)
	 */
	public void setContainerRevision(String containerRevision) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_CONTAINER_REVISION
				+ " " + containerRevision);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getDescription()
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setDescription(java.lang.String)
	 */
	public void setDescription(String desc) throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_DESCRIPTION + " "
				+ LDAPStringHelper.encode(desc));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeDescription()
	 */
	public void removeDescription() throws CollaborillaException {
		this.sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_DESCRIPTION);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getLdif()
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getTimestampCreated()
	 */
	public Date getTimestampCreated() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_INTERNAL_TIMESTAMP_CREATED);

		return LDAPStringHelper.parseTimestamp(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getTimestampCreated()
	 */
	public Date getTimestampModified() throws CollaborillaException {
		ResponseMessage resp = this.sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_INTERNAL_TIMESTAMP_MODIFIED);

		return LDAPStringHelper.parseTimestamp(resp.responseData[0]);
	}

}
