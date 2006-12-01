package se.kth.nada.kmr.collaborilla.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A data set to hold information of a Collaborilla data node.
 * Contains some functionality to convert between String[] and Collection. The
 * internal serializable fields are accessed via getters and setters. It implements
 * Serializable to make caching to e.g. local storage possible.
 * 
 * @author Hannes Ebner
 */
public final class CollaborillaDataSet implements Serializable {
	
	private static final long serialVersionUID = 4570510755164149152L;

	private String identifier = null;
	
	private String[] location = null;

	private String[] alignedLocation = null;

	private String containerRdfInfo = null;

	private String contextRdfInfo = null;

	private String[] uriOriginal = null;

	private String[] uriOther = null;

	private Date timestampCreated = null;

	private Date timestampModified = null;

	private String containerRevision = null;

	private String description = null;

	private int revisionNumber = -1;

	private String revisionInfo = null;
	
	/**
	 * This constructor doesn't do much. It just makes creation of the object possible,
	 * the fields can be set afterwards.
	 */
	public CollaborillaDataSet() {
	}
	
	/**
	 * By calling this constructor all fields are fetched automatically.
	 * 
	 * @param client An instance of CollaborillaAccessible
	 * @throws CollaborillaException
	 */
	public CollaborillaDataSet(CollaborillaAccessible client) throws CollaborillaException {
		try {
			this.setIdentifier(client.getIdentifier());
			this.setAlignedLocation(client.getAlignedLocation());
			this.setContainerRdfInfo(client.getContainerRdfInfo());
			this.setContextRdfInfo(client.getContextRdfInfo());
			this.setLocation(client.getLocation());
			this.setTimestampCreated(client.getTimestampCreated());
			this.setTimestampModified(client.getTimestampModified());
			this.setUriOriginal(client.getUriOriginal());
			this.setUriOther(client.getUriOther());
			this.setContainerRevision(client.getContainerRevision());
			this.setDescription(client.getDescription());
			this.setRevisionNumber(client.getRevisionNumber());
			this.setRevisionInfo(client.getRevisionInfo());
		} catch (CollaborillaException ce) {
			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
				throw ce;
			}
		}
	}
	
	private final static String[] collectionToString(Collection coll) {
		if (coll == null) {
			return null;
		}
		
		String[] result = new String[coll.size()];
		int i = 0;
		
		for (Iterator it = coll.iterator(); it.hasNext(); i++) {
			result[i] = (String)it.next();
		}
		
		return result;
	}
	
	private final static Collection stringArrayToCollection(String[] strArray) {
		if (strArray == null) {
			return null;
		}
		
		int size = strArray.length;
		List result = new ArrayList(size);
		
		for (int i = 0; i < size; i++) {
			result.add(strArray[i]);
		}
		
		return result;
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
	
	public void setIdentifier(String ident) {
		this.identifier = ident;
	}
	
	public Collection getLocation() {
		return stringArrayToCollection(this.location);
	}
	
	public void setLocation(Collection coll) {
		this.location = collectionToString(coll);
	}
	
	public Collection getAlignedLocation() {
		return stringArrayToCollection(this.alignedLocation);
	}
	
	public void setAlignedLocation(Collection coll) {
		this.alignedLocation = collectionToString(coll);
	}
	
	public String getContainerRdfInfo() {
		return this.containerRdfInfo;
	}
	
	public void setContainerRdfInfo(String rdfInfo) {
		this.containerRdfInfo = rdfInfo;
	}
	
	public String getContextRdfInfo() {
		return this.contextRdfInfo;
	}
	
	public void setContextRdfInfo(String rdfInfo) {
		this.contextRdfInfo = rdfInfo;
	}
	
	public Collection getUriOriginal() {
		return stringArrayToCollection(this.uriOriginal);
	}
	
	public void setUriOriginal(Collection coll) {
		this.uriOriginal = collectionToString(coll);
	}
	
	public Collection getUriOther() {
		return stringArrayToCollection(this.uriOther);
	}
	
	public void setUriOther(Collection coll) {
		this.uriOther = collectionToString(coll);
	}
	
	public Date getTimestampCreated() {
		return this.timestampCreated;
	}
	
	public void setTimestampCreated(Date date) {
		this.timestampCreated = date;
	}
	
	public Date getTimestampModified() {
		return this.timestampModified;
	}
	
	public void setTimestampModified(Date date) {
		this.timestampModified = date;
	}
	
	public String getContainerRevision() {
		return this.containerRevision;
	}
	
	public void setContainerRevision(String rev) {
		this.containerRevision = rev;
	}
	
	public String getRevisionInfo() {
		return this.revisionInfo;
	}
	
	public void setRevisionInfo(String revInfo) {
		this.revisionInfo = revInfo;
	}

	public int getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public void setRevisionNumber(int rev) {
		this.revisionNumber = rev;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

}