/*
 *  $Id$
 *
 *  Copyright (c) 2006-2007, Hannes Ebner
 *  Licensed under the GNU GPL. For full terms see the file LICENSE.
 */

package se.kth.nada.kmr.collaborilla.ldap;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import com.novell.ldap.util.Base64;

/**
 * Provides methods to do basic conversions and manipulations of URI and LDAP
 * Distinctive Name (DN) Strings.
 * <p>
 * All methods are also statically accessible.
 * 
 * @author Hannes Ebner
 * @version $Id$
 */
public class LDAPStringHelper {
	private String uri;

	private String serverDN;

	private String entryMainID = "ou";

	private String root = null;

	private static final int ENCODING_BASE64 = 1;

	private static final int ENCODING_ESCAPE = 2;

	private static final int ENCODING = ENCODING_ESCAPE;

	/*
	 * Constructors
	 */

	/**
	 * Initializes the object with the Distinctive Name (DN) of the LDAP server
	 * connection and the URI which should be converted.
	 * 
	 * @param serverDN
	 *            Distinctive Name (DN) of the server connection
	 * @param uri
	 *            URI
	 */
	public LDAPStringHelper(String serverDN, String uri) {
		this.serverDN = serverDN;
		this.uri = uri;
	}

	/**
	 * Initializes the object with the Distinctive Name (DN) of the LDAP server
	 * connection, the URI which should be converted and the ID of the leaf
	 * entry.
	 * 
	 * @param serverDN
	 *            Distinctive Name (DN) of the server connection
	 * @param uri
	 *            URI
	 * @param entryMainID
	 *            Identifier of the leaf entry.&nbsp;Example: "cn"
	 */
	public LDAPStringHelper(String root, String serverDN, String uri, String entryMainID) {
		this(serverDN, uri);
		this.entryMainID = entryMainID;
		this.root = root;
	}

	/*
	 * Setters for the private variables
	 */

	/**
	 * Sets the URI.
	 * 
	 * @param uri
	 *            URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Sets the Server Distinctive Name (DN).
	 * 
	 * @param serverDN
	 *            Server Distinctive Name (DN).&nbsp;Example: "dc=test,dc=com"
	 */
	public void setServerDN(String serverDN) {
		this.serverDN = serverDN;
	}

	/*
	 * Getters for the private variables
	 */

	/**
	 * Returns the current URI.
	 * 
	 * @return URI
	 */
	public String getUri() {
		return this.uri;
	}

	/**
	 * Returns the current server Distinctive Name (DN).
	 * 
	 * @return Server Distinctive Name (DN)
	 */
	public String getServerDN() {
		return this.serverDN;
	}

	/**
	 * Returns the Distinctive Name (DN) of the parent entry of the current URI.
	 * 
	 * @return DN of the parent entry
	 */
	public String getParentDN() {
		return dnToParentDN(this.getBaseDN());
	}

	/**
	 * Returns the Distinctive Name (DN) of the parent entry.
	 * 
	 * @param dn
	 *            DN of which the parent DN is demanded
	 * @return DN of the parent entry
	 */
	public static String dnToParentDN(String dn) {
		return dn.substring(dn.indexOf(",") + 1);
	}

	/**
	 * Converts a URI to a Distinctive Name (DN) and returns the parent DN.
	 * 
	 * @param serverDN
	 *            Server Distinctive Name (DN)
	 * @param uri
	 *            URI
	 * @param entryMainID
	 *            Identifier of the leaf entry.&nbsp;Example: "cn"
	 * @return Parent Distinctive Name (DN)
	 */
	public static String uriToParentDN(String root, String serverDN, String uri, String entryMainID) {
		return dnToParentDN(uriToBaseDN(root, serverDN, uri, entryMainID));
	}

	/**
	 * Get the ID of the LDAP Distinctive Name (DN) out of a URI or DN. Returns
	 * for example "test", and not "cn=test".
	 * 
	 * @return Relative DN of the LDAP entry
	 */
	public String getEntryID() {
		return dnToEntryID(this.getBaseDN());
	}

	/**
	 * Get the ID of an LDAP Distinctive Name (DN) out of a URI or DN. Returns
	 * for example "test", and not "cn=test".
	 * 
	 * @param dn
	 *            Full Distinctive Name (DN)
	 * @return Relative Distinctive Name (DN)
	 */
	public static String dnToEntryID(String dn) {
		return dn.substring(dn.indexOf("=") + 1, dn.indexOf(","));
	}

	/**
	 * Get the ID of an LDAP Distinctive Name (DN) out of a URI or DN. Returns
	 * for example "test", and not "cn=test".
	 * 
	 * @param serverDN
	 *            Server Distinctive Name (DN)
	 * @param uri
	 *            URI
	 * @param entryMainID
	 *            Identifier of the leaf entry.&nbsp;Example: "cn"
	 * @return Relative Distinctive Name (DN)
	 */
	public static String uriToEntryID(String root, String serverDN, String uri, String entryMainID) {
		return dnToEntryID(uriToBaseDN(root, serverDN, uri, entryMainID));
	}

	/*
	 * URI helpers
	 */

	/**
	 * Constructs the parent out of a given URI.
	 * 
	 * @param uri
	 *            A valid URI.
	 * @return Returns a parent URI to the given one. Returns null if the URI is
	 *         toplevel already, or if the URI is invalid.
	 */
	public static String getParentURI(String uri) {
		String tmp = uri;

		// Perform a check whether this URI is valid: we convert it to a Java
		// URI and check for an exception.
		try {
			new URI(uri);
		} catch (URISyntaxException e) {
			// throw new IllegalArgumentException("Given parameter is not a
			// valid URI.");
			// We just return null for now
			return null;
		}

		if (tmp.endsWith("/")) {
			tmp = tmp.substring(0, tmp.length() - 2);
		}

		if (tmp.indexOf("/") == tmp.lastIndexOf("/")) {
			return null;
		}

		tmp = tmp.substring(0, tmp.lastIndexOf("/"));

		return tmp;
	}

	/*
	 * URI -> DN conversion
	 */

	/**
	 * Returns a Distinctive Name (DN).
	 * 
	 * @return Base Distinctive Name (DN)
	 */
	public String getBaseDN() {
		return this.uriToBaseDN();
	}

	/**
	 * Converts a URI to a Distinctive Name (DN).
	 * 
	 * @return Base Distinctive Name (DN)
	 */
	public String uriToBaseDN() {
		return uriToBaseDN(this.root, this.serverDN, this.uri, this.entryMainID);
	}

	/**
	 * Converts a URI to a Distinctive Name (DN).
	 * 
	 * @param serverDN
	 *            Server Distinctive Name (DN)
	 * @param uriIn
	 *            URI
	 * @param entryMainID
	 *            Identifier of the leaf entry.&nbsp;Example: "cn"
	 * @return Base Distinctive Name (DN)
	 */
	public static String uriToBaseDN(String root, String serverDN, String uriIn, String entryMainID) {
		String checkSep1 = "://";
		String checkSep2 = ":/";
		String sep = checkSep2;
		String uri = null;
		int pos = -1;

		if (uriIn.indexOf("/") == -1) {
			return null;
		}

		if (uriIn.indexOf(checkSep1) > -1) {
			sep = checkSep1;
		}

		// workaround to avoid and IndexOutOfBoundsException
		String uriToConvert = uriIn + "/";

		if ((pos = uriToConvert.indexOf(sep)) > -1) {
			/* construct a suitable uri out of a generic <type>:/[/]<path> */

			/* get the type */
			String type = uriToConvert.substring(0, pos);

			/* get the path and the host */
			uri = uriToConvert.substring(pos + sep.length());
			String hostPart = uri.substring(0, uri.indexOf("/"));
			uri = uri.substring(uri.indexOf("/"));

			/* split the host into TLD, second level domain, subdomain, etc */
			StringTokenizer stHostPart = new StringTokenizer(hostPart, ".");
			while (stHostPart.hasMoreTokens()) {
				uri = "d:" + stHostPart.nextToken() + "/" + uri;
			}

			/* construct the generic uri */
			uri = "/_" + type + "/" + uri;
		} else if (uriToConvert.startsWith("/")) {
			/* we already get a "good" uri */
			uri = "/_generic" + uriToConvert;
		} else {
			return null;
		}

		if ((root != null) && (root.length() > 0)) {
			/* we prepend the root entry */
			uri = "/" + root + uri;
		}

		/* finally we construct an LDAP Base DN */
		String baseDN = new String();
		StringTokenizer st = new StringTokenizer(uri, "/");
		while (st.hasMoreTokens()) {
			baseDN = entryMainID + "=" + st.nextToken() + "," + baseDN;
		}

		return baseDN += serverDN;
	}

	/*
	 * Other helpers
	 */

	/**
	 * Encodes a given String. The type of "encoding" is hard-wired in the
	 * header of this class.
	 * 
	 * @param input
	 *            String in normal representation.
	 * @return Encoded string.
	 */
	public static String encode(String input) {
		switch (ENCODING) {
		case ENCODING_BASE64:
			return Base64.encode(input.getBytes());
		case ENCODING_ESCAPE:
			return input.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
		default:
			return input;
		}
	}

	/**
	 * Decodes a given String to the original representation. The type of
	 * "encoding" is hard-wired in the header of this class.
	 * 
	 * @param input
	 *            Encoded string.
	 * @return Decoded string.
	 */
	public static String decode(String input) {
		switch (ENCODING) {
		case ENCODING_BASE64:
			return new String(Base64.decode(input));
		case ENCODING_ESCAPE:
			return input.replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r");
		default:
			return input;
		}
	}

	/**
	 * Parses an X.208 formatted timestamp and creates a Date object.
	 * 
	 * @param utcTimestamp
	 *            X.208 formatted timestamp.
	 * @return Converted Date object.
	 */
	public static Date parseTimestamp(String utcTimestamp) {
		Date date = null;

		// Setup a generalized X.208 date/time formatter
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'");

		try {
			// parse UTC into Date
			date = formatter.parse(utcTimestamp);
		} catch (ParseException pe) {
		}

		return date;
	}

}
