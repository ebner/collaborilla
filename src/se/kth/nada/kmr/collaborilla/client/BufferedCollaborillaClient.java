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

		if (this.client != null) {
			this.refresh();
		}
	}

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

	public void addLocation(String url) throws CollaborillaException {
		this.client.addLocation(url);
	}

	public void addUriOriginal(String uri) throws CollaborillaException {
		this.client.addUriOriginal(uri);
	}

	public void addUriOther(String uri) throws CollaborillaException {
		this.client.addUriOther(uri);
	}

	public void connect() throws CollaborillaException {
		this.client.connect();
	}

	public void createRevision() throws CollaborillaException {
		this.client.createRevision();
	}

	public void disconnect() throws CollaborillaException {
		this.client.disconnect();
	}

	public Collection getAlignedLocation() throws CollaborillaException {
		return this.dataset.getAlignedLocation();
	}

	public String getContainerRdfInfo() throws CollaborillaException {
		return this.dataset.getContainerRdfInfo();
	}

	public String getContainerRevision() throws CollaborillaException {
		return this.dataset.getContainerRevision();
	}

	public String getContextRdfInfo() throws CollaborillaException {
		return this.dataset.getContextRdfInfo();
	}

	public String getDescription() throws CollaborillaException {
		return this.dataset.getDescription();
	}

	public String getLdif() throws CollaborillaException {
		return this.client.getLdif();
	}

	public Collection getLocation() throws CollaborillaException {
		return this.dataset.getLocation();
	}

	public int getRevisionCount() throws CollaborillaException {
		// we don't buffer information of other revisions
		return this.client.getRevisionCount();
	}

	public String getRevisionInfo() throws CollaborillaException {
		return this.dataset.getRevisionInfo();
	}

	public String getRevisionInfo(int rev) throws CollaborillaException {
		// we don't buffer information of other revisions
		return this.client.getRevisionInfo(rev);
	}

	public int getRevisionNumber() throws CollaborillaException {
		return this.dataset.getRevisionNumber();
	}

	public Date getTimestampCreated() throws CollaborillaException {
		return this.dataset.getTimestampCreated();
	}

	public Date getTimestampModified() throws CollaborillaException {
		return this.dataset.getTimestampModified();
	}

	public Collection getUriOriginal() throws CollaborillaException {
		return this.dataset.getUriOriginal();
	}

	public Collection getUriOther() throws CollaborillaException {
		return this.dataset.getUriOther();
	}

	public boolean isConnected() {
		return this.client.isConnected();
	}

	public void modifyLocation(String oldUrl, String newUrl) throws CollaborillaException {
		this.client.modifyLocation(oldUrl, newUrl);
	}

	public void modifyUriOriginal(String oldUri, String newUri) throws CollaborillaException {
		this.client.modifyUriOriginal(oldUri, newUri);
	}

	public void modifyUriOther(String oldUri, String newUri) throws CollaborillaException {
		this.client.modifyUriOther(oldUri, newUri);
	}

	public void removeContainerRdfInfo() throws CollaborillaException {
		this.client.removeContainerRdfInfo();
	}

	public void removeContextRdfInfo() throws CollaborillaException {
		this.client.removeContextRdfInfo();
	}

	public void removeDescription() throws CollaborillaException {
		this.client.removeDescription();
	}

	public void removeLocation(String url) throws CollaborillaException {
		this.client.removeLocation(url);
	}

	public void removeUriOriginal(String uri) throws CollaborillaException {
		this.client.removeUriOriginal(uri);
	}

	public void removeUriOther(String uri) throws CollaborillaException {
		this.client.removeUriOther(uri);
	}

	public void restoreRevision(int rev) throws CollaborillaException {
		this.client.restoreRevision(rev);
	}

	public void setContainerRdfInfo(String rdfLocationInfo) throws CollaborillaException {
		this.client.setContainerRdfInfo(rdfLocationInfo);
	}

	public void setContainerRevision(String containerRevision) throws CollaborillaException {
		this.client.setContainerRevision(containerRevision);
	}

	public void setContextRdfInfo(String rdfInfo) throws CollaborillaException {
		this.client.setContextRdfInfo(rdfInfo);
	}

	public void setDescription(String desc) throws CollaborillaException {
		this.client.setDescription(desc);
	}

	public void setIdentifier(String uri, boolean create) throws CollaborillaException {
		this.client.setIdentifier(uri, create);
	}

	public void setRevisionNumber(int rev) throws CollaborillaException {
		this.client.setRevisionNumber(rev);
	}

}