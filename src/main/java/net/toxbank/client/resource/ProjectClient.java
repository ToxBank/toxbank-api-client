package net.toxbank.client.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProjectIO;
import net.toxbank.client.task.RemoteTask;

import org.opentox.rest.HTTPClient;
import org.opentox.rest.RestException;

/**
 * REST operations on {@link Project}
 * @author nina
 *
 */
public class ProjectClient extends AbstractClient<Project> {


	@Override
	IOClass<Project> getIOClass() {
		return new ProjectIO();
	}
	/*
	@Override
	protected Form getWebForm(Project object) {
		Form form = new Form();
		form.add("name", object.getTitle());
		form.add("ldapgroup", object.getGroupName());
		return form;
	}
	*/
	@Override
	protected RemoteTask createAsync(Project object, URL collection) 
			throws RestException, UnsupportedEncodingException, IOException, URISyntaxException {
		String[][] form = new String[][] {
				{"name",object.getTitle()},
				{"ldapgroup",object.getGroupName()}
				};

		return new RemoteTask(collection, "text/uri-list", form, HTTPClient.POST);
	}	
}
