package net.toxbank.client.resource;

import java.io.Serializable;
import java.net.URL;

public abstract class AbstractToxBankResource implements IToxBankResource , Serializable{

	private URL resourceURL;

	public void setResourceURL(URL resourceURL) {
		this.resourceURL = resourceURL;
	}

	public URL getResourceURL() {
		return resourceURL;
	}
	
}
