/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;

import se.kth.nada.kmr.collaborilla.util.Configuration;
import se.kth.nada.kmr.collaborilla.util.InfoMessage;

/**
 * CollaborillaService is a server application which provides indirect access to
 * an LDAP directory. It allows for manipulating the Collaborilla meta-data
 * entries in the directory, including reading, modifying entry/attribues and
 * revision management.
 * 
 * The available commands can be requested by sending "HLP" to the server.
 * 
 * @author Hannes Ebner
 */
public class CollaborillaService {
	
	/**
	 * Application name.
	 */
	public static final String applicationName = "CollaborillaService";

	/**
	 * Allow connections.
	 */
	public static boolean allowConnections = true;

	/**
	 * Standard configuration file if no command line argument is given
	 */
	private static String configFile = "collaborilla.properties";

	// Following values are read from a properties file
	private static int listenPort;

	private static int clientTimeOut;

	private static int maxConnections = 0;

	// Use SSL
	private static boolean sslSocket;

	// Shutdown timeout
	private static int shutdownTimeout;

	private static String ldapHostname;

	private static String ldapServerDN;

	private static String ldapLoginDN;

	private static String ldapPassword;

	private static boolean verbose;

	private static InfoMessage log = InfoMessage.getInstance();

	/**
	 * Reads the configuration file and set the private fields accordingly.
	 * 
	 * @return True if the configuration file could be read
	 */
	private static boolean readConfiguration(String file) {
		boolean result = true;

		// Set the filename
		Configuration conf = new Configuration(file);

		// Load and assign values
		try {
			// Load the configuration file
			conf.load();

			// Port to listen on, default 5000
			listenPort = Integer.parseInt(conf.getProperty("server.listenport", "5000"));

			// Client timeout in seconds, default 1 minute
			clientTimeOut = Integer.parseInt(conf.getProperty("server.timeout", "60")) * 1000;

			// Number of maximum client connections before connection attempts
			// are rejected
			maxConnections = Integer.parseInt(conf.getProperty("server.maxconnections", "20"));

			// Time to wait during a shutdown before the clients are kicked out
			shutdownTimeout = Integer.parseInt(conf.getProperty("server.shutdowntimeout", "10")) * 1000;

			sslSocket = Boolean.valueOf(conf.getProperty("server.ssl", "false")).booleanValue();
			verbose = Boolean.valueOf(conf.getProperty("server.verbose", "false")).booleanValue();
			ldapServerDN = conf.getProperty("ldap.serverdn");
			ldapHostname = conf.getProperty("ldap.hostname");
			ldapLoginDN = conf.getProperty("ldap.logindn");
			ldapPassword = conf.getProperty("ldap.password");
		} catch (Exception e) {
			log.writeLog(applicationName, e.getMessage());
			result = false;
		}

		return result;
	}

	/**
	 * Main function. Creates the server socket and creates a thread for each
	 * client.
	 * 
	 * @param args
	 *            Commandline arguments.
	 */
	public static void main(String[] args) {
		ServerSocket listener = null;

		if (args.length > 0) {
			if (args[0].startsWith("--config=")) {
				configFile = args[0].substring(args[0].indexOf("=") + 1);
			} else {
				log.write(applicationName + "\n\n" + "Possible parameter: " + "--config=<path to config file>");
				System.exit(0);
			}
		}

		// Read the configuration
		if (!readConfiguration(configFile)) {
			log.write("Configuration error. Exiting.");
			System.exit(1);
		}

		try {
			// Add a shutdown hook
			Runtime.getRuntime().addShutdownHook(new ShutdownDisposer(shutdownTimeout));

			if (sslSocket) {
				// Create an SSL server socket
				// UNTESTED
				SSLServerSocketFactory sslFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				listener = sslFact.createServerSocket(listenPort);
			} else {
				// Create a plain text socket
				listener = new ServerSocket(listenPort);
			}

			log.writeLog(applicationName, "Started. Listening on port " + listenPort);

			if (maxConnections > 0) {
				log.writeLog(applicationName, "Allowing a maximum of " + maxConnections + " concurrent connections");
			}

			// Wait for incoming connections
			while (allowConnections) {
				// Create a new socket for each client
				Socket server = listener.accept();

				/*
				 * workaround if a shutdown occurs while the listener is
				 * blocking during waiting for a new connection
				 */
				if (!allowConnections) {
					server.close();
					break;
				}

				// Set a timeout on the connection
				server.setSoTimeout(clientTimeOut);
				log.writeLog(applicationName, "Client connection timeout set to " + clientTimeOut);

				// Create a communication object (thread)
				ClientConnector clientConnection = new ClientConnector(server, ldapServerDN, ldapHostname, ldapLoginDN,
						ldapPassword, verbose);

				// Create and start a new thread
				Thread clientThread = new Thread(clientConnection);
				clientThread.setDaemon(true);

				clientThread.start();

				if (maxConnections > 0) {
					/*
					 * if we already have reached max connections, we wait until
					 * a client thread finishes
					 */
					while (ClientConnector.getClientCount() >= maxConnections) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}
					}
				}
			}
		} catch (IOException ioe) {
			log.writeLog(applicationName, ioe.getMessage() + ". Exiting.");
		} catch (Exception e) {
			log.writeLog(applicationName, e.getMessage() + ". Exiting.");
		}
	}
}
