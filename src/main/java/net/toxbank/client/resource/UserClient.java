package net.toxbank.client.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.UserIO;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.opentox.rest.HTTPClient;
import org.opentox.rest.RestException;

/**
 * Modeled after FOAF where possible.
 * 
 * @author egonw
 */
public class UserClient extends AbstractClient<User> {

	protected UserClient() {}

	@Override
	protected RemoteTask createAsync(User user, URL collection)
			throws RestException, UnsupportedEncodingException, URISyntaxException {

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("username", user.getUserName()));
		formparams.add(new BasicNameValuePair("title", user.getTitle()));
		formparams.add(new BasicNameValuePair("firstname", user.getFirstname()));
		formparams.add(new BasicNameValuePair("lastname", user.getLastname()));
		formparams.add(new BasicNameValuePair("institute", user.getInstitute().getResourceURL().toString()));
		formparams.add(new BasicNameValuePair("weblog", user.getWeblog()==null?null:user.getWeblog().toString()));
		formparams.add(new BasicNameValuePair("homepage", user.getHomepage()==null?null:user.getHomepage().toString()));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		return new RemoteTask(collection, "text/uri-list", entity, HTTPClient.POST);		
	}
	@Override
	IOClass<User> getIOClass() {
		return new UserIO();
	}
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveList">API documentation</a>.
	 */
	public static User download(URL identifier) throws IOException, RestException {
		UserClient cli = new UserClient();
		List<User> users = cli.read(identifier, "application/rdf+xml");
		return users.size()>0?users.get(0):null;
	}
	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveList">API documentation</a>.
	 */
	public static List<URL> list(URL server)  throws IOException, RestException  {
		UserClient cli = new UserClient();
		return cli.readURI(server);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveProtocols">API documentation</a>.
	 */
	public static List<URL> listProtocols(User user) throws MalformedURLException, IOException, RestException {
		ProtocolClient cli = new ProtocolClient();
		return cli.listProtocols(user);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveProtocols">API documentation</a>.
	 * Equivalent to {@link #listProtocols()} but returns {@link ProtocolClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Protocol> getProtocols(User user) throws Exception {
		ProtocolClient cli = new ProtocolClient();
		return cli.getProtocols(user);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveStudies">API documentation</a>.
	 */
	public static List<URL> listStudies(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveStudies">API documentation</a>.
	 * Equivalent to {@link #listStudies()} but returns {@link StudyClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Study> getStudies(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveAlerts">API documentation</a>.
	 */
	public static List<URL> listAlerts(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveAlerts">API documentation</a>.
	 * Equivalent to {@link #listAlerts()} but returns {@link AlertClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Alert> getAlerts(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

}
