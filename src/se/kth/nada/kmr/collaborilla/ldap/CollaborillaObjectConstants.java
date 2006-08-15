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

package se.kth.nada.kmr.collaborilla.ldap;

/**
 * Provides string constants for accessing fields and attributes of the custom
 * LDAP ObjectClass.
 * 
 * @author Hannes Ebner
 */
public final class CollaborillaObjectConstants {

    /*
         * LDAP internal timestamp attributes
         */
    public static final String DATECREATED = "createTimestamp";

    public static final String DATEMODIFIED = "modifyTimestamp";

    /*
         * Prefix for ObjectClass and Attribute names
         */
    public static final String PREFIX = "collaborilla";

    /*
         * Tree root in the LDAP directory
         */
    public static final String ROOT = PREFIX + "DataTree";

    /*
         * Name of the custom ObjectClass
         */
    public static final String OBJECTCLASS = PREFIX + "Object";

    /*
         * Name and Type of the INFO node containing data (revision, ...)
         */
    public static final String INFONODE = PREFIX + "Data";

    public static final String INFONODETYPE = "cn"; /* common name */

    public static final String INFOCONTAINERTYPE = "ou"; /* organizationalUnit */

    /*
         * Attributes inherited from LDAP ObjectClass "top"
         */
    public static final String ID = "cn";

    public static final String DESCRIPTION = "description";

    /*
         * Referrer Attributes
         */
    public static final String URIORIG = PREFIX + "UriOriginal";

    public static final String URIOTHER = PREFIX + "UriOther";

    public static final String CONTEXTRDFINFO = PREFIX + "ContextRdfInfo";

    /*
         * Resolver Attributes
         */
    public static final String LOCATION = PREFIX + "Location";

    public static final String CONTAINERRDFINFO = PREFIX + "ContainerRdfInfo";

    public static final String CONTAINERREVISION = PREFIX + "ContainerRevision";

    /*
         * Not used / deprecated
         */
    public static final String TYPE = PREFIX + "ObjectType";

    public static final String DELETED = PREFIX + "ObjectDeleted";
}
