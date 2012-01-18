package net.toxbank.client.resource;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.OrganisationIO;

import org.apache.http.client.HttpClient;

/**
 * ToxBank <a href="http://api.toxbank.net/index.php/Organisation">Organisation</a> client,
 * implementing REST operations on {@link Organisation}. 
 * @author nina
 *
 */
public class OrganisationClient extends AbstractGroupClient<Organisation> {
	
	
	public OrganisationClient() {
		this(null);
	}
		
	public OrganisationClient(HttpClient httpclient) {
		super(httpclient);
	}
	
	
	@Override
	IOClass<Organisation> getIOClass() {
		return new OrganisationIO();
	}

}
