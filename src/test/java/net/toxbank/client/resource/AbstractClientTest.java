package net.toxbank.client.resource;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractClientTest<T extends IToxBankResource, C extends AbstractClient<T>> {
	//public final static String TEST_SERVER = "http://toxbanktest1.opentox.org:8080/toxbank";
	public final static String TEST_SERVER = config();
	//should be configured in the .m2/settings.xml 
	protected static final String test_server_property = "toxbank.test.server";
	@BeforeClass
	public static void setup() {
		System.out.println(TEST_SERVER);
	}
	
	public static String config()  {
		String local = "http://localhost:8080/toxbank";
		try {
			Properties p = new Properties();
			p.load(AbstractClientTest.class.getClassLoader().getResourceAsStream("net/toxbank/client/test/client.properties"));
			String testServer = p.getProperty(test_server_property);
			return testServer!=null?testServer.startsWith("http")?testServer:local:local;
		} catch (Exception x) {
			return local;
		}
	}
	
	@AfterClass
	public static void teardown() {
		
	}
	
	protected abstract C getToxBackClient();
	
	
	@Test
	public void testRead() throws Exception {
		Assert.fail("Not implemented");
	}
	
	@Test
	public void testDelete() throws Exception {
		Assert.fail("Not implemented");
	}
	
	@Test
	public void testCreate() throws Exception {
		Assert.fail("Not implemented");
	}
	
	@Test
	public void testUpdate() throws Exception {
		Assert.fail("Not implemented");
	}	
	
	@Test
	public void testList() throws Exception {
		Assert.fail("Not implemented");
	}
	
	
}
