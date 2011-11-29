package net.toxbank.client.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ProtocolClientTest {

	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testListProtocols() throws MalformedURLException {
		List<URL> protocols = ProtocolClient.listProtocols(new URL(TEST_SERVER));
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}
	
	@Test
	public void testUpload() throws MalformedURLException {
		URL url = ProtocolClient.upload(new Protocol(), new URL(TEST_SERVER));
		Assert.assertNotNull(url);
		Assert.assertTrue(url.toExternalForm().startsWith(TEST_SERVER));
	}
	
	@Test
	public void testRetrieveMetadata() throws MalformedURLException {
		Protocol protocol = ProtocolClient.download(new URL(TEST_SERVER + "protocol/1"));
		Assert.assertNotNull(protocol);
		Assert.assertEquals(
			TEST_SERVER + "organization/1",
			protocol.getOrganisation()
		);
	}
	
	@Test
	public void testListFiles() throws MalformedURLException {
		Protocol protocol = new Protocol(new URL(TEST_SERVER + "protocol/1"));
		URL file = ProtocolClient.listFile(protocol);
		Assert.assertNotNull(file);
	}
	
	@Test
	public void testListTemplate() throws MalformedURLException {
		Protocol protocol = new Protocol(new URL(TEST_SERVER + "protocol/1"));
		URL template = ProtocolClient.listTemplate(protocol);
		Assert.assertNotNull(template);
	}
	
	@Test
	public void testGetTemplates() throws MalformedURLException {
		Protocol protocol = new Protocol(new URL(TEST_SERVER + "protocol/1"));
		List<TemplateClient> templates = ProtocolClient.getTemplates(protocol);
		Assert.assertNotNull(templates);
		Assert.assertNotSame(0, templates.size());
	}
	
	@Test
	public void testListVersions() throws MalformedURLException {
		Protocol protocol = new Protocol(new URL(TEST_SERVER + "protocol/1"));
		List<URL> versions = ProtocolClient.listVersions(protocol);
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
	}
	
	@Test
	public void testGetVersions() throws MalformedURLException {
		Protocol protocol = new Protocol(new URL(TEST_SERVER + "protocol/1"));
		List<ProtocolVersionClient> versions = ProtocolClient.getVersions(protocol);
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
	}

	@Test
	public void testUploadingAndRetrieving() throws MalformedURLException {
		Protocol protocol = new Protocol();
		protocol.addKeyword("cytotoxicity");
		URL identifier = ProtocolClient.upload(protocol, new URL(TEST_SERVER));

		// now download it again, and compare
		Protocol dlProtocol = ProtocolClient.download(identifier);
		Assert.assertTrue(dlProtocol.getKeywords().contains("cytotoxicity"));
	}

	@Test
	public void testRoundtripTitle() throws MalformedURLException {
		Protocol protocol = new Protocol();
		protocol.setTitle("Title");
		URL resource = ProtocolClient.upload(protocol, new URL(TEST_SERVER));

		Protocol roundtripped = ProtocolClient.download(resource);
		Assert.assertEquals("Title", roundtripped.getTitle());
	}

	@Test
	public void testRoundtripIdentifier() throws MalformedURLException {
		Protocol protocol = new Protocol();
		protocol.setIdentifier("Title");
		URL resource = ProtocolClient.upload(protocol, new URL(TEST_SERVER));

		Protocol roundtripped = ProtocolClient.download(resource);
		Assert.assertEquals("Title", roundtripped.getIdentifier());
	}

	@Test
	public void testRoundtripAbstract() throws MalformedURLException {
		Protocol protocol = new Protocol();
		protocol.setAbstract("This is the funniest abstract ever!");
		URL resource = ProtocolClient.upload(protocol, new URL(TEST_SERVER));

		Protocol roundtripped = ProtocolClient.download(resource);
		Assert.assertEquals("This is the funniest abstract ever!", roundtripped.getAbstract());
	}

}
