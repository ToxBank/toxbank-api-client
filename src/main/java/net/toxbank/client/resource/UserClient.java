package net.toxbank.client.resource;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Modeled after FOAF where possible.
 * 
 * @author egonw
 */
public class UserClient {

	private UserClient() {}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveList">API documentation</a>.
	 */
	public static User download(URL identifier) {
		// FIXME: implement retrieving metadata from the URL and set the below fields
		return null;
	}
	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveList">API documentation</a>.
	 */
	public static List<URL> list(String server) {
		// FIXME: retrieve a list of all registered protocols
		return Collections.emptyList();
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveProtocols">API documentation</a>.
	 */
	public static List<URL> listProtocols(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/User:RetrieveProtocols">API documentation</a>.
	 * Equivalent to {@link #listProtocols()} but returns {@link ProtocolClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Protocol> getProtocols(User user) {
		// FIXME: implement uploading this protocol to the server
		return null;
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
