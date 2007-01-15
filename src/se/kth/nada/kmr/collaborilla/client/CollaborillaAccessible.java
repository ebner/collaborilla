/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.util.Date;
import java.util.Set;

import com.novell.ldap.LDAPException;

/**
 * Collaborilla client interface. Defines all necessary functions in order to
 * implement a working client.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public interface CollaborillaAccessible {

	/**
	 * Connects to the service.
	 */
	public abstract void connect() throws CollaborillaException;

	/**
	 * Disconnects from the service.
	 */
	public abstract void disconnect() throws CollaborillaException;

	/**
	 * Checks whether the connection is up.
	 */
	public abstract boolean isConnected();

	/**
	 * Retrieves all information of the most recent revision into a serializable
	 * object.
	 * 
	 * @return CollaborillaDataSet
	 * @throws CollaborillaException
	 */
	public abstract CollaborillaDataSet getDataSet() throws CollaborillaException;

	/**
	 * Gets the URI of the currently accessed LDAP entry.
	 */
	public abstract String getIdentifier() throws CollaborillaException;

	/**
	 * Sets the URI of the LDAP entry and rebuilds the Base DN.
	 * 
	 * @param uri
	 *            URI
	 * @param create
	 *            Tells the method to create the object of it does not exist yet
	 */
	public abstract void setIdentifier(String uri, boolean create) throws CollaborillaException;

	/**
	 * Returns the number of the current revision.
	 * 
	 * @return Current revision number.&nbsp;If we work with an up-to-date
	 *         object (the latest revision) the returned value is 0.
	 */
	public abstract int getRevisionNumber() throws CollaborillaException;

	/**
	 * Sets the number of the revision. After setting the revision the Base DN
	 * will be rebuilt and all operations will be performed at the revision with
	 * the number of the parameter.
	 * 
	 * @param rev
	 *            Revision number.&nbsp;Should be 0 to return to the most recent
	 *            LDAP entry.
	 */
	public abstract void setRevisionNumber(int rev) throws CollaborillaException;

	/**
	 * Returns the number of revisions in the LDAP directory.
	 * 
	 * @return Number of available revisions
	 * @throws LDAPException
	 */
	public abstract int getRevisionCount() throws CollaborillaException;

	/**
	 * Returns information of the current revision.
	 * 
	 * @return Info of the current revision, currently RDF info.&nbsp;Will be
	 *         probably changed in future.
	 * @throws LDAPException
	 */
	public abstract String getRevisionInfo() throws CollaborillaException;

	/**
	 * Returns information of a current revision.
	 * 
	 * @param rev
	 * @return Revision info
	 * @throws LDAPException
	 * @see #getRevisionNumber()
	 */
	public abstract String getRevisionInfo(int rev) throws CollaborillaException;

	/**
	 * Sets the current revision to the most recent entry and copies all data
	 * into a new revision. Performs a setRevision(0).
	 * 
	 * @throws LDAPException
	 */
	public abstract void createRevision() throws CollaborillaException;

	/**
	 * Restores a revision and makes it the most recent revision.
	 * <p>
	 * The current entry is copied to a revision, all fields removed and the
	 * fields of the to-be-restored revision are copied to the most recent
	 * entry.
	 * 
	 * @param rev
	 *            Revision which should be restored
	 */
	public abstract void restoreRevision(int rev) throws CollaborillaException;

	/**
	 * Reads all URLs of the entry and returns a String array. If the Location
	 * attribute of this entry does not exist it will try to construct Locations
	 * by querying the entries of the parent URIs.
	 * 
	 * @return Set of URLs
	 * @throws LDAPException
	 */
	public abstract Set getAlignedLocation() throws CollaborillaException;

	/**
	 * Reads all URLs of the entry and returns a collection of Strings.
	 * 
	 * @return Collection of URLs
	 * @throws LDAPException
	 */
	public abstract Set getLocation() throws CollaborillaException;

	/**
	 * Adds a new URL field to the LDAP entry.
	 * 
	 * @param url
	 *            URL
	 * @throws LDAPException
	 */
	public abstract void addLocation(String url) throws CollaborillaException;

	/**
	 * Modifies an already existing URL in the LDAP entry.
	 * 
	 * @param oldUrl
	 *            URL to be modified
	 * @param newUrl
	 *            New URL
	 * @throws LDAPException
	 */
	public abstract void modifyLocation(String oldUrl, String newUrl) throws CollaborillaException;

	/**
	 * Removes a URL from the LDAP entry.
	 * 
	 * @param url
	 *            URL to be removed
	 * @throws LDAPException
	 */
	public abstract void removeLocation(String url) throws CollaborillaException;

	/**
	 * Reads all URIs of the entry and returns a String array.
	 * 
	 * @return Set of URIs
	 * @throws LDAPException
	 */
	public abstract Set getUriOriginal() throws CollaborillaException;

	/**
	 * Adds a new URI field to the LDAP entry.
	 * 
	 * @param uri
	 *            URI
	 * @throws LDAPException
	 */
	public abstract void addUriOriginal(String uri) throws CollaborillaException;

	/**
	 * Modifies an already existing URI in the LDAP entry.
	 * 
	 * @param oldUri
	 *            URI to be modified
	 * @param newUri
	 *            New URI
	 * @throws LDAPException
	 */
	public abstract void modifyUriOriginal(String oldUri, String newUri) throws CollaborillaException;

	/**
	 * Removes a URI from the LDAP entry.
	 * 
	 * @param uri
	 *            URI to be removed
	 * @throws LDAPException
	 */
	public abstract void removeUriOriginal(String uri) throws CollaborillaException;

	/**
	 * Reads all URIs of the entry and returns a String array.
	 * 
	 * @return Set of URIs
	 * @throws LDAPException
	 */
	public abstract Set getUriOther() throws CollaborillaException;

	/**
	 * Adds a new URI field to the LDAP entry.
	 * 
	 * @param uri
	 *            URI
	 * @throws LDAPException
	 */
	public abstract void addUriOther(String uri) throws CollaborillaException;

	/**
	 * Modifies an already existing URI in the LDAP entry.
	 * 
	 * @param oldUri
	 *            URI to be modified
	 * @param newUri
	 *            New URI
	 * @throws LDAPException
	 */
	public abstract void modifyUriOther(String oldUri, String newUri) throws CollaborillaException;

	/**
	 * Removes a URI from the LDAP entry.
	 * 
	 * @param uri
	 *            URI to be removed
	 * @throws LDAPException
	 */
	public abstract void removeUriOther(String uri) throws CollaborillaException;

	/**
	 * Returns the RDF info field.
	 * 
	 * @return RDF info field
	 * @throws LDAPException
	 */
	public abstract String getContextRdfInfo() throws CollaborillaException;

	/**
	 * Sets the RDF info field.
	 * 
	 * @param rdfInfo
	 *            RDF info
	 * @throws LDAPException
	 */
	public abstract void setContextRdfInfo(String rdfInfo) throws CollaborillaException;

	/**
	 * Removes an eventually existing RDF info field.
	 * 
	 * @throws LDAPException
	 */
	public abstract void removeContextRdfInfo() throws CollaborillaException;

	/**
	 * Returns the RDF location info field.
	 * 
	 * @return RDF location info field
	 * @throws LDAPException
	 */
	public abstract String getContainerRdfInfo() throws CollaborillaException;

	/**
	 * Sets the RDF location info field.
	 * 
	 * @param rdfLocationInfo
	 *            RDF location info
	 * @throws LDAPException
	 */
	public abstract void setContainerRdfInfo(String rdfLocationInfo) throws CollaborillaException;

	/**
	 * Removes an eventually existing RDF location info field.
	 * 
	 * @throws LDAPException
	 */
	public abstract void removeContainerRdfInfo() throws CollaborillaException;

	/**
	 * Returns the revision of the container in the RCS.
	 * 
	 * @return RDF location info field
	 * @throws LDAPException
	 */
	public abstract String getContainerRevision() throws CollaborillaException;

	/**
	 * Sets the revision of the container in the RCS.
	 * 
	 * @param containerRevision
	 *            Revision of the checked in container which corresponds to the
	 *            collaboration information handled by this object.
	 * @throws LDAPException
	 */
	public abstract void setContainerRevision(String containerRevision) throws CollaborillaException;

	/**
	 * Returns the description field of the LDAP entry.
	 * 
	 * @return Description
	 * @throws LDAPException
	 */
	public abstract String getDescription() throws CollaborillaException;

	/**
	 * Sets the description field of the LDAP entry.
	 * 
	 * @param desc
	 *            Description
	 * @throws LDAPException
	 */
	public abstract void setDescription(String desc) throws CollaborillaException;

	/**
	 * Removes the description field of the LDAP entry.
	 * 
	 * @throws LDAPException
	 */
	public abstract void removeDescription() throws CollaborillaException;

	/**
	 * Returns the entry and its attributes in LDIF format. Can be used to
	 * export an existing entry from the LDAP directory.
	 * 
	 * @return LDIF data
	 * @throws LDAPException
	 */
	public String getLdif() throws CollaborillaException;

	/**
	 * Returns the date and time of the creation of the LDAP entry.
	 * 
	 * @return Timestamp
	 * @throws CollaborillaException
	 */
	public Date getTimestampCreated() throws CollaborillaException;

	/**
	 * Returns the date and time of the last modification of the LDAP entry.
	 * 
	 * @return Timestamp
	 * @throws CollaborillaException
	 */
	public Date getTimestampModified() throws CollaborillaException;

}
