package net.toxbank.client.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProtocolIO;
import net.toxbank.client.policy.PolicyRule;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.opentox.rest.RestException;

public class SearchClient extends AbstractClient<Protocol> {
	private static final String metadatasearch_param = "metadata";
	public SearchClient() {
		this(null);
	}
		
	public SearchClient(HttpClient httpclient) {
		super(httpclient);
	}
	
	@Override
	protected IOClass<Protocol> getIOClass() {
		return new ProtocolIO();
	}

	
	@Override
	public List<URL> searchURI(URL url,String query) throws  RestException, IOException {
		return listURI(url, new String[] {metadatasearch_param,query});
	}
	
	@Override
	protected HttpEntity createPOSTEntity(Protocol object,
			List<PolicyRule> accessRights) throws InvalidInputException,
			Exception {
		throw new Exception("Not implemented");
	}

	@Override
	protected HttpEntity createPUTEntity(Protocol object,
			List<PolicyRule> accessRights) throws InvalidInputException,
			Exception {
		throw new Exception("Not implemented");
	}
}