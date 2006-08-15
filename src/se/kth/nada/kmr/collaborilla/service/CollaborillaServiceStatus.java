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

/**
 * Definitions of status messages. (related to HTTP)
 * 
 * @author Hannes Ebner
 */
public class CollaborillaServiceStatus {
    public static final String PROTOCOLNAME = "COLLAB";

    public static final String PROTOCOLVERSION = "1.0";

    public static final String PROTOCOLFOOTPRINT = PROTOCOLNAME + "/" + PROTOCOLVERSION;

    /* 2XX: generally "OK" */
    public static final int SC_OK = 200;

    public static final int SC_CREATED = 201;

    /* 3XX: relocation/redirect */

    /* 4XX: client error */
    public static final int SC_BAD_REQUEST = 400;

    public static final int SC_UNAUTHORIZED = 401;

    public static final int SC_FORBIDDEN = 403;

    public static final int SC_NOT_FOUND = 404;

    public static final int SC_CLIENT_TIMEOUT = 408;

    /* 5XX: server error */
    public static final int SC_SERVER_ERROR = 500;

    public static final int SC_INTERNAL_ERROR = 501;

    public static final int SC_SERVICE_UNAVAILABLE = 503;

    /*
         * 6XX: request status THIS IS NON HTTP AND COLLABORILLA SPECIFIC
         */
    public static final int SC_CLIENT_DISCONNECT = 600;

    public static final int SC_NO_SUCH_OBJECT = 601;

    public static final int SC_NO_SUCH_ATTRIBUTE = 602;

    public static final int SC_NO_SUCH_VALUE = 603;

    public static final int SC_MODIFIED = 604;

    public static final int SC_SERVER_TIMEOUT = 605;

    public static final int SC_ATTRIBUTE_OR_VALUE_EXISTS = 606;

    public static final int SC_UNKNOWN = 999;

    /**
         * Returns a status message for the given status code.
         * 
         * @param code
         *                Status code
         * @return Status message
         */
    public static String getMessage(int code) {
	String message;

	switch (code) {
	case SC_OK:
	    message = "OK";
	    break;
	case SC_CREATED:
	    message = "CREATED";
	    break;
	case SC_NOT_FOUND:
	    message = "NOT FOUND";
	    break;
	case SC_BAD_REQUEST:
	    message = "BAD REQUEST";
	    break;
	case SC_INTERNAL_ERROR:
	    message = "INTERNAL ERROR";
	    break;
	case SC_NO_SUCH_ATTRIBUTE:
	    message = "NO SUCH ATTRIBUTE";
	    break;
	case SC_NO_SUCH_OBJECT:
	    message = "NO SUCH OBJECT";
	    break;
	case SC_NO_SUCH_VALUE:
	    message = "NO SUCH VALUE";
	    break;
	case SC_CLIENT_DISCONNECT:
	    message = "CLIENT DISCONNECT. BYE";
	    break;
	case SC_CLIENT_TIMEOUT:
	    message = "TIMEOUT EXCEEDED";
	    break;
	case SC_ATTRIBUTE_OR_VALUE_EXISTS:
	    message = "ATTRIBUTE OR VALUE EXISTS";
	    break;
	case SC_UNKNOWN:
	default:
	    message = "UNKNOWN ERROR";
	    break;
	}

	return PROTOCOLFOOTPRINT + " " + String.valueOf(code) + " " + message;
    }

}
