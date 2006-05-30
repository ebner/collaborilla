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

package se.kth.nada.kmr.collaborilla.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import se.kth.nada.kmr.collaborilla.ldap.LDAPStringHelper;
import se.kth.nada.kmr.collaborilla.service.*;

/**
 * Client class to communicate with CollaborillaService.
 * 
 * @author Hannes Ebner
 */
public final class CollaborillaServiceClient implements CollaborillaAccessible {
    private String serverHost;

    private int serverPort;

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

    private CollaborillaServiceResponse sendRequest(String request)
            throws CollaborillaException {
        String result = new String();
        String tmp = null;
        CollaborillaServiceResponse answer = null;

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
            throw new CollaborillaException(
                    CollaborillaServiceStatus.SC_SERVER_TIMEOUT);
        } catch (IOException ioe) {
            throw new CollaborillaException(ioe);
        }

        this.checkResponse(answer);

        return answer;
    }

    private CollaborillaServiceResponse parseResponse(String response) {
        CollaborillaServiceResponse result = new CollaborillaServiceResponse();
        String statMessage = null;

        StringTokenizer responseTokens = new StringTokenizer(response, "\n");
        int responseLines = responseTokens.countTokens();

        if (responseLines == 0) {
            return null;
        }

        result.responseMessage = new String[responseLines - 1];

        int i = 0;
        while (responseTokens.hasMoreTokens()) {
            String nextLine = responseTokens.nextToken();
            if (this.isStatusLine(nextLine)) {
                statMessage = nextLine;
            } else {
                result.responseMessage[i++] = nextLine;
            }
        }

        if (statMessage != null) {
            String strHelper = statMessage
                    .substring(statMessage.indexOf(" ") + 1);
            String statusCode = strHelper.substring(0, strHelper.indexOf(" "));
            result.statusCode = Integer.parseInt(statusCode);
        }

        return result;
    }

    private boolean isStatusLine(String line) {
        return line.toUpperCase().startsWith(
                CollaborillaServiceStatus.PROTOCOLFOOTPRINT.toUpperCase());
    }

    private void checkResponse(CollaborillaServiceResponse resp)
            throws CollaborillaException {
        if (resp != null) {
            if ((resp.statusCode != CollaborillaServiceStatus.SC_OK)
                    && (resp.statusCode != CollaborillaServiceStatus.SC_CLIENT_DISCONNECT)) {
                throw new CollaborillaException(resp.statusCode);
            }
        } else {
            throw new CollaborillaException(
                    CollaborillaServiceStatus.SC_UNKNOWN);
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

            out = new PrintWriter(new BufferedOutputStream(socket
                    .getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(socket.getInputStream())));
        } catch (UnknownHostException e) {
            throw new CollaborillaException(e);
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
            this.sendRequest(CollaborillaServiceCommands.CMD_QUIT);
            this.out.close();
            this.in.close();
            this.socket.close();
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
    public void setIdentifier(String uri, boolean create)
            throws CollaborillaException {
        if (create) {
            this.sendRequest(CollaborillaServiceCommands.CMD_URI + " "
                    + CollaborillaServiceCommands.CMD_URI_NEW + " " + uri);
        } else {
            this.sendRequest(CollaborillaServiceCommands.CMD_URI + " " + uri);
        }
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionNumber()
     */
    public int getRevisionNumber() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_REVISION);

        return Integer.parseInt(resp.responseMessage[0]);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setRevisionNumber(int)
     */
    public void setRevisionNumber(int rev) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_SET + " "
                + CollaborillaServiceCommands.ATTR_REVISION + " " + rev);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionCount()
     */
    public int getRevisionCount() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_REVISION_COUNT);

        return Integer.parseInt(resp.responseMessage[0]);
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
        String revInfo = new String();

        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_REVISION_INFO + " "
                        + rev);

        for (int i = 0; i < resp.responseMessage.length; i++) {
            revInfo += resp.responseMessage[i];
        }

        return revInfo;
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#createRevision()
     */
    public void createRevision() throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_ADD + " "
                + CollaborillaServiceCommands.ATTR_REVISION);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#restoreRevision(int)
     */
    public void restoreRevision(int rev) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_RESTORE + " "
                + CollaborillaServiceCommands.ATTR_REVISION + " " + rev);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getAlignedLocation()
     */
    public Collection getAlignedLocation() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_ALIGNEDLOCATION);

        List result = new ArrayList();

        if (resp.responseMessage != null) {
            for (int i = 0; i < resp.responseMessage.length; i++) {
                result.add(resp.responseMessage[i]);
            }
        }

        return result;
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getLocation()
     */
    public Collection getLocation() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_LOCATION);

        List result = new ArrayList();

        if (resp.responseMessage != null) {
            for (int i = 0; i < resp.responseMessage.length; i++) {
                result.add(resp.responseMessage[i]);
            }
        }

        return result;
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addLocation(java.lang.String)
     */
    public void addLocation(String url) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_ADD + " "
                + CollaborillaServiceCommands.ATTR_LOCATION + " " + url);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyLocation(java.lang.String,
     *      java.lang.String)
     */
    public void modifyLocation(String oldUrl, String newUrl)
            throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_MOD + " "
                + CollaborillaServiceCommands.ATTR_LOCATION + " " + oldUrl
                + " " + newUrl);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeLocation(java.lang.String)
     */
    public void removeLocation(String url) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_DEL + " "
                + CollaborillaServiceCommands.ATTR_LOCATION + " " + url);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getUriOriginal()
     */
    public Collection getUriOriginal() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_URI_ORIG);

        List result = new ArrayList();

        if (resp.responseMessage != null) {
            for (int i = 0; i < resp.responseMessage.length; i++) {
                result.add(resp.responseMessage[i]);
            }
        }

        return result;
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addUriOriginal(java.lang.String)
     */
    public void addUriOriginal(String uri) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_ADD + " "
                + CollaborillaServiceCommands.ATTR_URI_ORIG + " " + uri);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyUriOriginal(java.lang.String,
     *      java.lang.String)
     */
    public void modifyUriOriginal(String oldUri, String newUri)
            throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_MOD + " "
                + CollaborillaServiceCommands.ATTR_URI_ORIG + " " + oldUri
                + " " + newUri);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeUriOriginal(java.lang.String)
     */
    public void removeUriOriginal(String uri) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_DEL + " "
                + CollaborillaServiceCommands.ATTR_URI_ORIG + " " + uri);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getUriOther()
     */
    public Collection getUriOther() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_URI_OTHER);

        List result = new ArrayList();

        if (resp.responseMessage != null) {
            for (int i = 0; i < resp.responseMessage.length; i++) {
                result.add(resp.responseMessage[i]);
            }
        }

        return result;
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addUriOther(java.lang.String)
     */
    public void addUriOther(String uri) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_ADD + " "
                + CollaborillaServiceCommands.ATTR_URI_OTHER + " " + uri);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyUriOther(java.lang.String,
     *      java.lang.String)
     */
    public void modifyUriOther(String oldUri, String newUri)
            throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_MOD + " "
                + CollaborillaServiceCommands.ATTR_URI_OTHER + " " + oldUri
                + " " + newUri);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeUriOther(java.lang.String)
     */
    public void removeUriOther(String uri) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_DEL + " "
                + CollaborillaServiceCommands.ATTR_URI_OTHER + " " + uri);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContextRdfInfo()
     */
    public String getContextRdfInfo() throws CollaborillaException {
        String rdfInfo = new String();

        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_CONTEXT_RDFINFO);

        for (int i = 0; i < resp.responseMessage.length; i++) {
            rdfInfo += resp.responseMessage[i];
        }

        return LDAPStringHelper.decode(rdfInfo);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContextRdfInfo(java.lang.String)
     */
    public void setContextRdfInfo(String rdfInfo) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_SET + " "
                + CollaborillaServiceCommands.ATTR_CONTEXT_RDFINFO + " "
                + LDAPStringHelper.encode(rdfInfo));
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeContextRdfInfo()
     */
    public void removeContextRdfInfo() throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_DEL + " "
                + CollaborillaServiceCommands.ATTR_CONTEXT_RDFINFO);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContainerRdfInfo()
     */
    public String getContainerRdfInfo() throws CollaborillaException {
        String rdfInfo = new String();

        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_CONTAINER_RDFINFO);

        for (int i = 0; i < resp.responseMessage.length; i++) {
            rdfInfo += resp.responseMessage[i];
        }

        return LDAPStringHelper.decode(rdfInfo);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContainerRdfInfo(java.lang.String)
     */
    public void setContainerRdfInfo(String rdfInfo)
            throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_SET + " "
                + CollaborillaServiceCommands.ATTR_CONTAINER_RDFINFO + " "
                + LDAPStringHelper.encode(rdfInfo));
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeContainerRdfInfo()
     */
    public void removeContainerRdfInfo() throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_DEL + " "
                + CollaborillaServiceCommands.ATTR_CONTAINER_RDFINFO);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getDescription()
     */
    public String getDescription() throws CollaborillaException {
        String desc = new String();

        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_DESCRIPTION);

        for (int i = 0; i < resp.responseMessage.length; i++) {
            desc += resp.responseMessage[i];
        }

        return LDAPStringHelper.decode(desc);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setDescription(java.lang.String)
     */
    public void setDescription(String desc) throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_SET + " "
                + CollaborillaServiceCommands.ATTR_DESCRIPTION + " "
                + LDAPStringHelper.encode(desc));
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeDescription()
     */
    public void removeDescription() throws CollaborillaException {
        this.sendRequest(CollaborillaServiceCommands.CMD_DEL + " "
                + CollaborillaServiceCommands.ATTR_DESCRIPTION);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getLdif()
     */
    public String getLdif() throws CollaborillaException {
        String ldif = new String();

        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET + " "
                        + CollaborillaServiceCommands.ATTR_LDIF);

        for (int i = 0; i < resp.responseMessage.length; i++) {
            ldif += resp.responseMessage[i];

            if (i < (resp.responseMessage.length - 1)) {
                ldif += "\n";
            }
        }

        return ldif;
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getTimestampCreated()
     */
    public Date getTimestampCreated() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET
                        + " "
                        + CollaborillaServiceCommands.ATTR_INTERNAL_TIMESTAMP_CREATED);

        return LDAPStringHelper.parseTimestamp(resp.responseMessage[0]);
    }

    /**
     * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getTimestampCreated()
     */
    public Date getTimestampModified() throws CollaborillaException {
        CollaborillaServiceResponse resp = this
                .sendRequest(CollaborillaServiceCommands.CMD_GET
                        + " "
                        + CollaborillaServiceCommands.ATTR_INTERNAL_TIMESTAMP_MODIFIED);

        return LDAPStringHelper.parseTimestamp(resp.responseMessage[0]);
    }

}
