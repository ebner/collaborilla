/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logging helper class. Provides several logging destinations.
 * 
 * TODO Right now this class is a Singleton, but should be extended to a
 * Factory later (when we implement more than just one destination).
 * 
 * TODO Use log4j
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class InfoMessage {
	/*
	 * Available destinations
	 */
	public static final int DEST_CONSOLE = 1;

	public static final int DEST_FILE = 2;

	public static final int DEST_SYSLOG = 3;

	/**
	 * Destinaton to which the output should be written to.
	 */
	private int destination;

	/**
	 * Our private and only instance of this Object.
	 */
	private static InfoMessage instance;

	/**
	 * Initializes the object with the default destination "console".
	 */
	private InfoMessage() {
		this.destination = DEST_CONSOLE;
	}

	/**
	 * Initializes the object and sets a custom destination.
	 */
	public synchronized static InfoMessage getInstance() {
		if (instance == null) {
			instance = new InfoMessage();
		}
		return instance;
	}

	/**
	 * Writes the given message to the console.
	 * 
	 * @param message
	 *            Message
	 */
	private void writeToConsole(String message) {
		System.out.println(message);
	}

	/**
	 * Writes the given message to a file.
	 * 
	 * @param message
	 *            Message
	 */
	private void writeToFile(String message) {
		// TODO
	}

	/**
	 * Writes the given message to the Syslog.
	 * 
	 * @param message
	 *            Message
	 */
	private void writeToSyslog(String message) {
		// TODO
	}

	/**
	 * Default writing function. Writes the message based on the destination
	 * value to various locations.
	 * 
	 * @param message
	 *            Message
	 */
	public void write(String message) {
		switch (this.destination) {
		case DEST_CONSOLE:
			writeToConsole(message);
			break;
		case DEST_FILE:
			writeToFile(message);
			break;
		case DEST_SYSLOG:
			writeToSyslog(message);
			break;
		default:
			throw new IllegalArgumentException("Unknown destination");
		}
	}

	/**
	 * Writes a message with the current date/time.
	 * 
	 * @param object
	 *            Object like IP address
	 * @param activity
	 *            Activity like Connect or Disconnect
	 */
	public void writeLog(String object, String activity) {
		this.write(getCurrentDateTime() + "  [" + object + "] " + activity);
	}

	/**
	 * Creates a String with the current date and the time.
	 * 
	 * @return Current Date/Time
	 */
	public static String getCurrentDateTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz").format(new Date());
	}

	/**
	 * We don't allow a cloned Object, so we just throw an Exception.
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		// we want a Singleton, so we throw an Exception
		throw new CloneNotSupportedException();
	}

}
