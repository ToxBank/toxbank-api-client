package net.toxbank.client.resource;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class ProtocolClient {

	private ProtocolClient() {}
	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Upload">API documentation</a>.
	 */
	public static Protocol download(URL identifier) {
		// FIXME: implement retrieving metadata from the URL and set the below fields
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveList">API documentation</a>.
	 */
	public static List<URL> listProtocols(URL server) {
		// FIXME: retrieve a list of all registered protocols
		return Collections.emptyList();
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Upload">API documentation</a>.
	 */
	public static URL upload(Protocol protocol, URL server) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Retrieve">API documentation</a>.
	 */
	public static URL listFile(Protocol protocol) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveVersions">API documentation</a>.
	 */
	public static List<URL> listVersions(Protocol protocol) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveVersions">API documentation</a>.
	 * Equivalent to {@link #listVersions()} but returns {@link ProtocolVersionClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Protocol> getVersions(Protocol protocol) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveTemplates">API documentation</a>.
	 */
	public static URL listTemplate(Protocol protocol) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveTemplates">API documentation</a>.
	 * Equivalent to {@link #listTempaltes()} but returns {@link TemplateClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Template> getTemplates(Protocol protocol) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	public static void delete(Protocol protocol) {
		// FIXME: implement deleting a protocol from the server
	}

}
