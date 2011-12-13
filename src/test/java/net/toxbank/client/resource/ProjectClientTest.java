package net.toxbank.client.resource;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;
import net.toxbank.client.Resources;
import net.toxbank.client.task.RemoteTask;



public class ProjectClientTest extends AbstractClientTest<Project,ProjectClient> {

	@Override
	protected ProjectClient getToxBackClient() {
		return new ProjectClient();
	}
	
	@Override
	public void testList() throws Exception {
		ProjectClient tbClient = getToxBackClient();
		List<URL> uri = tbClient.readURI(new URL(String.format("%s%s", TEST_SERVER,Resources.project)));
		System.out.println(uri);
		Assert.assertTrue(uri.size()>0);
	}
	
	@Override
	public void testRead() throws Exception {
		ProjectClient tbClient = getToxBackClient();
		//get the first record
		List<URL> uri = tbClient.readURI(new URL(String.format("%s%s?page=0&pagesize=1", TEST_SERVER,Resources.project)));
		//verify one record is retrieved
		Assert.assertEquals(1,uri.size());
		//retrieve project details
		List<Project> projects = tbClient.readRDF_XML(uri.get(0));
		//verify one record is retrieved
		Assert.assertEquals(1,projects.size());
		Assert.assertEquals(uri.get(0),projects.get(0).getResourceURL());
		Assert.assertNotNull(projects.get(0).getTitle());
		//this fails, not implemented
		//Assert.assertNotNull(project.get(0).getGroupName());
	}	
	
	@Override
	public void testCreate() throws Exception {
		ProjectClient tbClient = getToxBackClient();
		Project project = new Project();
		project.setTitle(UUID.randomUUID().toString());
		RemoteTask task = tbClient.createAsync(project,new URL(String.format("%s%s", TEST_SERVER,Resources.project)));
		task.waitUntilCompleted(500);
		//verify if ok
		Assert.assertEquals(HttpURLConnection.HTTP_OK,task.getStatus());
		Assert.assertNull(task.getError());
		System.out.println(task.getResult());
		//should not be 0 ! http://toxbanktest1.opentox.org:8080/toxbank/project/G0 
		tbClient.delete(task.getResult());		
	}

	
}
