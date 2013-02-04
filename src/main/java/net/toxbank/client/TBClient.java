package net.toxbank.client;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.policy.AccessRights;
import net.toxbank.client.policy.GroupPolicyRule;
import net.toxbank.client.policy.PolicyRule;
import net.toxbank.client.policy.TBPolicyParser;
import net.toxbank.client.policy.UserPolicyRule;
import net.toxbank.client.resource.AlertClient;
import net.toxbank.client.resource.Group;
import net.toxbank.client.resource.InvestigationClient;
import net.toxbank.client.resource.OrganisationClient;
import net.toxbank.client.resource.ProjectClient;
import net.toxbank.client.resource.ProtocolClient;
import net.toxbank.client.resource.SearchClient;
import net.toxbank.client.resource.User;
import net.toxbank.client.resource.UserClient;
import net.toxbank.client.task.TaskClient;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
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

	private ClientConnectionManager createFullyTrustingClientManager() throws Exception {
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {                
            }
            public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
    };
    SSLContext sslContext = SSLContext.getInstance("SSL");
    sslContext.init(null, trustAllCerts, new SecureRandom());
    
    SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext, new X509HostnameVerifier() {
      public void verify(String host, SSLSocket ssl) throws IOException { }
      public void verify(String host, X509Certificate cert) throws SSLException { }
      public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException { }
      public boolean verify(String arg0, SSLSession arg1) {
        return true;
      }
    });
    Scheme httpsScheme = new Scheme("https", 443, sslSocketFactory);
    Scheme httpScheme = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(httpsScheme);
    schemeRegistry.register(httpScheme);
    
    ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);

    return cm;
	}
	
	protected HttpClient createHTTPClient() {
	  try {
	    ClientConnectionManager cm = createFullyTrustingClientManager();
	    DefaultHttpClient cli = new DefaultHttpClient(cm);
	    cli.addRequestInterceptor(new HttpRequestInterceptor() {
			@Override
			public void process(HttpRequest request, HttpContext context)
			    throws HttpException, IOException {
				  if (ssoToken != null)
				    request.addHeader("subjectid",ssoToken.getToken());
			}
	    });
	    return cli;
	  }
	  catch (Exception e) {
	    e.printStackTrace();
	    return null;
	  }
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
	
	public SearchClient getSearchClient() {
		return new SearchClient(getHttpClient());
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
	public AlertClient getAlertClient() {
		return new AlertClient(getHttpClient());
	}
	public InvestigationClient getInvestigationClient() {
	  return new InvestigationClient(getHttpClient());
	}
	
	public TaskClient getTaskClient() {
		return new TaskClient(getHttpClient());
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
	 * Deletes a policy, give its policyID (in accessRights.getPolicyID() )
	 * @param url
	 * @param accessRights
	 * @return HTTP status code
	 * @throws Exception
	 */
	public int deletePolicy(AccessRights accessRights) throws Exception {
		return deletePolicy(accessRights==null?null:accessRights.getPolicyID());
	}
	/**
	 * Deletes a policy, given its policyID
	 * @param url
	 * @param policyID
	 * @return HTTP status code
	 * @throws Exception
	 */
	public int deletePolicy(String policyID) throws Exception {
		if (policyID!=null) {
			OpenSSOPolicy policy = getOpenSSOPolicyInstance();
			return policy.deletePolicy(ssoToken, policyID);
		} else throw new Exception("No policy ID!");
	}
	/**
	 * Deletes all policies for an URI
	 * @param url
	 * @throws Exception
	 */
	public void deleteAllPolicies(URL url) throws Exception {
		OpenSSOPolicy policy = getOpenSSOPolicyInstance();
		IOpenToxUser user = new OpenToxUser();
		
		Hashtable<String, String> policies = new Hashtable<String, String>();
		int status = policy.getURIOwner(ssoToken, url.toExternalForm(), user, policies);
		if (HttpStatus.SC_OK == status) {
			Enumeration<String> e = policies.keys();
			while (e.hasMoreElements()) {
				String policyID = e.nextElement();
				deletePolicy(policyID);
			}
		} //else throw new RestException(status);
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
	
	public void updatePolicy(AccessRights accessRights) throws Exception {
		if ((accessRights==null) || (accessRights.getResource()==null) || (accessRights.getRules()==null)) throw new InvalidInputException("Policy");
		deleteAllPolicies(accessRights.getResource());
		//then send the new policy
		sendPolicy(accessRights);
		
	}
	public void sendPolicy(AccessRights accessRights) throws Exception {
		if ((accessRights==null) || (accessRights.getResource()==null) || (accessRights.getRules()==null)) throw new InvalidInputException("Policy");
		OpenSSOPolicy policy = getOpenSSOPolicyInstance();
		List<String> xmls = createPolicyXML(accessRights);
		for (String xml : xmls) try {
			int status = policy.sendPolicy(ssoToken, xml);
			if (HttpStatus.SC_OK!=status) 
				throw new RestException(status,String.format("Error when creating policy for URI %s",accessRights.getResource()));
		} catch (RestException x) {
			throw x;
		} catch (Exception x) {
			throw new Exception(String.format("Error when creating policy for URI %s",accessRights.getResource()),x);
		}

	}
	public List<String> createPolicyXML(AccessRights accessRights) throws Exception {
		OpenSSOPolicy policy = getOpenSSOPolicyInstance();
		String xmlpolicy = null;
		if ((accessRights.getResource()!=null) && (accessRights.getRules()!=null)) {
			List<String> xmlpolicies = new ArrayList<String>();
			for (PolicyRule rule: accessRights.getRules()) {
				xmlpolicy = null;
				if (rule instanceof UserPolicyRule) {
					UserPolicyRule<? extends User> userRule = (UserPolicyRule) rule;
					xmlpolicy = policy.createUserPolicyXML(
									userRule.getSubject().getUserName(),accessRights.getResource(),userRule.getActionsAsArray());
				} else if (rule instanceof GroupPolicyRule) {
					GroupPolicyRule<? extends Group> groupRule = (GroupPolicyRule) rule;
					xmlpolicy = policy.createGroupPolicyXML(
							groupRule.getSubject().getGroupName(),accessRights.getResource(),groupRule.getActionsAsArray());
				}
				if (xmlpolicy!=null) xmlpolicies.add(xmlpolicy);
			}
			return xmlpolicies;
		}
		return null;		
	}
	
  /**
   * Attempts to get a user-service user for the currently logged in user
   * @param userServerURL the root url of the user service
   * @return the user that is logged in - null if not logged in or if the user does not exist in the service
   */
  public User getLoggedInUser(String userServerURL) throws Exception {
    if (ssoToken == null) {
      return null;
    }	  
    Hashtable<String, String> results = new Hashtable<String, String>();
    if (!ssoToken.getAttributes(new String[] {"uid"},results)) {
      return null;
    }
    
    String username = results.get("uid");
    URL userUrl = new URL(userServerURL + "?username=" + URLEncoder.encode(username, "UTF-8"));
    List<User> userResult = getUserClient().get(userUrl);
    if (userResult.size() == 0) {
      return null;
    }
    else {
      return userResult.get(0);
    }
  }
  
  /**
   * Gets the full user object for the given user name
   * @param userServerURL root url of the server
   * @param username the user name of interest
   * @return the filled in user
   * @throws Exception
   */
  public User getUserByUsername(String userServerURL, String username) throws Exception {
    URL userUrl = new URL(userServerURL + "?username=" + URLEncoder.encode(username, "UTF-8"));
    List<User> userResult = getUserClient().get(userUrl);
    if (userResult.size() == 0) {
      return null;
    }
    else {
      return userResult.get(0);
    }
  }
}
