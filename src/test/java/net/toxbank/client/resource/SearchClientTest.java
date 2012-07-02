package net.toxbank.client.resource;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;


public class SearchClientTest extends AbstractClientTest<Protocol, SearchClient> {
	public final static String TEST_SERVER_SEARCH = configTestServerSearch();
	@Override
	protected SearchClient getToxBackClient() {
		return tbclient.getSearchClient();
	}
	
	public static String configTestServerSearch() {
		try {
			Properties properties = new Properties();
			properties.load(AbstractClientTest.class.getClassLoader().getResourceAsStream("net/toxbank/client/test/client.properties"));
			String testServer = properties.getProperty(test_search_server_property);
			if (testServer==null || !testServer.startsWith("http")) return null;
			return testServer;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	@Override
	public void testList() throws Exception {
		if (TEST_SERVER_SEARCH == null) 
			 throw new Exception("Search server not configured.");
	    System.out.println("Connecting to: " + TEST_SERVER_SEARCH);
		List<URL> protocols = getToxBackClient().searchURI(new URL(String.format("%s%s",TEST_SERVER_SEARCH,"search")),"cell");
		Assert.assertNotNull(protocols);
		Assert.assertNotSame(0, protocols.size());
		for (URL url : protocols) {
			Assert.assertTrue(url.toExternalForm().startsWith("http://toxbanktest1.opentox.org") ||
			    url.toExternalForm().startsWith("https://toxbanktest1.opentox.org"));
		}
	}
	
	@Override
	public void testRead() throws Exception {
	}

	@Override
	public void testCreate() throws Exception {
	}
	
	@Override
	public void testUpdate() throws Exception {
	}
	
	@Override
	public void testDelete() throws Exception {
	}
}
