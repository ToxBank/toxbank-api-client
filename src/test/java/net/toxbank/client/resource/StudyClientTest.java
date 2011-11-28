package net.toxbank.client.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StudyClientTest {
	
	private final static String TEST_SERVER = "http://demo.toxbank.net/";

	@Test
	public void testList() {
		List<URL> studies = StudyClient.list(TEST_SERVER);
		Assert.assertNotNull(studies);
		Assert.assertNotSame(0, studies.size());
	}

	@Test
	public void testUpload() throws MalformedURLException {
		Study study = new Study();
		URL url = StudyClient.upload(study, new URL(TEST_SERVER));
		Assert.assertNotNull(url);
		Assert.assertTrue(url.toExternalForm().startsWith(TEST_SERVER));
	}

	@Test
	public void testListVersions() throws MalformedURLException {
		List<URL> versions = StudyClient.listVersions();
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
	}
	
	@Test
	public void testGetVersions() throws MalformedURLException {
		List<Study> versions = StudyClient.getVersions();
		Assert.assertNotNull(versions);
		Assert.assertNotSame(0, versions.size());
	}

	@Test
	public void testRoundtripVersions() throws MalformedURLException {
		Study study = new Study(new URL(TEST_SERVER + "protocol/1"));
		URL resource = StudyClient.upload(study, new URL(TEST_SERVER));

		Study roundtripped = StudyClient.download(resource);
		Assert.assertNotNull(roundtripped);
		Assert.assertNotSame(0, StudyClient.getVersions().size());
		Assert.assertTrue(StudyClient.getVersions().contains(study));
	}

	@Test
	public void testRoundtripKeywords() throws MalformedURLException {
		Study study = new Study();
		Assert.assertEquals(0, study.getKeywords().size());
		study.addKeyword("foo");
		URL resource = StudyClient.upload(study, new URL(TEST_SERVER));

		Study roundtripped = StudyClient.download(resource);
		Assert.assertEquals(1, roundtripped.getKeywords().size());
		Assert.assertTrue(roundtripped.getKeywords().contains("foo"));
	}

	@Test
	public void testRoundtripAbstract() throws MalformedURLException {
		Study study = new Study();
		Assert.assertNull(study.getAbstract());
		study.setAbstract("This is an abstract");
		URL resource = StudyClient.upload(study, new URL(TEST_SERVER));

		Study roundtripped = StudyClient.download(resource);
		Assert.assertNotNull(roundtripped.getAbstract());
		Assert.assertEquals(19, roundtripped.getAbstract().length());
	}

	@Test
	public void testRoundtripVersionInfo() throws MalformedURLException {
		Study study = new Study();
		Assert.assertNull(study.getVersionInfo());
		study.setVersionInfo("1");
		URL resource = StudyClient.upload(study, new URL(TEST_SERVER));

		Study roundtripped = StudyClient.download(resource);
		Assert.assertNotNull(roundtripped.getVersionInfo());
		Assert.assertEquals("1", roundtripped.getVersionInfo());
	}
}
