package net.toxbank.client.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProjectIO;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
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
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("name", object.getTitle()));
		formparams.add(new BasicNameValuePair("ldapgroup", object.getGroupName()));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		return new RemoteTask(collection, "text/uri-list", entity, HTTPClient.POST);
	}	
}
