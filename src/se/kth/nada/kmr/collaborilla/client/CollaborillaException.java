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

package se.kth.nada.kmr.collaborilla.client;

import se.kth.nada.kmr.collaborilla.service.CollaborillaServiceStatus;

public class CollaborillaException extends Exception {
	public static final class ErrorCode extends CollaborillaServiceStatus {
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
