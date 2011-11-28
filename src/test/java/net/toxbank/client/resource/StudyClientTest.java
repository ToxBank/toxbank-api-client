package net.toxbank.client.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StudyClientTest {
	
	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testConstructor() {
		StudyClient clazz = new StudyClient();
		Assert.assertNotNull(clazz);
	}

	@Test
	public void testRetrieveMetadata() throws MalformedURLException {
		StudyClient study = new StudyClient(new URL(TEST_SERVER + "protocol/1"));
		Assert.assertNotNull(study);
		Assert.assertEquals(
			TEST_SERVER + "organization/1",
			study.getOwner()
		);
	}

	@Test
	public void testList() {
		List<URL> studies = StudyClient.list(TEST_SERVER);
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}

	@Test
	public void testUpload() {
		StudyClient study = new StudyClient();
		URL url = study.upload(TEST_SERVER);
		Assert.assertNotNull(url);
		Assert.assertTrue(url.toExternalForm().startsWith(TEST_SERVER));
	}

	@Test
	public void testListVersions() throws MalformedURLException {
		StudyClient study = new StudyClient(new URL(TEST_SERVER + "protocol/1"));
		List<URL> versions = study.listVersions();
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
	}
	
	@Test
	public void testGetVersions() throws MalformedURLException {
		StudyClient study = new StudyClient(new URL(TEST_SERVER + "protocol/1"));
		List<StudyClient> versions = study.getVersions();
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
		Assert.assertTrue(versions.contains(study));
	}

	@Test
	public void testRoundtripVersions() throws MalformedURLException {
		StudyClient study = new StudyClient(new URL(TEST_SERVER + "protocol/1"));
		URL resource = study.upload(TEST_SERVER);

		StudyClient roundtripped = new StudyClient(resource);
		Assert.assertNotNull(roundtripped);
		Assert.assertNotSame(0, roundtripped.getVersions().size());
		Assert.assertTrue(roundtripped.getVersions().contains(study));
	}

	@Test
	public void testRoundtripKeywords() {
		StudyClient study = new StudyClient();
		Assert.assertEquals(0, study.getKeywords().size());
		study.addKeyword("foo");
		URL resource = study.upload(TEST_SERVER);

		StudyClient roundtripped = new StudyClient(resource);
		Assert.assertEquals(1, roundtripped.getKeywords().size());
		Assert.assertTrue(roundtripped.getKeywords().contains("foo"));
	}

	@Test
	public void testRoundtripAbstract() {
		StudyClient study = new StudyClient();
		Assert.assertNull(study.getAbstract());
		study.setAbstract("This is an abstract");
		URL resource = study.upload(TEST_SERVER);

		StudyClient roundtripped = new StudyClient(resource);
		Assert.assertNotNull(roundtripped.getAbstract());
		Assert.assertEquals(19, roundtripped.getAbstract().length());
	}

	@Test
	public void testRoundtripVersionInfo() {
		StudyClient study = new StudyClient();
		Assert.assertNull(study.getVersionInfo());
		study.setVersionInfo("1");
		URL resource = study.upload(TEST_SERVER);

		StudyClient roundtripped = new StudyClient(resource);
		Assert.assertNotNull(roundtripped.getVersionInfo());
		Assert.assertEquals("1", roundtripped.getVersionInfo());
	}
}
