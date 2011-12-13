package net.toxbank.client.resource;

import java.io.File;
import java.net.URL;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.task.RemoteTask;

import org.junit.Assert;

public class TemplateClientTest  extends AbstractClientTest<Protocol, ProtocolClient> {
	
	private final static String TEST_SERVER_PROTOCOL = String.format("%s%s",AbstractClientTest.TEST_SERVER,Resources.protocol);
	
	@Override
	protected ProtocolClient getToxBackClient() {
		return new ProtocolClient();
	}

	@Override
	public void testCreate() throws Exception {
		//TODO get the user from the token
		Protocol protocol = ProtocolClient.download(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		//this is the file to upload
		URL url = getClass().getClassLoader().getResource("net/toxbank/client/test/protocol-sample.pdf");
		ProtocolClient cli = new ProtocolClient();
		RemoteTask task = cli.uploadTemplate(protocol,new File(url.getFile()));
		task.waitUntilCompleted(500);
		Assert.assertNotNull(task.getResult());
		Assert.assertTrue(task.getResult().toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
		
	}
	
	@Override
	public void testUpdate() throws Exception {
		// TODO Auto-generated method stub
		super.testUpdate();
	}
	
	@Override
	public void testDelete() throws Exception {
		super.testDelete();
	}
	
	@Override
	public void testRead() throws Exception {
		// TODO Auto-generated method stub
		super.testRead();
	}

	/**
	 * List templates
	 */
	@Override
	public void testList() throws Exception {
		List<URL> url = ProtocolClient.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		URL template = ProtocolClient.listTemplate(protocol);
		Assert.assertNotNull(template);
	}	
}
