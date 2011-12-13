package net.toxbank.client.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import net.toxbank.client.Resources;
import net.toxbank.client.task.RemoteTask;

import org.junit.Assert;
import org.junit.Test;
import org.opentox.rest.RestException;

import com.itextpdf.text.pdf.PdfReader;

public class ProtocolClientTest {

	private final static String TEST_SERVER_PROTOCOL = String.format("%s%s",AbstractClientTest.TEST_SERVER,Resources.protocol);

	@Test
	public void testListProtocols() throws Exception {
		List<URL> protocols = ProtocolClient.listProtocols(new URL(TEST_SERVER_PROTOCOL));
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}
	
	public static void compare(File f1,File f2) throws IOException {
		InputStream in1 = new FileInputStream(f1);
		InputStream in2 = new FileInputStream(f2);
		
		byte[] bytes1 = new byte[490];
		byte[] bytes2 = new byte[490];
		int len1;
		int len2;
		long count1 = 0;
		long count2 = 0;
		while ((len1 = in1.read(bytes1, 0, bytes1.length)) != -1) {
			
			len2 = in2.read(bytes2, 0, bytes2.length);
			
			System.out.println("1-------->>\n"+new String(bytes1));
			System.out.println("2-------->>\n"+new String(bytes2));
			
			Assert.assertArrayEquals(bytes1,bytes2);
			count1 += len1;
			count2 += len2;
		}
		Assert.assertEquals(count1,count2);
		in1.close();
		in2.close();
	}		
	@Test
	public void testUpload() throws Exception {
		//TODO get the user from the token
		Protocol protocol = new Protocol();
		protocol.setProject(new Project(new URL(String.format("%s%s/G1",AbstractClientTest.TEST_SERVER,Resources.project))));
		protocol.setOrganisation(new Organisation(new URL(String.format("%s%s/G1",AbstractClientTest.TEST_SERVER,Resources.organisation))));
		protocol.setOwner(new User(new URL(String.format("%s%s/U1",AbstractClientTest.TEST_SERVER,Resources.user))));
		protocol.addAuthor(new User(new URL(String.format("%s%s/U1",AbstractClientTest.TEST_SERVER,Resources.user))));
		protocol.setTitle("title");
		protocol.setAbstract("abstrakt");
		protocol.setSearchable(true);
		protocol.addKeyword("one");
		protocol.addKeyword("two");
		protocol.addKeyword("three");
		//this is the file to upload
		URL file = getClass().getClassLoader().getResource("net/toxbank/client/test/protocol-sample.pdf");
		protocol.setDocument(new Document(file));
		
		URL newProtocol = ProtocolClient.upload(protocol, new URL(TEST_SERVER_PROTOCOL));
		Assert.assertNotNull(newProtocol);
		Assert.assertTrue(newProtocol.toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
		//now let's download the file 
		File download = ProtocolClient.downloadFile(new Protocol(newProtocol));
		Assert.assertTrue(download.exists());
		//verify if the pdf is readable
		InputStream in = new FileInputStream(download);
		PdfReader reader = new PdfReader(in);
		Assert.assertEquals("Slide 1", reader.getInfo().get("Title"));
		Assert.assertEquals("Nina Jeliazkova", reader.getInfo().get("Author"));
		in.close();
		download.delete();
		ProtocolClient cli = new ProtocolClient();
		cli.delete(newProtocol);
	}
	
	@Test
	public void testUploadNewVersion() throws Exception {
		
		Protocol protocol = ProtocolClient.download(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		System.out.println(protocol.getIdentifier());
		List<URL> versions = ProtocolClient.listVersions(protocol);
		
		Assert.assertNotNull(protocol.getResourceURL());
		protocol.addKeyword(UUID.randomUUID().toString());
		protocol.setAbstract("new "+protocol.getAbstract());
		URL file = getClass().getClassLoader().getResource("net/toxbank/client/test/protocol-sample.pdf");
		protocol.setDocument(new Document(file));
		ProtocolClient cli = new ProtocolClient();
		RemoteTask task = cli.createNewVersion(protocol);
		task.waitUntilCompleted(500);
		Assert.assertNotNull(task.getResult());
		Assert.assertTrue(task.getResult().toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
		
		List<URL> newversions = ProtocolClient.listVersions(protocol);
		Assert.assertEquals(versions.size()+1,newversions.size());
		
		//the new version task doesn't return the new url...
		Protocol newVersion = ProtocolClient.download(task.getResult());
		System.out.println(newVersion.getIdentifier());
		Assert.assertTrue(newVersion.getVersion()>protocol.getVersion());
		
	}
	
	@Test
	public void testRetrieveMetadata() throws Exception {
		
		List<URL> url = ProtocolClient.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());
		
		Protocol protocol = ProtocolClient.download(url.get(0));
		Assert.assertNotNull(protocol);
		Assert.assertNotNull(protocol.getOrganisation().getResourceURL());
		Assert.assertNotNull(protocol.getProject().getResourceURL());
		Assert.assertNotNull(protocol.getAbstract());
		Assert.assertNotNull(protocol.getDocument());
		Assert.assertNotNull(protocol.getTitle());
		Assert.assertNotNull(protocol.getIdentifier());
		Assert.assertNotNull(protocol.getOwner());
		Assert.assertTrue(protocol.getVersion()>0);
			
	}
	
	@Test
	public void testListFiles() throws Exception {
		List<URL> url = ProtocolClient.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		URL file = ProtocolClient.listFile(protocol);
		Assert.assertNotNull(file);
		//TODO verify whether it can be retrieved
	}
	

	
	@Test
	public void testGetTemplates() throws Exception {
		List<URL> url = ProtocolClient.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		List<Template> templates = ProtocolClient.getTemplates(protocol);
		Assert.assertNotNull(templates);
		Assert.assertNotSame(0, templates.size());
	}
	
	@Test
	public void testListVersions() throws RestException, IOException {
		List<URL> url = ProtocolClient.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));		
		List<URL> versions = ProtocolClient.listVersions(protocol);
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
	}
	
	@Test
	public void testGetVersions() throws Exception {
		List<URL> url = ProtocolClient.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		List<Protocol> versions = ProtocolClient.getVersions(protocol);
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
		for (Protocol p: versions) Assert.assertNotNull(p.getResourceURL());
	}

	@Test
	public void testUploadingAndRetrieving() throws Exception {
		//this will not work 
		Protocol protocol = new Protocol();
		protocol.addKeyword("cytotoxicity");
		URL identifier = ProtocolClient.upload(protocol, new URL(TEST_SERVER_PROTOCOL));

		// now download it again, and compare
		Protocol dlProtocol = ProtocolClient.download(identifier);
		Assert.assertTrue(dlProtocol.getKeywords().contains("cytotoxicity"));
	}

	@Test
	public void testRoundtripTitle() throws Exception {
		//this will not work 
		Protocol protocol = new Protocol();
		protocol.setTitle("Title");
		URL resource = ProtocolClient.upload(protocol, new URL(TEST_SERVER_PROTOCOL));

		Protocol roundtripped = ProtocolClient.download(resource);
		Assert.assertEquals("Title", roundtripped.getTitle());
	}

	@Test
	public void testRoundtripIdentifier() throws Exception {
		Protocol protocol = new Protocol();
		protocol.setIdentifier("Title");
		URL resource = ProtocolClient.upload(protocol, new URL(TEST_SERVER_PROTOCOL));

		Protocol roundtripped = ProtocolClient.download(resource);
		Assert.assertEquals("Title", roundtripped.getIdentifier());
	}

	@Test
	public void testRoundtripAbstract() throws Exception {
		Protocol protocol = new Protocol();
		protocol.setAbstract("This is the funniest abstract ever!");
		URL resource = ProtocolClient.upload(protocol, new URL(TEST_SERVER_PROTOCOL));

		Protocol roundtripped = ProtocolClient.download(resource);
		Assert.assertEquals("This is the funniest abstract ever!", roundtripped.getAbstract());
	}

	@Test
	public void testRoundtripSearchable() throws MalformedURLException {
		Protocol version = new Protocol();
		Assert.assertFalse(version.isSearchable());
		version.setSearchable(true);
		URL resource = ProtocolVersionClient.upload(version, new URL(TEST_SERVER_PROTOCOL));

		Protocol roundtripped = ProtocolVersionClient.download(resource);
		Assert.assertTrue(roundtripped.isSearchable());
	}
	
	@Test
	public void testRoundtripSubmissionDate() throws MalformedURLException {
		Protocol version = new Protocol();
		version.setSubmissionDate("2011-09-15");
		URL resource = ProtocolVersionClient.upload(version, new URL(TEST_SERVER_PROTOCOL));

		Protocol roundtripped = ProtocolVersionClient.download(resource);
		Assert.assertEquals("2011-09-15", roundtripped.getSubmissionDate());
	}
	


}
