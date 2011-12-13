package net.toxbank.client.resource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractClientTest<T extends IToxBankResource, C extends AbstractClient<T>> {
	public final static String TEST_SERVER = "http://toxbanktest1.opentox.org:8080/toxbank";

	@BeforeClass
	public static void setup() {
		
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
