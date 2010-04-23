/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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
	
	private Set<String> locations;

	private Set<String> alignedLocations;

	private String metaData;

	private Set<String> requiredContainers;

	private Set<String> optionalContainers;

	private Date timestampCreated;

	private Date timestampModified;

	private String containerRevision;

	private String description;
	
	private String type;

	private int revisionNumber = -1;

	private String revisionInfo;
	
	/**
	 * Is used to determine whether a dataset (e.g. metadata) has been modified
	 * locally without publishing it (yet).
	 */
	private boolean modifiedLocally;
	
	/**
	 * This constructor doesn't do much. It just makes creation of the object possible,
	 * the fields can be set afterwards.
	 */
	public CollaborillaDataSet() {
	}
	
//	/**
//	 * By calling this constructor all fields are fetched automatically via a stateful client.
//	 * 
//	 * @param client An instance of CollaborillaStatefulClient
//	 * @throws CollaborillaException
//	 */
//	public CollaborillaDataSet(CollaborillaStatefulClient client) throws CollaborillaException {
//		try {
//			setIdentifier(client.getIdentifier());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setAlignedLocations(client.getAlignedLocations());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setMetaData(client.getMetaData());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setLocations(client.getLocations());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setTimestampCreated(client.getTimestampCreated());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setTimestampModified(client.getTimestampModified());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setRequiredContainers(client.getRequiredContainers());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setOptionalContainers(client.getOptionalContainers());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setContainerRevision(client.getContainerRevision());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setDescription(client.getDescription());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setType(client.getType());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setRevisionNumber(client.getRevisionNumber());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//		
//		try {
//			setRevisionInfo(client.getRevisionInfo());
//		} catch (CollaborillaException ce) {
//			if (!(ce.getResultCode() == CollaborillaException.ErrorCode.SC_NO_SUCH_ATTRIBUTE)) {
//				throw ce;
//			}
//		}
//
//		setModifiedLocally(false);
//	}
		
//	/**
//	 * By calling this constructor all fields are fetched automatically via a stateless client.
//	 * 
//	 * @param client An instance of CollaborillaStatelessClient
//	 * @throws CollaborillaException
//	 */
//	public CollaborillaDataSet(CollaborillaStatelessClient client) throws CollaborillaException {
//
//	}
	
	/**
	 * Decodes a String (an encoded CollaborillaDataSet) into a
	 * CollaborillaDataSet object.
	 * 
	 * @param xml
	 *            The CollaborillaDataSet encoded as XML String.
	 * @return A decoded CollaborillaDataSet object.
	 */
	public static CollaborillaDataSet decodeXML(String xml) {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		XMLDecoder input = new XMLDecoder(stream);
		CollaborillaDataSet result = (CollaborillaDataSet) input.readObject();
		input.close();
		return result;
	}

	/**
	 * @return Returns this object encoded as an XML String.
	 */
	public String toXML() {
		String result = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLEncoder output = new XMLEncoder(new BufferedOutputStream(stream));
		output.writeObject(this);
		output.flush();
		output.close();
		try {
			result = stream.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	/**
	 * Converts a Set to an array of Strings.
	 * 
	 * @param coll
	 *            Set to convert.
	 * @return Array of Strings.
	 */
	public final static String[] setToStringArray(Set<String> coll) {
		if (coll == null) {
			return null;
		}

		String[] result = new String[coll.size()];
		int i = 0;
		for (Iterator it = coll.iterator(); it.hasNext(); i++) {
			result[i] = (String) it.next();
		}

		return result;
	}

	/**
	 * Converts an array of String to a Set.
	 * 
	 * @param strArray
	 *            String array to convert.
	 * @return Set of Strings
	 */
	public final static Set<String> stringArrayToSet(String[] strArray) {
		if (strArray == null) {
			return null;
		}

		int size = strArray.length;
		Set<String> result = new HashSet<String>(size);

		for (int i = 0; i < size; i++) {
			result.add(strArray[i]);
		}

		return result;
	}
	
	public String getIdentifier() {
		if (identifier != null) {
			try {
				URI uri = new URI(identifier);
				return uri.toASCIIString();
			} catch (URISyntaxException e) {
			}
		}
		return identifier;
	}
	
	public void setIdentifier(String ident) {
		if (identifier != null) {
			try {
				URI uri = new URI(ident);
				this.identifier = uri.toASCIIString();
			} catch (URISyntaxException e) {
			}
		}
		this.identifier = ident;
	}
	
	public Set<String> getLocations() {
		return this.locations;
	}
	
	public void setLocations(Set<String> coll) {
		this.locations = coll;
	}
	
	public Set<String> getAlignedLocations() {
		return this.alignedLocations;
	}
	
	public void setAlignedLocations(Set<String> coll) {
		this.alignedLocations = coll;
	}
	
	public String getMetaData() {
		return this.metaData;
	}
	
	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	
	public Set<String> getRequiredContainers() {
		return this.requiredContainers;
	}
	
	public void setRequiredContainers(Set<String> coll) {
		this.requiredContainers = coll;
	}
	
	public Set<String> getOptionalContainers() {
		return this.optionalContainers;
	}
	
	public void setOptionalContainers(Set<String> coll) {
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

	public boolean isModifiedLocally() {
		return modifiedLocally;
	}

	public void setModifiedLocally(boolean modified) {
		this.modifiedLocally = modified;
	}

}