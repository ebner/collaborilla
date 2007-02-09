/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.util.Date;
import java.util.Set;

/**
 * Collaborilla client interface. Defines all necessary functions in order to
 * implement a working client.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public interface CollaborillaStatefulClient {

	/**
	 * Connects to the service.
	 * 
	 * @throws CollaborillaException
	 */
	void connect() throws CollaborillaException;

	/**
	 * Disconnects from the service.
	 * 
	 * @throws CollaborillaException
	 */
	void disconnect() throws CollaborillaException;

	/**
	 * Checks whether the connection is up.
	 */
	boolean isConnected();

	/**
	 * Retrieves all information of the most recent revision into a serializable
	 * object.
	 * 
	 * @return CollaborillaDataSet
	 * @throws CollaborillaException
	 */
	CollaborillaDataSet getDataSet() throws CollaborillaException;

	/**
	 * Gets the URI of the currently accessed LDAP entry.
	 * 
	 * @throws CollaborillaException
	 */
	String getIdentifier() throws CollaborillaException;

	/**
	 * Sets the URI of the LDAP entry and rebuilds the Base DN.
	 * 
	 * @param uri
	 *            URI
	 * @param create
	 *            Tells the method to create the object of it does not exist yet
	 * @throws CollaborillaException
	 */
	void setIdentifier(String uri, boolean create) throws CollaborillaException;

	/**
	 * Returns the number of the current revision.
	 * 
	 * @return Current revision number.&nbsp;If we work with an up-to-date
	 *         object (the latest revision) the returned value is 0.
	 * @throws CollaborillaException
	 */
	int getRevisionNumber() throws CollaborillaException;

	/**
	 * Sets the number of the revision. After setting the revision the Base DN
	 * will be rebuilt and all operations will be performed at the revision with
	 * the number of the parameter.
	 * 
	 * @param rev
	 *            Revision number.&nbsp;Should be 0 to return to the most recent
	 *            LDAP entry.
	 * @throws CollaborillaException
	 */
	void setRevisionNumber(int rev) throws CollaborillaException;

	/**
	 * Returns the number of revisions in the LDAP directory.
	 * 
	 * @return Number of available revisions
	 * @throws CollaborillaException
	 */
	int getRevisionCount() throws CollaborillaException;

	/**
	 * Returns information of the current revision.
	 * 
	 * @return Info of the current revision, currently RDF info.&nbsp;Will be
	 *         probably changed in future.
	 * @throws CollaborillaException
	 */
	String getRevisionInfo() throws CollaborillaException;

	/**
	 * Returns information of a current revision.
	 * 
	 * @param rev
	 * @return Revision info
	 * @throws CollaborillaException
	 * @see #getRevisionNumber()
	 */
	String getRevisionInfo(int rev) throws CollaborillaException;

	/**
	 * Sets the current revision to the most recent entry and copies all data
	 * into a new revision. Performs a setRevision(0).
	 * 
	 * @return Returns the number of the revision which has been outdated by
	 *         this command. The value will be: number_of_the_new_revison - 1.
	 * @throws CollaborillaException
	 */
	int createRevision() throws CollaborillaException;

	/**
	 * Restores a revision and makes it the most recent revision.
	 * <p>
	 * The current entry is copied to a revision, all fields removed and the
	 * fields of the to-be-restored revision are copied to the most recent
	 * entry.
	 * 
	 * @param rev
	 *            Revision which should be restored
	 * @throws CollaborillaException
	 */
	void restoreRevision(int rev) throws CollaborillaException;

	/**
	 * Reads all URLs of the entry and returns a String array. If the Location
	 * attribute of this entry does not exist it will try to construct Locations
	 * by querying the entries of the parent URIs.
	 * 
	 * @return Set of URLs
	 * @throws CollaborillaException
	 */
	Set getAlignedLocations() throws CollaborillaException;

	/**
	 * Reads all locations of the entry and returns a collection of Strings.
	 * 
	 * @return Collection of URLs
	 * @throws CollaborillaException
	 */
	Set getLocations() throws CollaborillaException;

	/**
	 * Sets the locations of the entry, overwriting already existing values.
	 * 
	 * @param locations Collection of URLs
	 * @throws CollaborillaException
	 */
	void setLocations(Set locations) throws CollaborillaException;

	/**
	 * Removes all locations of the entry.
	 * 
	 * @throws CollaborillaException
	 */
	void clearLocations() throws CollaborillaException;
	
	/**
	 * Adds a new location to the LDAP entry.
	 * 
	 * @param url
	 *            URL
	 * @throws CollaborillaException
	 */
	void addLocation(String url) throws CollaborillaException;

	/**
	 * Removes a location from the LDAP entry.
	 * 
	 * @param url
	 *            URL to be removed
	 * @throws CollaborillaException
	 */
	void removeLocation(String url) throws CollaborillaException;

	/**
	 * Reads all URIs of the entry and returns a Set.
	 * 
	 * @return Set of URI Strings.
	 * @throws CollaborillaException
	 */
	Set getRequiredContainers() throws CollaborillaException;
	
	/**
	 * Sets the container URIs, overwriting already existing values.
	 * 
	 * @param containers Set of URI Strings.
	 * @throws CollaborillaException
	 */
	void setRequiredContainers(Set containers) throws CollaborillaException;
	
	/**
	 * Removes all container URIs.
	 * 
	 * @throws CollaborillaException
	 */
	void clearRequiredContainers() throws CollaborillaException;
	
	/**
	 * Adds a new URI field to the LDAP entry.
	 * 
	 * @param uri
	 *            URI
	 * @throws CollaborillaException
	 */
	void addRequiredContainer(String uri) throws CollaborillaException;

	/**
	 * Removes a URI from the LDAP entry.
	 * 
	 * @param uri
	 *            URI to be removed
	 * @throws CollaborillaException
	 */
	void removeRequiredContainer(String uri) throws CollaborillaException;

	/**
	 * Reads all URIs of the entry and returns a String array.
	 * 
	 * @return Set of URIs
	 * @throws CollaborillaException
	 */
	Set getOptionalContainers() throws CollaborillaException;
	
	/**
	 * Sets the container URIs, overwriting already existing values.
	 * 
	 * @param containers Set of URI Strings.
	 * @throws CollaborillaException
	 */
	void setOptionalContainers(Set containers) throws CollaborillaException;

	/**
	 * Removes all container URIs.
	 * 
	 * @throws CollaborillaException
	 */
	void clearOptionalContainers() throws CollaborillaException;
	
	/**
	 * Adds a new URI field to the LDAP entry.
	 * 
	 * @param uri
	 *            URI
	 * @throws CollaborillaException
	 */
	void addOptionalContainer(String uri) throws CollaborillaException;

	/**
	 * Removes a URI from the LDAP entry.
	 * 
	 * @param uri
	 *            URI to be removed
	 * @throws CollaborillaException
	 */
	void removeOptionalContainer(String uri) throws CollaborillaException;

	/**
	 * Returns the RDF info field.
	 * 
	 * @return RDF info field
	 * @throws CollaborillaException
	 */
	String getContextRdfInfo() throws CollaborillaException;

	/**
	 * Sets the RDF info field.
	 * 
	 * @param rdfInfo
	 *            RDF info
	 * @throws CollaborillaException
	 */
	void setContextRdfInfo(String rdfInfo) throws CollaborillaException;

	/**
	 * Removes an eventually existing RDF info field.
	 * 
	 * @throws CollaborillaException
	 */
	void removeContextRdfInfo() throws CollaborillaException;

	/**
	 * Returns the RDF location info field.
	 * 
	 * @return RDF location info field
	 * @throws CollaborillaException
	 */
	String getContainerRdfInfo() throws CollaborillaException;

	/**
	 * Sets the RDF location info field.
	 * 
	 * @param rdfLocationInfo
	 *            RDF location info
	 * @throws CollaborillaException
	 */
	void setContainerRdfInfo(String rdfLocationInfo) throws CollaborillaException;

	/**
	 * Removes an eventually existing RDF location info field.
	 * 
	 * @throws CollaborillaException
	 */
	void removeContainerRdfInfo() throws CollaborillaException;

	/**
	 * Returns the revision of the container in the RCS.
	 * 
	 * @return RDF location info field
	 * @throws CollaborillaException
	 */
	String getContainerRevision() throws CollaborillaException;

	/**
	 * Sets the revision of the container in the RCS.
	 * 
	 * @param containerRevision
	 *            Revision of the checked in container which corresponds to the
	 *            collaboration information handled by this object.
	 * @throws CollaborillaException
	 */
	void setContainerRevision(String containerRevision) throws CollaborillaException;

	/**
	 * Returns the description field of the LDAP entry.
	 * 
	 * @return Description
	 * @throws CollaborillaException
	 */
	String getDescription() throws CollaborillaException;

	/**
	 * Sets the description field of the LDAP entry.
	 * 
	 * @param desc
	 *            Description
	 * @throws CollaborillaException
	 */
	void setDescription(String desc) throws CollaborillaException;

	/**
	 * Removes the description field of the LDAP entry.
	 * 
	 * @throws CollaborillaException
	 */
	void removeDescription() throws CollaborillaException;

	/**
	 * Returns the entry and its attributes in LDIF format. Can be used to
	 * export an existing entry from the LDAP directory.
	 * 
	 * @return LDIF data
	 * @throws CollaborillaException
	 */
	String getLdif() throws CollaborillaException;

	/**
	 * Returns the date and time of the creation of the LDAP entry.
	 * 
	 * @return Timestamp
	 * @throws CollaborillaException
	 */
	Date getTimestampCreated() throws CollaborillaException;

	/**
	 * Returns the date and time of the last modification of the LDAP entry.
	 * 
	 * @return Timestamp
	 * @throws CollaborillaException
	 */
	Date getTimestampModified() throws CollaborillaException;

}