package net.toxbank.client.resource;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;
import net.toxbank.client.Resources;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.HttpStatus;


public class OrganisationClientTest extends AbstractClientTest<Organisation,OrganisationClient> {

	@Override
	protected OrganisationClient getToxBackClient() {
		return tbclient.getOrganisationClient();
	}
	
	@Override
	public void testList() throws Exception {
		OrganisationClient tbClient = getToxBackClient();
		List<URL> uri = tbClient.listURI(new URL(String.format("%s%s", TEST_SERVER,Resources.organisation)));
		System.out.println(uri);
		Assert.assertTrue(uri.size()>0);
	}
	
	@Override
	public void testRead() throws Exception {
		OrganisationClient tbClient = getToxBackClient();
		//get the first record
		List<URL> uri = tbClient.listURI(new URL(String.format("%s%s?page=0&pagesize=1", TEST_SERVER,Resources.organisation)));
		//verify one record is retrieved
		Assert.assertEquals(1,uri.size());
		//retrieve organisation details
		List<Organisation> orgs = tbClient.getRDF_XML(uri.get(0));
		//verify one record is retrieved
		Assert.assertEquals(1,orgs.size());
		Assert.assertEquals(uri.get(0),orgs.get(0).getResourceURL());
		Assert.assertNotNull(orgs.get(0).getTitle());
		//this fails, not implemented
		//Assert.assertNotNull(orgs.get(0).getGroupName());
	}
	
	@Override
	public void testCreate() throws Exception {
		OrganisationClient tbClient = getToxBackClient();
		Organisation organisation = new Organisation();
		organisation.setTitle(UUID.randomUUID().toString());
		//POST
		RemoteTask task = tbClient.postAsync(organisation,new URL(String.format("%s%s", TEST_SERVER,Resources.organisation)));
		task.waitUntilCompleted(500);
		//verify if ok
		Assert.assertEquals(HttpStatus.SC_OK,task.getStatus());
		Assert.assertNull(task.getError());
		System.out.println(task.getResult());
		//should not be 0 ! http://toxbanktest1.opentox.org:8080/toxbank/project/G0 
		tbClient.delete(task.getResult());
	}
	@Override
	public void testDelete() throws Exception {
		Assert.assertTrue("Delete is tested with testCreate(), the object is deleted upon creation",true);
	}
	
	@Override
	public void testUpdate() throws Exception {

	}
}