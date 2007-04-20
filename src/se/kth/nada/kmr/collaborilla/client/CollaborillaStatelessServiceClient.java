/*  $Id$
 *
 *  Copyright (c) 2006, KMR group at KTH (Royal Institute of Technology)
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.client;

import java.net.URI;

/**
 * TODO right now just get() methods are implemented, with the help of the
 * stateful CollaborillaServiceClient
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class CollaborillaStatelessServiceClient implements CollaborillaStatelessClient {
	
	private CollaborillaServiceClient client;
	
	public CollaborillaStatelessServiceClient(String host, int port) {
		client = new CollaborillaServiceClient(host, port);
	}
	
	/* Interface implementation */

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#delete(java.net.URI)
	 */
	public void delete(URI uri) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#get(java.net.URI)
	 */
	public CollaborillaDataSet get(URI uri) {
		return get(uri, 0);
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#get(java.net.URI, int)
	 */
	public CollaborillaDataSet get(URI uri, int revision) {
		CollaborillaDataSet dataSet = null;
		try {
			client.connect();
			dataSet = client.getDataSet(uri.toString(), revision);
			client.disconnect();
		} catch (CollaborillaException ce) {
		}
		
		return dataSet;
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#post(java.net.URI, se.kth.nada.kmr.collaborilla.client.CollaborillaDataSet)
	 */
	public void post(URI uri, CollaborillaDataSet dataSet) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * @see se.kth.nada.kmr.collaborilla.client.CollaborillaStatelessClient#put(java.net.URI, se.kth.nada.kmr.collaborilla.client.CollaborillaDataSet)
	 */
	public void put(URI uri, CollaborillaDataSet dataSet) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}