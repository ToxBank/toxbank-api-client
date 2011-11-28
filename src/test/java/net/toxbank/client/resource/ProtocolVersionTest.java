package net.toxbank.client.resource;

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProtocolVersionTest {

	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testConstructor() {
		ProtocolVersion clazz = new ProtocolVersion();
		Assert.assertNotNull(clazz);
	}

	@Test
	public void testUpload() {
		ProtocolVersion protocol = new ProtocolVersion();
		URL url = protocol.upload(TEST_SERVER);
		Assert.assertNotNull(url);
		Assert.assertTrue(url.toExternalForm().startsWith(TEST_SERVER));
	}

	@Test
	public void testGetSetAbstract() {
		ProtocolVersion version = new ProtocolVersion();
		version.setAbstract("This is the funniest abstract ever!");
		Assert.assertEquals("This is the funniest abstract ever!", version.getAbstract());
	}

	@Test
	public void testRoundtripAbstract() {
		ProtocolVersion version = new ProtocolVersion();
		version.setAbstract("This is the funniest abstract ever!");
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersion roundtripped = new ProtocolVersion(resource);
		Assert.assertEquals("This is the funniest abstract ever!", roundtripped.getAbstract());
	}

	@Test
	public void testRoundtripInfo() {
		ProtocolVersion version = new ProtocolVersion();
		version.setInfo("2011-09-15");
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersion roundtripped = new ProtocolVersion(resource);
		Assert.assertEquals("2011-09-15", roundtripped.getInfo());
	}

	@Test
	public void testRoundtripSubmissionDate() {
		ProtocolVersion version = new ProtocolVersion();
		version.setSubmissionDate("2011-09-15");
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersion roundtripped = new ProtocolVersion(resource);
		Assert.assertEquals("2011-09-15", roundtripped.getSubmissionDate());
	}

	@Test
	public void testRoundtripSearchable() {
		ProtocolVersion version = new ProtocolVersion();
		Assert.assertFalse(version.isSearchable());
		version.setSearchable(true);
		URL resource = version.upload(TEST_SERVER);

		ProtocolVersion roundtripped = new ProtocolVersion(resource);
		Assert.assertTrue(roundtripped.isSearchable());
	}
}
