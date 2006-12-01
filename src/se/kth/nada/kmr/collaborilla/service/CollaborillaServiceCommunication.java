/* $Id$ */
/* 
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import se.kth.nada.kmr.collaborilla.ldap.LDAPAccess;
import se.kth.nada.kmr.collaborilla.util.InfoMessage;

import com.novell.ldap.LDAPException;

/**
 * Implements the thread for the server/client communication in
 * CollaborillaService. Provides access to the LDAP directory through a
 * CollaborillaObject and manages the commands which are sent by the client.
 * 
 * @author Hannes Ebner
 * @version $Id$
 * @see se.kth.nada.kmr.collaborilla.service.CollaborillaService
 */
public class CollaborillaServiceCommunication implements Runnable {
	private Socket serverSocket;

	private static InfoMessage log = InfoMessage.getInstance();

	private LDAPAccess ldapConnection;

	private String serverDN;

	private boolean verbose;

	private String clientIP;

	// thread/client counter
	private static int clientCount = 0;

	/**
	 * Initializes the object and creates a new LDAP connection.
	 * 
	 * @param server
	 *            Socket to communicate with the client
	 * @param serverDN
	 *            Server DN for the LDAP connection
	 * @param ldapHostname
	 *            Hostname for the LDAP connection
	 * @param ldapLoginDN
	 *            Login DN for the LDAP connection
	 * @param ldapPassword
	 *            Password for the LDAP connection
	 */

	/*
	 * shared LDAP connection CollaborillaServiceCommunication(Socket server,
	 * String serverDN, LDAPAccess ldapConnection, boolean verbose)
	 */
	CollaborillaServiceCommunication(Socket server, String serverDN, String ldapHostname, String ldapLoginDN,
			String ldapPassword, boolean verbose) {
		this.serverSocket = server;
		this.serverDN = serverDN;
		this.verbose = verbose;

		this.clientIP = this.serverSocket.getInetAddress().getHostAddress();

		// non-shared connection
		this.ldapConnection = new LDAPAccess(ldapHostname, ldapLoginDN, ldapPassword);

		/*
		 * shared connection this.ldapConnection = ldapConnection;
		 */

		incClientCount();
	}

	/**
	 * Overrides java.lang.Runnable.run(), necessary for the tread.
	 */
	public void run() {
		String request = null;
		String statusMessage = null;
		CollaborillaServiceResponse response = null;
		BufferedReader in = null;
		PrintStream out = null;

		try {
			if (verbose) {
				log.writeLog(clientIP, "Client connected");
				log.writeLog("CollaborillaService", "Active clients: " + getClientCount());
			}

			// setup reader and writer
			in = new BufferedReader(new InputStreamReader(new BufferedInputStream(serverSocket.getInputStream())));
			out = new PrintStream(new BufferedOutputStream(serverSocket.getOutputStream()), true);

			// create protocol handler
			CollaborillaServiceProtocol protocol = new CollaborillaServiceProtocol(ldapConnection, serverDN);

			// connect to the LDAP server
			ldapConnection.bind();

			// Get input from the client
			while ((request = in.readLine()) != null) {
				if (verbose) {
					log.writeLog(this.clientIP, "> " + request);
				}

				// check whether client wants to quit the connection
				if (request.equalsIgnoreCase(CollaborillaServiceCommands.CMD_QUIT)) {
					out.println(CollaborillaServiceStatus.getMessage(CollaborillaServiceStatus.SC_CLIENT_DISCONNECT));
					break;
				}

				// parse the request in a seperate method
				response = protocol.requestDistributor(request);

				// return the response
				if (response.responseMessage != null) {
					for (int i = 0; i < response.responseMessage.length; i++) {
						if (response.responseMessage[i] != null) {
							out.println(response.responseMessage[i]);
						}
					}
				}

				statusMessage = CollaborillaServiceStatus.getMessage(response.statusCode);

				// write the status message
				out.println(statusMessage);

				if (verbose) {
					log.writeLog(this.clientIP, "< " + statusMessage);
				}
			}
		} catch (SocketTimeoutException ste) {
			out.println(CollaborillaServiceStatus.getMessage(CollaborillaServiceStatus.SC_CLIENT_TIMEOUT));
			log.writeLog(this.clientIP, "Client timeout exceeded");
		} catch (IOException e) {
			log.writeLog(this.clientIP, e.getMessage());
		} catch (Exception e) {
			log.writeLog("CollaborillaService", e.getMessage());
		} finally {
			try {
				// close the socket to the client, implicitly closes also
				// this.in and this.out
				serverSocket.close();
			} catch (IOException ioe) {
				log.writeLog(this.clientIP, ioe.getMessage());
			}

			try {
				// close connection to the LDAP server
				ldapConnection.disconnect();
			} catch (LDAPException ldape) {
				log.writeLog(this.clientIP, ldape.getMessage());
			}

			decClientCount();

			if (verbose) {
				log.writeLog(this.clientIP, "Client disconnected");
				log.writeLog("CollaborillaService", "Active clients: " + getClientCount());
			}
		}
	}

	public synchronized static int getClientCount() {
		return clientCount;
	}

	private synchronized static void incClientCount() {
		clientCount++;
	}

	private synchronized static void decClientCount() {
		clientCount--;
	}

}
