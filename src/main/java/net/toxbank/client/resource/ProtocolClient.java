package net.toxbank.client.resource;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProtocolIO;
import net.toxbank.client.task.RemoteTask;

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
		String[][] form = new String[8+(protocol.getAuthors()==null?0:protocol.getAuthors().size())][];
		
		form[0] = new String[] {"project_uri",protocol.getProject().getResourceURL().toString()};
		form[1] = new String[] {"organisation_uri",protocol.getOrganisation().getResourceURL().toString()};
		form[2] = new String[] {"user_uri",protocol.getOwner().getResourceURL().toString()};
		form[3] = new String[] {"title",protocol.getTitle()};
		form[4] = new String[] {"anabstract",protocol.getAbstract()};
		form[5] = new String[] {"summarySearchable",Boolean.toString(protocol.isSearchable())};
		form[6] = new String[] {"anabstract",protocol.getAbstract()};
		form[7] = new String[] {"keywords",b.toString()};
		if (protocol.getAuthors()!=null)
		for (int i=0; i < protocol.getAuthors().size(); i++)
			form[i+8] = new String[] {"author_uri",protocol.getAuthors().get(i).getResourceURL().toString()};
		
		String[] multipart = getMultipartWebFormRepresentation(
							form, "filename", 
							new File(protocol.getDocument().getResourceURL().toURI()), 
							"application/pdf");
		return new RemoteTask(collection, "text/uri-list", multipart[0], multipart[1], HTTPClient.POST);
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
