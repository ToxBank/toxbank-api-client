package net.toxbank.client.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.InvestigationIO;
import net.toxbank.client.io.rdf.TOXBANK_ISA;
import net.toxbank.client.policy.GroupPolicyRule;
import net.toxbank.client.policy.PolicyRule;
import net.toxbank.client.policy.UserPolicyRule;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.opentox.aa.policy.Method;
import org.opentox.rest.RestException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A client for accessing investigation resources. This does not inherit from AbstractClient as
 * the investigation service operates significantly different from the other services. 
 */
public class InvestigationClient {
  protected static final Charset utf8 = Charset.forName("UTF-8");
  protected static final String mime_rdfxml = "application/rdf+xml";  
  protected static final String query_param = "query";
  protected static final String file_param = "file";
  protected static final String published_param = "published";
  
  private Writer queryDebuggingWriter;
  
  protected HttpClient httpClient;
  
  public HttpClient getHttpClient() throws IOException {
    if (httpClient==null) throw new IOException("No HttpClient!"); 
    return httpClient;
  }
  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public InvestigationClient() {
    this(null);
  }
    
  public InvestigationClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }
  
  IOClass<Investigation> getIOClass() {
    return new InvestigationIO();
  }
 
  public void setQueryDebuggingWriter(Writer queryDebuggingWriter) {
    this.queryDebuggingWriter = queryDebuggingWriter;
  }
  
  public Writer getQueryDebuggingWriter() {
    return queryDebuggingWriter;
  }
    
  /**
   * Lists the uris of investigations loaded (owned) by the given user
   * @param user the owner of the investigations
   * @return the list of available investigation urls
   */
  public List<URL> listInvestigationUrls(URL rootUrl, User user) throws Exception {
    List<URL> urls = new ArrayList<URL>();
    Map<String, String> additionalHeaders = new HashMap<String, String>();
    additionalHeaders.put("user", user.getResourceURL().toString());
    Model model = requestToModel(rootUrl);
    for (ResIterator iter = model.listResourcesWithProperty(RDF.type, TOXBANK_ISA.INVESTIGATION); iter.hasNext(); ) {
      Resource res = iter.next();
      urls.add(new URL(res.getURI()));
    }
    return urls;
  }
  
  /**
   * Lists the urls of investigations in the given investigation service
   * @param rootUrl the root url of the investigation service
   * @return the list of available investigation urls
   * @throws Exception
   */
  public List<URL> listInvestigationUrls(URL rootUrl) throws Exception {
    HttpGet httpGet = new HttpGet(rootUrl.toString());
    httpGet.addHeader("Accept", "text/uri-list");
    httpGet.addHeader("Accept-Charset", "utf-8");
    
    InputStream in = null;
    try {
      HttpResponse response = getHttpClient().execute(httpGet);
      HttpEntity entity  = response.getEntity();
      in = entity.getContent();
      if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
        List<URL> investigationUrls = new ArrayList<URL>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          line = line.trim();
          if (line.length() > 0) {
            URL url = new URL(line);
            investigationUrls.add(url);
          }
        }
        return investigationUrls;
      }
      else if (response.getStatusLine().getStatusCode()== HttpStatus.SC_NOT_FOUND) {
        return Collections.emptyList();
      }
      else {
        throw new RestException(response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase());
      }
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (Exception e) { }
      }
    }
  }
  
  private static Pattern investigationUrlPattern = Pattern.compile("(.*)/([0-9\\-a-f]+)");
  
  /**
   * Creates an investigation meta data object associated with the given url
   * @param investigationUrl the url of the investigation to query
   * @return the investigation meta data
   * @throws Exception
   */
  public Investigation getInvestigation(URL investigationUrl) throws Exception {
    String urlString = investigationUrl.toString();
    Matcher matcher = investigationUrlPattern.matcher(urlString.toString());
    if (!matcher.matches()) {
      throw new RuntimeException("Invalid investigation url: " + urlString);
    }
    String investigationId = matcher.group(2);
    String rootUrl = matcher.group(1);
    String seuratId = "SEURAT-Investigation-" + investigationId;
    
    Model model = ModelFactory.createDefaultModel();
    requestIntoModel(new URL(rootUrl + "/" + investigationId + "/metadata"), model);
    requestIntoModel(new URL(rootUrl + "/" + investigationId + "/protocol"), model);        
            
    List<Investigation> investigations = getIOClass().fromJena(model);
    if (investigations.size() == 0) {
      return null;
    }
    else if (investigations.size() > 1) {
      throw new RuntimeException("URL: " + investigationUrl + " yielded more than one investigation");
    }
    else {
      Investigation investigation = investigations.get(0);
      investigation.setSeuratId(seuratId);
      return investigation;
    }
  }
  
  /**
   * Runs the given sparql query, which is assumed to have a subject variable ?s, a
   * predicate variable ?p and an object variable ?o. The results are then loaded into
   * a newly created model and returned
   * @param sparqlQuery a query containing variables s, p, and o
   * @param rootUrl the root url where the query should be performed
   * @return a new model populated with the results of the query
   * @throws Exception
   */
  public Model querySparql(String sparqlQuery, URL rootUrl) throws Exception {
    Model model = ModelFactory.createDefaultModel();
    querySparqlIntoModel(sparqlQuery, rootUrl, model);
    return model;
  }
    
  /**
   * Runs the given sparql query, which is assumed to have a subject variable ?s, a
   * predicate variable ?p and an object variable ?o. The results are then loaded into
   * the provided model
   * @param sparqlQuery a query containing variables s, p, and o
   * @param rootUrl the root url where the query should be performed
   * @param model the model to populated with the results of the query
   * @throws Exception
   */
  public void querySparqlIntoModel(String sparqlQuery, URL rootUrl, Model model) throws Exception {
    String encodedQuery = URLEncoder.encode(sparqlQuery, "UTF-8");
    String queryUrl = rootUrl.toString() + "/?" + query_param + "=" + encodedQuery;
    
    HttpGet httpGet = new HttpGet(queryUrl);
    httpGet.addHeader("Accept", mime_rdfxml);
    httpGet.addHeader("Accept-Charset", "utf-8");

    if (queryDebuggingWriter != null) {
      queryDebuggingWriter.write("--------------------\n");
      queryDebuggingWriter.write(sparqlQuery);
      queryDebuggingWriter.write("\n---\n");
    }
    
    InputStream in = null;
    try {
      HttpResponse response = getHttpClient().execute(httpGet);
      HttpEntity entity  = response.getEntity();
      in = entity.getContent();
      if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          sb.append(line);
          sb.append("\n");
        }
        String result = sb.toString();
        if (queryDebuggingWriter != null) {
          queryDebuggingWriter.write(result);
          queryDebuggingWriter.write("\n---------------------\n");
        }
        model.read(new StringReader(result), rootUrl.toString(), "RDF/XML");
      }
      else {
        throw new RestException(response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase());
      }
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (Exception e) { }
      }
    }    
  }

  /**
   * Runs the given request. The results are then loaded into
   * a newly created model and returned
   * @param url the url where the query should be performed
   * @return a new model populated with the results of the query
   * @throws Exception
   */
  public Model requestToModel(URL url) throws Exception {
    Model model = ModelFactory.createDefaultModel();
    requestIntoModel(url, model);
    return model;
  }
  
  /**
   * Runs the given request. The results are then loaded into
   * a newly created model and returned
   * @param url the url where the query should be performed
   * @param additionalHeaders additional headers that should be included in the request
   * @return a new model populated with the results of the query
   * @throws Exception
   */
  public Model requestToModel(URL url, Map<String, String> additionalHeaders) throws Exception {
    Model model = ModelFactory.createDefaultModel();
    requestIntoModel(url, model, additionalHeaders);
    return model;
  }
  
  /**
   * Runs the given request, and the results are then loaded into
   * the provided model
   * @param url the url which will provide the rdf data
   * @param model the model to populated with the results of the query
   * @throws Exception
   */
  public void requestIntoModel(URL url, Model model) throws Exception {
    requestIntoModel(url, model, new HashMap<String, String>(0));
  }
  
  /**
   * Runs the given request, and the results are then loaded into
   * the provided model
   * @param url the url which will provide the rdf data
   * @param model the model to populated with the results of the query
   * @param additionalHeaders additional headers that should be included in the request
   * @throws Exception
   */
  public void requestIntoModel(URL url, Model model, Map<String, String> additionalHeaders) throws Exception {    
    HttpGet httpGet = new HttpGet(url.toString());
    httpGet.addHeader("Accept", mime_rdfxml);
    httpGet.addHeader("Accept-Charset", "utf-8");
    for (String header : additionalHeaders.keySet()) {
      httpGet.addHeader(header, additionalHeaders.get(header));
    }

    InputStream in = null;
    try {
      HttpResponse response = getHttpClient().execute(httpGet);
      HttpEntity entity  = response.getEntity();
      in = entity.getContent();
      if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          sb.append(line);
          sb.append("\n");
        }
        String result = sb.toString();
        if (queryDebuggingWriter != null) {
          queryDebuggingWriter.write(result);
          queryDebuggingWriter.write("\n---------------------\n");
        }
        model.read(new StringReader(result), url.toString(), "RDF/XML");
      }
      else {
        throw new RestException(response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase());
      }
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (Exception e) { }
      }
    }    
  }

  /**
   * Deletes the investigation at the given url
   * @param url the url of the investigation to delete 
   */
  public void deleteInvestigation(URL url) throws Exception {
    HttpDelete httpDelete = new HttpDelete(url.toString());
    httpDelete.addHeader("Accept", "text/plain");
    httpDelete.addHeader("Accept-Charset", "utf-8");
    
    InputStream in = null;
    try {
      HttpResponse response = getHttpClient().execute(httpDelete);
      HttpEntity entity  = response.getEntity();
      in = entity.getContent();
      if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) { 
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          // do something with message? SC_OK should mean success
        }
      }
      else {
        throw new RestException(response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase());
      }
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (Exception e) { }
      }
    }
  }
  
  /**
   * Posts the investigation as a new version
   * @param zipFile the investigation zip file
   * @param rootUrl the root url of the service
   * @return the remote task created
   */
  public RemoteTask postInvestigation(File zipFile, URL rootUrl, List<PolicyRule> accessRights) throws Exception {
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, utf8);
    entity.addPart(file_param, new FileBody(zipFile, zipFile.getName(), "application/zip", null));
    AbstractClient.addPolicyRules(entity, accessRights);
    RemoteTask task = new RemoteTask(getHttpClient(), rootUrl, "text/uri-list", entity, HttpPost.METHOD_NAME);
    return task;
  }
  
  /**
   * Sets the published status for an investigation
   * @param investigation the investigation to update - only url is used
   * @param published true iff the investigation should be published
   */
  public RemoteTask publishInvestigation(Investigation investigation, boolean published) throws Exception {
    if (investigation.getResourceURL() == null) {
      throw new IllegalArgumentException("investigation has not been assigned a resource url");
    }
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, utf8);
    entity.addPart(published_param, new StringBody(String.valueOf(published)));
    RemoteTask task = new RemoteTask(getHttpClient(), investigation.getResourceURL(), "text/uri-list", entity, HttpPut.METHOD_NAME);
    return task;
  }

  /**
   * Updates an investigation at the given url
   * @param zipFile the new investigation zip file - null indicates that the zip file should remain unchanged
   * @param investigation the investigation object to update
   * @return the remote task created
   */
  public RemoteTask updateInvestigation(File zipFile, Investigation investigation, List<PolicyRule> accessRights) throws Exception {
    if (investigation.getResourceURL() == null) {
      throw new IllegalArgumentException("investigation has not been assigned a resource url");
    }
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, utf8);
    if (zipFile != null) {
      entity.addPart(file_param, new FileBody(zipFile, zipFile.getName(), "application/zip", null));
    }
    
    addPolicyRules(entity, accessRights);

    if (investigation.isPublished() != null) {
      entity.addPart(published_param, new StringBody(investigation.isPublished().toString()));
    }
    RemoteTask task = new RemoteTask(getHttpClient(), investigation.getResourceURL(), "text/uri-list", entity, HttpPut.METHOD_NAME);
    return task;
  }
  
  public void addPolicyRules(MultipartEntity entity, List<PolicyRule> accessRights) throws Exception {
    HashSet<String> emptyFields = new HashSet<String>();
    
    for (Method method : Method.values()) {
      for (Boolean allows : Arrays.asList(Boolean.TRUE, Boolean.FALSE)) {
        String userField = AbstractClient.getPolicyRuleWebField(new User(),method,allows);
        String groupField = AbstractClient.getPolicyRuleWebField(new Group(),method,allows);
        emptyFields.add(userField);
        emptyFields.add(groupField);
      }
    }
        
    if (accessRights != null) {
      for (PolicyRule rule : accessRights) {
        for (Method method: Method.values()) {
          Boolean allows = rule.allows(method.name());
          if (allows==null) continue;
          String field = null;
          if (rule instanceof  UserPolicyRule) 
            field = AbstractClient.getPolicyRuleWebField((User)rule.getSubject(),method,allows); 
          else if (rule instanceof GroupPolicyRule) 
            field = AbstractClient.getPolicyRuleWebField((Group)rule.getSubject(),method,allows);
          if (field==null) continue;
          entity.addPart(field, new StringBody(rule.getSubject().getResourceURL().toExternalForm(),utf8));
          emptyFields.remove(field);
        }
      }
    }
    
    // add placeholders for fields that were not defined to ensure the investigation service
    // deletes any old ones
    for (String field : emptyFields) {
      entity.addPart(field, new StringBody("",utf8));
    }
  }
  
  /**
   * Updates an investigation at the given url without changing the file
   * @param zipFile the new investigation zip file
   * @param investigation the investigation object to update
   * @return the remote task created
   */
  public RemoteTask updateInvestigation(Investigation investigation, List<PolicyRule> accessRights) throws Exception {
    return updateInvestigation(null, investigation, accessRights);
  }
}
