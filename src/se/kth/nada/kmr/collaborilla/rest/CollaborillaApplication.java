package se.kth.nada.kmr.collaborilla.rest;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

import se.kth.nada.kmr.collaborilla.rest.resource.ContainerResource;
import se.kth.nada.kmr.collaborilla.rest.resource.DatasetResource;
import se.kth.nada.kmr.collaborilla.rest.resource.DefaultResource;
import se.kth.nada.kmr.collaborilla.rest.resource.MetadataResource;
import se.kth.nada.kmr.collaborilla.rest.resource.PublishedMapsFeedResource;

public class CollaborillaApplication extends Application {
	
    public CollaborillaApplication(Context parentContext) {
        super(parentContext);
    }
    
    @Override
    public synchronized Restlet createRoot() {
        Router router = new Router(getContext());

        router.attach("/v1/element/{uri}", DatasetResource.class);				// deps, metadata, locations
        router.attach("/v1/element/{uri}/metadata", MetadataResource.class);	// metadata (RDF, JSON)
        //router.attach("/v1/element/{uri}/title", TitleResource.class);		// title extracted from RDF info
        router.attach("/v1/container/{uri}", ContainerResource.class);			// redirect to RDF file
        
        // router.attach("/v1/conglomerate/{uri}", MetadataResource.class);		// dependencies (JSON)
        // router.attach("/v1/location/{uri}", MetadataResource.class);			// URI -> URL resolution (JSON)
        
        // router.attach("/feed/{scope}", PublishedMapsFeedResource.class);
        // router.attach("/feed/{scope}/{format}", PublishedMapsFeedResource.class);
        router.attach("/feed/contextmaps", PublishedMapsFeedResource.class);
        router.attach("/feed/contextmaps/{format}", PublishedMapsFeedResource.class);
        
        router.attachDefault(DefaultResource.class);

        return router;
    }
    
	public static void main(String[] argv) {
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 8182);
		component.getDefaultHost().attach(new CollaborillaApplication(component.getContext()).createRoot());
		
		try {
			component.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}