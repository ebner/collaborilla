/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A data set to hold information of a Collaborilla data node.
 * Contains some functionality to convert between String[] and Collection. The
 * internal serializable fields are accessed via getters and setters. It implements
 * Serializable to make caching to e.g. local storage possible.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public final class CollaborillaDataSet implements Serializable, EntryTypes {
	
	private static final long serialVersionUID = -1923564557152264954L;

	private String identifier;
	
	private Set location;

	private Set alignedLocation;

	private String metaData;

	private Set requiredContainers;

	private Set optionalContainers;

	private Date timestampCreated;

	private Date timestampModified;

	private String containerRevision;

	private String description;
	
	private String type;

	private int revisionNumber = -1;

	private String revisionInfo;
	
	/**
	 * This constructor doesn't do much. It just makes creation of the object possible,
	 * the fields can be set afterwards.
	 */
	public CollaborillaDataSet() {
	}
	
	/**
	 * By calling this constructor all fields are fetched automatically via a stateful client.
	 * 
	 * @param client An instance of CollaborillaStatefulClient
	 * @throws CollaborillaException
	 */
	public CollaborillaDataSet(CollaborillaStatefulClient client) throws CollaborillaException {
		try {
			this.setIdentifier(client.getIdentifier());
			this.setAlignedLocation(client.getAlignedLocations());
			this.setMetaData(client.getMetaData());
			this.setLocation(client.getLocations());
			this.setTimestampCreated(client.getTimestampCreated());
			this.setTimestampModified(client.getTimestampModified());
			this.setRequiredContainers(client.getRequiredContainers());
			this.setOptionalContainers(client.getOptionalContainers());
			this.setContainerRevision(client.getContainerRevision());
			this.setDescription(client.getDescription());
			this.setType(client.getType());
			this.setRevisionNumber(client.getRevisionNumber());
			this.setRevisionInfo(client.getRevisionInfo());
		} catch (CollaborillaException ce) {
			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
				throw ce;
			}
		}
	}
	
	/**
	 * By calling this constructor all fields are fetched automatically via a stateless client.
	 * 
	 * @param client An instance of CollaborillaStatelessClient
	 * @throws CollaborillaException
	 */
	public CollaborillaDataSet(CollaborillaStatelessClient client) throws CollaborillaException {
		// TODO
	}
	
	public final static String[] setToStringArray(Set coll) {
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
	
	public final static Set stringArrayToSet(String[] strArray) {
		if (strArray == null) {
			return null;
		}
		
		int size = strArray.length;
		Set result = new HashSet(size);
		
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
	
	public Set getLocation() {
		return this.location;
	}
	
	public void setLocation(Set coll) {
		this.location = coll;
	}
	
	public Set getAlignedLocation() {
		return this.alignedLocation;
	}
	
	public void setAlignedLocation(Set coll) {
		this.alignedLocation = coll;
	}
	
	public String getMetaData() {
		return this.metaData;
	}
	
	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	
	public Set getRequiredContainers() {
		return this.requiredContainers;
	}
	
	public void setRequiredContainers(Set coll) {
		this.requiredContainers = coll;
	}
	
	public Set getOptionalContainers() {
		return this.optionalContainers;
	}
	
	public void setOptionalContainers(Set coll) {
		this.optionalContainers = coll;
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
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

}