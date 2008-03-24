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
//		String configFile = "collaborilla.properties";
//		Configuration conf = new Configuration(configFile);
//		ldapServerDN = conf.getProperty("ldap.serverdn");
//		ldapHostname = conf.getProperty("ldap.hostname");
//		ldapLoginDN = conf.getProperty("ldap.logindn");
//		ldapPassword = conf.getProperty("ldap.password");
		ldapHostname = "collaborilla.conzilla.org";
		ldapServerDN = "dc=collaborilla,dc=conzilla,dc=org";
		ldapLoginDN = "cn=admin,dc=collaborilla,dc=conzilla,dc=org";
		ldapPassword = "conzilla-collab";
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