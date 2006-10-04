package se.kth.nada.kmr.collaborilla.client;

import java.util.Collection;
import java.util.Date;

/**
 * Wrapper around the interface CollaborillaAccessible.
 * 
 * Provides buffered read access to a Collaborilla data node. It would also be
 * possible to add buffered write access which is completed with a commit(). The
 * purpose of buffering is to speed up the data access and make caching through
 * serialization to a local storage possible.
 * 
 * @author Hannes Ebner
 */
public class BufferedCollaborillaClient implements CollaborillaAccessible {

	private CollaborillaAccessible client = null;
	
	private CollaborillaDataSet dataset = null;

	public BufferedCollaborillaClient(CollaborillaAccessible client) throws CollaborillaException {
		this.client = client;
		this.dataset = new CollaborillaDataSet();

		if (this.client != null) {
			this.refresh();
		}
	}

	/**
	 * If the Collaborilla client is online and this method is called, all fields which
	 * make sense to be buffered are fetched and stored in memory in a data set object.
	 * 
	 * @throws CollaborillaException
	 */
	public void refresh() throws CollaborillaException {
		if (this.isConnected()) {
			try {
				this.dataset.setAlignedLocation(this.client.getAlignedLocation());
				this.dataset.setContainerRdfInfo(this.client.getContainerRdfInfo());
				this.dataset.setContextRdfInfo(this.client.getContextRdfInfo());
				this.dataset.setLocation(this.client.getLocation());
				this.dataset.setTimestampCreated(this.client.getTimestampCreated());
				this.dataset.setTimestampModified(this.client.getTimestampModified());
				this.dataset.setUriOriginal(this.client.getUriOriginal());
				this.dataset.setUriOther(this.client.getUriOther());
				this.dataset.setContainerRevision(this.client.getContainerRevision());
				this.dataset.setDescription(this.client.getDescription());
				this.dataset.setRevisionNumber(this.client.getRevisionNumber());
				this.dataset.setRevisionInfo(this.client.getRevisionInfo());
			} catch (CollaborillaException ce) {
				if (!((ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE) ||
						(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_VALUE))) {
					throw ce;
				}
			}
		}
	}
	
	public CollaborillaDataSet getDataSet() {
		return this.dataset;
	}
	
	/* Interface implementation */

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addLocation(java.lang.String)
	 */
	public void addLocation(String url) throws CollaborillaException {
		this.client.addLocation(url);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addUriOriginal(java.lang.String)
	 */
	public void addUriOriginal(String uri) throws CollaborillaException {
		this.client.addUriOriginal(uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#addUriOther(java.lang.String)
	 */
	public void addUriOther(String uri) throws CollaborillaException {
		this.client.addUriOther(uri);
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
	public Collection getAlignedLocation() throws CollaborillaException {
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getLocation()
	 */
	public Collection getLocation() throws CollaborillaException {
		return this.dataset.getLocation();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getRevisionCount()
	 */
	public int getRevisionCount() throws CollaborillaException {
		// we don't buffer information of other revisions
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
		// we don't buffer information of other revisions
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getUriOriginal()
	 */
	public Collection getUriOriginal() throws CollaborillaException {
		return this.dataset.getUriOriginal();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#getUriOther()
	 */
	public Collection getUriOther() throws CollaborillaException {
		return this.dataset.getUriOther();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#isConnected()
	 */
	public boolean isConnected() {
		return this.client.isConnected();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyLocation(java.lang.String, java.lang.String)
	 */
	public void modifyLocation(String oldUrl, String newUrl) throws CollaborillaException {
		this.client.modifyLocation(oldUrl, newUrl);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyUriOriginal(java.lang.String, java.lang.String)
	 */
	public void modifyUriOriginal(String oldUri, String newUri) throws CollaborillaException {
		this.client.modifyUriOriginal(oldUri, newUri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#modifyUriOther(java.lang.String, java.lang.String)
	 */
	public void modifyUriOther(String oldUri, String newUri) throws CollaborillaException {
		this.client.modifyUriOther(oldUri, newUri);
	}

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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeUriOriginal(java.lang.String)
	 */
	public void removeUriOriginal(String uri) throws CollaborillaException {
		this.client.removeUriOriginal(uri);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#removeUriOther(java.lang.String)
	 */
	public void removeUriOther(String uri) throws CollaborillaException {
		this.client.removeUriOther(uri);
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
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setIdentifier(java.lang.String, boolean)
	 */
	public void setIdentifier(String uri, boolean create) throws CollaborillaException {
		this.client.setIdentifier(uri, create);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaAccessible#setRevisionNumber(int)
	 */
	public void setRevisionNumber(int rev) throws CollaborillaException {
		this.client.setRevisionNumber(rev);
	}

}