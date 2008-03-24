/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.net.URI;

/**
 * Collaborilla client interface for stateless clients.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public interface CollaborillaStatelessClient {

	/**
	 * Provides read-access to the information directory.
	 * 
	 * @param uri
	 *            The URI of the entry (contextmap, container, ...) in the
	 *            information directory.
	 * @return A full dataset from the information directory.
	 */
	CollaborillaDataSet get(URI uri);

	/**
	 * Provides read-access to the information directory, and the possibility to
	 * access specific revisions.
	 * 
	 * @param uri
	 *            The URI of the entry (contextmap, container, ...) in the
	 *            information directory.
	 * @param revision
	 *            Revision of the entry.
	 * @return A full dataset from the information directory.
	 */
	CollaborillaDataSet get(URI uri, int revision);

	/**
	 * Update or create an entry.
	 * 
	 * A rollback of an entry to an earlier revision can be done by requesting a
	 * revision with a get() operation and storing it again with put(). This
	 * automatically creates a new revision with old content.
	 * 
	 * @param uri
	 *            URI of the entry.
	 * @param dataSet
	 *            Dataset to be stored.
	 */
	boolean put(URI uri, CollaborillaDataSet dataSet);

	/**
	 * Already published information cannot be removed from the directory, it
	 * can just be marked as deleted (which would be to withdraw the support for
	 * it; it would be abandoned).
	 * 
	 * Implementation details:
	 * 
	 * We don't do a delete on fields, we set it to null in the dataset and send
	 * it with a put() operation to the server. If a value of the dataset is
	 * null it is removed automatically from the entry in the directory.
	 * 
	 * (LDAP specific, worth mentioning for the low-level implementation in
	 * CollaborillaObject or LDAPObject: If we create a new revision we don't
	 * copy the old values over to the new entry, we get everything we want from
	 * the new dataset anyway.)
	 * 
	 * @param uri
	 *            URI of the entry to be deleted.
	 */
	void delete(URI uri);

}