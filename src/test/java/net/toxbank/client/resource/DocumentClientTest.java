package net.toxbank.client.resource;


public class DocumentClientTest extends AbstractClientTest<Protocol, ProtocolClient> {

	@Override
	protected ProtocolClient getToxBackClient() {
		return new ProtocolClient();
	}


}
