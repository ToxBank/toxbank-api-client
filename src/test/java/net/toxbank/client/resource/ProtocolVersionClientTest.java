package net.toxbank.client.resource;

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProtocolVersionClientTest {

	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testConstructor() {
		ProtocolVersionClient clazz = new ProtocolVersionClient();
		Assert.assertNotNull(clazz);
	}

	@Test
	public void testUpload() {
		ProtocolVersionClient protocol = new ProtocolVersionClient();
		URL url = protocol.upload(TEST_SERVER);
		Assert.assertNotNull(url);
		Assert.assertTrue(url.toExternalForm().startsWith(TEST_SERVER));
	}

	@Test
	public void testGetSetAbstract() {
		ProtocolVersionClient version = new ProtocolVersionClient();
		version.setAbstract("This is the funniest abstract ever!");
		Assert.assertEquals("This is the funniest abstract ever!", version.getAbstract());
	}

	@Test
	public void testRoundtripAbstract() {
		ProtocolVersionClient version = new ProtocolVersionClient();
		version.setAbstract("This is the funniest abstract ever!");
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersionClient roundtripped = new ProtocolVersionClient(resource);
		Assert.assertEquals("This is the funniest abstract ever!", roundtripped.getAbstract());
	}

	@Test
	public void testRoundtripInfo() {
		ProtocolVersionClient version = new ProtocolVersionClient();
		version.setInfo("2011-09-15");
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersionClient roundtripped = new ProtocolVersionClient(resource);
		Assert.assertEquals("2011-09-15", roundtripped.getInfo());
	}

	@Test
	public void testRoundtripSubmissionDate() {
		ProtocolVersionClient version = new ProtocolVersionClient();
		version.setSubmissionDate("2011-09-15");
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersionClient roundtripped = new ProtocolVersionClient(resource);
		Assert.assertEquals("2011-09-15", roundtripped.getSubmissionDate());
	}

	@Test
	public void testRoundtripSearchable() {
		ProtocolVersionClient version = new ProtocolVersionClient();
		Assert.assertFalse(version.isSearchable());
		version.setSearchable(true);
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersionClient roundtripped = new ProtocolVersionClient(resource);
		Assert.assertTrue(roundtripped.isSearchable());
	}
}
