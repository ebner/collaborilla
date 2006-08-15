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

import se.kth.nada.kmr.collaborilla.util.InfoMessage;

/**
 * This thread is executed when a shutdown signal is sent to the daemon. Does
 * not work with forced immediate kills like "kill -9".
 * 
 * @author Hannes Ebner
 */
public class CollaborillaServiceShutdown extends Thread {
    /* status message handling */
    private static InfoMessage log = InfoMessage.getInstance();

    private int shutdownTimeout;

    /**
         * @param shutdownTimeout
         *                Timeout in milliseconds
         */
    public CollaborillaServiceShutdown(int shutdownTimeout) {
	this.shutdownTimeout = shutdownTimeout;
    }

    public void run() {
	// disallow new connections
	CollaborillaService.allowConnections = false;

	// sleep 100 ms while waiting for clients to disconnect
	int sleepIntervall = 100;
	int timer = 0;

	log.writeLog(CollaborillaService.applicationName, "Received signal to exit");

	if (CollaborillaServiceCommunication.getClientCount() > 0) {
	    log.writeLog(CollaborillaService.applicationName, "Waiting a max. of " + shutdownTimeout / 1000
		    + " seconds for client(s) to disconnect");

	    while ((CollaborillaServiceCommunication.getClientCount() > 0)) {
		timer += sleepIntervall;

		if (timer >= this.shutdownTimeout) {
		    log.writeLog(CollaborillaService.applicationName, "Shutdown timeout expired. Clients still connected");
		    break;
		}

		try {
		    sleep(sleepIntervall);
		} catch (InterruptedException ie) {
		}
	    }

	    log.writeLog(CollaborillaService.applicationName, "All connections closed");
	}

	log.writeLog(CollaborillaService.applicationName, "Shutting down");
    }
}
