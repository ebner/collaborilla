package se.kth.nada.kmr.collaborilla.rest;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.kth.nada.kmr.collaborilla.ldap.CollaborillaObject;
import se.kth.nada.kmr.collaborilla.ldap.LDAPAccess;

import com.novell.ldap.LDAPException;

public class LDAPCommunicator {
	
	Log log = LogFactory.getLog(LDAPCommunicator.class);
	
	private String ldapServerDN;
	
	private String ldapHostname;
	
	private String ldapLoginDN;
	
	private String ldapPassword;
	
	public LDAPCommunicator() {
		this.ldapHostname = CollaborillaApplication.getLdapHostname();
		this.ldapServerDN = CollaborillaApplication.getLdapServerDN();
		this.ldapLoginDN = CollaborillaApplication.getLdapLoginDN();
		this.ldapPassword = CollaborillaApplication.getLdapPassword();
	}
	
	public LDAPAccess getLDAPConnection() {
		return new LDAPAccess(ldapHostname, ldapLoginDN, ldapPassword);
	}
	
	public CollaborillaObject getCollaborillaObject(URI uri, boolean create) throws ResourceException {
		CollaborillaObject co = null;
		try {
			co = new CollaborillaObject(getLDAPConnection(), ldapServerDN, uri.toASCIIString(), create);
		} catch (LDAPException e) {
			log.info(e.getMessage());
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage());
		}
		return co;
	}
	
	public String getBaseDN() {
		return ldapServerDN;
	}

}