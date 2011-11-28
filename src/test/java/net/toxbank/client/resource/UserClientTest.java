package net.toxbank.client.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class UserClientTest {
	
	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testConstructor() {
		UserClient clazz = new UserClient();
		Assert.assertNotNull(clazz);
	}

	@Test
	public void testRetrieveMetadata() throws MalformedURLException {
		UserClient user = new UserClient(new URL(TEST_SERVER + "user/ab7f235ccd"));
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
		UserClient protocol = new UserClient(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<URL> protocols = protocol.listProtocols();
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}
	
	@Test
	public void testGetProtocols() throws MalformedURLException {
		UserClient protocol = new UserClient(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<ProtocolClient> protocols = protocol.getProtocols();
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
	}

	@Test
	public void testListStudies() throws MalformedURLException {
		UserClient protocol = new UserClient(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<URL> studies = protocol.listStudies();
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}
	
	@Test
	public void testGetStudies() throws MalformedURLException {
		UserClient protocol = new UserClient(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<StudyClient> studies = protocol.getStudies();
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}

	@Test
	public void testListAlerts() throws MalformedURLException {
		UserClient protocol = new UserClient(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<URL> alerts = protocol.listAlerts();
		Assert.assertNotNull(alerts);
		Assert.assertNotSame(0, alerts.size());
	}
	
	@Test
	public void testGetAlerts() throws MalformedURLException {
		UserClient protocol = new UserClient(new URL(TEST_SERVER + "user/ab7f235ccd"));
		List<AlertClient> alerts = protocol.getAlerts();
		Assert.assertNotNull(alerts);
		Assert.assertNotSame(0, alerts.size());
	}
}
