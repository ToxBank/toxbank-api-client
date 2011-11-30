package net.toxbank.client.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class UserClientTest {
	
	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testRetrieveMetadata() throws MalformedURLException {
		User user = UserClient.download(new URL(TEST_SERVER + "user/ab7f235ccd"));
		Assert.assertNotNull(user);
		// FIXME: test loaded metadata
	}
	
	@Test
	public void testList() {
		List<URL> users = UserClient.list(TEST_SERVER);
		Assert.assertNotNull(users);
		Assert.assertNotSame(0, users.size());
	}
	
	@Test
	public void testListProtocols() throws MalformedURLException {
		User user = new User(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<URL> protocols = UserClient.listProtocols(user);
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}
	
	@Test
	public void testGetProtocols() throws MalformedURLException {
		User user = new User(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<Protocol> protocols = UserClient.getProtocols(user);
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}

	@Test
	public void testListStudies() throws MalformedURLException {
		User user = new User(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<URL> studies = UserClient.listStudies(user);
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}
	
	@Test
	public void testGetStudies() throws MalformedURLException {
		User user = new User(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<Study> studies = UserClient.getStudies(user);
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}

	@Test
	public void testListAlerts() throws MalformedURLException {
		User user = new User(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<URL> alerts = UserClient.listAlerts(user);
		Assert.assertNotNull(alerts);
		Assert.assertNotSame(0, alerts.size());
	}
	
	@Test
	public void testGetAlerts() throws MalformedURLException {
		User user = new User(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<Alert> alerts = UserClient.getAlerts(user);
		Assert.assertNotNull(alerts);
		Assert.assertNotSame(0, alerts.size());
	}
}
