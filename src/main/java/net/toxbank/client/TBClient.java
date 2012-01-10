package net.toxbank.client;

import java.io.IOException;

import net.toxbank.client.resource.OrganisationClient;
import net.toxbank.client.resource.ProjectClient;
import net.toxbank.client.resource.ProtocolClient;
import net.toxbank.client.resource.ProtocolVersionClient;
import net.toxbank.client.resource.UserClient;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.opentox.aa.opensso.OpenSSOToken;

public class TBClient {
	protected HttpClient httpClient;
	protected OpenSSOToken ssoToken;
	
	public TBClient(OpenSSOToken ssoToken) {
		super();
		this.ssoToken = ssoToken;
	}
	public TBClient() {
		this(null);
		httpClient = createHTTPClient();
	}
	public HttpClient getHttpClient() {
		if (httpClient==null) httpClient = createHTTPClient();
		return httpClient;
	}

	protected HttpClient createHTTPClient() {
		HttpClient cli = new DefaultHttpClient();
		((DefaultHttpClient)cli).addRequestInterceptor(new HttpRequestInterceptor() {
			@Override
			public void process(HttpRequest request, HttpContext context)
					throws HttpException, IOException {
				if (ssoToken != null)
					request.addHeader("subjectid",ssoToken.getToken());
			}
		});
		return cli;
	}
	
	public boolean login(String username,String password) throws Exception {
		//get this from client.properties file
		return login("http://opensso.in-silico.ch/opensso/identity",username,password);
	}
	/**
	 * Login
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public boolean login(String opensso_server,String username,String password) throws Exception {
		if (ssoToken==null) ssoToken = new OpenSSOToken(opensso_server);
		//AAServicesConfig.getSingleton().getOpenSSOService());
		return ssoToken.login(username,password);
	}
	/**
	 * Logout
	 * @throws Exception
	 */
	public void logout() throws Exception {
		if (ssoToken!=null) ssoToken.logout();
	}
	
	
	public void close() throws Exception {
		if (httpClient !=null) {
			httpClient.getConnectionManager().shutdown();
			httpClient = null;
		}
	}
	
	public ProtocolClient getProtocolClient() {
		return new ProtocolClient(getHttpClient());
	}
	
	public ProjectClient getProjectClient() {
		return new ProjectClient(getHttpClient());
	}
	
	public UserClient getUserClient() {
		return new UserClient(getHttpClient());
	}
	public OrganisationClient getOrganisationClient() {
		return new OrganisationClient(getHttpClient());
	}
	
	public ProtocolVersionClient getProtocolVersionClient() {
		return new ProtocolVersionClient(getHttpClient());
	}
	
	
}
