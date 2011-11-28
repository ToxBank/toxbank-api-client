package net.toxbank.client.resource;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class Study {

	public Study() {}
	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:Retrieve">API documentation</a>.
	 */
	public Study(URL identifier) {
		// FIXME: implement retrieving metadata from the URL and set the below fields
	}

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
	public URL upload(String server) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	public void addKeyword(String keyword) {
	}

	public void removeKeyword(String keyword) {
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:RetrieveVersions">API documentation</a>.
	 */
	public List<URL> listVersions() {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Study:RetrieveVersions">API documentation</a>.
	 * Equivalent to {@link #listVersions()} but returns {@link Study}s
	 * already populated with metadata from the database.
	 */
	public List<Study> getVersions() {
		// FIXME: implement uploading this protocol to the server
		return null;
	}
}
