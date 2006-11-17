/*
 $Id$
 
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

import java.util.*;
import com.novell.ldap.util.Base64;
import com.novell.ldap.*;

/**
 * Provides methods to access and manipulate LDAP objects. The ObjectClasses and
 * its attributes are configurable.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class LDAPObject implements Cloneable {
	/**
	 * LDAP connection.
	 */
	public LDAPAccess ldapAccess;

	/**
	 * Defines the type of the identifier of the Relative Distinctive Name
	 * (RDN). The default value is "commonname" (cn).
	 */
	public String entryAttributeType = "cn";

	/**
	 * The Base DN to operate with.
	 */
	protected String baseDN;

	/*
	 * Constructors
	 * 
	 * 
	 */

	/**
	 * Creates an object and initializes the necessary fields. If this
	 * constructor is used the default attribute type of the identifier is
	 * "commonname" (cn).
	 * 
	 * @param ldapAccess
	 *            LDAP connection
	 * @param baseDN
	 *            Base Distinctive Name (DN)
	 */
	public void LdapAccess(LDAPAccess ldapAccess, String baseDN) {
		this.ldapAccess = ldapAccess;
		this.baseDN = baseDN;
	}

	/**
	 * Creates an object and initializes the necessary fields.
	 * 
	 * @param ldapAccess
	 *            LDAP connection
	 * @param baseDN
	 *            Base Distinctive Name (DN)
	 * @param entryAttributeType
	 *            The type of the identifier of the Relative Distinctive Names
	 *            (RDN)
	 */
	public void LdapAccess(LDAPAccess ldapAccess, String baseDN, String entryAttributeType) {
		this.ldapAccess = ldapAccess;
		this.baseDN = baseDN;
		this.entryAttributeType = entryAttributeType;
	}

	/*
	 * Overriding methods
	 * 
	 * 
	 */

	/**
	 * Returns the Base DN as a String value.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.baseDN;
	}

	/**
	 * Returns a copy of the current object, sharing the LDAP connection.
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		LDAPObject newObject = (LDAPObject) super.clone();

		return newObject;
	}

	/*
	 * Read internal attributes (Timestamps, ...)
	 * 
	 * 
	 */

	/*
	 * Timestamp created
	 */

	public Date getTimestampCreated() throws LDAPException {
		return this.getTimestampCreated(this.baseDN);
	}

	public Date getTimestampCreated(String dn) throws LDAPException {
		this.ldapAccess.checkConnection();

		return LDAPStringHelper.parseTimestamp(this.readAttribute(dn, "createTimeStamp")[0]);
	}

	public String getTimestampCreatedAsString() throws LDAPException {
		return this.getTimestampCreatedAsString(this.baseDN);
	}

	public String getTimestampCreatedAsString(String dn) throws LDAPException {
		this.ldapAccess.checkConnection();

		return this.readAttribute(dn, "createTimeStamp")[0];
	}

	/*
	 * Timestamp modified
	 */

	public Date getTimestampModified() throws LDAPException {
		return this.getTimestampModified(this.baseDN);
	}

	public Date getTimestampModified(String dn) throws LDAPException {
		this.ldapAccess.checkConnection();

		return LDAPStringHelper.parseTimestamp(this.readAttribute(dn, "modifyTimeStamp")[0]);
	}

	public String getTimestampModifiedAsString() throws LDAPException {
		return this.getTimestampModifiedAsString(this.baseDN);
	}

	public String getTimestampModifiedAsString(String dn) throws LDAPException {
		this.ldapAccess.checkConnection();

		return this.readAttribute(dn, "modifyTimeStamp")[0];
	}

	/*
	 * Entry manipulation
	 * 
	 * 
	 */

	/**
	 * Returns the number of the children in the tree.
	 * 
	 * @param searchScope
	 *            Defines how deep the search should go.&nbsp; Must be a value
	 *            of LDAPConnection.SCOPE_*.
	 * 
	 * @return Number of children
	 * @throws LDAPException
	 */
	public int childCount(int searchScope) throws LDAPException {
		return this.childCount(this.baseDN, searchScope);
	}

	/**
	 * Returns the number of the children in the tree.
	 * 
	 * @param dn
	 *            Distinctive Name (DN)
	 * @param searchScope
	 *            Defines how deep the search should go.&nbsp; Must be a value
	 *            of LDAPConnection.SCOPE_*.
	 * @return Number of children
	 * @throws LDAPException
	 */
	public int childCount(String dn, int searchScope) throws LDAPException {
		int count = 0;

		/* lets look for all entries */
		String searchFilter = "(objectclass=*)";

		this.ldapAccess.checkConnection();

		/* perform the search */
		LDAPSearchResults searchResults = this.ldapAccess.ldapConnection.search(dn, searchScope, searchFilter, null,
				true);

		/*
		 * we cannot determine the result count automatically, so we have to
		 * loop through the results with next() and count them
		 */
		while (searchResults.hasMore()) {
			searchResults.next();
			count++;
		}

		return count;
	}

	/**
	 * Removes the current entry from the tree. Does only work on leaf entries.
	 * 
	 * @throws LDAPException
	 */
	public void deleteEntry() throws LDAPException {
		this.deleteEntry(this.baseDN);
	}

	/**
	 * Removes a specific entry from the tree. Does only work on leaf entries.
	 * 
	 * @param dn
	 *            Distinctive Name (DN)
	 * @throws LDAPException
	 */
	public void deleteEntry(String dn) throws LDAPException {
		this.ldapAccess.checkConnection();
		this.ldapAccess.ldapConnection.delete(dn);
	}

	/**
	 * Moves the current entry to a new location in the tree. Does only work on
	 * leaf entries and the parent DN (container) of the destination has to
	 * exist.
	 * 
	 * @param newDN
	 *            Distinctive Name (DN) of the destination
	 * @throws LDAPException
	 */
	public void moveEntry(String newDN) throws LDAPException {
		this.moveEntry(this.baseDN, newDN);
	}

	/**
	 * Moves a specific entry to a new location in the tree. Does only work on
	 * leaf entries and the parent DN (container) of the destination has to
	 * exist.
	 * 
	 * @param oldDN
	 *            Distinctive Name (DN) of the source
	 * @param newDN
	 *            Distinctive Name (DN) of the destination
	 * @throws LDAPException
	 */
	public void moveEntry(String oldDN, String newDN) throws LDAPException {
		/* container path (parent/superior) of the destination DN has to exist */

		this.ldapAccess.checkConnection();
		this.ldapAccess.ldapConnection.rename(oldDN, entryAttributeType + "=" + LDAPStringHelper.dnToEntryID(newDN),
				LDAPStringHelper.dnToParentDN(newDN), true);
	}

	/**
	 * Renames the current entry.
	 * 
	 * @param newRDN
	 *            New Relativ Distinctive Name (RDN)
	 * @throws LDAPException
	 */
	public void renameEntry(String newRDN) throws LDAPException {
		this.renameEntry(this.baseDN, newRDN);
	}

	/**
	 * Renames a specific entry.
	 * 
	 * @param oldDN
	 *            Distinctive Name (DN) of the entry
	 * @param newRDN
	 *            New Relativ Distinctive Name (RDN)
	 * @throws LDAPException
	 */
	public void renameEntry(String oldDN, String newRDN) throws LDAPException {
		this.ldapAccess.checkConnection();
		this.ldapAccess.ldapConnection.rename(oldDN, newRDN, true);
	}

	/**
	 * Copies the current entry to another location in the tree. The Container
	 * path (parent/superior) of the destination DN has to exist.
	 * 
	 * @param destDN
	 *            Distinctive Name (DN) to which the entry should be copied to
	 * @throws LDAPException
	 */
	public void copyEntry(String destDN) throws LDAPException {
		this.copyEntry(this.baseDN, destDN);
	}

	/**
	 * Copies a specific entry to another location in the tree. The Container
	 * path (parent/superior) of the destination DN has to exist.
	 * 
	 * @param sourceDN
	 *            Distinctive Name (DN) of the source
	 * @param destDN
	 *            Distinctive Name (DN) to which the entry should be copied to
	 * @throws LDAPException
	 */
	public void copyEntry(String sourceDN, String destDN) throws LDAPException {
		/* check whether connection is alive and connect if necessary */
		this.ldapAccess.checkConnection();

		/* read original entry with all attributes */
		LDAPEntry sourceLdapEntry = this.ldapAccess.ldapConnection.read(sourceDN);
		LDAPAttributeSet attributeSet = sourceLdapEntry.getAttributeSet();

		/* remove old CN */
		attributeSet.remove(new LDAPAttribute(entryAttributeType));
		/*
		 * old method: attributeSet.remove(new LDAPAttribute(entryAttributeType,
		 * LdapStringHelper.dnToEntryID(sourceDN)));
		 */

		/* add new CN */
		attributeSet.add(new LDAPAttribute(entryAttributeType, LDAPStringHelper.dnToEntryID(destDN)));

		/* create new entry and add it to the directory */
		LDAPEntry newEntry = new LDAPEntry(destDN, attributeSet);
		this.ldapAccess.ldapConnection.add(newEntry);
	}

	/**
	 * Creates a new entry at the given destination in the tree. If the
	 * Container path (parent/superior) of the destination DN does not exist, it
	 * will be created. The automatically created entries to create a container
	 * path will be of the ObjectClass "organizationaUnit".
	 * 
	 * @param containerPath
	 *            Parent DN of the new entry
	 * @param entryObjectClass
	 *            ObjectClass of the new entry
	 * @param entryIDField
	 *            Identifying field (Example: "cn")
	 * @param entryIDValue
	 *            Value of the ID field
	 * @throws LDAPException
	 */
	public void createEntryWithContainer(String containerPath, String entryObjectClass, String entryIDField,
			String entryIDValue) throws LDAPException {
		this.createEntryWithContainer(containerPath, "organizationalUnit", "ou", entryObjectClass, entryIDField,
				entryIDValue);
	}

	/**
	 * Creates a new entry at the given destination in the tree. If the
	 * Container path (parent/superior) of the destination DN does not exist, it
	 * will be created.
	 * 
	 * @param containerPath
	 *            Parent DN of the new entry
	 * @param pathObjectClass
	 *            ObjectClass of the (created if they don't exist yet) parent
	 *            entries
	 * @param pathIDField
	 *            Identifying field of the parent entries (Example: "ou" for
	 *            ObjectClass organizationalUnit)
	 * @param entryObjectClass
	 *            ObjectClass of the new entry
	 * @param entryIDField
	 *            Identifying field (Example: "cn")
	 * @param entryIDValue
	 *            Value of the ID field
	 * @throws LDAPException
	 */
	public void createEntryWithContainer(String containerPath, String pathObjectClass, String pathIDField,
			String entryObjectClass, String entryIDField, String entryIDValue) throws LDAPException {
		String parent = null;

		/*
		 * recursively calls itself until the container path for the entry
		 * exists
		 */
		if (!entryExists(containerPath)) {
			/* create a string with the parent dn */
			parent = LDAPStringHelper.dnToParentDN(containerPath);

			this.createEntryWithContainer(parent, pathObjectClass, pathIDField, pathObjectClass, pathIDField,
					LDAPStringHelper.dnToEntryID(containerPath));
		}

		this.createEntry(containerPath, entryObjectClass, entryIDField, entryIDValue);
	}

	/**
	 * Creates a new entry at the given destination in the tree. The container
	 * path has to exist.
	 * 
	 * @param containerPath
	 *            Parent DN of the new entry
	 * @param entryObjectClass
	 *            ObjectClass of the new entry
	 * @param entryIDField
	 *            Identifying field (Example: "cn")
	 * @param entryIDValue
	 *            Value of the ID field
	 * @throws LDAPException
	 */
	public void createEntry(String containerPath, String entryObjectClass, String entryIDField, String entryIDValue)
			throws LDAPException {
		LDAPAttributeSet attributeSet = new LDAPAttributeSet();

		/* add the necessary fields */
		attributeSet.add(new LDAPAttribute("objectclass", entryObjectClass));
		attributeSet.add(new LDAPAttribute(entryIDField, entryIDValue));

		/* build the DN of the new entry */
		String dn = entryIDField + "=" + entryIDValue + "," + containerPath;
		/* make a new entry out of the attribute set */
		LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);

		this.ldapAccess.checkConnection();

		/* add the new entry to the directory */
		this.ldapAccess.ldapConnection.add(newEntry);
	}

	/**
	 * Exports the current entry in LDIF format.
	 * 
	 * @param encodeBase64
	 *            Encode the output with Base64 (to make it LDIF safe)
	 * @return LDIF export
	 * @throws LDAPException
	 */
	public String exportEntryLdif(boolean encodeBase64) throws LDAPException {
		return this.exportEntryLdif(this.baseDN, encodeBase64);
	}

	/**
	 * Exports a specific entry in LDIF format.
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry
	 * @param encodeBase64
	 *            Encode the output with Base64 (to make it LDIF safe)
	 * @return LDIF export
	 * @throws LDAPException
	 */
	public String exportEntryLdif(String dn, boolean encodeBase64) throws LDAPException {
		/* build the whole DN */
		StringBuffer entryLdif = new StringBuffer().append("dn:" + dn + "\n");

		this.ldapAccess.checkConnection();

		/* read the whole entry with all attributes */
		LDAPEntry ldapEntry = this.ldapAccess.ldapConnection.read(dn);
		/* get an attribute set */
		LDAPAttributeSet attributeSet = ldapEntry.getAttributeSet();
		/* set an iterator to the attribute set */
		Iterator allAttributes = attributeSet.iterator();

		/*
		 * iterate through the attributes it is possible that an attribute has
		 * more than one value
		 */
		while (allAttributes.hasNext()) {
			/* get the attribute and read the name into a string */
			LDAPAttribute attribute = (LDAPAttribute) allAttributes.next();
			String attributeName = attribute.getName();

			/* returns all values as an enumeration */
			Enumeration allValues = attribute.getStringValues();

			/* if there are no values we continue with the next attribute */
			if (allValues != null) {
				/* we loop until we have got all values */
				while (allValues.hasMoreElements()) {
					/* lets get the value */
					String attributeValue = (String) allValues.nextElement();

					if (encodeBase64) {
						/* if the value contains non LDIF safe values we encode */
						if (!Base64.isLDIFSafe(attributeValue)) {
							attributeValue = Base64.encode(attributeValue.getBytes());
						}
					}

					/* we add the name and the value to the export string */
					entryLdif.append(attributeName + ": " + attributeValue);

					if (allAttributes.hasNext() || allValues.hasMoreElements()) {
						entryLdif.append("\n");
					}
				}
			}
		}

		/* done. we return the LDIF data */
		return entryLdif.toString();
	}

	/**
	 * Checks whether the current entry exists.
	 * 
	 * @return False if the entry does not exist
	 * @throws LDAPException
	 */
	public boolean entryExists() throws LDAPException {
		return this.entryExists(this.baseDN);
	}

	/**
	 * Checks whether a specific entry exists.
	 * 
	 * @param dn
	 *            Distinctive Name (DN)
	 * @return False if the entry does not exist
	 * @throws LDAPException
	 */
	public boolean entryExists(String dn) throws LDAPException {
		this.ldapAccess.checkConnection();

		try {
			/* read the whole entry */
			this.ldapAccess.ldapConnection.read(dn);
		} catch (LDAPException e) {
			/* if this exception is triggered the entry does not exist */
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				return false;
			}
			/* if something else happens rethrow the exception */
			else {
				throw e;
			}
		}

		return true;
	}

	/*
	 * Field manipulation
	 * 
	 * 
	 */

	/**
	 * Checks whether a specific attribute of the current entry exists.
	 * 
	 * @param attribute
	 *            Name of the attribute
	 * @return False if the attribute does not exist
	 * @throws LDAPException
	 */
	public boolean attributeExists(String attribute) throws LDAPException {
		return attributeExists(this.baseDN, attribute);
	}

	/**
	 * Checks whether a specific attribute of a specific entry exists.
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry to check
	 * @param attribute
	 *            Name of the attribute
	 * @return False if the attribute does not exist
	 * @throws LDAPException
	 */
	public boolean attributeExists(String dn, String attribute) throws LDAPException {
		/* if readAttribute returns null the attribute does not exist */
		return (this.readAttribute(dn, attribute) != null);
	}

	/**
	 * Returns the value(s) of an attribute of the current entry.
	 * 
	 * @param attribute
	 *            Name of the attribute
	 * @return String array with the attribute values or null if the attribute
	 *         does not exist
	 * @throws LDAPException
	 */
	public String[] readAttribute(String attribute) throws LDAPException {
		return this.readAttribute(this.baseDN, attribute);
	}

	/**
	 * Returns the value(s) of an attribute of a specific entry.
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry
	 * @param attribute
	 *            Name of the attribute
	 * @return String array with the attribute values or null if the attribute
	 *         does not exist
	 * @throws LDAPException
	 */
	public String[] readAttribute(String dn, String attribute) throws LDAPException {
		String[] attrArray = { attribute };

		this.ldapAccess.checkConnection();

		/* read the whole entry */
		LDAPEntry ldapEntry = this.ldapAccess.ldapConnection.read(dn, attrArray);

		/* we extract the attributes */
		LDAPAttributeSet ldapAttributeSet = ldapEntry.getAttributeSet();

		/* we return null if the entry does not have attributes */
		if (ldapAttributeSet.isEmpty()) {
			return null;
		}

		/* we try to read the values */
		LDAPAttribute ldapAttribute = ldapAttributeSet.getAttribute(attribute);

		/* if we didn't a single value we return with null */
		if (ldapAttribute == null) {
			return null;
		}

		/* return the value array */
		return ldapAttribute.getStringValueArray();
	}

	/**
	 * Adds an attribute to the current entry.
	 * 
	 * @param attrType
	 *            Type of the attribute
	 * @param attrValue
	 *            Value of the attribute
	 * @throws LDAPException
	 */
	public void addAttribute(String attrType, String attrValue) throws LDAPException {
		this.addAttribute(this.baseDN, attrType, attrValue);
	}

	/**
	 * Adds an attribute to a specific entry
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry
	 * @param attrType
	 *            Type of the attribute
	 * @param attrValue
	 *            Value of the attribute
	 * @throws LDAPException
	 */
	public void addAttribute(String dn, String attrType, String attrValue) throws LDAPException {
		/* build a new attribute */
		LDAPAttribute attribute = new LDAPAttribute(attrType, attrValue);

		/* build a new modification */
		LDAPModification modification = new LDAPModification(LDAPModification.ADD, attribute);

		this.ldapAccess.checkConnection();

		/* submit the modification to the directory */
		this.ldapAccess.ldapConnection.modify(dn, modification);
	}

	/**
	 * Removes an attribute from the current entry.
	 * 
	 * @param attrType
	 *            Type of attribute
	 * @param attrValue
	 *            Value of the attribute.&nbsp;If the value is null, all values
	 *            of the attribute will be removed.
	 * @throws LDAPException
	 */
	public void removeAttribute(String attrType, String attrValue) throws LDAPException {
		this.removeAttribute(this.baseDN, attrType, attrValue);
	}

	/**
	 * Removes an attribute from a specific entry.
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry
	 * @param attrType
	 *            Type of the attribute
	 * @param attrValue
	 *            Value of the attribute.&nbsp;If the value is null, all values
	 *            of the attribute will be removed.
	 * @throws LDAPException
	 */
	public void removeAttribute(String dn, String attrType, String attrValue) throws LDAPException {
		LDAPAttribute attribute;

		/* build an attribute object */
		if (attrValue == null) {
			attribute = new LDAPAttribute(attrType);
		} else {
			attribute = new LDAPAttribute(attrType, attrValue);
		}

		/* build a new modification */
		LDAPModification modification = new LDAPModification(LDAPModification.DELETE, attribute);

		this.ldapAccess.checkConnection();

		/* submit the modification to the server */
		this.ldapAccess.ldapConnection.modify(dn, modification);
	}

	/**
	 * Removes all attributes - except the Relativ Distinctive Name (RDN) - of
	 * the current entry.
	 * 
	 * @throws LDAPException
	 */
	public void removeAllAttributes() throws LDAPException {
		this.removeAllAttributes(this.baseDN);
	}

	/**
	 * Removes all attributes - except the Relativ Distinctive Name (RDN) - of a
	 * specific entry.
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry
	 * @throws LDAPException
	 */
	public void removeAllAttributes(String dn) throws LDAPException {
		this.ldapAccess.checkConnection();

		/* get the whole entry */
		LDAPEntry ldapEntry = this.ldapAccess.ldapConnection.read(dn);
		LDAPAttributeSet attributeSet = ldapEntry.getAttributeSet();
		Iterator allAttributes = attributeSet.iterator();

		/* iterate through all attributes */
		while (allAttributes.hasNext()) {
			LDAPAttribute attribute = (LDAPAttribute) allAttributes.next();
			String attributeName = attribute.getName();

			/* if the attribute is part of the RDN we skip the mod */
			if (attributeName.equalsIgnoreCase(this.entryAttributeType)
					|| attributeName.equalsIgnoreCase("objectclass")) {
				continue;
			}

			/* build a new attribute for the modification */
			LDAPAttribute attrToRemove = new LDAPAttribute(attributeName);

			/* build a modification to remove the given attribute */
			LDAPModification modification = new LDAPModification(LDAPModification.DELETE, attrToRemove);

			/* submit the modification to the server */
			this.ldapAccess.ldapConnection.modify(dn, modification);
		}
	}

	/**
	 * Modifies an attribute of the current entry.
	 * 
	 * @param attrType
	 *            Type of the attribute
	 * @param oldValue
	 *            Old value
	 * @param newValue
	 *            New value
	 * @throws LDAPException
	 */
	public void modifyAttribute(String attrType, String oldValue, String newValue) throws LDAPException {
		this.modifyAttribute(this.baseDN, attrType, oldValue, newValue);
	}

	/**
	 * Modifies an attribute of a specific entry.
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry
	 * @param attrType
	 *            Type of the attribute
	 * @param oldValue
	 *            Old Value
	 * @param newValue
	 *            New Value
	 * @throws LDAPException
	 */
	public void modifyAttribute(String dn, String attrType, String oldValue, String newValue) throws LDAPException {
		/* remove the old attribute */
		this.removeAttribute(dn, attrType, oldValue);

		/* add the new one */
		this.addAttribute(dn, attrType, newValue);
	}

	/**
	 * Sets the value of an attribute of the current entry.
	 * 
	 * @param attrType
	 *            Type of the attribute
	 * @param newValue
	 *            New value
	 * @throws LDAPException
	 */
	public void resetAttribute(String attrType, String newValue) throws LDAPException {
		this.resetAttribute(this.baseDN, attrType, newValue);
	}

	/**
	 * Sets the value of an attribute of a specific entry.
	 * 
	 * @param dn
	 *            Distinctive Name (DN) of the entry
	 * @param attrType
	 *            Type of the attribute
	 * @param newValue
	 *            New value
	 * @throws LDAPException
	 */
	public void resetAttribute(String dn, String attrType, String newValue) throws LDAPException {
		this.ldapAccess.checkConnection();

		/* build an attribute */
		LDAPAttribute attr = new LDAPAttribute(attrType, newValue);

		/* build a modification with the attribute */
		LDAPModification modification = new LDAPModification(LDAPModification.REPLACE, attr);

		/* submit the modification to the server */
		this.ldapAccess.ldapConnection.modify(dn, modification);
	}

	/**
	 * Copies all attributes - without the Relative Distinctive Name (RDN) - to
	 * an existing entry. The container path of the destination DN has to exist.
	 * 
	 * @param sourceDN
	 *            Then entry from which the attributes should be copied from
	 * @param destDN
	 *            The destination to which the attributes should be copied to
	 * @throws LDAPException
	 */
	public void copyAttributes(String sourceDN, String destDN) throws LDAPException {
		/* container path (parent/superior) of the destination DN has to exist */

		/* check whether connection is alive and connect if necessary */
		this.ldapAccess.checkConnection();

		/* read original entry with all attributes */
		LDAPEntry sourceLdapEntry = this.ldapAccess.ldapConnection.read(sourceDN);
		LDAPAttributeSet attributeSet = sourceLdapEntry.getAttributeSet();

		/* remove old CN */
		attributeSet.remove(new LDAPAttribute(entryAttributeType));
		Iterator allAttributes = attributeSet.iterator();

		/* iterate through the attributes and modify the destination */
		while (allAttributes.hasNext()) {
			LDAPAttribute attribute = (LDAPAttribute) allAttributes.next();

			/* make it a replacement */
			LDAPModification modification = new LDAPModification(LDAPModification.REPLACE, attribute);

			/* modify entry */
			this.ldapAccess.ldapConnection.modify(destDN, modification);
		}
	}
}
