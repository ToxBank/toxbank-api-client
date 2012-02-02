package net.toxbank.client.resource;

import java.io.File;
import java.net.URL;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.task.RemoteTask;

import org.junit.Assert;

import com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler;

public class TemplateClientTest  extends AbstractClientTest<Protocol, ProtocolClient> {
	
	private final static String TEST_SERVER_PROTOCOL = String.format("%s%s",AbstractClientTest.TEST_SERVER,Resources.protocol);
	
	@Override
	protected ProtocolClient getToxBackClient() {
		return tbclient.getProtocolClient();
	}

	@Override
	public void testCreate() throws Exception {
		//TODO get the user from the token
		ProtocolClient cli = tbclient.getProtocolClient();
		
		Protocol protocol = cli.download(readFirst(cli));
		//this is the file to upload
		URL url = getClass().getClassLoader().getResource("net/toxbank/client/test/protocol-sample.pdf");

		RemoteTask task = cli.uploadTemplate(protocol,new File(url.getFile()));
		task.waitUntilCompleted(500);
		Assert.assertNotNull(task.getResult());
		Assert.assertTrue(task.getResult().toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
		
	}
	
	protected URL readFirst(ProtocolClient cli) throws Exception {
		User user = tbclient.getUserClient().myAccount(new URL(TEST_SERVER));
		List<URL> url = tbclient.getProtocolClient().listURI(new URL(String.format("%s%s?page=0&pagesize=1", user.getResourceURL(),Resources.protocol)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());
		return url.get(0);
	}
	
	@Override
	public void testUpdate() throws Exception {

	}
	
	@Override
	public void testDelete() throws Exception {
	
	}
	
	@Override
	public void testRead() throws Exception {
	
	}

	/**
	 * List templates
	 */
	@Override
	public void testList() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		List<URL> url = cli.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		URL template = cli.listTemplate(protocol);
		Assert.assertNotNull(template);
	}	
}
