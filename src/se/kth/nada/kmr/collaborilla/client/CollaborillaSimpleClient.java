/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import se.kth.nada.kmr.collaborilla.ldap.CollaborillaObject;
import se.kth.nada.kmr.collaborilla.ldap.LDAPAccess;
import se.kth.nada.kmr.collaborilla.ldap.LDAPStringHelper;

import com.novell.ldap.LDAPException;

/**
 * Implements a simple Collaborilla client interface. Basically just inherits
 * the methods from CollaborillaObject since we have there all the functionality
 * we need for a "simple" usage.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class CollaborillaSimpleClient implements CollaborillaStatefulClient {
	private CollaborillaObject collab = null;

	private LDAPAccess ldapConn = null;

	private String ldapHost = null;

	private String ldapLoginDN = null;

	private String ldapPassword = null;

	private String identifier = null;

	private String serverDN = null;

	public CollaborillaSimpleClient(String ldapHost, String ldapLoginDN, String ldapPassword, String serverDN) {
		this.ldapHost = ldapHost;
		this.ldapLoginDN = ldapLoginDN;
		this.ldapPassword = ldapPassword;
		this.serverDN = serverDN;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#connect()
	 */
	public void connect() throws CollaborillaException {
		try {
			this.ldapConn = new LDAPAccess(ldapHost, ldapLoginDN, ldapPassword);
			this.ldapConn.bind();
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#disconnect()
	 */
	public void disconnect() throws CollaborillaException {
		try {
			this.collab.ldapAccess.disconnect();
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#isConnected()
	 */
	public boolean isConnected() {
		if (this.collab.ldapAccess.ldapConnection == null) {
			return false;
		}

		return this.collab.ldapAccess.ldapConnection.isConnected();
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getIdentifier()
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setIdentifier(java.lang.String,
	 *      boolean)
	 */
	public void setIdentifier(String uri, boolean create) throws CollaborillaException {
		try {
			this.collab = new CollaborillaObject(this.ldapConn, this.serverDN, uri, create);
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
		
		this.identifier = uri;
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getDataSet()
	 */
	public CollaborillaDataSet getDataSet() throws CollaborillaException {
		return new CollaborillaDataSet(this);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionNumber()
	 */
	public int getRevisionNumber() throws CollaborillaException {
		return this.collab.getRevision();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setRevisionNumber(int)
	 */
	public void setRevisionNumber(int rev) throws CollaborillaException {
		try {
			this.collab.setRevision(rev);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_OBJECT);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionCount()
	 */
	public int getRevisionCount() throws CollaborillaException {
		try {
			return this.collab.getRevisionCount();
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionInfo()
	 */
	public String getRevisionInfo() throws CollaborillaException {
		try {
			return this.collab.getRevisionInfo();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_OBJECT);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRevisionInfo(int)
	 */
	public String getRevisionInfo(int rev) throws CollaborillaException {
		try {
			return this.collab.getRevisionInfo(rev);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_OBJECT);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#createRevision()
	 */
	public int createRevision() throws CollaborillaException {
		int oldRevision = 0;
		try {
			oldRevision = this.collab.getRevisionCount();
			this.collab.createRevision();
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
		return oldRevision;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#restoreRevision(int)
	 */
	public void restoreRevision(int rev) throws CollaborillaException {
		try {
			this.collab.restoreRevision(rev);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_OBJECT);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getAlignedLocations()
	 */
	public Set getAlignedLocations() throws CollaborillaException {
		try {
			String uris[] = this.collab.getAlignedLocation();
			return CollaborillaDataSet.stringArrayToSet(uris);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getLocations()
	 */
	public Set getLocations() throws CollaborillaException {
		try {
			String uris[] = this.collab.getLocation();
			return CollaborillaDataSet.stringArrayToSet(uris);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setLocations(java.util.Set)
	 */
	public void setLocations(Set locations) throws CollaborillaException {
		this.clearLocations();
		Iterator locIt = locations.iterator();
		while (locIt.hasNext()) {
			this.addLocation((String)locIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearLocations()
	 */
	public void clearLocations() throws CollaborillaException {
		Set oldLocations = this.getLocations();
		Iterator oldLocIt = oldLocations.iterator();
		while (oldLocIt.hasNext()) {
			this.removeLocation((String)oldLocIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addLocation(java.lang.String)
	 */
	public void addLocation(String url) throws CollaborillaException {
		try {
			this.collab.addLocation(url);
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeLocation(java.lang.String)
	 */
	public void removeLocation(String url) throws CollaborillaException {
		try {
			this.collab.removeLocation(url);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getRequiredContainers()
	 */
	public Set getRequiredContainers() throws CollaborillaException {
		try {
			String uris[] = this.collab.getRequiredContainers();
			return CollaborillaDataSet.stringArrayToSet(uris);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setRequiredContainers(java.util.Set)
	 */
	public void setRequiredContainers(Set containers) throws CollaborillaException {
		this.clearRequiredContainers();
		Iterator contIt = containers.iterator();
		while (contIt.hasNext()) {
			this.addRequiredContainer((String)contIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearRequiredContainers()
	 */
	public void clearRequiredContainers() throws CollaborillaException {
		Set oldContainers = this.getRequiredContainers();
		Iterator oldContIt = oldContainers.iterator();
		while (oldContIt.hasNext()) {
			this.removeRequiredContainer((String)oldContIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addRequiredContainer(java.lang.String)
	 */
	public void addRequiredContainer(String uri) throws CollaborillaException {
		try {
			this.collab.addRequiredContainer(uri);
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeRequiredContainer(java.lang.String)
	 */
	public void removeRequiredContainer(String uri) throws CollaborillaException {
		try {
			this.collab.removeRequiredContainer(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getOptionalContainers()
	 */
	public Set getOptionalContainers() throws CollaborillaException {
		try {
			String uris[] = this.collab.getOptionalContainers();
			return CollaborillaDataSet.stringArrayToSet(uris);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setOptionalContainers(java.util.Set)
	 */
	public void setOptionalContainers(Set containers) throws CollaborillaException {
		this.clearOptionalContainers();
		Iterator contIt = containers.iterator();
		while (contIt.hasNext()) {
			this.addOptionalContainer((String)contIt.next());
		}
	}
	
	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#clearOptionalContainers()
	 */
	public void clearOptionalContainers() throws CollaborillaException {
		Set oldContainers = this.getOptionalContainers();
		Iterator oldContIt = oldContainers.iterator();
		while (oldContIt.hasNext()) {
			this.removeOptionalContainer((String)oldContIt.next());
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#addOptionalContainer(java.lang.String)
	 */
	public void addOptionalContainer(String uri) throws CollaborillaException {
		try {
			this.collab.addOptionalContainer(uri);
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeOptionalContainer(java.lang.String)
	 */
	public void removeOptionalContainer(String uri) throws CollaborillaException {
		try {
			this.collab.removeOptionalContainer(uri);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getContextRdfInfo()
	 */
	public String getContextRdfInfo() throws CollaborillaException {
		try {
			return LDAPStringHelper.decode(this.collab.getContextRdfInfo());
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setContextRdfInfo(java.lang.String)
	 */
	public void setContextRdfInfo(String rdfInfo) throws CollaborillaException {
		try {
			this.collab.setContextRdfInfo(LDAPStringHelper.encode(rdfInfo));
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeContextRdfInfo()
	 */
	public void removeContextRdfInfo() throws CollaborillaException {
		try {
			this.collab.removeContextRdfInfo();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getContainerRdfInfo()
	 */
	public String getContainerRdfInfo() throws CollaborillaException {
		try {
			return LDAPStringHelper.decode(this.collab.getContainerRdfInfo());
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setContainerRdfInfo(java.lang.String)
	 */
	public void setContainerRdfInfo(String rdfInfo) throws CollaborillaException {
		try {
			this.collab.setContainerRdfInfo(LDAPStringHelper.encode(rdfInfo));
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeContainerRdfInfo()
	 */
	public void removeContainerRdfInfo() throws CollaborillaException {
		try {
			this.collab.removeContainerRdfInfo();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getContainerRevision()
	 */
	public String getContainerRevision() throws CollaborillaException {
		try {
			return this.collab.getContainerRevision();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setContainerRevision(java.lang.String)
	 */
	public void setContainerRevision(String containerRevision) throws CollaborillaException {
		try {
			this.collab.setContainerRdfInfo(containerRevision);
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getDescription()
	 */
	public String getDescription() throws CollaborillaException {
		try {
			return LDAPStringHelper.decode(this.collab.getDescription());
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#setDescription(java.lang.String)
	 */
	public void setDescription(String desc) throws CollaborillaException {
		try {
			this.collab.setDescription(LDAPStringHelper.encode(desc));
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#removeDescription()
	 */
	public void removeDescription() throws CollaborillaException {
		try {
			this.collab.removeDescription();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_ATTRIBUTE) {
				throw new CollaborillaException(CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE);
			} else {
				throw new CollaborillaException(e);
			}
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getLdif()
	 */
	public String getLdif() throws CollaborillaException {
		try {
			return this.collab.getLdif();
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getTimestampCreated()
	 */
	public Date getTimestampCreated() throws CollaborillaException {
		try {
			return this.collab.getTimestampCreated();
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatefulClient#getTimestampCreated()
	 */
	public Date getTimestampModified() throws CollaborillaException {
		try {
			return this.collab.getTimestampModified();
		} catch (LDAPException e) {
			throw new CollaborillaException(e);
		}
	}

}
