package net.toxbank.client.resource;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import net.toxbank.client.policy.AccessRights;
import net.toxbank.client.policy.GroupPolicyRule;
import net.toxbank.client.policy.PolicyRule;
import net.toxbank.client.policy.UserPolicyRule;

import org.junit.Assert;
import org.junit.Test;

public class PolicyTest extends  AbstractClientTest<Protocol, ProtocolClient> {

	@Override
	protected ProtocolClient getToxBackClient() {
		return tbclient.getProtocolClient();
	}


	
	@Test
	public void testRead() throws Exception {

	}
	
	@Test
	public void testDelete() throws Exception {
		
	}
	
	@Test
	public void testCreate() throws Exception {
		URL url = new URL(String.format("http://example.com/%s",UUID.randomUUID().toString()));
		AccessRights accessRights = new AccessRights(url);

		accessRights.addUserRule("guest",true,false,null,true);
		accessRights.addGroupRule("member",true,null,null,null);
		tbclient.sendPolicy(accessRights);
		//ok, will be 2 policies, we create one policy per rule so far
		List<AccessRights> roundTrip = tbclient.readPolicy(url);
		Assert.assertNotNull(roundTrip);
		Assert.assertEquals(2,roundTrip.size());
		for (AccessRights rtAccessRights : roundTrip) {
			Assert.assertEquals(url.toExternalForm(),rtAccessRights.getResource().toString());
			Assert.assertNotNull(rtAccessRights.getPolicyID());
			Assert.assertNotNull(rtAccessRights.getRules());
			Assert.assertEquals(1,rtAccessRights.getRules().size());
			PolicyRule rule = rtAccessRights.getRules().get(0);
			if (rule.getSubject() instanceof User) {
				Assert.assertTrue(rule.allowsGET());
				Assert.assertNull(rule.allowsPOST());
				Assert.assertNull(rule.allowsPUT());
				Assert.assertTrue(rule.allowsDELETE());
			} else {
				Assert.assertTrue(rule.allowsGET());
				Assert.assertNull(rule.allowsPOST());
				Assert.assertNull(rule.allowsPUT());
				Assert.assertNull(rule.allowsDELETE());				
			}
		}
		for (AccessRights rtAccessRights : roundTrip) {
			tbclient.deletePolicy(rtAccessRights);
		}

	}
	
	@Test
	public void testUpdate() throws Exception {
		URL url = new URL("http://example.com");
		AccessRights accessRights = new AccessRights(url);
		accessRights.addUserRule("guest",true,false,null,true);
		
		tbclient.updatePolicy(accessRights);
		List<AccessRights> roundTrip = tbclient.readPolicy(url);
		Assert.assertNotNull(roundTrip);
		Assert.assertEquals(1,roundTrip.size());
	}	
	
	@Test
	public void testList() throws Exception {
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
	
	
}
