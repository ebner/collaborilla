/*
 $Id: $
 
 This file is part of the project Collaborilla (http://collaborilla.sf.net)
 Copyright (c) 2006 Hannes Ebner
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package se.kth.nada.kmr.collaborilla.ldap;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.security.Security;
import javax.security.auth.callback.*;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPException;
import com.novell.security.sasl.*;

/**
 * Manages a connection to an LDAP server. Several binding (authentication)
 * methods are supported. Created to be used from other classes which need
 * straight-forward LDAP access.
 * 
 * @author Hannes Ebner
 */
public class LDAPAccess {
	/**
	 * Hostname or IP address of the LDAP server
	 */
	public String ldapHost;

	/**
	 * Port number of the LDAP server.&nbsp;Standard values are
	 * LDAPConnection.DEFAULT_PORT (port 389) or LDAPConnection.DEFAULT_SSL_PORT
	 * (port 636).
	 */
	public int ldapPort = LDAPConnection.DEFAULT_PORT;

	/**
	 * Login DN.&nbsp;Example: "cn=admin,dc=test,dc=com"
	 */
	public String ldapLoginDN;

	/**
	 * Password to connect with the Login DN
	 */
	public String ldapPassword;

	/**
	 * Connection to the LDAP server
	 * 
	 * @see com.novell.ldap.LDAPConnection
	 */
	public LDAPConnection ldapConnection;

	/*
	 * Provides possible values for connection methods.
	 */
	public static final int BIND_NONE = 1;

	public static final int BIND_SIMPLE = 2;

	public static final int BIND_SSL = 3;

	public static final int BIND_MD5 = 4;

	/**
	 * CallbackHandler needed for the DIGEST-MD5 bind mechanism. Taken from an
	 * example in the Novell JLDAP documentation.
	 * 
	 * @author Novell
	 * @see javax.security.auth.callback.CallbackHandler
	 */
	class BindCallbackHandler implements CallbackHandler {
		private char[] m_password;

		BindCallbackHandler(String password) {
			m_password = new char[password.length()];
			password.getChars(0, password.length(), m_password, 0);
		}

		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			for (int i = 0; i < callbacks.length; i++) {
				if (callbacks[i] instanceof PasswordCallback) {
					((PasswordCallback) callbacks[i]).setPassword(m_password);
				} else if (callbacks[i] instanceof NameCallback) {
					((NameCallback) callbacks[i]).setName(((NameCallback) callbacks[i]).getDefaultName());
				} else if (callbacks[i] instanceof RealmCallback) {
					((RealmCallback) callbacks[i]).setText(((RealmCallback) callbacks[i]).getDefaultText());
				} else if (callbacks[i] instanceof RealmChoiceCallback) {
					((RealmChoiceCallback) callbacks[i]).setSelectedIndex(0);
				}
			}
		}
	}

	private LDAPJSSESecureSocketFactory ssf;

	private int ldapVersion = LDAPConnection.LDAP_V3;

	private int bindingMethod;

	/*
	 * Constructors
	 */

	/**
	 * Initializes the object with the given hostname.
	 * 
	 * @param host
	 *            Hostname or IP address of the LDAP server
	 */
	public LDAPAccess(String host) {
		this.ldapHost = host;
		this.ldapConnection = new LDAPConnection();
	}

	/**
	 * Initializes the object with the given parameters. The chosen binding
	 * method is not encrypted.
	 * 
	 * @param host
	 *            Hostname or IP address of the LDAP server
	 * @param loginDN
	 *            Login DN
	 * @param password
	 *            Password
	 */
	public LDAPAccess(String host, String loginDN, String password) {
		this(host);

		this.ldapLoginDN = loginDN;
		this.ldapPassword = password;
		this.bindingMethod = BIND_SIMPLE;
	}

	/**
	 * Initializes the object with the given parameters. It is possible to
	 * choose between several binding methods.
	 * 
	 * @param host
	 *            Hostname or IP address of the LDAP server
	 * @param loginDN
	 *            Login DN
	 * @param password
	 *            Password
	 * @param port
	 *            Port of the LDAP server
	 * @param binding
	 *            Binding method. See Binding
	 */
	public LDAPAccess(String host, String loginDN, String password, int port, int binding) {
		this.ldapHost = host;
		this.ldapLoginDN = loginDN;
		this.ldapPassword = password;
		this.ldapPort = port;
		this.bindingMethod = binding;

		switch (bindingMethod) {
		case BIND_SSL:
			this.ssf = new LDAPJSSESecureSocketFactory();
			this.ldapConnection = new LDAPConnection(ssf);
			break;
		case BIND_MD5:
			Security.addProvider(new com.novell.sasl.client.SaslProvider());
			this.ldapConnection = new LDAPConnection();
			break;
		default:
			this.ldapConnection = new LDAPConnection();
			break;
		}

	}

	/*
	 * Connection wrappers: connect, bind, disconnect, ...
	 */

	/**
	 * Connects to the LDAP server with the configured values. Checks for an
	 * eventually existing connection.
	 * 
	 * @throws LDAPException
	 */
	private void connect() throws LDAPException {
		if (!this.ldapConnection.isConnectionAlive()) {
			this.ldapConnection.connect(this.ldapHost, this.ldapPort);
		}
	}

	/**
	 * Checks whether a connection is still alive and attempts to reconnect if
	 * this is not the case.
	 * 
	 * @throws LDAPException
	 */
	public void checkConnection() throws LDAPException {
		if (!this.ldapConnection.isConnectionAlive()) {
			this.bind();
		}
	}

	/**
	 * Disconnects from the LDAP server.
	 * 
	 * @throws LDAPException
	 */
	public void disconnect() throws LDAPException {
		if (this.ldapConnection.isConnected() || this.ldapConnection.isBound()) {
			this.ldapConnection.disconnect();
		}
	}

	/**
	 * Authenticates after a connection was established. Chooses between several
	 * binding methods.
	 * 
	 * @throws LDAPException
	 * @throws IllegalArgumentException
	 */
	public void bind() throws LDAPException, IllegalArgumentException {
		if (!this.ldapConnection.isBound()) {
			switch (this.bindingMethod) {
			case BIND_NONE:
				this.bindAnonymous();
				break;
			case BIND_SIMPLE:
				this.bindSimple();
				break;
			case BIND_SSL:
				this.bindSSL();
				break;
			case BIND_MD5:
				this.bindMD5();
				break;
			default:
				throw new IllegalArgumentException("Unknown binding method: " + String.valueOf(this.bindingMethod));
			}
		}
	}

	/**
	 * Binds without authentication.
	 * 
	 * @throws LDAPException
	 */
	private void bindAnonymous() throws LDAPException {
		this.connect();
	}

	/**
	 * Binds with authentication. Not encrypted.
	 * 
	 * @throws LDAPException
	 */
	private void bindSimple() throws LDAPException {
		this.connect();

		try {
			this.ldapConnection.bind(this.ldapVersion, this.ldapLoginDN, this.ldapPassword.getBytes("UTF8"));
		} catch (UnsupportedEncodingException u) {
			throw new LDAPException("UTF8 Invalid Encoding", LDAPException.LOCAL_ERROR, (String) null, u);
		}
	}

	/**
	 * Binds via an SSL connection. Encryption is used for the whole
	 * communication to and from the server, also after the authentication
	 * process.
	 * <p>
	 * The property "javax.net.ssl.trustStore" must be set to the path of a
	 * keystore that holds the certificate of the server.
	 * 
	 * @throws LDAPException
	 */
	private void bindSSL() throws LDAPException {
		this.connect();

		try {
			this.ldapConnection.bind(this.ldapVersion, this.ldapLoginDN, this.ldapPassword.getBytes("UTF8"));
		} catch (UnsupportedEncodingException u) {
			throw new LDAPException("UTF8 Invalid Encoding", LDAPException.LOCAL_ERROR, (String) null, u);
		}
	}

	/**
	 * Authenticate to an LDAP server using a the DIGEST-MD5 SASL mechanism. No
	 * certificates required, the data transfer after the authentication is not
	 * encrypted.
	 * 
	 * @throws LDAPException
	 */
	public void bindMD5() throws LDAPException {
		String[] mechanisms = { "DIGEST-MD5" };

		this.connect();

		try {
			this.ldapConnection.bind(this.ldapLoginDN, "dn: " + this.ldapLoginDN, mechanisms, null,
					new BindCallbackHandler(this.ldapPassword));
		} catch (NullPointerException ne) {
			throw new LDAPException("Coult not bind to LDAP server", LDAPException.AUTH_UNKNOWN, (String) null, ne);
		}
	}

}