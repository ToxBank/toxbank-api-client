package net.toxbank.client.resource;

import java.net.URL;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProtocolIO;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;

/**
 * A protocol version is a {@link Protocol}
 * @author nina
 *
 */
public class ProtocolVersionClient extends AbstractClient<Protocol> {

	public ProtocolVersionClient() {
		this(null);
	}
		
	public ProtocolVersionClient(HttpClient httpclient) {
		super(httpclient);
	}

	/**
	 * Described in this <a href="http://api.toxbank.net/index.php/API_Protocol:Uploadt">API documentation</a>.
	 */
	public URL upload(Protocol version, URL server) {
		// FIXME: implement uploading this protocol to the server
		return null;
	}

	public Protocol download(URL resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected HttpEntity createPOSTEntity(Protocol object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected HttpEntity createPUTEntity(Protocol object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	IOClass<Protocol> getIOClass() {
		return new ProtocolIO();
	}

}
