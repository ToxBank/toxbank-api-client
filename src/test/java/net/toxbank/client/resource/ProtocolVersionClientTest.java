package net.toxbank.client.resource;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class ProtocolVersionClientTest {

	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testUpload() throws MalformedURLException {
		Protocol version = new Protocol();
		URL url = ProtocolVersionClient.upload(version, new URL(TEST_SERVER));
		Assert.assertNotNull(url);
		Assert.assertTrue(url.toExternalForm().startsWith(TEST_SERVER));
	}

	@Test
	public void testRoundtripAbstract() throws MalformedURLException {
		Protocol version = new Protocol();
		version.setAbstract("This is the funniest abstract ever!");
		URL resource = ProtocolVersionClient.upload(version, new URL(TEST_SERVER));

		Protocol roundtripped = ProtocolVersionClient.download(resource);
		Assert.assertEquals("This is the funniest abstract ever!", roundtripped.getAbstract());
	}
	/*
	@Test
	public void testRoundtripInfo() throws MalformedURLException {
		Protocol version = new Protocol();
		version.setInfo("2011-09-15");
		URL resource = ProtocolVersionClient.upload(version, new URL(TEST_SERVER));

		ProtocolVersion roundtripped = ProtocolVersionClient.download(resource);
		Assert.assertEquals("2011-09-15", roundtripped.getInfo());
	}
	*/
	@Test
	public void testRoundtripSubmissionDate() throws MalformedURLException {
		Protocol version = new Protocol();
		version.setSubmissionDate("2011-09-15");
		URL resource = ProtocolVersionClient.upload(version, new URL(TEST_SERVER));

		Protocol roundtripped = ProtocolVersionClient.download(resource);
		Assert.assertEquals("2011-09-15", roundtripped.getSubmissionDate());
	}

	@Test
	public void testRoundtripSearchable() throws MalformedURLException {
		Protocol version = new Protocol();
		Assert.assertFalse(version.isSearchable());
		version.setSearchable(true);
		URL resource = ProtocolVersionClient.upload(version, new URL(TEST_SERVER));

		Protocol roundtripped = ProtocolVersionClient.download(resource);
		Assert.assertTrue(roundtripped.isSearchable());
	}
}
