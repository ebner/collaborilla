/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import se.kth.nada.kmr.collaborilla.ldap.LDAPAccess;
import se.kth.nada.kmr.collaborilla.util.Configuration;
import se.kth.nada.kmr.collaborilla.util.InfoMessage;

import com.novell.ldap.LDAPException;

/**
 * Implements the thread for the server/client communication in
 * CollaborillaService. Calls CommandHandler to fulfill the requests of the
 * client.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class ClientConnector implements Runnable {

	private Socket serverSocket;

	private static InfoMessage log = InfoMessage.getInstance();

	private LDAPAccess ldapConnection;

	private String serverDN;

	private boolean verbose;

	private String clientIP;
	
	private static Object mutex;

	// thread/client counter
	private static int clientCount = 0;
	
	static {
		mutex = new Object();
	}

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
	 * @param verbose
	 *            Do verbose logging.
	 */
	ClientConnector(Socket server, String serverDN, String ldapHostname, String ldapLoginDN, String ldapPassword,
			boolean verbose) {
		this.serverSocket = server;
		this.serverDN = serverDN;
		this.verbose = verbose;

		clientIP = serverSocket.getInetAddress().getHostAddress();
		
		if (verbose) {
			log.writeLog(clientIP, "Client connected");
			log.writeLog("CollaborillaService", "Active clients: " + incClientCount());
		}

		// Non-shared connection:
		// We create a new connection with the LDAP server.
		// TODO replace this with a connection manager/connection pool.
		this.ldapConnection = new LDAPAccess(ldapHostname, ldapLoginDN, ldapPassword);
	}

	/**
	 * Let's the client connect, receives commands and forwards them to a
	 * CommandHandler. Sends the response from the CommandHandler back to the
	 * client.
	 */
	public void run() {
		String request = null;
		String statusMessage = null;
		ResponseMessage response = null;
		BufferedReader in = null;
		OutputStreamWriter writer = null;
		BufferedWriter out = null;

		try {
			// setup reader and writer
			in = new BufferedReader(new InputStreamReader(new BufferedInputStream(serverSocket.getInputStream())));
			writer = new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8");
			out = new BufferedWriter(writer);

			// create protocol handler
			CommandHandler protocol = new CommandHandler(ldapConnection, serverDN);

			// connect to the LDAP server
			ldapConnection.bind();

			// Get input from the client
			while ((request = in.readLine()) != null) {
				if (verbose) {
					log.writeLog(clientIP, "> " + request);
				}

				// check whether client wants to quit the connection
				if (request.equalsIgnoreCase(ServiceCommands.CMD_QUIT)) {
					out.write(Status.getMessage(Status.SC_CLIENT_DISCONNECT) + Configuration.LINEFEED);
					break;
				}

				// parse the request in a seperate method
				response = protocol.processRequest(request);

				// return the response
				if (response.responseData != null) {
					for (int i = 0; i < response.responseData.length; i++) {
						if (response.responseData[i] != null) {
							out.write(response.responseData[i] + Configuration.LINEFEED);
						}
					}
				}

				statusMessage = Status.getMessage(response.statusCode);

				// write the status message
				out.write(statusMessage + Configuration.LINEFEED);
				out.flush();

				if (verbose) {
					log.writeLog(clientIP, "< " + statusMessage);
				}
				
				if (!protocol.hasCollaborillaObject()) {
					break;
				}
			}
		} catch (SocketTimeoutException ste) {
			try {
				out.write(Status.getMessage(Status.SC_CLIENT_TIMEOUT) + Configuration.LINEFEED);
			} catch (IOException e) {
				log.writeLog(clientIP, e.getMessage());
			}
			log.writeLog(clientIP, "Client timeout exceeded");
		} catch (IOException e) {
			log.writeLog(clientIP, e.getMessage());
		} catch (Exception e) {
			log.writeLog("CollaborillaService", e.getMessage());
		} finally {
			try {
				out.flush();
			} catch (IOException e) {
				log.writeLog(clientIP, e.getMessage());
			}
			
			try {
				// close the socket to the client, implicitly closes also
				// this.in and this.out
				serverSocket.close();
			} catch (IOException ioe) {
				log.writeLog(clientIP, ioe.getMessage());
			}

			try {
				// close connection to the LDAP server
				ldapConnection.disconnect();
			} catch (LDAPException ldape) {
				log.writeLog(clientIP, ldape.getMessage());
			}

			if (verbose) {
				log.writeLog(clientIP, "Client disconnected");
				log.writeLog("CollaborillaService", "Active clients: " + decClientCount());
			}
		}
	}

	/**
	 * @return Returns the current amount of connected clients.
	 */
	public static int getClientCount() {
		return clientCount;
	}

	/**
	 * Increases client counter and returns the new value.
	 *
	 * @return Amount of connected clients.
	 */
	private static int incClientCount() {
		synchronized (mutex) {
			return ++clientCount;
		}
	}

	/**
	 * Decreases the client counter and returns the new value.
	 * 
	 * @return Amount of connected clients.
	 */
	private static int decClientCount() {
		synchronized (mutex) {
			return --clientCount;
		}
	}

}
