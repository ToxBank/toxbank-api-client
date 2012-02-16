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
import net.toxbank.client.policy.PolicyRule;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
		status,
		published
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
	protected HttpEntity createPOSTEntity(Protocol protocol,List<PolicyRule> accessRights) throws InvalidInputException,Exception {
		StringBuilder b = new StringBuilder();
		String d = "";
		for (String keyword: protocol.getKeywords()) {
			b.append(d);
			b.append(keyword);
			d = ";";
		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,utf8);
		if ((protocol.getProject()==null) || (protocol.getProject().getResourceURL()==null)) throw new InvalidInputException("No Project URI!");
		entity.addPart(webform.project_uri.name(), new StringBody(protocol.getProject().getResourceURL().toString(),utf8));
		if ((protocol.getOrganisation()==null) || (protocol.getOrganisation().getResourceURL()==null)) throw new InvalidInputException("No Organisation URI!");
		entity.addPart(webform.organisation_uri.name(), new StringBody(protocol.getOrganisation().getResourceURL().toString(),utf8));
		if ((protocol.getOwner()==null) || (protocol.getOwner().getResourceURL()==null)) throw new InvalidInputException("No User URI!");
		entity.addPart(webform.user_uri.name(), new StringBody(protocol.getOwner().getResourceURL().toString(),utf8));
		if (protocol.getTitle().length()>255) throw new InvalidInputException(String.format("Title length %d, expected <=255",protocol.getTitle().length()));
		entity.addPart(webform.title.name(), new StringBody(protocol.getTitle(),utf8));
		entity.addPart(webform.anabstract.name(), new StringBody(protocol.getAbstract(),utf8));
		entity.addPart(webform.summarySearchable.name(), new StringBody(protocol.isSearchable()==null?Boolean.FALSE.toString():Boolean.toString(protocol.isSearchable()),utf8));

		entity.addPart(webform.published.name(), new StringBody(Boolean.toString(protocol.isPublished()==null?false:protocol.isPublished()),utf8));
		entity.addPart(webform.keywords.name(), new StringBody(b.toString(),utf8));
		entity.addPart(webform.status.name(), new StringBody(protocol.getStatus().toString(),utf8));
		if (protocol.getAuthors()!=null)
			for (int i=0; i < protocol.getAuthors().size(); i++)
				entity.addPart(webform.author_uri.name(),new StringBody(protocol.getAuthors().get(i).getResourceURL().toString(),utf8));
		entity.addPart(webform.filename.name(), new FileBody(new File(protocol.getDocument().getResourceURL().toURI())));
		 
		addPolicyRules(entity, accessRights);
		return entity;
	}
	
	
	
	protected HttpEntity createPublishFlagOnly(boolean isPublished) throws Exception {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,utf8);
		entity.addPart(webform.published.name(), new StringBody(Boolean.toString(isPublished),utf8));

		return entity;
	}	
	/**
	 * Almost same as {@link #createPOSTEntity(Protocol)} , but used for updating an existing protocol and allows missing fields 
	 */
	@Override
	protected HttpEntity createPUTEntity(Protocol protocol,List<PolicyRule> accessRights) throws Exception {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,utf8);
		
		if ((protocol.getProject()!=null) && (protocol.getProject().getResourceURL()!=null)) 
			entity.addPart(webform.project_uri.name(), new StringBody(protocol.getProject().getResourceURL().toString(),utf8));
		
		if ((protocol.getOrganisation()!=null) && (protocol.getOrganisation().getResourceURL()!=null))
			entity.addPart(webform.organisation_uri.name(), new StringBody(protocol.getOrganisation().getResourceURL().toString(),utf8));
		
		if (protocol.getTitle()!=null) {
			if (protocol.getTitle().length()>255) throw new InvalidInputException(String.format("Title length %d, expected <=255",protocol.getTitle().length()));
			entity.addPart(webform.title.name(), new StringBody(protocol.getTitle(),utf8));
		}
		if (protocol.getAbstract()!=null) 
			entity.addPart(webform.anabstract.name(), new StringBody(protocol.getAbstract(),utf8));

		if (protocol.isPublished()!=null)
			entity.addPart(webform.published.name(), new StringBody(Boolean.toString(protocol.isPublished()),utf8));

		if (protocol.isSearchable()!=null)
			entity.addPart(webform.summarySearchable.name(), new StringBody(Boolean.toString(protocol.isSearchable()),utf8));
		
		if ((protocol.getDocument()!=null) && (protocol.getDocument().getResourceURL()!=null))
			entity.addPart(webform.filename.name(), new FileBody(new File(protocol.getDocument().getResourceURL().toURI())));
		
		if (protocol.getKeywords()!=null) {
			StringBuilder b = new StringBuilder();
			String d = "";
			for (String keyword: protocol.getKeywords()) {
				b.append(d);
				b.append(keyword);
				d = ";";
			}
			entity.addPart(webform.keywords.name(), new StringBody(b.toString(),utf8));
		}
		if (protocol.getStatus()!=null)
			entity.addPart(webform.status.name(), new StringBody(protocol.getStatus().toString(),utf8));
		
		if (protocol.getAuthors()!=null)
			for (int i=0; i < protocol.getAuthors().size(); i++)
				entity.addPart(webform.author_uri.name(),new StringBody(protocol.getAuthors().get(i).getResourceURL().toString(),utf8));
		//no file update
		//entity.addPart(webform.filename.name(), new FileBody(new File(protocol.getDocument().getResourceURL().toURI())));
		addPolicyRules(entity, accessRights);		 
		return entity;
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
	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Uploadt">API documentation</a>.
	 */
	public RemoteTask createNewVersion(Protocol protocol,List<PolicyRule> accessRights) throws Exception {
		return postAsync(protocol,new URL(String.format("%s%s", protocol.getResourceURL(),Resources.versions)),accessRights);
	}
	/**
	 * Same as {@link #createNewVersion(Protocol,null)}
	 * @param protocol
	 * @return
	 * @throws Exception
	 */
	public RemoteTask createNewVersion(Protocol protocol) throws Exception {
		return createNewVersion(protocol,null);
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
	 * @param protocol
	 * @param server
	 * @param accessRights {@link List} of {@link PolicyRule} or null
	 * @return
	 * @throws Exception
	 */
	public URL upload(Protocol protocol, URL server,List<PolicyRule> accessRights) throws Exception {
		Protocol p = post(protocol,server,accessRights);
		return p.getResourceURL();
	}
	/**
	 * Same as {@link #upload(Protocol, URL, null)}
	 * @param protocol
	 * @param server
	 * @return
	 * @throws Exception
	 */
	public URL upload(Protocol protocol, URL server) throws Exception {
		return upload(protocol, server,null);
	}
	/**
	 * Asynchronous upload
	 * @param protocol
	 * @param server
	 * @param accessRights {@link List} of {@link PolicyRule} or null
	 * @return {@link RemoteTask}
	 * @throws Exception
	 */
	public RemoteTask uploadAsync(Protocol protocol, URL server,List<PolicyRule> accessRights) throws Exception {
		return postAsync(protocol,server,accessRights);
	}

	/**
	 * Sets the "isPublished" flag only. 
	 * @param protocol
	 * @param isPublished
	 * @return
	 * @throws Exception
	 */
	public RemoteTask publishAsync(Protocol protocol, boolean isPublished) throws Exception {
		if (protocol.getResourceURL()==null) throw new MalformedURLException("No protocol URI");
		return sendAsync(protocol.getResourceURL(), createPublishFlagOnly(isPublished), HttpPut.METHOD_NAME);
	}
	/**
	 * Modifies all non-null fields. File is not modified.
	 * @param protocol
	 * @param accessRights {@link List} of {@link PolicyRule} or null
	 * @return
	 * @throws Exception
	 */
	public URL update(Protocol protocol,List<PolicyRule> accessRights) throws Exception {
		if (protocol.getResourceURL()==null) throw new MalformedURLException("No protocol URI");
		Protocol p = put(protocol,accessRights);
		return p.getResourceURL();
	}
	/**
	 * Same as {@link #update(Protocol,null)}
	 * @param protocol
	 * @return
	 * @throws Exception
	 */
	public URL update(Protocol protocol) throws Exception {
		return update(protocol,null);
	}
	/**
	 * Modifies all non-null fields. File is not modified.
	 * @param protocol
	 * @param accessRights {@link List} of {@link PolicyRule} or null
	 * @return
	 * @throws Exception
	 */
	public RemoteTask updateAsync(Protocol protocol,List<PolicyRule> accessRights) throws Exception {
		if (protocol.getResourceURL()==null) throw new MalformedURLException("No protocol URI");
		return putAsync(protocol,accessRights);
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

	public List<URL> listPreviousVersion(Protocol protocol) throws IOException, RestException{
		return listURI(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.previous)));
	}
	
	public List<Protocol> getPreviousVersion(Protocol protocol) throws Exception {
		return getRDF_XML(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.previous)));
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
