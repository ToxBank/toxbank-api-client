package net.toxbank.client.resource;

import java.net.URL;
import java.util.List;

import net.toxbank.client.policy.AccessRights;

import org.junit.Assert;
import org.junit.Test;
import org.opentox.aa.policy.Method;

public class PolicyTest extends  AbstractClientTest<Protocol, ProtocolClient> {

	@Override
	protected ProtocolClient getToxBackClient() {
		return tbclient.getProtocolClient();
	}


	
	@Test
	public void testRead() throws Exception {
		UserClient cli = tbclient.getUserClient();
		User user = cli.myAccount(new URL(TEST_SERVER));
		ProtocolClient pcli = getToxBackClient();
		List<URL> url = pcli.listProtocols(user);
		Assert.assertTrue(url.size()>0);
		List<AccessRights> rights = tbclient.readPolicy(url.get(0));
		Assert.assertTrue(rights.size()>0);
		Assert.assertTrue(rights.get(0).getRules().get(0).allows("GET"));
		Assert.assertTrue(rights.get(0).getRules().get(0).allows("POST"));
		Assert.assertTrue(rights.get(0).getRules().get(0).allows("PUT"));
		Assert.assertTrue(rights.get(0).getRules().get(0).allows("DELETE"));
		Assert.assertNotNull(rights.get(0).getPolicyID());
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
