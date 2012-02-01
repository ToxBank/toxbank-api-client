package net.toxbank.client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.toxbank.client.policy.AccessRights;
import net.toxbank.client.policy.TBPolicyParser;
import net.toxbank.client.resource.OrganisationClient;
import net.toxbank.client.resource.ProjectClient;
import net.toxbank.client.resource.ProtocolClient;
import net.toxbank.client.resource.UserClient;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.opentox.aa.IOpenToxUser;
import org.opentox.aa.OpenToxUser;
import org.opentox.aa.opensso.OpenSSOPolicy;
import org.opentox.aa.opensso.OpenSSOToken;
import org.opentox.rest.RestException;

/**
 * Top level ToxBank API client.
 * @author nina
 *
 */
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

	
	/**
	 *  Returns true if authorized
	 * @param uri
	 * @param httpAction
	 * @return
	 * @throws Exception
	 */
	public boolean authorize(URL uri, String httpAction) throws Exception {
		return ssoToken.authorize(uri.toString(), httpAction);
	}
	
	/**
	 * Returns true if post is allowed
	 * @param protocolURI  expects  "http://host/protocol"
	 * @return
	 * @throws Exception
	 */
	public boolean isProtocolUploadAllowed(URL protocolURI) throws Exception {
		return authorize(protocolURI, "POST");
	}
	
	public Hashtable<String, String> getUserAttributes() throws Exception {
		Hashtable<String, String> results = new Hashtable<String, String>();
		ssoToken.getAttributes(null,results);
		return results;
	}
	
	protected static OpenSSOPolicy getOpenSSOPolicyInstance() throws Exception {
		//TODO get form config
		return new OpenSSOPolicy("http://opensso.in-silico.ch/Pol/opensso-pol");
	}
	/**
	 * 
	 * @param url
	 * @param accessRights
	 * @return HTTP status code
	 * @throws Exception
	 */
	public int deletePolicy(URL url, AccessRights accessRights) throws Exception {
		
		OpenSSOPolicy policy = getOpenSSOPolicyInstance();
		if (accessRights.getPolicyID()!=null)
			return policy.deletePolicy(ssoToken, accessRights.getPolicyID());
		else throw new Exception("No policy ID!");
	}
	
	public List<AccessRights> readPolicy(URL url) throws Exception {
		OpenSSOPolicy policy = getOpenSSOPolicyInstance();
		IOpenToxUser user = new OpenToxUser();
		
		List<AccessRights> lotofpolicies = new ArrayList<AccessRights>();
		Hashtable<String, String> policies = new Hashtable<String, String>();
		int status = policy.getURIOwner(ssoToken, url.toExternalForm(), user, policies);
		if (HttpStatus.SC_OK == status) {
			Enumeration<String> e = policies.keys();
			while (e.hasMoreElements()) {
				String policyId = e.nextElement();
				status = policy.listPolicy(ssoToken, policyId, policies);
				if (HttpStatus.SC_OK == status) {
					String xml = policies.get(policyId);
					TBPolicyParser parser = new TBPolicyParser(xml);
					AccessRights accessRights = parser.getAccessRights();
					lotofpolicies.add(accessRights);
				} else throw new RestException(status);
			}
		} else throw new RestException(status);
		return lotofpolicies;
	}
	
}
