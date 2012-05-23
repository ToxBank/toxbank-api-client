package net.toxbank.client.resource;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toxbank.client.io.rdf.*;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.opentox.rest.RestException;

import com.hp.hpl.jena.rdf.model.*;
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
 
  /**
   * Lists the uris of investigations in the given investigation service
   * @param rootUrl the root url of the investigation service
   * @return the list of available investigation urls
   * @throws Exception
   */
  public List<String> listInvestigationFullUrls(URL rootUrl) throws Exception {
    String sparqlQuery = 
        "PREFIX tb:<http://onto.toxbank.net/api/>\n"+
        "PREFIX isa:<http://onto.toxbank.net/isa/>\n" +
        "PREFIX dcterms:<http://purl.org/dc/terms/>\n" + 
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "CONSTRUCT {?investigation rdf:type isa:Investigation.}\n" +
        "where {\n" +
        " ?investigation rdf:type isa:Investigation.\n" +
        "}";
    
    List<String> uris = new ArrayList<String>();
    Model model = querySparql(sparqlQuery, rootUrl);
    for (ResIterator iter = model.listResourcesWithProperty(RDF.type, TOXBANK_ISA.INVESTIGATION); iter.hasNext(); ) {
      Resource res = iter.next();
      uris.add(res.getURI());
    }
    return uris;
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
          URL url = new URL(line);
          investigationUrls.add(url);
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
  
  private static Pattern investigationUrlPattern = Pattern.compile("(.*)/([0-9]+)");
  
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
    String rootUrl = matcher.group(1);
    
    Model model = ModelFactory.createDefaultModel();
    
    String investigationQuery = String.format(
        "PREFIX : <%s/>\n" +
        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
        "CONSTRUCT {\n" +
        "  ?investigation ?investigation_pred ?investigation_obj .\n" +
        "}\n" +
        "FROM <%s>\n" +
        "WHERE {\n" +
        "  : owl:sameAs ?investigation .\n"+
        "  ?investigation ?investigation_pred ?investigation_obj . \n"+
        "}\n",
        investigationUrl.toString(),
        investigationUrl.toString());
    querySparqlIntoModel(investigationQuery, new URL(rootUrl), model);
    
    String protocolQuery = String.format(
        "PREFIX : <%s/>\n" +  
        "PREFIX tb: <%s>\n" +
        "PREFIX isa: <%s>\n" +
        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "CONSTRUCT {\n" +
        "  ?study isa:hasProtocol ?protocol .\n" +
        "  ?protocol rdf:type tb:Protocol .\n" + 
        "}\n" +
        "FROM <%s>\n" +
        "WHERE {\n" +
        "  : owl:sameAs ?investigation .\n"+
        "  ?investigation isa:hasStudy ?study . \n"+
        "  ?study isa:hasProtocol ?protocol . \n"+
        "  ?protocol rdf:type tb:Protocol . \n"+
        "}\n",
        investigationUrl.toString(),
        TOXBANK.URI,
        TOXBANK_ISA.URI,
        investigationUrl.toString());
    querySparqlIntoModel(protocolQuery, new URL(rootUrl), model);        
            
    List<Investigation> investigations = getIOClass().fromJena(model);
    if (investigations.size() == 0) {
      return null;
    }
    else if (investigations.size() > 1) {
      throw new RuntimeException("URL: " + investigationUrl + " yielded more than one investigation");
    }
    else {
      return investigations.get(0);
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
    
    InputStream in = null;
    try {
      HttpResponse response = getHttpClient().execute(httpGet);
      HttpEntity entity  = response.getEntity();
      in = entity.getContent();
      if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
        model.read(in, rootUrl.toString(), "RDF/XML");
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
  public RemoteTask postInvestigation(File zipFile, URL rootUrl) throws Exception {
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, utf8);
    entity.addPart(file_param, new FileBody(zipFile, zipFile.getName(), "application/zip", null));
    RemoteTask task = new RemoteTask(getHttpClient(), rootUrl, "text/uri-list", entity, HttpPost.METHOD_NAME);
    return task;
  }
}
