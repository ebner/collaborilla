/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.ldap;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPConnection;

/**
 * Provides methods to directly access and manipulate entries and fields needed
 * to allow collaboration in the concept browser "Conzilla".
 * <p>
 * Extends the generic class LdapObject.
 * 
 * @author Hannes Ebner
 * @version $Id$
 * @see LDAPObject
 */
public class CollaborillaObject extends LDAPObject implements Cloneable {
	private int revision = 0;

	private String serverDN;

	private String uri = new String();

	/*
	 * Constructor
	 * 
	 * 
	 */

	/**
	 * Creates an object and sets all necessary fields. If the LDAP entry
	 * described by the URI parameter does not exist yet it will be created by
	 * the constructor.
	 * 
	 * @param dir
	 *            LDAP connection
	 * @param serverDN
	 *            Server Distinctive Name (DN).&nbsp;Example: "dc=test,dc=com".
	 * @param uri
	 *            URI
	 * @param create
	 *            Create the LDAP entry of the given DN should be created if it
	 *            does not exist yet
	 * @throws LDAPException
	 */
	public CollaborillaObject(LDAPAccess dir, String serverDN, String uri, boolean create) throws LDAPException {
		this.ldapAccess = dir;
		this.serverDN = serverDN;
		this.setAccessUri(uri);

		if (!this.entryExists()) {
			if (create) {
				this.createEntryWithContainer(LDAPStringHelper.dnToParentDN(this.baseDN),
						CollaborillaObjectConstants.OBJECTCLASS, CollaborillaObjectConstants.INFONODETYPE,
						CollaborillaObjectConstants.INFONODE);
			} else {
				throw new LDAPException("NO SUCH OBJECT", LDAPException.NO_SUCH_OBJECT, LDAPException
						.resultCodeToString(LDAPException.NO_SUCH_OBJECT), this.baseDN);
			}
		}
	}

	/*
	 * Overriding methods
	 * 
	 * 
	 */

	/**
	 * Returns a copy of the current object. The LDAP connection is shared.
	 * 
	 * @see LDAPObject#clone()
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		CollaborillaObject newObject = (CollaborillaObject) super.clone();

		return newObject;
	}

	/**
	 * Returns a combination of URI and Base DN as a String value.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.uri + ":" + this.baseDN;
	}

	/*
	 * Internal
	 * 
	 * 
	 */

	/**
	 * Updates the Base DN for accessing the right LDAP entry. Takes the Server
	 * DN, URI and the revision into consideration for creating a Base DN.
	 */
	private void updateBaseDN() {
		String tmpDN = CollaborillaObjectConstants.INFONODETYPE
				+ "="
				+ CollaborillaObjectConstants.INFONODE
				+ ","
				+ LDAPStringHelper.uriToBaseDN(CollaborillaObjectConstants.ROOT, this.serverDN, this.uri,
						CollaborillaObjectConstants.INFOCONTAINERTYPE);

		if (this.revision > 0) {
			tmpDN = CollaborillaObjectConstants.INFONODETYPE + "=" + this.revision + "," + tmpDN;
		}

		this.baseDN = tmpDN;
	}

	/**
	 * Checks whether the currently selected entry (and its respective revision)
	 * can be modified. Throws an exception if a modification is against the
	 * policy. (E.g. a revisioned entry cannot be modified.)
	 * 
	 * @throws LDAPException
	 */
	private void handleWriteAttempt() throws LDAPException {
		if (!this.isEditable()) {
			throw new LDAPException("UNWILLING TO PERFORM", LDAPException.UNWILLING_TO_PERFORM,
					"Policy violation: Not allowed to modify revision", this.baseDN);
		}
	}

	/*
	 * Public
	 * 
	 * 
	 */

	/**
	 * Tells whether we are allowed to edit the node to which the current object
	 * points. It should not be allowed to edit the history of a node.
	 * 
	 * @return true or false
	 */
	public boolean isEditable() {

		if (this.getRevision() == 0) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the current Base DN.
	 * 
	 * @return Base DN
	 */
	public String getBaseDN() {
		return this.baseDN;
	}

	/**
	 * Returns the URI of the LDAP entry.
	 * 
	 * @return URI
	 */
	public String getAccessUri() {
		return this.uri;
	}

	/**
	 * Sets the URI of the LDAP entry and rebuilds the Base DN.
	 * 
	 * @see #updateBaseDN()
	 * @param uri
	 *            URI
	 */
	public void setAccessUri(String uri) {
		this.uri = uri;
		this.revision = 0;
		this.updateBaseDN();
	}

	/*
	 * Revisions
	 * 
	 * 
	 */

	/**
	 * Returns the number of the current revision.
	 * 
	 * @return Current revision number.&nbsp;If we work with an up-to-date
	 *         object (the latest revision) the returned value is 0.
	 */
	public int getRevision() {
		return this.revision;
	}

	/**
	 * Sets the number of the revision. After setting the revision the Base DN
	 * will be rebuilt and all operations will be performed at the revision with
	 * the number of the parameter.
	 * 
	 * @see #updateBaseDN()
	 * @param rev
	 *            Revision number.&nbsp;Should be 0 to return to the most recent
	 *            LDAP entry.
	 */
	public void setRevision(int rev) throws LDAPException {
		int oldRevision = getRevision();

		/*
		 * if the following two lines are changed, probably the similar lines
		 * after the if have to be changed too. (this is to prevent an
		 * eventually occurring endless loop)
		 */
		this.revision = rev;
		this.updateBaseDN();

		String tmpDN = this.baseDN;

		if (!this.entryExists()) {
			this.revision = oldRevision;
			this.updateBaseDN();

			throw new LDAPException("NO SUCH OBJECT", LDAPException.NO_SUCH_OBJECT, LDAPException
					.resultCodeToString(LDAPException.NO_SUCH_OBJECT), tmpDN);
		}
	}

	/**
	 * Returns the number of revisions in the LDAP directory.
	 * 
	 * @return Number of available revisions
	 * @throws LDAPException
	 */
	public int getRevisionCount() throws LDAPException {
		if (this.revision == 0) {
			return this.childCount(LDAPConnection.SCOPE_ONE);
		} else {
			return this.childCount(LDAPStringHelper.dnToParentDN(this.baseDN), LDAPConnection.SCOPE_ONE);
		}
	}

	/**
	 * Returns information of the current revision.
	 * 
	 * @return Info of the current revision, currently RDF info.&nbsp;Will be
	 *         probably changed in future.
	 * @throws LDAPException
	 */
	public String getRevisionInfo() throws LDAPException {
		/*
		 * perhaps return other information than description? - object with date
		 * of creation and last change - ...?
		 */
		return this.getDescription();
	}

	/**
	 * Returns information of a current revision.
	 * 
	 * @param rev
	 * @return Revision info
	 * @throws LDAPException
	 * @see #getRevision()
	 */
	public String getRevisionInfo(int rev) throws LDAPException {
		String revInfo;
		int currentRev = this.getRevision();

		try {
			this.setRevision(rev);
			revInfo = this.getRevisionInfo();
		} catch (LDAPException e) {
			this.setRevision(currentRev);
			throw new LDAPException("NO SUCH OBJECT", LDAPException.NO_SUCH_OBJECT, LDAPException
					.resultCodeToString(LDAPException.NO_SUCH_OBJECT), this.baseDN);
		}

		return revInfo;
	}

	/**
	 * Sets the current revision to the most recent entry and copies all data
	 * into a new revision. Performs a setRevision(0).
	 * 
	 * @throws LDAPException
	 */
	public void createRevision() throws LDAPException {
		this.setRevision(0);
		int revisionNumber = this.getRevisionCount() + 1;
		String destDN = CollaborillaObjectConstants.INFONODETYPE + "=" + revisionNumber + "," + this.baseDN;

		this.copyEntry(this.baseDN, destDN);
	}

	/**
	 * Returns the Distinctive Name (DN) of a specific revision.
	 * 
	 * @param rev
	 *            Number of revision
	 * @return DN of the requested revision
	 */
	public String getRevisionDN(int rev) {
		if (rev == 0) {
			return this.baseDN;
		} else {
			return CollaborillaObjectConstants.INFONODETYPE + "=" + rev + "," + this.baseDN;
		}
	}

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
	public void restoreRevision(int rev) throws LDAPException {
		if (rev > this.getRevisionCount()) {
			throw new LDAPException("Revision does not exist", LDAPException.NO_SUCH_OBJECT, "Revision does not exist");
		}

		this.setRevision(0);
		this.createRevision();
		this.removeAllAttributes();
		this.copyAttributes(this.getRevisionDN(rev), this.baseDN);
	}

	/*
	 * URL
	 * 
	 * 
	 */

	/**
	 * Reads all URLs of the entry and returns a String array.
	 * 
	 * @return Array of URLs
	 * @throws LDAPException
	 */
	public String[] getLocation() throws LDAPException {
		return this.readAttribute(CollaborillaObjectConstants.LOCATION);
	}

	/**
	 * Reads all URLs of the entry and returns a String array. If the Location
	 * attribute of this entry does not exist it will try to construct Locations
	 * by querying the entries of the parent URIs.
	 * 
	 * @return Array of URLs
	 * @throws LDAPException
	 */
	public String[] getAlignedLocation() throws LDAPException {
		String[] result = null;
		String parentURI = this.uri;
		String originalURI = this.uri;

		result = this.readAttribute(CollaborillaObjectConstants.LOCATION);

		if (result == null) {
			// FLOW
			//
			// 1 check if we can go one level higher, if not -> throw
			// NO_SUCH_ATTRIBUTE
			// 2 one level up, increase level counter
			// 3 get location
			// 4 if NO_SUCH_ATTRIBUTE or NO_SUCH_OBJECT -> 1 (one level up)
			// 5 if we get a location:
			// 5.1 get last part of URI depending on the level counter
			// 5.2 loop through the returned URL and append 5.1
			// 6 return result

			while ((parentURI = LDAPStringHelper.getParentURI(parentURI)) != null) {
				try {
					this.setAccessUri(parentURI);
					result = this.readAttribute(CollaborillaObjectConstants.LOCATION);
				} catch (LDAPException e) {
					if ((e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE)
							|| (e.getResultCode() == LDAPException.NO_SUCH_OBJECT)) {
						continue;
					} else {
						this.setAccessUri(originalURI);
						throw e;
					}
				}

				if (result != null) {
					String append = originalURI.substring(parentURI.length(), originalURI.length());

					for (int i = 0; i < result.length; i++) {
						if (result[i].endsWith("/") && append.startsWith("/")) {
							result[i] = result[i].substring(0, result[i].length() - 1);
						}

						result[i] += append;
					}

					this.setAccessUri(originalURI);

					return result;
				}
			}

			this.setAccessUri(originalURI);

			throw new LDAPException("NO SUCH ATTRIBUTE", LDAPException.NO_SUCH_ATTRIBUTE,
					"Unable to construct a URL from parent entries", this.baseDN);
		}

		return result;
	}

	/**
	 * Adds a new URL field to the LDAP entry.
	 * 
	 * @param url
	 *            URL
	 * @throws LDAPException
	 */
	public void addLocation(String url) throws LDAPException {
		this.handleWriteAttempt();
		this.addAttribute(CollaborillaObjectConstants.LOCATION, url);
	}

	/**
	 * Modifies an already existing URL in the LDAP entry.
	 * 
	 * @param oldUrl
	 *            URL to be modified
	 * @param newUrl
	 *            New URL
	 * @throws LDAPException
	 */
	public void modifyLocation(String oldUrl, String newUrl) throws LDAPException {
		this.handleWriteAttempt();
		this.modifyAttribute(CollaborillaObjectConstants.LOCATION, oldUrl, newUrl);
	}

	/**
	 * Removes a URL from the LDAP entry.
	 * 
	 * @param url
	 *            URL to be removed
	 * @throws LDAPException
	 */
	public void removeLocation(String url) throws LDAPException {
		this.handleWriteAttempt();
		this.removeAttribute(CollaborillaObjectConstants.LOCATION, url);
	}

	/*
	 * URI (Original)
	 * 
	 * 
	 */

	/**
	 * Reads all URIs of the entry and returns a String array.
	 * 
	 * @return Array of URIs
	 * @throws LDAPException
	 */
	public String[] getUriOriginal() throws LDAPException {
		return this.readAttribute(CollaborillaObjectConstants.URIORIG);
	}

	/**
	 * Adds a new URI field to the LDAP entry.
	 * 
	 * @param uri
	 *            URI
	 * @throws LDAPException
	 */
	public void addUriOriginal(String uri) throws LDAPException {
		this.handleWriteAttempt();
		this.addAttribute(CollaborillaObjectConstants.URIORIG, uri);
	}

	/**
	 * Modifies an already existing URI in the LDAP entry.
	 * 
	 * @param oldUri
	 *            URI to be modified
	 * @param newUri
	 *            New URI
	 * @throws LDAPException
	 */
	public void modifyUriOriginal(String oldUri, String newUri) throws LDAPException {
		this.handleWriteAttempt();
		this.modifyAttribute(CollaborillaObjectConstants.URIORIG, oldUri, newUri);
	}

	/**
	 * Removes a URI from the LDAP entry.
	 * 
	 * @param uri
	 *            URI to be removed
	 * @throws LDAPException
	 */
	public void removeUriOriginal(String uri) throws LDAPException {
		this.handleWriteAttempt();
		this.removeAttribute(CollaborillaObjectConstants.URIORIG, uri);
	}

	/*
	 * URI (Other)
	 * 
	 * 
	 */

	/**
	 * Reads all URIs of the entry and returns a String array.
	 * 
	 * @return Array of URIs
	 * @throws LDAPException
	 */
	public String[] getUriOther() throws LDAPException {
		return this.readAttribute(CollaborillaObjectConstants.URIOTHER);
	}

	/**
	 * Adds a new URI field to the LDAP entry.
	 * 
	 * @param uri
	 *            URI
	 * @throws LDAPException
	 */
	public void addUriOther(String uri) throws LDAPException {
		this.handleWriteAttempt();
		this.addAttribute(CollaborillaObjectConstants.URIOTHER, uri);
	}

	/**
	 * Modifies an already existing URI in the LDAP entry.
	 * 
	 * @param oldUri
	 *            URI to be modified
	 * @param newUri
	 *            New URI
	 * @throws LDAPException
	 */
	public void modifyUriOther(String oldUri, String newUri) throws LDAPException {
		this.handleWriteAttempt();
		this.modifyAttribute(CollaborillaObjectConstants.URIOTHER, oldUri, newUri);
	}

	/**
	 * Removes a URI from the LDAP entry.
	 * 
	 * @param uri
	 *            URI to be removed
	 * @throws LDAPException
	 */
	public void removeUriOther(String uri) throws LDAPException {
		this.handleWriteAttempt();
		this.removeAttribute(CollaborillaObjectConstants.URIOTHER, uri);
	}

	/*
	 * RDF info
	 * 
	 * 
	 */

	/**
	 * Returns the RDF info field.
	 * 
	 * @return RDF info field
	 * @throws LDAPException
	 */
	public String getContextRdfInfo() throws LDAPException {
		if (this.attributeExists(CollaborillaObjectConstants.CONTEXTRDFINFO)) {
			return this.readAttribute(CollaborillaObjectConstants.CONTEXTRDFINFO)[0];
		} else {
			return null;
		}
	}

	/**
	 * Sets the RDF info field.
	 * 
	 * @param rdfInfo
	 *            RDF info
	 * @throws LDAPException
	 */
	public void setContextRdfInfo(String rdfInfo) throws LDAPException {
		this.handleWriteAttempt();

		if (this.attributeExists(CollaborillaObjectConstants.CONTEXTRDFINFO)) {
			this.resetAttribute(CollaborillaObjectConstants.CONTEXTRDFINFO, rdfInfo);
		} else {
			this.addAttribute(CollaborillaObjectConstants.CONTEXTRDFINFO, rdfInfo);
		}
	}

	/**
	 * Removes an eventually existing RDF info field.
	 * 
	 * @throws LDAPException
	 */
	public void removeContextRdfInfo() throws LDAPException {
		this.handleWriteAttempt();

		if (this.attributeExists(CollaborillaObjectConstants.CONTEXTRDFINFO)) {
			this.removeAttribute(CollaborillaObjectConstants.CONTEXTRDFINFO, this.getContextRdfInfo());
		}
	}

	/**
	 * Returns the RDF location info field.
	 * 
	 * @return RDF location info field
	 * @throws LDAPException
	 */
	public String getContainerRdfInfo() throws LDAPException {
		if (this.attributeExists(CollaborillaObjectConstants.CONTAINERRDFINFO)) {
			return this.readAttribute(CollaborillaObjectConstants.CONTAINERRDFINFO)[0];
		} else {
			return null;
		}
	}

	/**
	 * Sets the RDF location info field.
	 * 
	 * @param rdfLocationInfo
	 *            RDF location info
	 * @throws LDAPException
	 */
	public void setContainerRdfInfo(String rdfLocationInfo) throws LDAPException {
		this.handleWriteAttempt();

		if (this.attributeExists(CollaborillaObjectConstants.CONTAINERRDFINFO)) {
			this.resetAttribute(CollaborillaObjectConstants.CONTAINERRDFINFO, rdfLocationInfo);
		} else {
			this.addAttribute(CollaborillaObjectConstants.CONTAINERRDFINFO, rdfLocationInfo);
		}
	}

	/**
	 * Removes an eventually existing RDF location info field.
	 * 
	 * @throws LDAPException
	 */
	public void removeContainerRdfInfo() throws LDAPException {
		this.handleWriteAttempt();

		if (this.attributeExists(CollaborillaObjectConstants.CONTAINERRDFINFO)) {
			this.removeAttribute(CollaborillaObjectConstants.CONTAINERRDFINFO, this.getContainerRdfInfo());
		}
	}

	/*
	 * Description
	 * 
	 * 
	 */

	/**
	 * Returns the description field of the LDAP entry.
	 * 
	 * @return Description
	 * @throws LDAPException
	 */
	public String getDescription() throws LDAPException {
		if (this.attributeExists(CollaborillaObjectConstants.DESCRIPTION)) {
			return this.readAttribute(CollaborillaObjectConstants.DESCRIPTION)[0];
		} else {
			return null;
		}
	}

	/**
	 * Sets the description field of the LDAP entry.
	 * 
	 * @param desc
	 *            Description
	 * @throws LDAPException
	 */
	public void setDescription(String desc) throws LDAPException {
		this.handleWriteAttempt();

		if (this.attributeExists(CollaborillaObjectConstants.DESCRIPTION)) {
			this.resetAttribute(CollaborillaObjectConstants.DESCRIPTION, desc);
		} else {
			this.addAttribute(CollaborillaObjectConstants.DESCRIPTION, desc);
		}
	}

	/**
	 * Removes the description field of the LDAP entry.
	 * 
	 * @throws LDAPException
	 */
	public void removeDescription() throws LDAPException {
		this.handleWriteAttempt();

		if (this.attributeExists(CollaborillaObjectConstants.DESCRIPTION)) {
			this.removeAttribute(CollaborillaObjectConstants.DESCRIPTION, this.getDescription());
		}
	}

	/*
	 * LDIF
	 * 
	 * 
	 */

	/**
	 * Returns the entry and its attributes in LDIF format. Can be used to
	 * export an existing entry from the LDAP directory.
	 * 
	 * @return LDIF data
	 * @throws LDAPException
	 */
	public String getLdif() throws LDAPException {
		return this.exportEntryLdif(false);
	}

	/*
	 * Misc
	 * 
	 * 
	 */

	/**
	 * Returns the revision number of the container file in the RCS.
	 * 
	 * @return Revision number; value -1 if the attribute does not exist
	 * @throws LDAPException
	 */
	public String getContainerRevision() throws LDAPException {
		if (this.attributeExists(CollaborillaObjectConstants.CONTAINERREVISION)) {
			return this.readAttribute(CollaborillaObjectConstants.CONTAINERREVISION)[0];
		} else {
			return null;
		}
	}

	/**
	 * Sets the revision number of the container file in the RCS.
	 * 
	 * @param containerRevision
	 *            Revision number
	 * @throws LDAPException
	 */
	public void setContainerRevision(String containerRevision) throws LDAPException {
		this.handleWriteAttempt();

		if (this.attributeExists(CollaborillaObjectConstants.CONTAINERREVISION)) {
			this.resetAttribute(CollaborillaObjectConstants.CONTAINERREVISION, containerRevision);
		} else {
			this.addAttribute(CollaborillaObjectConstants.CONTAINERREVISION, containerRevision);
		}
	}

}
