/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
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
public final class CollaborillaDataSet implements Serializable {
	
	private static final long serialVersionUID = -1923564557152264954L;

	private String identifier = null;
	
	private Set location = null;

	private Set alignedLocation = null;

	private String containerRdfInfo = null;

	private String contextRdfInfo = null;

	private Set requiredContainers = null;

	private Set optionalContainers = null;

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
			this.setRequiredContainers(client.getRequiredContainers());
			this.setOptionalContainers(client.getOptionalContainers());
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

}