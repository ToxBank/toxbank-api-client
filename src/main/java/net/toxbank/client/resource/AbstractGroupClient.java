package net.toxbank.client.resource;

import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.policy.PolicyRule;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * Common top class for {@link ProjectClient} and {@link OrganisationClient}
 * @author nina
 *
 * @param <PO>
 */
public abstract class AbstractGroupClient<PO extends Group> extends AbstractClient<PO> {
	protected enum webform {
		name,ldapgroup
	}
	

	public AbstractGroupClient() {
		this(null);
	}
		
	public AbstractGroupClient(HttpClient httpclient) {
		super(httpclient);
	}
	
	@Override
	protected HttpEntity createPOSTEntity(PO object,List<PolicyRule> accessRights) throws InvalidInputException,Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (object.getTitle()!=null)
			formparams.add(new BasicNameValuePair(webform.name.name(), object.getTitle()));
		if (object.getGroupName()!=null)
			formparams.add(new BasicNameValuePair(webform.ldapgroup.name(), object.getGroupName()));
		if (formparams.size()==0) throw new InvalidInputException("No content!");
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}
	


	@Override
	protected HttpEntity createPUTEntity(PO object,List<PolicyRule> accessRights) throws InvalidInputException,Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (object.getTitle()!=null)
			formparams.add(new BasicNameValuePair(webform.name.name(), object.getTitle()));
		if (object.getGroupName()!=null)
			formparams.add(new BasicNameValuePair(webform.ldapgroup.name(), object.getGroupName()));
		if (formparams.size()==0) throw new InvalidInputException("Nothing to update!");
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}
	
}
