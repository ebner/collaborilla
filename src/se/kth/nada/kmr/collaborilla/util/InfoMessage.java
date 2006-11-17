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

package se.kth.nada.kmr.collaborilla.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logging helper class. Provides several logging destinations.
 * 
 * TODO: Right now this class is a Singleton, but should be extended to a
 * Factory later (when we implement more than just one destination).
 * 
 * @author Hannes Ebner
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

	/*
	 * Initializes the object and sets a custom destination.
	 * 
	 * @param dest Destination.&nbsp;Expects a value from enum Destination.
	 */
	/*
	 * private InfoMessage(int dest) { this.destination = dest; }
	 */

	public synchronized static InfoMessage getInstance() {
		if (instance == null) {
			instance = new InfoMessage();
		}

		return instance;
	}

	/*
	 * public synchronized static InfoMessage getInstance(int dest) { if
	 * (infoMessage == null) { infoMessage = new InfoMessage(dest); }
	 * 
	 * return infoMessage; }
	 */

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
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").format(new Date());
	}

	/**
	 * We don't allow a cloned Object, so we just throw an Exception.
	 * 
	 * An example how a clone could be done without this: SingletonObject clone =
	 * (SingletonObject) obj.clone();
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		// we want a Singleton, so we throw an Exception
		throw new CloneNotSupportedException();
	}

}
