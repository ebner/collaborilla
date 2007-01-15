/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import se.kth.nada.kmr.collaborilla.service.Status;

/**
 * Custom exception class to help getting better exception messages.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class CollaborillaException extends Exception {
	
	private static final long serialVersionUID = 990737578487684483L;

	public static final class ErrorCode extends Status {
	}

	private int resultCode = 0;

	public CollaborillaException(int error) {
		this.resultCode = error;
	}

	public CollaborillaException(int error, Throwable cause) {
		this.resultCode = error;
		this.initCause(cause);
	}

	public CollaborillaException(String message) {
		super(message);
	}

	public CollaborillaException(Throwable cause) {
		super(cause);
	}

	public CollaborillaException(String message, Throwable cause) {
		super(message, cause);
	}

	public int getResultCode() {
		return this.resultCode;
	}

	public String getMessage() {
		return resultCodeToString(this.resultCode);
	}

	public static String resultCodeToString(int resultCode) {
		String status;

		switch (resultCode) {
		case ErrorCode.SC_CONNECTION_FAILED:
			status = "Connection failed.";
			break;
		case ErrorCode.SC_BAD_REQUEST:
			status = "Bad request to server.";
			break;
		case ErrorCode.SC_CLIENT_TIMEOUT:
			status = "Connection timeout exceeded.";
			break;
		case ErrorCode.SC_UNAUTHORIZED:
			status = "Authorization required";
			break;
		case ErrorCode.SC_FORBIDDEN:
			status = "Activity not allowed.";
			break;
		case ErrorCode.SC_INTERNAL_ERROR:
			status = "Internal server error.";
			break;
		case ErrorCode.SC_NO_SUCH_ATTRIBUTE:
			status = "Attribute not found.";
			break;
		case ErrorCode.SC_NO_SUCH_VALUE:
			status = "Value not found.";
			break;
		case ErrorCode.SC_NO_SUCH_OBJECT:
		case ErrorCode.SC_NOT_FOUND:
			status = "Object not found.";
			break;
		case ErrorCode.SC_SERVER_ERROR:
			status = "Server error.";
			break;
		case ErrorCode.SC_SERVICE_UNAVAILABLE:
			status = "Service not available.";
			break;
		case ErrorCode.SC_ATTRIBUTE_OR_VALUE_EXISTS:
			status = "Attribute or value exists.";
			break;
		case ErrorCode.SC_REVISION_NOT_EDITABLE:
			status = "Policy violation. Revision not editable.";
			break;
		default:
			status = "Unknown error.";
			break;
		}

		return status;
	}

}
