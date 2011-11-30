package net.toxbank.client.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class TemplateClient {

	public TemplateClient() {}
	
	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:RetrieveTemplates">API documentation</a>.
	 */
	public TemplateClient(URL identifier) {
		// FIXME: implement retrieving metadata from the URL and set the below fields
	}

	/**
	 * Returns an {@link InputStream} from which the template can be read.
	 * @return
	 */
	public InputStream retrieve() {
		return null;
	}

	/**
	 * Returns an {@link OutputStream} to which the template can be written.
	 * @return
	 */
	public void upload(InputStream content, String server) {
		// FIXME: does this approach even work??
	}

}
