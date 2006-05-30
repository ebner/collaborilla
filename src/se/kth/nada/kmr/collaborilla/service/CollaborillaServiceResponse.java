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

public class CollaborillaServiceResponse {
    public int statusCode = CollaborillaServiceStatus.SC_UNKNOWN;

    public String[] responseMessage = null;

    public CollaborillaServiceResponse() {

    }

    public CollaborillaServiceResponse(int code) {
        this();
        this.statusCode = code;
    }

    public CollaborillaServiceResponse(int code, String response) {
        this(code);
        this.responseMessage = new String[1];
        this.responseMessage[0] = response;
    }

    public CollaborillaServiceResponse(int code, String[] response) {
        this(code);
        this.responseMessage = response;
    }

}
