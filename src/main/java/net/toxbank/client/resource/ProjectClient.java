package net.toxbank.client.resource;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.ProjectIO;

import org.apache.http.client.HttpClient;

/**
 * ToxBank <a href="http://api.toxbank.net/index.php/Project">Project</a> client,
 * implementing REST operations on {@link Project}.
 * @author nina
 *
 */
public class ProjectClient extends AbstractGroupClient<Project> {

	public ProjectClient() {
		this(null);
	}
		
	public ProjectClient(HttpClient httpclient) {
		super(httpclient);
	}
	
	@Override
	IOClass<Project> getIOClass() {
		return new ProjectIO();
	}
	
	
}
