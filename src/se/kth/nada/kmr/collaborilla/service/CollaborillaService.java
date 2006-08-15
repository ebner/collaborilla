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
    /* application name */
    public static final String applicationName = "CollaborillaService";

    /* allow connections */
    public static boolean allowConnections = true;

    /* standard value if no command line argument is given */
    private static String configFile = "collaborilla.properties";

    /* following values are read from a properties file */
    private static int listenPort;

    private static int clientTimeOut;

    private static int maxConnections = 0;

    /* use SSL */
    private static boolean sslSocket;

    /* shutdown timeout */
    private static int shutdownTimeout;

    private static String ldapHostname;

    private static String ldapServerDN;

    private static String ldapLoginDN;

    private static String ldapPassword;

    private static boolean verbose;

    /* status message handling */
    private static InfoMessage log = InfoMessage.getInstance();

    /**
         * Reads the configuration file and set the private fields accordingly.
         * 
         * @return True if the configuration file could be read
         */
    private static boolean readConfiguration(String file) {
	boolean result = true;

	/* set the filename */
	Configuration conf = new Configuration(file);

	/* load and assign values */
	try {
	    /* load the configuration file */
	    conf.load();

	    /* port to listen on, default 5000 */
	    listenPort = Integer.parseInt(conf.getProperty("server.listenport", "5000"));

	    /* client timeout in seconds, default 1 minute */
	    clientTimeOut = Integer.parseInt(conf.getProperty("server.timeout", "60")) * 1000;

	    /*
                 * number of maximum client connections before connection
                 * attempts are rejected
                 */
	    maxConnections = Integer.parseInt(conf.getProperty("server.maxconnections", "20"));

	    /*
                 * time to wait during a shutdown before the clients are kicked
                 * out
                 */
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
         * Main function. Creates the server socket and creates a thread for
         * each client.
         * 
         * @param args
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

	/* read the configuration */
	if (!readConfiguration(configFile)) {
	    log.write("Configuration error. Exiting.");
	    System.exit(1);
	}

	try {
	    /* add a shutdown hook */
	    Runtime.getRuntime().addShutdownHook(new CollaborillaServiceShutdown(shutdownTimeout));

	    /* create an SSL server socket */
	    /* UNTESTED */
	    if (sslSocket) {
		SSLServerSocketFactory sslFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		listener = sslFact.createServerSocket(listenPort);
	    }
	    /* create a plain text socket */
	    else {
		listener = new ServerSocket(listenPort);
	    }

	    log.writeLog(applicationName, "Started. Listening on port " + listenPort);

	    if (maxConnections > 0) {
		log.writeLog(applicationName, "Allowing a maximum of " + maxConnections + " concurrent connections");
	    }

	    /*
                 * shared LDAP connection LDAPAccess ldapConnection = new
                 * LDAPAccess(ldapHostname, ldapLoginDN, ldapPassword);
                 */

	    /* wait for incoming connections */
	    while (allowConnections) {
		/* create a new socket for each client */
		Socket server = listener.accept();

		/*
                 * workaround if a shutdown occurs while the listener is
                 * blocking during waiting for a new connection
                 */
		if (!allowConnections) {
		    server.close();
		    break;
		}

		/* set a timeout on the connection */
		server.setSoTimeout(clientTimeOut);

		/* create a communication object (thread) */
		CollaborillaServiceCommunication clientConnection = new CollaborillaServiceCommunication(server, ldapServerDN,
			ldapHostname, ldapLoginDN, ldapPassword, verbose);

		/*
                 * share LDAP connection CollaborillaServiceCommunication
                 * clientConnection = new CollaborillaServiceCommunication(
                 * server, ldapServerDN, ldapConnection, verbose);
                 */

		/* create and start a new thread */
		Thread clientThread = new Thread(clientConnection);
		clientThread.setDaemon(true);

		clientThread.start();

		if (maxConnections > 0) {
		    /*
                         * if we already have max connections, we wait until a
                         * client thread finishes
                         */
		    while (CollaborillaServiceCommunication.getClientCount() >= maxConnections) {
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
