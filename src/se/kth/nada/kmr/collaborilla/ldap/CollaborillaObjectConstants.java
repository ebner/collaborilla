/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.ldap;

/**
 * Provides string constants for accessing fields and attributes of the custom
 * LDAP ObjectClass.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public final class CollaborillaObjectConstants {

	/**
	 * LDAP internal timestamp attribute: date of creation.
	 */
	public static final String DATECREATED = "createTimestamp";

	/**
	 * LDAP internal timestamp attribute: date of last modification.
	 */
	public static final String DATEMODIFIED = "modifyTimestamp";

	/**
	 * Prefix for ObjectClass and Attribute names.
	 */
	public static final String PREFIX = "collaborilla";

	/**
	 * Tree root in the LDAP directory.
	 */
	public static final String ROOT = PREFIX + "DataTree";

	/**
	 * Name of the custom ObjectClass
	 */
	public static final String OBJECTCLASS = PREFIX + "Object";

	/**
	 * Name of the INFO node containing data (revision, ...)
	 */
	public static final String INFONODE = PREFIX + "Data";

	/**
	 * Type of the INFO node containing data: common name (cn).
	 */
	public static final String INFONODETYPE = "cn";

	/**
	 * Type of the LDAP structure building entries: organizationalUnit (ou).
	 */
	public static final String INFOCONTAINERTYPE = "ou";

	/**
	 * Attribute inherited from LDAP ObjectClass "top": common name (cn).
	 */
	public static final String ID = "cn";

	/**
	 * Identifier (URI) of the entry.
	 */
	public static final String URI = PREFIX + "URI";
	
	/**
	 * Tells the type of the entry (e.g. container, contextmap, agent, etc.).
	 */
	public static final String TYPE = PREFIX + "EntryType";
	
	/**
	 * Attribute inherited from LDAP ObjectClass "top": description.
	 */
	public static final String DESCRIPTION = "description";

	public static final String METADATA = PREFIX + "MetaData";
	
	public static final String DELETED = PREFIX + "EntryDeleted";
	
	/*
	 * Referrer Attributes
	 */
	public static final String REQUIREDCONTAINER = PREFIX + "RequiredContainer";

	public static final String OPTIONALCONTAINER = PREFIX + "OptionalContainer";

	/*
	 * Resolver Attributes
	 */
	public static final String LOCATION = PREFIX + "Location";

	public static final String CONTAINERRDFINFO = PREFIX + "ContainerRdfInfo";

	public static final String CONTAINERREVISION = PREFIX + "ContainerRevision";

}
