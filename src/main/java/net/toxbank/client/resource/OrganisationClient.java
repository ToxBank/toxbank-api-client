package net.toxbank.client.resource;

import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.OrganisationIO;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * REST operations on {@link Organisation}
 * @author nina
 *
 */
public class OrganisationClient extends AbstractClient<Organisation> {
	protected enum webform {
		name,ldapgroup
	}
	
	public OrganisationClient() {
		this(null);
	}
		
	public OrganisationClient(HttpClient httpclient) {
		super(httpclient);
	}
	
	
	@Override
	IOClass<Organisation> getIOClass() {
		return new OrganisationIO();
	}
	
	@Override
	protected HttpEntity createPOSTEntity(Organisation object) throws InvalidInputException,Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (object.getTitle()!=null)
			formparams.add(new BasicNameValuePair(webform.name.name(), object.getTitle()));
		if (object.getGroupName()!=null)
			formparams.add(new BasicNameValuePair(webform.ldapgroup.name(), object.getGroupName()));
		if (formparams.size()==0) throw new InvalidInputException("No content!");
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}
	
	@Override
	protected HttpEntity createPUTEntity(Organisation object) throws InvalidInputException,Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (object.getTitle()!=null)
			formparams.add(new BasicNameValuePair(webform.name.name(), object.getTitle()));
		if (object.getGroupName()!=null)
			formparams.add(new BasicNameValuePair(webform.ldapgroup.name(), object.getGroupName()));
		if (formparams.size()==0) throw new InvalidInputException("Nothing to update!");
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}
}
