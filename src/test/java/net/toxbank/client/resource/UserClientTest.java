package net.toxbank.client.resource;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;
import net.toxbank.client.Resources;
import net.toxbank.client.task.RemoteTask;

import org.junit.Test;

public class UserClientTest {
	
	private final static String TEST_SERVER_USER = String.format("%s%s",AbstractClientTest.TEST_SERVER,Resources.user);

	@Test
	public void testRetrieveMetadata() throws Exception {
		//List users, but only the first one to get a valid user url
		List<URL> users = UserClient.list(new URL(String.format("%s?page=%d&pagesize=%d",TEST_SERVER_USER,0,1)));
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		//now retrieve the user content
		User user = UserClient.download(users.get(0));
		Assert.assertNotNull(user);
		Assert.assertNotNull(user.getFirstname());
		Assert.assertNotNull(user.getLastname());
		Assert.assertEquals(users.get(0),user.getResourceURL());
		Assert.assertNotNull(user.getHomepage());
		// FIXME: test the rest of the loaded metadata
	}
	
	@Test
	public void testList() throws Exception  {
		List<URL> users = UserClient.list(new URL(TEST_SERVER_USER));
		Assert.assertNotNull(users);
		Assert.assertNotSame(0, users.size());
	}
	
	@Test
	public void testListProtocols() throws Exception {
		User user = new User(new URL(TEST_SERVER_USER + "user/ab7f235ccd"));
		List<URL> protocols = UserClient.listProtocols(user);
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}
	
	@Test
	public void testGetProtocols() throws Exception {
		User user = new User(new URL(TEST_SERVER_USER + "user/ab7f235ccd"));
		List<Protocol> protocols = UserClient.getProtocols(user);
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}

	@Test
	public void testListStudies() throws Exception {
		User user = new User(new URL(TEST_SERVER_USER + "user/ab7f235ccd"));
		List<URL> studies = UserClient.listStudies(user);
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}
	
	@Test
	public void testGetStudies() throws Exception {
		User user = new User(new URL(TEST_SERVER_USER + "user/ab7f235ccd"));
		List<Study> studies = UserClient.getStudies(user);
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}

	@Test
	public void testListAlerts() throws Exception {
		User user = new User(new URL(TEST_SERVER_USER + "user/ab7f235ccd"));
		List<URL> alerts = UserClient.listAlerts(user);
		Assert.assertNotNull(alerts);
		Assert.assertNotSame(0, alerts.size());
	}
	
	@Test
	public void testGetAlerts() throws Exception {
		User user = new User(new URL(TEST_SERVER_USER + "user/ab7f235ccd"));
		List<Alert> alerts = UserClient.getAlerts(user);
		Assert.assertNotNull(alerts);
		Assert.assertNotSame(0, alerts.size());
	}
	
	@Test
	public void testCreate() throws Exception {
		UserClient tbClient = new UserClient();
		User user = new User();
		user.setTitle("Dr.");
		user.setFirstname("Test");
		user.setLastname("Tester");
		//user.setUserName("guest");
		user.setInstitute(new Organisation(new URL(String.format("%s%s/G1",AbstractClientTest.TEST_SERVER,Resources.organisation))));
		user.setWeblog(new URL("http://example.org/blog"));
		user.setHomepage(new URL("http://example.org/home"));
		
		RemoteTask task = tbClient.createAsync(user,new URL(TEST_SERVER_USER));
		task.waitUntilCompleted(500);
		//verify if ok
		Assert.assertEquals(HttpURLConnection.HTTP_OK,task.getStatus());
		Assert.assertNull(task.getError());
		System.out.println(task.getResult());
		//should not be 0 ! http://toxbanktest1.opentox.org:8080/toxbank/project/G0 
		tbClient.delete(task.getResult());		
	}
}
