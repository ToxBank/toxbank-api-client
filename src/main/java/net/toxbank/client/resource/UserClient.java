package net.toxbank.client.resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.UserIO;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.opentox.rest.RestException;

/**
 * ToxBank <a href="http://api.toxbank.net/index.php/User">User</a> client,
 * implementing REST operations on {@link User}. 
 * Modeled after FOAF where possible.
 * 
 * @author egonw
 */
public class UserClient extends AbstractClient<User> {
	protected enum webform {
		username,title,firstname,lastname,organisation_uri,project_uri,weblog,homepage
	}
	
	public UserClient() {
		this(null);
	}
		
	public UserClient(HttpClient httpclient) {
		super(httpclient);
	}
	

	@Override
	protected HttpEntity createPOSTEntity(User user) throws Exception {

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair(webform.username.name(), user.getUserName()));
		formparams.add(new BasicNameValuePair(webform.title.name(), user.getTitle()));
		formparams.add(new BasicNameValuePair(webform.firstname.name(), user.getFirstname()));
		formparams.add(new BasicNameValuePair(webform.lastname.name(), user.getLastname()));
		
		if (user.getOrganisations()!=null)
			for (Organisation org: user.getOrganisations())
				if (org.getResourceURL()!=null)
				formparams.add(new BasicNameValuePair(webform.organisation_uri.name(), org.getResourceURL().toString()));
		if (user.getProjects()!=null)
			for (Project project: user.getProjects())
				if (project.getResourceURL()!=null)
				formparams.add(new BasicNameValuePair(webform.project_uri.name(), project.getResourceURL().toString()));
				
		formparams.add(new BasicNameValuePair(webform.weblog.name(), user.getWeblog()==null?null:user.getWeblog().toString()));
		formparams.add(new BasicNameValuePair(webform.homepage.name(), user.getHomepage()==null?null:user.getHomepage().toString()));
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}
	
	@Override
	protected HttpEntity createPUTEntity(User user) throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (user.getUserName()!=null)
			formparams.add(new BasicNameValuePair(webform.username.name(), user.getUserName()));
		if (user.getTitle()!=null)
			formparams.add(new BasicNameValuePair(webform.title.name(), user.getTitle()));
		if (user.getFirstname()!=null)
			formparams.add(new BasicNameValuePair(webform.firstname.name(), user.getFirstname()));
		if (user.getLastname()!=null)
			formparams.add(new BasicNameValuePair(webform.lastname.name(), user.getLastname()));
		
		if (user.getOrganisations()!=null)
			for (Organisation org: user.getOrganisations())
				if (org.getResourceURL()!=null)
				formparams.add(new BasicNameValuePair(webform.organisation_uri.name(), org.getResourceURL().toString()));
		if (user.getProjects()!=null)
			for (Project project: user.getProjects())
				if (project.getResourceURL()!=null)
				formparams.add(new BasicNameValuePair(webform.project_uri.name(), project.getResourceURL().toString()));
		
		if ((user.getWeblog()!=null))
			formparams.add(new BasicNameValuePair(webform.weblog.name(), user.getWeblog()==null?null:user.getWeblog().toString()));
		if ((user.getHomepage()!=null))
			formparams.add(new BasicNameValuePair(webform.homepage.name(), user.getHomepage()==null?null:user.getHomepage().toString()));
		if (formparams.size()==0) throw new InvalidInputException("No content!");
		return new UrlEncodedFormEntity(formparams, "UTF-8");
	}
	@Override
	IOClass<User> getIOClass() {
		return new UserIO();
	}
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveList">API documentation</a>.
	 */
	public User download(URL identifier) throws IOException, RestException {
		List<User> users = get(identifier, "application/rdf+xml");
		return users.size()>0?users.get(0):null;
	}
	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveList">API documentation</a>.
	 */
	public List<URL> list(URL server)  throws IOException, RestException  {
		return listURI(server);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveProtocols">API documentation</a>.
	 * Same as {@link ProtocolClient#listProtocols(User)}
	 */
	public List<URL> listProtocols(User user) throws MalformedURLException, IOException, RestException {
		ProtocolClient cli = new ProtocolClient(getHttpClient());
		return cli.listProtocols(user);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveProtocols">API documentation</a>.
	 * Equivalent to {@link #listProtocols()} but returns {@link ProtocolClient}s
	 * already populated with metadata from the database.
	 * Same as {@link ProtocolClient#listProtocols(User)}
	 */
	public List<Protocol> getProtocols(User user) throws Exception {
		ProtocolClient cli = new ProtocolClient(getHttpClient());
		return cli.getProtocols(user);
	}
	/**
	 * 
	 * @param user
	 * @return Projects
	 * @throws Exception
	 */
	public List<Project> getProjects(User user) throws Exception {
		ProjectClient cli = new ProjectClient(getHttpClient());
		return cli.getRDF_XML(new URL(String.format("%s%s",user.getResourceURL(),Resources.project)));
	}

	/**
	 * 
	 * @param user
	 * @return Organisations
	 * @throws Exception
	 */
	public List<Organisation> getOrganisaitons(User user) throws Exception {
		OrganisationClient cli = new OrganisationClient(getHttpClient());
		return cli.getRDF_XML(new URL(String.format("%s%s",user.getResourceURL(),Resources.organisation)));
	}	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveStudies">API documentation</a>.
	 */
	public List<URL> listStudies(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveStudies">API documentation</a>.
	 * Equivalent to {@link #listStudies()} but returns {@link StudyClient}s
	 * already populated with metadata from the database.
	 */
	public List<Study> getStudies(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveAlerts">API documentation</a>.
	 */
	public List<URL> listAlerts(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveAlerts">API documentation</a>.
	 * Equivalent to {@link #listAlerts()} but returns {@link AlertClient}s
	 * already populated with metadata from the database.
	 */
	public List<Alert> getAlerts(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * 
	 * @param serverURL , root URL ; e.g. http://localhost:8080/toxbank
	 * @return  Information about the logged in user.
	 * @throws IOException 
	 * @throws RestException  404 if not found; 403 if not logged in 
	 */
	public User myAccount(URL serverURL) throws IOException, RestException {
		List<User> users = get(new URL(String.format("%s/myaccount",serverURL)), "application/rdf+xml");
		return users.size()>0?users.get(0):null;
	}
}
