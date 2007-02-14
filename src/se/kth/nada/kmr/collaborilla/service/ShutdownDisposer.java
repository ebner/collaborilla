/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.service;

import se.kth.nada.kmr.collaborilla.util.InfoMessage;

/**
 * This thread is executed when a shutdown signal is sent to the daemon.<br>
 * Does not work with forced immediate kills like "kill -9".
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class ShutdownDisposer extends Thread {

	/* status message handling */
	private static InfoMessage log = InfoMessage.getInstance();

	/* timeout before all connections are interrupted */
	private int shutdownTimeout;

	/**
	 * @param shutdownTimeout
	 *            Timeout in milliseconds
	 */
	public ShutdownDisposer(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	public void run() {
		// disallow new connections
		CollaborillaService.allowConnections = false;

		// sleep 100 ms while waiting for clients to disconnect
		int sleepIntervall = 100;
		int timer = 0;

		log.writeLog(CollaborillaService.applicationName, "Received signal to exit");

		if (ClientConnector.getClientCount() > 0) {
			log.writeLog(CollaborillaService.applicationName, "Waiting a max. of " + shutdownTimeout / 1000
					+ " seconds for client(s) to disconnect");

			while ((ClientConnector.getClientCount() > 0)) {
				timer += sleepIntervall;

				if (timer >= this.shutdownTimeout) {
					log.writeLog(CollaborillaService.applicationName,
							"Shutdown timeout expired. Clients still connected");
					break;
				}

				try {
					sleep(sleepIntervall);
				} catch (InterruptedException ie) {
				}
			}

			if (ClientConnector.getClientCount() == 0) {
				log.writeLog(CollaborillaService.applicationName, "All connections closed");
			}
		}

		log.writeLog(CollaborillaService.applicationName, "Shutting down");
	}
}
