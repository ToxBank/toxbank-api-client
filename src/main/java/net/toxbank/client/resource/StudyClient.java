package net.toxbank.client.resource;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class StudyClient {

	private StudyClient() {}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:RetrieveList">API documentation</a>.
	 */
	public static List<URL> list(String server) {
		// FIXME: retrieve a list of all registered protocols
		return Collections.emptyList();
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:Upload">API documentation</a>.
	 */
	public static URL upload(Study study, URL server) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:RetrieveVersions">API documentation</a>.
	 */
	public static List<URL> listVersions() {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:RetrieveVersions">API documentation</a>.
	 * Equivalent to {@link #listVersions()} but returns {@link StudyClient}s
	 * already populated with metadata from the database.
	 */
	public static List<Study> getVersions() {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:Retrieve">API documentation</a>.
	 */
	public static Study download(URL resource) {
		// TODO Auto-generated method stub
		return null;
	}
}
