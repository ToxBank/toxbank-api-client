package net.toxbank.client.resource;

import java.net.URL;

public abstract class AbstractToxBankResource {

	private URL resourceURL;

	public void setResourceURL(URL resourceURL) {
		this.resourceURL = resourceURL;
	}

	public URL getResourceURL() {
		return resourceURL;
	}
	
}
