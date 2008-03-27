package se.kth.nada.kmr.collaborilla.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.Protocol;

import se.kth.nada.kmr.collaborilla.rest.resource.ContainerResource;
import se.kth.nada.kmr.collaborilla.rest.resource.DatasetResource;
import se.kth.nada.kmr.collaborilla.rest.resource.DefaultResource;
import se.kth.nada.kmr.collaborilla.rest.resource.MetadataResource;
import se.kth.nada.kmr.collaborilla.rest.resource.PublishedMapsFeedResource;
import se.kth.nada.kmr.collaborilla.util.Configuration;

public class CollaborillaApplication extends Application {
	
	static Log log = LogFactory.getLog(CollaborillaApplication.class);
	
	private static String configFile = "collaborilla.properties";
	
	private static String ldapHostname;

	private static String ldapServerDN;

	private static String ldapLoginDN;

	private static String ldapPassword;

	private static int listenPort;
	
    public CollaborillaApplication(Context parentContext) {
        super(parentContext);
        log.info("Created CollaborillaApplication");
    }
    
    @Override
    public synchronized Restlet createRoot() {
        Router router = new Router(getContext());

        Route element = router.attach("/rest/v1/element?{uri}", DatasetResource.class);				// deps, metadata, locations
        element.extractQuery("uri", "uri", true);
        
        router.attach("/rest/v1/element/metadata?{uri}", MetadataResource.class);	// metadata (RDF, JSON)
        element.extractQuery("uri", "uri", true);
        
        //router.attach("/rest/v1/element/title?{uri}", TitleResource.class);		// title extracted from RDF info
        //element.extractQuery("uri", "uri", true);
        
        router.attach("/rest/v1/container?{uri}", ContainerResource.class);			// redirect to RDF file
        element.extractQuery("uri", "uri", true);
        
        // router.attach("/rest/v1/conglomerate/{uri}", MetadataResource.class);	// dependencies (JSON)
        // router.attach("/rest/v1/location/{uri}", MetadataResource.class);		// URI -> URL resolution (JSON)
        
        // router.attach("/rest/feed/{scope}", PublishedMapsFeedResource.class);
        // router.attach("/rest/feed/{scope}/{format}", PublishedMapsFeedResource.class);
        router.attach("/rest/feed/contextmaps", PublishedMapsFeedResource.class);
        router.attach("/rest/feed/contextmaps/{format}", PublishedMapsFeedResource.class);
        
        router.attachDefault(DefaultResource.class);
        
        log.info("Attached resources to locations");

        return router;
    }
    
	private static boolean readConfiguration(String file) {
		boolean result = true;

		Configuration conf = new Configuration(file);
		try {
			conf.load();
			
			listenPort = Integer.parseInt(conf.getProperty("server.listenport", "8182"));
			log.info("server.listenport: " + listenPort);
			
			ldapServerDN = conf.getProperty("ldap.serverdn");
			log.info("ldap.serverdn: " + ldapServerDN);
			
			ldapHostname = conf.getProperty("ldap.hostname");
			log.info("ldap.hostname: " + ldapHostname);
			
			ldapLoginDN = conf.getProperty("ldap.logindn");
			log.info("ldap.logindn: " + ldapLoginDN);
			
			ldapPassword = conf.getProperty("ldap.password");
			log.info("ldap.password: *********");
		} catch (Exception e) {
			log.error(e.getMessage());
			result = false;
		}

		return result;
	}
    
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].startsWith("--config=")) {
				configFile = args[0].substring(args[0].indexOf("=") + 1);
			} else {
				log.info("Possible parameter: " + "--config=<path to config file>");
				System.exit(0);
			}
		}

		// Read the configuration
		if (!readConfiguration(configFile)) {
			log.error("Configuration error. Exiting.");
			System.exit(1);
		}
		
		
		Configuration config = new Configuration("collaborilla.properties");
		int port = Integer.valueOf(config.getProperty("server.listenport", "8182"));
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, port);
		component.getDefaultHost().attach(new CollaborillaApplication(component.getContext()).createRoot());
		
		try {
			component.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getConfigFile() {
		return configFile;
	}

	public static String getLdapHostname() {
		return ldapHostname;
	}

	public static String getLdapLoginDN() {
		return ldapLoginDN;
	}

	public static String getLdapPassword() {
		return ldapPassword;
	}

	public static String getLdapServerDN() {
		return ldapServerDN;
	}

	public static int getListenPort() {
		return listenPort;
	}

}