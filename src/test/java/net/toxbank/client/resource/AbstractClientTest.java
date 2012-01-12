package net.toxbank.client.resource;

import java.util.Properties;

import net.toxbank.client.TBClient;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractClientTest<T extends IToxBankResource, C extends AbstractClient<T>> {
	//public final static String TEST_SERVER = "http://toxbanktest1.opentox.org:8080/toxbank";
	public final static String TEST_SERVER = config();
	//should be configured in the .m2/settings.xml 
	protected static final String test_server_property = "toxbank.test.server";
	protected static final String aa_server_property = "toxbank.aa.opensso";
	protected static final String aa_user_property = "toxbank.aa.user";
	protected static final String aa_pass_property = "toxbank.aa.pass";
	protected static Properties properties;
	
	public final static TBClient tbclient = new TBClient();
	@BeforeClass
	public static void setup() throws Exception {
		String username = properties.getProperty(aa_user_property);
		String pass = properties.getProperty(aa_pass_property);
		//ensure maven profile properties are configured and set correctly
		Assert.assertNotNull(username);
		Assert.assertNotNull(pass);
		if (String.format("${%s}",aa_user_property).equals(username) ||
			String.format("${%s}",aa_pass_property).equals(pass)) 
			throw new Exception(String.format("The following properties are not found in the acive Maven profile ${%s} ${%s}",
					aa_user_property,aa_pass_property));
		boolean ok = tbclient.login(username,pass);
		Assert.assertTrue(ok);
	}
	

	@AfterClass
	public static void teardown() throws Exception {
		tbclient.logout();
		tbclient.close();
	}
	public static String config()  {
		String local = "http://localhost:8080/toxbank";
		try {
			properties = new Properties();
			properties.load(AbstractClientTest.class.getClassLoader().getResourceAsStream("net/toxbank/client/test/client.properties"));
			String testServer = properties.getProperty(test_server_property);
			return testServer!=null?testServer.startsWith("http")?testServer:local:local;
		} catch (Exception x) {
			return local;
		}
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
