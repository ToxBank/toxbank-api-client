package net.toxbank.client.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.io.rdf.AlertIO;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.policy.PolicyRule;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.opentox.rest.RestException;

public class AlertClient extends AbstractClient<Alert> {
	protected enum webform {
		name,query,qformat,rfrequency,rinterval
	}
	

	public AlertClient() {
		this(null);
	}
		
	public AlertClient(HttpClient httpclient) {
		super(httpclient);
	}
	public List<URL> listAlerts(URL server) throws IOException, RestException {
		return listURI(server);
	}
	public List<URL> listAlerts(URL server,String... params) throws IOException, RestException {
		return listURI(server,params);
	}
	public List<URL> listAlerts(User user) throws IOException, RestException {
		return listURI(new URL(String.format("%s%s", user.getResourceURL(),Resources.alert)));
	}
	
	public List<Alert> getAlerts(User user) throws Exception {
		return getRDF_XML(new URL(String.format("%s%s", user.getResourceURL(),Resources.alert)));
	}	
	
	@Override
	protected HttpEntity createPOSTEntity(Alert object,List<PolicyRule> accessRights) throws InvalidInputException,Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (object.getTitle()!=null)
			formparams.add(new BasicNameValuePair(webform.name.name(), object.getTitle()));
		if (object.getQueryString()!=null)
			formparams.add(new BasicNameValuePair(webform.query.name(), object.getQueryString()));
		else throw new InvalidInputException("No query string!");
		if (object.getType()!=null)
			formparams.add(new BasicNameValuePair(webform.qformat.name(), object.getType().name()));
		if (object.getRecurrenceFrequency()!=null)
			formparams.add(new BasicNameValuePair(webform.rfrequency.name(), object.getRecurrenceFrequency().name()));
		formparams.add(new BasicNameValuePair(webform.rinterval.name(), Integer.toString(object.getRecurrenceInterval())));
		
		if (formparams.size()==0) throw new InvalidInputException("No content!");
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}
	


	@Override
	protected HttpEntity createPUTEntity(Alert object,List<PolicyRule> accessRights) throws InvalidInputException,Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (object.getTitle()!=null)
			formparams.add(new BasicNameValuePair(webform.name.name(), object.getTitle()));
		if (object.getQueryString()!=null)
			formparams.add(new BasicNameValuePair(webform.query.name(), object.getQueryString()));
		if (object.getType()!=null)
			formparams.add(new BasicNameValuePair(webform.qformat.name(), object.getType().name()));
		if (object.getRecurrenceFrequency()!=null)
			formparams.add(new BasicNameValuePair(webform.rfrequency.name(), object.getRecurrenceFrequency().name()));
		formparams.add(new BasicNameValuePair(webform.rinterval.name(), Integer.toString(object.getRecurrenceInterval())));
		
		if (formparams.size()==0) throw new InvalidInputException("Nothing to update!");
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}

	@Override
	protected IOClass<Alert> getIOClass() {
		return new AlertIO();
	}
	
}
