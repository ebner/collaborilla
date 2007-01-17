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
 * Wrapper around the interface CollaborillaAccessible.
 * 
 * Provides buffered read access to a Collaborilla data node. It would also be
 * possible to add buffered write access which is completed with a commit(). The
 * purpose of buffering is to speed up the data access and make caching through
 * serialization to a local storage possible.
 * 
 * TODO The getters access already cached data, the same should be introduced
 * for the setters. Perhaps introduce a commit()-method, or set the values
 * directly if the connection is established. If there is no connection, the
 * changes are cached and written at once when we connect or commit.
 * 
 * TODO Add modified flag and do refresh automatically if we called set before
 * refresh/get. Alternatively send the changes to the server AND update the
 * CollaborillaDataSet structure, so there is no refresh necessary. (The second
 * approach makes only sense with proper locking on server side.)
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class BufferedCollaborillaClient implements CollaborillaAccessible {

	private CollaborillaAccessible client = null;

	private CollaborillaDataSet dataset = null;

	/**
	 * @param client A valid and initialized CollaborillaAccessible object.
	 * @throws CollaborillaException
	 */
	public BufferedCollaborillaClient(CollaborillaAccessible client) throws CollaborillaException {
		this.client = client;
		this.dataset = new CollaborillaDataSet();

		if (this.client != null) {
			this.refresh();
		}
	}

	/**
	 * If the Collaborilla client is online and this method is called, all
	 * fields which make sense to be buffered are fetched and stored in memory
	 * in a data set object.
	 * 
	 * @throws CollaborillaException
	 */
	public void refresh() throws CollaborillaException {
		if (this.isConnected()) {
			this.dataset = new CollaborillaDataSet(this);
		}
	}

	/* Interface implementation */

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getDataSet()
	 */
	public CollaborillaDataSet getDataSet() {
		return this.dataset;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addLocation(java.lang.String)
	 */
	public void addLocation(String url) throws CollaborillaException {
		this.client.addLocation(url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addRequiredContainer(java.lang.String)
	 */
	public void addRequiredContainer(String uri) throws CollaborillaException {
		this.client.addRequiredContainer(uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addOptionalContainer(java.lang.String)
	 */
	public void addOptionalContainer(String uri) throws CollaborillaException {
		this.client.addOptionalContainer(uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#connect()
	 */
	public void connect() throws CollaborillaException {
		this.client.connect();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#createRevision()
	 */
	public void createRevision() throws CollaborillaException {
		this.client.createRevision();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#disconnect()
	 */
	public void disconnect() throws CollaborillaException {
		this.client.disconnect();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getAlignedLocation()
	 */
	public Set getAlignedLocation() throws CollaborillaException {
		return this.dataset.getAlignedLocation();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContainerRdfInfo()
	 */
	public String getContainerRdfInfo() throws CollaborillaException {
		return this.dataset.getContainerRdfInfo();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContainerRevision()
	 */
	public String getContainerRevision() throws CollaborillaException {
		return this.dataset.getContainerRevision();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getContextRdfInfo()
	 */
	public String getContextRdfInfo() throws CollaborillaException {
		return this.dataset.getContextRdfInfo();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getDescription()
	 */
	public String getDescription() throws CollaborillaException {
		return this.dataset.getDescription();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getLdif()
	 */
	public String getLdif() throws CollaborillaException {
		return this.client.getLdif();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getIdentifier()
	 */
	public String getIdentifier() throws CollaborillaException {
		return this.dataset.getIdentifier();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getLocation()
	 */
	public Set getLocation() throws CollaborillaException {
		return this.dataset.getLocation();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionCount()
	 */
	public int getRevisionCount() throws CollaborillaException {
		// we don't buffer information of other revisions,
		// so we use "client" instead of "dataset"
		return this.client.getRevisionCount();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionInfo()
	 */
	public String getRevisionInfo() throws CollaborillaException {
		return this.dataset.getRevisionInfo();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionInfo(int)
	 */
	public String getRevisionInfo(int rev) throws CollaborillaException {
		// we don't buffer information of other revisions,
		// so we use "client" instead of "dataset"
		return this.client.getRevisionInfo(rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionNumber()
	 */
	public int getRevisionNumber() throws CollaborillaException {
		return this.dataset.getRevisionNumber();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getTimestampCreated()
	 */
	public Date getTimestampCreated() throws CollaborillaException {
		return this.dataset.getTimestampCreated();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getTimestampModified()
	 */
	public Date getTimestampModified() throws CollaborillaException {
		return this.dataset.getTimestampModified();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRequiredContainers()
	 */
	public Set getRequiredContainers() throws CollaborillaException {
		return this.dataset.getRequiredContainers();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getOptionalContainers()
	 */
	public Set getOptionalContainers() throws CollaborillaException {
		return this.dataset.getOptionalContainers();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#isConnected()
	 */
	public boolean isConnected() {
		return this.client.isConnected();
	}

	/*
	 * From here including the following methods we access the client directly,
	 * without any caching for write-access. This will be probably implemented
	 * at a later point
	 */

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeContainerRdfInfo()
	 */
	public void removeContainerRdfInfo() throws CollaborillaException {
		this.client.removeContainerRdfInfo();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeContextRdfInfo()
	 */
	public void removeContextRdfInfo() throws CollaborillaException {
		this.client.removeContextRdfInfo();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeDescription()
	 */
	public void removeDescription() throws CollaborillaException {
		this.client.removeDescription();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeLocation(java.lang.String)
	 */
	public void removeLocation(String url) throws CollaborillaException {
		this.client.removeLocation(url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeRequiredContainer(java.lang.String)
	 */
	public void removeRequiredContainer(String uri) throws CollaborillaException {
		this.client.removeRequiredContainer(uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeOptionalContainer(java.lang.String)
	 */
	public void removeOptionalContainer(String uri) throws CollaborillaException {
		this.client.removeOptionalContainer(uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#restoreRevision(int)
	 */
	public void restoreRevision(int rev) throws CollaborillaException {
		this.client.restoreRevision(rev);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContainerRdfInfo(java.lang.String)
	 */
	public void setContainerRdfInfo(String rdfLocationInfo) throws CollaborillaException {
		this.client.setContainerRdfInfo(rdfLocationInfo);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContainerRevision(java.lang.String)
	 */
	public void setContainerRevision(String containerRevision) throws CollaborillaException {
		this.client.setContainerRevision(containerRevision);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setContextRdfInfo(java.lang.String)
	 */
	public void setContextRdfInfo(String rdfInfo) throws CollaborillaException {
		this.client.setContextRdfInfo(rdfInfo);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setDescription(java.lang.String)
	 */
	public void setDescription(String desc) throws CollaborillaException {
		this.client.setDescription(desc);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setIdentifier(java.lang.String,
	 *      boolean)
	 */
	public void setIdentifier(String uri, boolean create) throws CollaborillaException {
		this.client.setIdentifier(uri, create);
		this.refresh();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setRevisionNumber(int)
	 */
	public void setRevisionNumber(int rev) throws CollaborillaException {
		this.client.setRevisionNumber(rev);
	}

}