/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.net.URI;

/**
 * Collaborilla client interface for stateless clients. ReST oriented.
 * 
 * TODO finish comments and clean up.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public interface CollaborillaStatelessClient {

	/**
	 * @param uri
	 * @return
	 */
	CollaborillaDataSet get(URI uri);
	
	/**
	 * @param uri
	 * @param revision
	 * @return
	 */
	CollaborillaDataSet get(URI uri, int revision);
	
	/**
	 * Create a new resource.
	 * 
	 * @param uri
	 * @param dataSet
	 */
	void post(URI uri, CollaborillaDataSet dataSet);
	
	/**
	 * Update an already existing resource.
	 * 
	 * We do a restore of a revision by requesting it and putting it in.
	 * This will automatically create a new revision.
	 * 
	 * @param uri
	 * @param dataSet
	 */
	void put(URI uri, CollaborillaDataSet dataSet);

	/**
	 * We don't do a delete on fields, we set it to null in the dataset
	 * and send it with a put to the server. if a value of the dataset is null
	 * it is removed automatically from the ldap entry -> if we create a new
	 * revision we don't copy the old values over to the new entry, because we get
	 * everything we want from the new dataset anyway -> simpler.
	 * 
	 * If we call a del() on a URI we just mark it as deleted, this means we abandon it.
	 */
	void delete(URI uri);
	
}