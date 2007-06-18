/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import se.kth.nada.kmr.collaborilla.ldap.LDAPStringHelper;
import se.kth.nada.kmr.collaborilla.service.ResponseMessage;
import se.kth.nada.kmr.collaborilla.service.ServiceCommands;
import se.kth.nada.kmr.collaborilla.service.Status;
import se.kth.nada.kmr.collaborilla.util.Configuration;

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

	private OutputStreamWriter writer;
	
	private BufferedWriter out;

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
			out.write(request + Configuration.LINEFEED);
			out.flush();

			while ((tmp = in.readLine()) != null) {
				if (result.length() > 0) {
					result += Configuration.LINEFEED;
				}

				result += tmp;

				if (isStatusLine(tmp)) {
					break;
				}
			}
			
			// make sure we get UTF-8
			result = new String(result.getBytes(), "UTF-8");

			answer = parseResponse(result);
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

		StringTokenizer responseTokens = new StringTokenizer(response, Configuration.LINEFEED);
		int responseLines = responseTokens.countTokens();

		if (responseLines == 0) {
			return null;
		}

		result.responseData = new String[responseLines - 1];

		int i = 0;
		while (responseTokens.hasMoreTokens()) {
			String nextLine = responseTokens.nextToken();
			if (isStatusLine(nextLine)) {
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
	
	private String encodeURI(String uri) {
		try {
			return URLEncoder.encode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return uri;
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
			socket = new Socket(serverHost, serverPort);

			if (responseTimeOut != -1) {
				socket.setSoTimeout(this.responseTimeOut * 1000);
			}

			writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
			out = new BufferedWriter(writer);
			in = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream())));
		} catch (UnknownHostException e) {
			throw new CollaborillaException(e);
		} catch (ConnectException ce) {
			throw new CollaborillaException(CollaborillaException.ErrorCode.SC_CONNECTION_FAILED, ce);
		} catch (IOException ioe) {
			disconnect();
			throw new CollaborillaException(ioe);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#disconnect()
	 */
	public void disconnect() throws CollaborillaException {
		try {
			if (isConnected()) {
				sendRequest(ServiceCommands.CMD_QUIT);
			}

			if (out != null) {
				out.close();
			}

			if (in != null) {
				in.close();
			}

			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#isConnected()
	 */
	public boolean isConnected() {
		if (socket == null) {
			return false;
		}
		return socket.isConnected();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setIdentifier(String,
	 *      boolean)
	 */
	public void setIdentifier(String uri, boolean create) throws CollaborillaException {
		if (create) {
			sendRequest(ServiceCommands.CMD_URI + " " + ServiceCommands.CMD_URI_NEW + " "
					+ uri);
		} else {
			sendRequest(ServiceCommands.CMD_URI + " " + uri);
		}
		
		identifier = uri;
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getIdentifier()
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionNumber()
	 */
	public int getRevisionNumber() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION);

		return Integer.parseInt(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setRevisionNumber(int)
	 */
	public void setRevisionNumber(int rev) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_REVISION + " "
				+ rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionCount()
	 */
	public int getRevisionCount() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION_COUNT);

		return Integer.parseInt(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionInfo()
	 */
	public String getRevisionInfo() throws CollaborillaException {
		return getRevisionInfo(0);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionInfo(int)
	 */
	public String getRevisionInfo(int rev) throws CollaborillaException {
		String revisionInfo = new String();

		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REVISION_INFO + " " + rev);

		for (int i = 0; i < resp.responseData.length; i++) {
			revisionInfo += resp.responseData[i];
		}

		return revisionInfo;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#createRevision()
	 */
	public int createRevision() throws CollaborillaException {
		int oldRevision = getRevisionCount();
		sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_REVISION);
		return oldRevision;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#restoreRevision(int)
	 */
	public void restoreRevision(int rev) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_RESTORE + " " + ServiceCommands.ATTR_REVISION
				+ " " + rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getAlignedLocations()
	 */
	public Set getAlignedLocations() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_ALIGNEDLOCATION);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getLocations()
	 */
	public Set getLocations() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_LOCATION);
		
		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setLocations(java.util.Set)
	 */
	public void setLocations(Set locations) throws CollaborillaException {
		clearLocations();
		Iterator locIt = locations.iterator();
		while (locIt.hasNext()) {
			addLocation((String)locIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearLocations()
	 */
	public void clearLocations() throws CollaborillaException {
		Set oldLocations = new HashSet();
		try {
			oldLocations = getLocations();
		} catch (CollaborillaException ce) {
			if (ce.getResultCode() != CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE) {
				throw ce;
			}
		}
		Iterator oldLocIt = oldLocations.iterator();
		while (oldLocIt.hasNext()) {
			removeLocation((String)oldLocIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addLocation(java.lang.String)
	 */
	public void addLocation(String url) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_LOCATION + " "
				+ url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeLocation(java.lang.String)
	 */
	public void removeLocation(String url) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_LOCATION + " "
				+ url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRequiredContainers()
	 */
	public Set getRequiredContainers() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_REQUIRED_CONTAINER);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setRequiredContainers(java.util.Set)
	 */
	public void setRequiredContainers(Set containers) throws CollaborillaException {
		clearRequiredContainers();
		Iterator contIt = containers.iterator();
		while (contIt.hasNext()) {
			addRequiredContainer((String)contIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearRequiredContainers()
	 */
	public void clearRequiredContainers() throws CollaborillaException {
		Set oldContainers = new HashSet();
		try {
			oldContainers = this.getRequiredContainers();
		} catch (CollaborillaException ce) {
			if (ce.getResultCode() != CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE) {
				throw ce;
			}
		}
		Iterator oldContIt = oldContainers.iterator();
		while (oldContIt.hasNext()) {
			removeRequiredContainer((String)oldContIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addRequiredContainer(java.lang.String)
	 */
	public void addRequiredContainer(String uri) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_REQUIRED_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeRequiredContainer(java.lang.String)
	 */
	public void removeRequiredContainer(String uri) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_REQUIRED_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getOptionalContainers()
	 */
	public Set getOptionalContainers() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_OPTIONAL_CONTAINER);

		return CollaborillaDataSet.stringArrayToSet(resp.responseData);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setOptionalContainers(java.util.Set)
	 */
	public void setOptionalContainers(Set containers) throws CollaborillaException {
		clearOptionalContainers();
		Iterator contIt = containers.iterator();
		while (contIt.hasNext()) {
			addOptionalContainer((String)contIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearOptionalContainers()
	 */
	public void clearOptionalContainers() throws CollaborillaException {
		Set oldContainers = new HashSet();
		try {
			oldContainers = getOptionalContainers();
		} catch (CollaborillaException ce) {
			if (ce.getResultCode() != CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE) {
				throw ce;
			}
		}
		Iterator oldContIt = oldContainers.iterator();
		while (oldContIt.hasNext()) {
			removeOptionalContainer((String)oldContIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addOptionalContainer(java.lang.String)
	 */
	public void addOptionalContainer(String uri) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_ADD + " " + ServiceCommands.ATTR_OPTIONAL_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeOptionalContainer(java.lang.String)
	 */
	public void removeOptionalContainer(String uri) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_OPTIONAL_CONTAINER + " "
				+ uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getMetaData()
	 */
	public String getMetaData() throws CollaborillaException {
		String metaData = new String();

		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_METADATA);

		for (int i = 0; i < resp.responseData.length; i++) {
			metaData += resp.responseData[i];
		}

		return LDAPStringHelper.decode(metaData);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setMetaData(java.lang.String)
	 */
	public void setMetaData(String metaData) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_METADATA
				+ " " + LDAPStringHelper.encode(metaData));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeMetaData()
	 */
	public void removeMetaData() throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_METADATA);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getContainerRevision()
	 */
	public String getContainerRevision() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_CONTAINER_REVISION);

		return resp.responseData[0];
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setContainerRevision(java.lang.String)
	 */
	public void setContainerRevision(String containerRevision) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_CONTAINER_REVISION
				+ " " + containerRevision);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getDescription()
	 */
	public String getDescription() throws CollaborillaException {
		String desc = new String();

		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
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
		sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_DESCRIPTION + " "
				+ LDAPStringHelper.encode(desc));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeDescription()
	 */
	public void removeDescription() throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_DESCRIPTION);
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getType()
	 */
	public String getType() throws CollaborillaException {
		String type = new String();

		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_TYPE);

		for (int i = 0; i < resp.responseData.length; i++) {
			type += resp.responseData[i];
		}

		return LDAPStringHelper.decode(type);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setType(java.lang.String)
	 */
	public void setType(String type) throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_SET + " " + ServiceCommands.ATTR_TYPE + " "
				+ LDAPStringHelper.encode(type));
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeType()
	 */
	public void removeType() throws CollaborillaException {
		sendRequest(ServiceCommands.CMD_DEL + " " + ServiceCommands.ATTR_TYPE);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getLdif()
	 */
	public String getLdif() throws CollaborillaException {
		String ldif = new String();

		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_LDIF);

		for (int i = 0; i < resp.responseData.length; i++) {
			ldif += resp.responseData[i];

			if (i < (resp.responseData.length - 1)) {
				ldif += Configuration.LINEFEED;
			}
		}

		return ldif;
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getDataSet()
	 */
	public CollaborillaDataSet getDataSet() throws CollaborillaException {
		String xml = new String();

		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_DATASET);

		for (int i = 0; i < resp.responseData.length; i++) {
			xml += resp.responseData[i];

			if (i < (resp.responseData.length - 1)) {
				xml += Configuration.LINEFEED;
			}
		}

		return CollaborillaDataSet.decodeXML(xml);
	}
	
	/**
	 * Retrieves all information into a serializable object.
	 * 
	 * @param uri
	 *            URI.
	 * @param revision
	 *            Revision. 0 means latest revision.
	 * @return A DataSet object.
	 * @throws CollaborillaException
	 */
	public CollaborillaDataSet getDataSet(String uri, int revision) throws CollaborillaException {
		String strRev = Integer.toString(revision);
		
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ encodeURI(uri) + " " + strRev);
		
		String xml = new String();
		for (int i = 0; i < resp.responseData.length; i++) {
			xml += resp.responseData[i];

			if (i < (resp.responseData.length - 1)) {
				xml += Configuration.LINEFEED;
			}
		}

		return CollaborillaDataSet.decodeXML(xml);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getTimestampCreated()
	 */
	public Date getTimestampCreated() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_INTERNAL_TIMESTAMP_CREATED);

		return LDAPStringHelper.parseTimestamp(resp.responseData[0]);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getTimestampCreated()
	 */
	public Date getTimestampModified() throws CollaborillaException {
		ResponseMessage resp = sendRequest(ServiceCommands.CMD_GET + " "
				+ ServiceCommands.ATTR_INTERNAL_TIMESTAMP_MODIFIED);

		return LDAPStringHelper.parseTimestamp(resp.responseData[0]);
	}

}