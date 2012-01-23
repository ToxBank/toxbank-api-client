package net.toxbank.client.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProtocolIO;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.opentox.rest.HTTPClient;
import org.opentox.rest.RestException;

/**
 * ToxBank <a href="http://api.toxbank.net/index.php/Protocol">Protocol</a> client,
 * implementing REST operations on {@link Protocol}. 
 * @author nina
 *
 */
public class ProtocolClient extends AbstractClient<Protocol> {
	protected enum webform {
		project_uri,
		organisation_uri,
		user_uri,
		author_uri,
		title,
		anabstract,
		summarySearchable,
		keywords,
		filename,
		status
	}

	public ProtocolClient() {
		this(null);
	}
		
	public ProtocolClient(HttpClient httpclient) {
		super(httpclient);
	}
	
	@Override
	IOClass<Protocol> getIOClass() {
		return new ProtocolIO();
	}

	@Override
	protected HttpEntity createPOSTEntity(Protocol protocol) throws InvalidInputException,Exception {
		StringBuilder b = new StringBuilder();
		String d = "";
		for (String keyword: protocol.getKeywords()) {
			b.append(d);
			b.append(keyword);
			d = ";";
		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		if ((protocol.getProject()==null) || (protocol.getProject().getResourceURL()==null)) throw new InvalidInputException("No Project URI!");
		entity.addPart(webform.project_uri.name(), new StringBody(protocol.getProject().getResourceURL().toString()));
		if ((protocol.getOrganisation()==null) || (protocol.getOrganisation().getResourceURL()==null)) throw new InvalidInputException("No Organisation URI!");
		entity.addPart(webform.organisation_uri.name(), new StringBody(protocol.getOrganisation().getResourceURL().toString()));
		if ((protocol.getOwner()==null) || (protocol.getOwner().getResourceURL()==null)) throw new InvalidInputException("No User URI!");
		entity.addPart(webform.user_uri.name(), new StringBody(protocol.getOwner().getResourceURL().toString()));
		if (protocol.getTitle().length()>255) throw new InvalidInputException(String.format("Title length %d, expected <=255",protocol.getTitle().length()));
		entity.addPart(webform.title.name(), new StringBody(protocol.getTitle()));
		entity.addPart(webform.anabstract.name(), new StringBody(protocol.getAbstract()));
		entity.addPart(webform.summarySearchable.name(), new StringBody(Boolean.toString(protocol.isSearchable())));
		entity.addPart(webform.keywords.name(), new StringBody(b.toString()));
		entity.addPart(webform.status.name(), new StringBody(protocol.getStatus().toString()));
		if (protocol.getAuthors()!=null)
			for (int i=0; i < protocol.getAuthors().size(); i++)
				entity.addPart(webform.author_uri.name(),new StringBody(protocol.getAuthors().get(i).getResourceURL().toString()));
		entity.addPart(webform.filename.name(), new FileBody(new File(protocol.getDocument().getResourceURL().toURI())));
		 
		return entity;
	}
	
	@Override
	protected HttpEntity createPUTEntity(Protocol object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Upload template.
	 * @param protocol
	 * @return
	 * @throws RestException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public RemoteTask uploadTemplate(Protocol protocol, File template)	
					throws RestException,UnsupportedEncodingException, IOException, URISyntaxException {

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("template", new FileBody(template));
		 
		return new RemoteTask(getHttpClient(),new URL(String.format("%s%s",protocol.getResourceURL(),Resources.datatemplate)), 
											"text/uri-list", entity, HttpPost.METHOD_NAME);
	}
	
	
	public RemoteTask createNewVersion(Protocol protocol) throws Exception {
		return postAsync(protocol,new URL(String.format("%s%s", protocol.getResourceURL(),Resources.versions)));
	}
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Upload">API documentation</a>.
	 */
	public Protocol download(URL identifier) throws Exception {
		List<Protocol> protocol = getRDF_XML(identifier);
		return protocol.size()==0?null:protocol.get(0);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveList">API documentation</a>.
	 */
	public List<URL> listProtocols(URL server) throws IOException, RestException {
		return listURI(server);
	}
	public List<URL> listProtocols(URL server,String... params) throws IOException, RestException {
		return listURI(server,params);
	}
	public List<URL> listProtocols(User user) throws IOException, RestException {
		return listURI(new URL(String.format("%s%s", user.getResourceURL(),Resources.protocol)));
	}
	
	public List<Protocol> getProtocols(User user) throws Exception {
		return getRDF_XML(new URL(String.format("%s%s", user.getResourceURL(),Resources.protocol)));
	}	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Upload">API documentation</a>.
	 */
	public URL upload(Protocol protocol, URL server) throws Exception {
		Protocol p = post(protocol,server);
		return p.getResourceURL();
	}
	/**
	 * Asynchronous upload
	 * @param protocol
	 * @param server
	 * @return {@link RemoteTask}
	 * @throws Exception
	 */
	public RemoteTask uploadAsync(Protocol protocol, URL server) throws Exception {
		return postAsync(protocol,server);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Retrieve">API documentation</a>.
	 */
	public URL listFile(Protocol protocol) throws MalformedURLException {
		return new URL(String.format("%s%s",protocol.getResourceURL(),Resources.document));
	}
	
	public File downloadFile(Protocol protocol) throws MalformedURLException, IOException, RestException {
		URL url = new URL(String.format("%s%s",protocol.getResourceURL(),Resources.document));
		
		HttpGet httpGet = new HttpGet(url.toExternalForm());
		httpGet.addHeader("Accept","");
		
		InputStream in = null;
		try {
			HttpResponse response = getHttpClient().execute(httpGet);
			HttpEntity entity  = response.getEntity();
			if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
				in = entity.getContent();
				File temp = File.createTempFile("download_", ".pdf");
				HTTPClient.download(in, temp);
				return temp;
			} else throw new RestException(response.getStatusLine().getStatusCode());
		
		} finally {
			try {if (in !=null) in.close();} catch (Exception x) {}
		}		
	}
	
	public File downloadTemplate(Protocol protocol) throws MalformedURLException, IOException , RestException {
		URL url = new URL(String.format("%s%s",protocol.getResourceURL(),Resources.datatemplate));
		HttpGet httpGet = new HttpGet(url.toExternalForm());
		httpGet.addHeader("Accept","");
		
		InputStream in = null;
		try {
			HttpResponse response = getHttpClient().execute(httpGet);
			HttpEntity entity  = response.getEntity();
			if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
				in = entity.getContent();
				File temp = File.createTempFile("download_", ".txt");
				HTTPClient.download(in, temp);
				return temp;
			} else throw new RestException(response.getStatusLine().getStatusCode());
		
		} finally {
			try {if (in !=null) in.close();} catch (Exception x) {}
		}			
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveVersions">API documentation</a>.
	 */
	public List<URL> listVersions(Protocol protocol) throws IOException, RestException{
		return listURI(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.versions)));
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveVersions">API documentation</a>.
	 * Equivalent to {@link #listVersions()} but returns {@link ProtocolVersionClient}s
	 * already populated with metadata from the database.
	 */
	public List<Protocol> getVersions(Protocol protocol) throws Exception {
		return getRDF_XML(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.versions)));
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveTemplates">API documentation</a>.
	 */

	public URL listTemplate(Protocol protocol) throws MalformedURLException  {
		return new URL(String.format("%s%s",protocol.getResourceURL(),Resources.datatemplate));
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveTemplates">API documentation</a>.
	 * Equivalent to {@link #listTempaltes()} but returns {@link TemplateClient}s
	 * already populated with metadata from the database.
	 */
	public List<Template> getTemplates(Protocol protocol) throws MalformedURLException {
		List<Template> templates = new ArrayList<Template>();
		templates.add(new Template(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.datatemplate))));
		return templates;
	}

	public void deleteProtocol(Protocol protocol) throws Exception {
		delete(protocol);
	}
	
	public List<Protocol> getModifiedSinceRDF_XML(URL url,Long unixtimestamp) throws Exception {
		return get(url,mime_rdfxml,unixtimestamp==null?null:new String[] {modified_param,unixtimestamp.toString()});
	}	

	public List<Protocol> getModifiedSinceRDF_N3(URL url,Long unixtimestamp) throws Exception {
		return get(url,mime_n3,unixtimestamp==null?null:new String[] {modified_param,unixtimestamp.toString()});
	}	
	public List<URL> getModifiedSinceURI(URL url,Long unixtimestamp) throws  RestException, IOException {
		return listURI(url, unixtimestamp==null?null:new String[] {modified_param,unixtimestamp.toString()});
	}
}
