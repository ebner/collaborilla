package se.kth.nada.kmr.collaborilla.rest.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import se.kth.nada.kmr.collaborilla.ldap.CollaborillaObjectConstants;
import se.kth.nada.kmr.collaborilla.ldap.LDAPAccess;
import se.kth.nada.kmr.collaborilla.ldap.LDAPStringHelper;
import se.kth.nada.kmr.collaborilla.ldap.SearchResultEntry;
import se.kth.nada.kmr.collaborilla.rest.LDAPCommunicator;
import se.kth.nada.kmr.collaborilla.util.Configuration;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

public class PublishedMapsFeedResource extends Resource {

	Log log = LogFactory.getLog(PublishedMapsFeedResource.class);
	
	String scope;
	
	String feedType;

	public PublishedMapsFeedResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		if (request.getAttributes().get("scope") != null) {
			scope = request.getAttributes().get("scope").toString();
		}
		
		if (request.getAttributes().get("format") != null) {
			String format = request.getAttributes().get("format").toString();
			if (format.equals("atom")) {
				feedType = "atom_1.0";
			} else if (format.equals("rss")) {
				feedType = "rss_1.0";
			} else if (format.equals("rss2")) {
				feedType = "rss_2.0";
			}
		} else {
			feedType = "atom_1.0";
		}
		
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		Representation result = null;
        
        SyndFeed feed = getFeed(50);
        feed.setFeedType(feedType);

        SyndFeedOutput output = new SyndFeedOutput();
        try {
			result = new StringRepresentation(output.outputString(feed), MediaType.TEXT_XML);
		} catch (FeedException e) {
			log.error(e.getMessage());
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
		
		return result;
	}
	
	public List<SearchResultEntry> getLatestContextMaps(int items) {
		LDAPCommunicator ldapC = new LDAPCommunicator();
		LDAPAccess la = ldapC.getLDAPConnection();
		List<SearchResultEntry> maps = null;
		try {
			maps = getLatestContextMaps(la, ldapC.getBaseDN());
		} catch (LDAPException e) {
			log.error(e.getMessage());
		}
		
		if (maps == null) {
			return null;
		}
		Collections.sort(maps);
		List<SearchResultEntry> latestMaps = maps;
		if (maps.size() > items) {
			latestMaps = maps.subList(0, items);
		}
		
		return latestMaps;
	}
	
	public SyndFeed getFeed(int items) {
		SyndFeed feed = new SyndFeedImpl();
		feed.setAuthor("Collaborilla " + Configuration.APPVERSION);
		feed.setTitle("Published Conzilla Context-Maps");
		feed.setPublishedDate(new Date());
		feed.setDescription("This feed contains the latest published and updated Conzilla Context-maps.");
		feed.setLink("http://conzilla.org");
		
		List<SearchResultEntry> searchResult = getLatestContextMaps(items);
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		
		for (SearchResultEntry searchEntry : searchResult) {
			SyndEntry entry;
	        SyndContent description;
	        entry = new SyndEntryImpl();
	        entry.setTitle(searchEntry.getTitle());
	        entry.setLink(searchEntry.getUri());
	        entry.setPublishedDate(searchEntry.getCreationDate());
	        entry.setUpdatedDate(searchEntry.getModificationDate());
	        entry.setAuthor(searchEntry.getAuthorName());
	        description = new SyndContentImpl();
	        description.setType(MediaType.APPLICATION_RDF_XML.toString());
	        description.setValue(searchEntry.getMetadata());
	        entry.setDescription(description);
	        entries.add(entry);
		}
		
		feed.setEntries(entries);
		
		return feed;
	}
	
	private List<SearchResultEntry> getLatestContextMaps(LDAPAccess ldapAccess, String baseDN) throws LDAPException {
		ldapAccess.checkConnection();
		List<SearchResultEntry> result = new ArrayList<SearchResultEntry>();
		LDAPSearchConstraints constraints = new LDAPSearchConstraints();
		constraints.setMaxResults(0); // 0 means unlimited
		String[] attributes = new String[] {
				CollaborillaObjectConstants.URI,
				CollaborillaObjectConstants.DATEMODIFIED,
				CollaborillaObjectConstants.DATECREATED,
				CollaborillaObjectConstants.METADATA
				};
		LDAPSearchResults searchResults = ldapAccess.ldapConnection.search(
				baseDN,
				LDAPConnection.SCOPE_SUB,
				"(&(cn=collaborillaData)(collaborillaEntryType=CONTEXTMAP))",
				attributes,
				false,
				constraints);
		while (searchResults.hasMore()) {
			LDAPEntry entry = null;
			try {
				entry = searchResults.next();
			} catch (LDAPException e) {
				if(e.getResultCode() == LDAPException.LDAP_TIMEOUT ||
						e.getResultCode() == LDAPException.CONNECT_ERROR)	{
					break;
				} else {
					continue;
				}
			}
			String resUri = null;
			String resMetadata = null;
			Date resCreationDate = null;
			Date resModificationDate = null;
			LDAPAttributeSet attributeSet = entry.getAttributeSet();
			Iterator allAttributes = attributeSet.iterator();
			while (allAttributes.hasNext()) {
				LDAPAttribute attribute = (LDAPAttribute) allAttributes.next();
				String attributeName = attribute.getName();
				String value = attribute.getStringValue();
				if (attributeName.equals(CollaborillaObjectConstants.URI)) {
					resUri = value;
				} else if (attributeName.equals(CollaborillaObjectConstants.METADATA)) {
					resMetadata = value;
				} else if (attributeName.equals(CollaborillaObjectConstants.DATECREATED)) {
					resCreationDate = LDAPStringHelper.parseTimestamp(value);
				} else if (attributeName.equals(CollaborillaObjectConstants.DATEMODIFIED)) {
					resModificationDate = LDAPStringHelper.parseTimestamp(value);
				}
			}
			SearchResultEntry sre = new SearchResultEntry(resUri, resMetadata, resCreationDate, resModificationDate);
			result.add(sre);
		}
		return result;
	}
   
}