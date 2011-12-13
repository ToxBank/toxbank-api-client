package net.toxbank.client.resource;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.OrganisationIO;
import net.toxbank.client.task.RemoteTask;

import org.opentox.rest.HTTPClient;
import org.opentox.rest.RestException;

/**
 * REST operations on {@link Organisation}
 * @author nina
 *
 */
public class OrganisationClient extends AbstractClient<Organisation> {

	@Override
	IOClass<Organisation> getIOClass() {
		return new OrganisationIO();
	}

	@Override
	protected RemoteTask createAsync(Organisation object, URL collection) 
			throws RestException, UnsupportedEncodingException, URISyntaxException {
		String[][] form = new String[][] {{"name",object.getTitle()},{"ldapgroup",object.getGroupName()}};
		return new RemoteTask(collection, "text/uri-list", form, HTTPClient.POST);
	}	
	
}
