package net.toxbank.client.resource;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProtocolIO;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.opentox.rest.HTTPClient;
import org.opentox.rest.RestException;

public class ProtocolClient extends AbstractClient<Protocol> {

	protected ProtocolClient() {}
	
	@Override
	IOClass<Protocol> getIOClass() {
		return new ProtocolIO();
	}

	@Override
	protected RemoteTask createAsync(Protocol protocol, URL collection)	
					throws RestException,UnsupportedEncodingException, IOException, URISyntaxException {

		StringBuilder b = new StringBuilder();
		String d = "";
		for (String keyword: protocol.getKeywords()) {
			b.append(d);
			b.append(keyword);
			d = ";";
		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("project_uri", new StringBody(protocol.getProject().getResourceURL().toString()));
		entity.addPart("organisation_uri", new StringBody(protocol.getOrganisation().getResourceURL().toString()));
		entity.addPart("user_uri", new StringBody(protocol.getOwner().getResourceURL().toString()));
		entity.addPart("title", new StringBody(protocol.getTitle()));
		entity.addPart("anabstract", new StringBody(protocol.getAbstract()));
		entity.addPart("summarySearchable", new StringBody(Boolean.toString(protocol.isSearchable())));
		entity.addPart("keywords", new StringBody(b.toString()));
		if (protocol.getAuthors()!=null)
			for (int i=0; i < protocol.getAuthors().size(); i++)
				entity.addPart("author_uri",new StringBody(protocol.getAuthors().get(i).getResourceURL().toString()));
		entity.addPart("filename", new FileBody(new File(protocol.getDocument().getResourceURL().toURI())));
		 
		return new RemoteTask(collection, "text/uri-list", entity, HTTPClient.POST);
		

	}
	
	public RemoteTask createNewVersion(Protocol protocol)	
		throws RestException,UnsupportedEncodingException, IOException, URISyntaxException {
		return createAsync(protocol,new URL(String.format("%s%s", protocol.getResourceURL(),Resources.versions)));
	}
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Upload">API documentation</a>.
	 */
	public static Protocol download(URL identifier) throws Exception {
		ProtocolClient cli = new ProtocolClient();
		List<Protocol> protocol = cli.readRDF_XML(identifier);
		return protocol.size()==0?null:protocol.get(0);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveList">API documentation</a>.
	 */
	public static List<URL> listProtocols(URL server) throws IOException, RestException {
		ProtocolClient cli = new ProtocolClient();
		return cli.readURI(server);
	}

	public List<URL> listProtocols(User user) throws IOException, RestException {
		return readURI(new URL(String.format("%s%s", user.getResourceURL(),Resources.protocol)));
	}
	
	public List<Protocol> getProtocols(User user) throws Exception {
		return readRDF_XML(new URL(String.format("%s%s", user.getResourceURL(),Resources.protocol)));
	}	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Upload">API documentation</a>.
	 */
	public static URL upload(Protocol protocol, URL server) throws Exception {
		ProtocolClient cli = new ProtocolClient();
		Protocol p = cli.createSync(protocol,server);
		return p.getResourceURL();
	}


	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Retrieve">API documentation</a>.
	 */
	public static URL listFile(Protocol protocol) throws MalformedURLException {
		return new URL(String.format("%s%s",protocol.getResourceURL(),Resources.document));
	}
	
	public static File downloadFile(Protocol protocol) throws MalformedURLException, IOException {
		URL url = new URL(String.format("%s%s",protocol.getResourceURL(),Resources.document));
		HttpURLConnection c = HTTPClient.getHttpURLConnection(url.toExternalForm(), HTTPClient.GET,"");
		try {
			c.connect();
			File temp = File.createTempFile("download_", ".pdf");
			HTTPClient.download(c.getInputStream(), temp);
			c.disconnect();
			return temp;
		} finally {
			try {c.disconnect(); } catch (Exception x) {}
		}
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveVersions">API documentation</a>.
	 */
	public static List<URL> listVersions(Protocol protocol) throws IOException, RestException{
		ProtocolClient cli = new ProtocolClient();
		return cli.readURI(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.versions)));
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveVersions">API documentation</a>.
	 * Equivalent to {@link #listVersions()} but returns {@link ProtocolVersionClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Protocol> getVersions(Protocol protocol) throws Exception {
		ProtocolClient cli = new ProtocolClient();
		return cli.readRDF_XML(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.versions)));
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveTemplates">API documentation</a>.
	 */

	public static URL listTemplate(Protocol protocol) throws MalformedURLException  {
		return new URL(String.format("%s%s",protocol.getResourceURL(),Resources.datatemplate));
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveTemplates">API documentation</a>.
	 * Equivalent to {@link #listTempaltes()} but returns {@link TemplateClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Template> getTemplates(Protocol protocol) throws MalformedURLException {
		List<Template> templates = new ArrayList<Template>();
		templates.add(new Template(new URL(String.format("%s%s",protocol.getResourceURL(),Resources.datatemplate))));
		return templates;
	}

	public static void deleteProtocol(Protocol protocol) throws Exception {
		ProtocolClient cli = new ProtocolClient();
		cli.delete(protocol);
	}

}
