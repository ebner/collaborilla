package se.kth.nada.kmr.collaborilla.ldap;

import java.io.StringReader;
import java.util.Date;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.DC;

public class SearchResultEntry implements Comparable<SearchResultEntry> {

	private String uri;
	
	private Date modificationDate;
	
	private Date creationDate;
	
	private String metadata;
	
	private Model model;
	
	public SearchResultEntry(String uri, String metadata, Date creationDate, Date modificationDate) {
		this.uri = uri;
		this.metadata = metadata;
		this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		if (uri != null && metadata != null) {
			model = readModel(metadata, uri);
		}
	}
	
	public int compareTo(SearchResultEntry sr) {
		// we want the entry with the newest date first
		if (sr.getModificationDate() != null) {
			return sr.getModificationDate().compareTo(modificationDate);
		}
		return 0;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getMetadata() {
		return metadata;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public String getUri() {
		return uri;
	}
	
	public String getTitle() {
		if (model == null) {
			return null;
		}
		return getTitle(model, uri, "en");
	}
	
	public String getAuthorName() {
		if (model == null) {
			return null;
		}
		String name = getAuthorName(model);
		if (name == null) {
			return "Unknown";
		}
		return name;
	}
	
	public String getDescription() {
		if (model == null) {
			return null;
		}
		return getDescription(model, uri, "en");
	}
	
	/* RDF helpers */
	
	private static Model readModel(String rdfString, String uri) {
		Model m = ModelFactory.createDefaultModel();
        StringReader sr = new StringReader(rdfString);
        m.read(sr, uri);
        return m;
	}
	
    private static String getTitle(Model model, String uri, String language) {
    	String title = "http://purl.org/dc/elements/1.1/title";
    	String none = null;
        String fallback = null;
        String other = null;
    	NodeIterator nodes = model.listObjectsOfProperty(model.createResource(uri), model.createProperty(title));
    	while (nodes.hasNext()) {
    		RDFNode node = nodes.nextNode();
    		if (node instanceof Literal) {
    			String actualLanguage = ((Literal) node).getLanguage();
    			String literal = ((Literal) node).getString();
                boolean aLNull = actualLanguage == null || actualLanguage.length() == 0;
                if (aLNull) {
                	none = literal;
                } else if (actualLanguage.equals(language)) {
                    return literal;
                } else if (actualLanguage.equals("en")) {
                    fallback = literal;
                } else {
                	other = literal;
                }
    		}
    	}
    	if (fallback != null) {
            return fallback;
        } else if (none != null) {
            return none;
        }
    	if (other != null) {
    		return other;
    	}
    	return uri;
    }
    
    private static String getDescription(Model model, String uri, String language) {
    	String description = "http://purl.org/dc/elements/1.1/description";
    	String none = null;
        String fallback = null;
        String other = null;
    	NodeIterator nodes = model.listObjectsOfProperty(model.createResource(uri), model.createProperty(description));
    	while (nodes.hasNext()) {
    		RDFNode node = nodes.nextNode();
    		if (node instanceof Literal) {
    			String actualLanguage = ((Literal) node).getLanguage();
    			String literal = ((Literal) node).getString();
                boolean aLNull = actualLanguage == null || actualLanguage.length() == 0;
                if (aLNull) {
                	none = literal;
                } else if (actualLanguage.equals(language)) {
                    return literal;
                } else if (actualLanguage.equals("en")) {
                    fallback = literal;
                } else {
                	other = literal;
                }
    		}
    	}
    	if (fallback != null) {
            return fallback;
        } else if (none != null) {
            return none;
        }
    	if (other != null) {
    		return other;
    	}
    	return uri;
    }    
	
    private static String getAuthorName(Model model) {
    	Property foafName = new PropertyImpl("http://xmlns.com/foaf/0.1/name");
    	StmtIterator statements = model.listStatements(null, DC.creator, (String) null);
    	String authorName = null;
    	if (statements.hasNext()) {
    		Statement statement = statements.nextStatement();
    		String creatorURI = statement.getObject().toString();
    		StmtIterator names = model.listStatements(model.createResource(creatorURI), foafName, (String) null);
    		if (names.hasNext()) {
    			authorName = names.nextStatement().getObject().toString();
    		}
    	}
    	return authorName;
    }

}