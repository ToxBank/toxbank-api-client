package net.toxbank.client.task;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.TaskIO;
import net.toxbank.client.policy.PolicyRule;
import net.toxbank.client.resource.AbstractClient;
import net.toxbank.client.resource.Task;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.opentox.rest.RestException;


public class TaskClient extends AbstractClient<Task<URL, String>> {

	public TaskClient() {
		this(null);
		
	}
		
	public TaskClient(HttpClient httpclient) {
		super(httpclient);
	}
	public List<URL> listTasks(URL server) throws IOException, RestException {
		return listURI(server);
	}
	public List<URL> listTasks(URL server,String... params) throws IOException, RestException {
		return listURI(server,params);
	}
	
	public Task<URL, String> getTask(URL url) throws Exception {
		List<Task<URL, String>> tasks =  getRDF_XML(url);
		if (tasks!=null && tasks.size()>0) return tasks.get(0);
		else return null;
	}
	@Override
	protected HttpEntity createPOSTEntity(Task<URL, String> object,
			List<PolicyRule> accessRights) throws InvalidInputException,
			Exception {
		throw new Exception("Not implemented. Use RemoteTask class.");
	}
	
	@Override
	protected HttpEntity createPUTEntity(Task<URL, String> object,
			List<PolicyRule> accessRights) throws InvalidInputException,
			Exception {
		throw new Exception("Not supported");
	}

	@Override
	protected IOClass<Task<URL, String>> getIOClass() {
		TaskIO ioclass = new TaskIO();
		return (IOClass<Task<URL, String>>) ioclass;
	}
}