package net.toxbank.client.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.toxbank.client.Resources;
import net.toxbank.client.policy.AccessRights;
import net.toxbank.client.policy.GroupPolicyRule;
import net.toxbank.client.policy.PolicyRule;
import net.toxbank.client.policy.UserPolicyRule;
import net.toxbank.client.resource.Protocol.STATUS;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.junit.Assert;
import org.junit.Test;
import org.opentox.rest.RestException;

import com.itextpdf.text.pdf.PdfReader;

public class ProtocolClientTest  extends AbstractClientTest<Protocol, ProtocolClient> {

	private final static String TEST_SERVER_PROTOCOL = String.format("%s%s",AbstractClientTest.TEST_SERVER,Resources.protocol);

	@Override
	protected ProtocolClient getToxBackClient() {
		return tbclient.getProtocolClient();
	}
	
	@Override
	public void testList() throws Exception {
	  System.out.println("Connecting to: " + TEST_SERVER_PROTOCOL);
		List<URL> protocols = getToxBackClient().listProtocols(new URL(TEST_SERVER_PROTOCOL));
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
	
	@Override
	public void testCreate() throws Exception {
		//TODO get the user from the token
		UserClient ucli = tbclient.getUserClient();
		User myself = ucli.myAccount(new URL(TEST_SERVER));
				
		Protocol protocol = new Protocol();
		protocol.setStatus(STATUS.SOP);
		protocol.addProject(new Project(new URL(String.format("%s%s/G1",AbstractClientTest.TEST_SERVER,Resources.project))));
		protocol.setOrganisation(new Organisation(new URL(String.format("%s%s/G1",AbstractClientTest.TEST_SERVER,Resources.organisation))));
		protocol.setOwner(myself);
		protocol.addAuthor(myself);
		protocol.setTitle("title");
		protocol.setAbstract("My \u2122 abstract");
		protocol.setSearchable(true);
		protocol.addKeyword("one");
		protocol.addKeyword("two");
		protocol.addKeyword("three");
		//this is the file to upload
		URL file = getClass().getClassLoader().getResource("net/toxbank/client/test/protocol-sample.pdf");
		protocol.setDocument(new Document(file));
		
		ProtocolClient cli = tbclient.getProtocolClient();
		AccessRights accessRights = new AccessRights();
		accessRights.addUserRule(myself, true,true,true,true);
		User somebodyElse = new User(new URL(String.format("%s%s/U1",AbstractClientTest.TEST_SERVER,Resources.user)));
		accessRights.addUserRule(somebodyElse, true,false,false,false);
		
		URL newProtocol = cli.upload(protocol, new URL(TEST_SERVER_PROTOCOL),accessRights.getRules());
		Assert.assertNotNull(newProtocol);
		Assert.assertTrue(newProtocol.toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
		//now let's download the file 
		File download = cli.downloadFile(new Protocol(newProtocol));
		Assert.assertTrue(download.exists());
		//verify if the pdf is readable
		InputStream in = new FileInputStream(download);
		PdfReader reader = new PdfReader(in);
		Assert.assertEquals("Slide 1", reader.getInfo().get("Title"));
		Assert.assertEquals("Nina Jeliazkova", reader.getInfo().get("Author"));
		in.close();
		download.delete();
		List<Protocol> newp = cli.get(newProtocol);
		Assert.assertEquals(1,newp.size());
		Assert.assertEquals(STATUS.SOP,newp.get(0).getStatus());
		Assert.assertNotNull(newp.get(0).getProjects());
		Assert.assertEquals(1,newp.get(0).getProjects().size());
		Assert.assertNotNull(newp.get(0).getProjects().get(0).getTitle());
		Assert.assertNotNull(newp.get(0).getProjects().get(0).getGroupName());
		
		Assert.assertTrue(newProtocol.toString(),newp.get(0).getAbstract().contains("\u2122"));
		cli.delete(newProtocol);
		//TODO verify if policies are deleted
	}
	
	@Test
	public void testUploadNewVersion() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		List<Protocol> protocols = cli.get(url);
		Protocol protocol = protocols.get(0);
		System.out.println(protocol.getIdentifier());
		List<URL> versions = cli.listVersions(protocol);
		
		Assert.assertNotNull(protocol.getResourceURL());
		protocol.addKeyword(UUID.randomUUID().toString());
		protocol.setAbstract("new "+protocol.getAbstract());
		URL file = getClass().getClassLoader().getResource("net/toxbank/client/test/protocol-sample.pdf");
		protocol.setDocument(new Document(file));

		RemoteTask task = cli.createNewVersion(protocol);
		task.waitUntilCompleted(500);
		Assert.assertNotNull(task.getResult());
		Assert.assertTrue(task.getResult().toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
		
		List<URL> newversions = cli.listVersions(protocol);
		Assert.assertEquals(versions.size()+1,newversions.size());
		
		//the new version task doesn't return the new url...
		Protocol newVersion = cli.download(task.getResult());
		System.out.println(newVersion.getIdentifier());
		Assert.assertTrue(newVersion.getVersion()>protocol.getVersion());
		
	}
	
	@Test
	public void testPublish() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		List<Protocol> protocols = cli.get(url);
		Protocol protocol = new Protocol();
		protocol.setResourceURL(protocols.get(0).getResourceURL());
		
		RemoteTask task = cli.publishAsync(protocol,true);
		task.waitUntilCompleted(500);
		Assert.assertNotNull(task.getResult());
		//System.out.println(task.getResult());
		Assert.assertTrue(task.getResult().toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
	}
	
	@Test
	public void testPublishWithoutSettingRights() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		Protocol proto = cli.download(new URL("http://localhost:8080/toxbank/protocol/SEURAT-Protocol-13-1"));
		proto.setPublished(true);
		proto.setDocument(null);
		RemoteTask task = cli.putAsync(proto, null);
		
		task.waitUntilCompleted(500);
		Assert.assertNotNull(task.getResult());
		//System.out.println(task.getResult());
		Assert.assertTrue(task.getResult().toExternalForm().startsWith(TEST_SERVER_PROTOCOL));
	}
	
	protected URL readFirst(ProtocolClient cli) throws Exception {
		User user = tbclient.getUserClient().myAccount(new URL(TEST_SERVER));
		List<URL> url = tbclient.getProtocolClient().listURI(new URL(String.format("%s%s?page=0&pagesize=1", user.getResourceURL(),Resources.protocol)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());
		return url.get(0);
	}
	/*
	protected URL readFirst(ProtocolClient cli) throws Exception {
		List<URL> url = cli.listProtocols(new URL(TEST_SERVER_PROTOCOL),new String[] {"page","0","pagesize","1"});
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());
		return url.get(0);
	}
	*/
	@Override
	public void testRead() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);

		Assert.assertNotNull(url);
		
		Protocol protocol = cli.download(url);
		Assert.assertNotNull(protocol);
		Assert.assertNotNull(protocol.getOrganisation().getResourceURL());
		Assert.assertNotNull(protocol.getProjects());
		Assert.assertEquals(1,protocol.getProjects().size());
		Assert.assertNotNull(protocol.getProjects().get(0).getResourceURL());
		Assert.assertNotNull(protocol.getAbstract());
		Assert.assertNotNull(protocol.getDocument());
		Assert.assertNotNull(protocol.getTitle());
		Assert.assertNotNull(protocol.getIdentifier());
		Assert.assertNotNull(protocol.getOwner());
		Assert.assertNotNull(protocol.getStatus());
		Assert.assertTrue(protocol.getVersion()>0);
			
	}

	
	@Test
	public void testListFiles() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		List<URL> url = cli.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		URL file = cli.listFile(protocol);
		Assert.assertNotNull(file);
		//TODO verify whether it can be retrieved
	}
	

	
	@Test
	public void testGetTemplates() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		List<URL> url = cli.listProtocols(new URL(String.format("%s?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		List<Template> templates = cli.getTemplates(protocol);
		Assert.assertNotNull(templates);
		Assert.assertNotSame(0, templates.size());
	}
	
	@Test
	public void testListPreviousVersion() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		Protocol protocol = new Protocol(url);		
		List<Protocol> versions = cli.getPreviousVersion(protocol);
		//Assert.assertNotNull(protocol.getPreviousVersion());
	}
	
	@Test
	public void testListVersions() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		Assert.assertNotNull(url);

		Protocol protocol = new Protocol(url);		
		List<URL> versions = cli.listVersions(protocol);
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
	}
	
	@Test
	public void testGetVersions() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		Protocol protocol = new Protocol(url);
		List<Protocol> versions = cli.getVersions(protocol);
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
		for (Protocol p: versions) Assert.assertNotNull(p.getResourceURL());
	}

	@Test
	public void testUploadingAndRetrieving() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		//this will not work 
		Protocol protocol = new Protocol();
		protocol.addKeyword("cytotoxicity");
		URL identifier = cli.upload(protocol, new URL(TEST_SERVER_PROTOCOL));

		// now download it again, and compare
		Protocol dlProtocol = cli.download(identifier);
		Assert.assertTrue(dlProtocol.getKeywords().contains("cytotoxicity"));
	}

	@Test
	public void testRoundtripTitle() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		Protocol protocol = new Protocol(url);
		String title = UUID.randomUUID().toString();
		protocol.setTitle(title);
		URL resource = cli.update(protocol);
		Assert.assertEquals(url,resource);
		Protocol roundtripped = cli.download(url);
		Assert.assertEquals(title, roundtripped.getTitle());
	}

	@Test
	public void testRoundtripIdentifier() throws Exception {
		throw new Exception("Identifier should not be changed");
	}

	@Test
	public void testRoundtripAbstract() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		Protocol protocol = new Protocol(url);
		String abstrakt = UUID.randomUUID().toString();
		protocol.setAbstract(abstrakt);
		URL resource = cli.update(protocol);
		Assert.assertEquals(url,resource);
		Protocol roundtripped = cli.download(url);
		Assert.assertEquals(abstrakt, roundtripped.getAbstract());
	}

	@Test
	public void testRoundtripSearchable() throws Exception {
		//get the first protocol
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		Protocol protocol = cli.download(url);
		boolean searchable = protocol.isSearchable();
		
		//update only the flag, not anything else
		//toggle the flag
		Protocol toUpdate = new Protocol(url);
		toUpdate.setSearchable(!searchable);
		URL resource = cli.update(toUpdate);
		
		Assert.assertEquals(url,resource);
		
		Protocol roundtripped = cli.download(url);
		Assert.assertEquals(!searchable,roundtripped.isSearchable());
	}
	
	@Test
	public void testRoundtripSubmissionDate() throws Exception {
		//submission date should not change, why test?
		throw new Exception("submission date should not be changed");
		/*
		Protocol version = new Protocol();
		Long now = System.currentTimeMillis();
		version.setSubmissionDate(now);
		ProtocolVersionClient cli = tbclient.getProtocolVersionClient();
		URL resource = cli.upload(version, new URL(TEST_SERVER_PROTOCOL));

		Protocol roundtripped = cli.download(resource);
		Assert.assertEquals(now, roundtripped.getSubmissionDate());
		Assert.assertEquals(now, roundtripped.getTimeModified());
		*/
	}
	
	@Override
	public void testDelete() throws Exception {
		
	}

	@Override
	public void testUpdate() throws Exception {
		ProtocolClient cli = tbclient.getProtocolClient();
		URL url = readFirst(cli);
		Protocol protocol = cli.download(url);
		long updated = protocol.getTimeModified();
		//this is the file to upload
		URL file = getClass().getClassLoader().getResource("net/toxbank/client/test/protocol-sample.pdf");
		protocol.setDocument(new Document(file));
		cli.update(protocol);
		protocol = cli.download(url);
		Assert.assertNotSame(updated,protocol.getTimeModified());
	}
	
	@Test
	public void testUploadAllowed() throws Exception {
		Assert.assertTrue(tbclient.isProtocolUploadAllowed(new URL(TEST_SERVER_PROTOCOL)));
	}
	
	public void testAccessRights() throws Exception {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,AbstractClient.utf8);
		List<PolicyRule> accessRights = new ArrayList<PolicyRule>();
		accessRights.add(new UserPolicyRule<User>(new User(new URL("http://example.com/user/1"))));
		accessRights.add(new UserPolicyRule<User>(new User(new URL("http://example.com/user/2")),true,null,null,true));
		accessRights.add(new GroupPolicyRule<Group>(new Project(new URL("http://example.com/group/1"))));
		AbstractClient.addPolicyRules(entity, accessRights);
		entity.writeTo(System.out);
		
	}
	@Test
	public void testReadPolicy() throws Exception {
		User user = tbclient.getUserClient().myAccount(new URL(TEST_SERVER));
		List<Protocol> protocols = tbclient.getProtocolClient().getProtocols(user);
		List<AccessRights> rights = tbclient.readPolicy(protocols.get(0).getResourceURL());
		Assert.assertNotNull(rights);
		Assert.assertTrue(rights.size()>0);
	}
}
