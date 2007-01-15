/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.service;

/**
 * Helper class to contain a status code and an eventually existing message
 * (with data that has been requested).
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class ResponseMessage {

	/**
	 * The status code of a reply from the server.
	 */
	public int statusCode = Status.SC_UNKNOWN;

	/**
	 * The data of a reply from the server.
	 */
	/**
	 * 
	 */
	public String[] responseData = null;

	public ResponseMessage() {
	}

	/**
	 * @param code Status code. An integer from CollaborillaStatus.SC_*.
	 */
	public ResponseMessage(int code) {
		this();
		this.statusCode = code;
	}

	/**
	 * @param code Status code. An integer from CollaborillaStatus.SC_*.
	 * @param response Data received from the server.
	 */
	public ResponseMessage(int code, String response) {
		this(code);
		this.responseData = new String[1];
		this.responseData[0] = response;
	}

	/**
	 * @param code Status code. An integer from CollaborillaStatus.SC_*.
	 * @param response Data received from the server.
	 */
	public ResponseMessage(int code, String[] response) {
		this(code);
		this.responseData = response;
	}

}