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
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.io.rdf.InvestigationIO;
import net.toxbank.client.io.rdf.OPENTOX;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.opentox.aa.policy.Method;
import org.opentox.rest.RestException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A client for accessing investigation resources. This does not inherit from AbstractClient as
 * the investigation service operates significantly different from the other services. 
 */
public class InvestigationClient {
  public static enum ValueType {foldChange, qValue, pValue};
  public static enum ComparatorType {below, above};
  
  protected static final Charset utf8 = Charset.forName("UTF-8");
  protected static final String mime_rdfxml = "application/rdf+xml";  
  protected static final String query_param = "query";
  protected static final String file_param = "file";
  protected static final String published_param = "published";
  protected static final String searchable_param = "summarySearchable";
  protected static final String ftp_file_param = "ftpFile";
  protected static final String title_param = "title";
  protected static final String abstract_param = "abstract";
  protected static final String owning_org_param = "owningOrg";
  protected static final String authors_param = "authors";
  protected static final String keywords_param = "keywords";
  protected static final String license_param = "licenses";
  protected static final String data_type_param = "type";
  protected static final String projects_param = "owningPro";
  
  private static List<String> doseFactorNames = Arrays.asList("dose", "concentration");
  private static List<String> timeFactorNames = Arrays.asList("sample timepoint", "duration of exposure", "time of measurement");
  
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
  public List<TimestampedUrl> listTimestampedInvestigations(URL rootUrl, User user) throws Exception {
    List<TimestampedUrl> urls = new ArrayList<TimestampedUrl>();

    JSONObject obj = requestToJson(rootUrl, user);
    if (obj != null) {
      JSONObject results = obj.getJSONObject("results");
      if (results != null) {
        JSONArray investigations = results.getJSONArray("bindings");
        for (int i = 0; i < investigations.length(); i++) {
          JSONObject investigation = investigations.getJSONObject(i);
          JSONObject uri = investigation.getJSONObject("uri");
          JSONObject timestamp = investigation.getJSONObject("updated");
          String uriValue = uri.getString("value");
          if (uriValue.endsWith("/")) {
            uriValue = uriValue.substring(0, uriValue.length()-1);
          }
          long timestampValue = timestamp.getLong("value");
          TimestampedUrl url = new TimestampedUrl(new URL(uriValue), timestampValue);
          urls.add(url);
        }
      }
    }            
    
    return urls;
  }
  
  /**
   * Lists the ftp files that a user has available
   * @param  rootUrl the url of the investigation service
   */
  public List<String> listFtpFilenames(URL rootUrl) throws Exception {
    URL requestUrl = new URL(rootUrl + "/ftpfiles"); 
    List<String> filenames = new ArrayList<String>();
    JSONObject obj = requestToJson(requestUrl, null);
    if (obj != null) {
      JSONObject results = obj.getJSONObject("results");
      if (results != null) {
        JSONArray fileArray = results.getJSONArray("bindings");
        for (int i = 0; i < fileArray.length(); i++) {
          JSONObject fileObj = fileArray.getJSONObject(i);
          JSONObject filenameObj = fileObj.getJSONObject("filename");
          filenames.add(filenameObj.getString("value"));
        }
      }
    }
    return filenames;
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
        handleError(in, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        throw new RuntimeException("handleError should have thrown exception");
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
   * Gets adjunct info for the given investigation url
   * @parma investigationUrl the url of the investigation to query
   * @return the adjunct info
   */
  public AdjunctInvestigationInfo getAdjunctInvestigationInfo(URL investigationUrl) throws Exception {
    String urlString = investigationUrl.toString();
    Matcher matcher = investigationUrlPattern.matcher(urlString.toString());
    if (!matcher.matches()) {
      throw new RuntimeException("Invalid investigation url: " + urlString);
    }
    String investigationId = matcher.group(2);
    String rootUrl = matcher.group(1);
    
    AdjunctInvestigationInfo info = new AdjunctInvestigationInfo(urlString, investigationId);
    
    URL factorsUrl = new URL(rootUrl + "/" + investigationId + "/dashboard");
    JSONArray factorsJson = requestToJsonBindings(factorsUrl, null);
    addSamples(info.getBioSamples(), factorsJson);
        
    URL characteristicsUrl = new URL(rootUrl + "/" + investigationId + "/sparql/characteristics_by_investigation");
    JSONArray characteristicsJson = requestToJsonBindings(characteristicsUrl, null);
    for (int i = 0; i < characteristicsJson.length(); i++) {
      JSONObject characteristicJson = characteristicsJson.getJSONObject(i);
      AdjunctInvestigationDatum characteristic = characteristicFromJson(characteristicJson);
      if (characteristic != null) {
        info.addCharacteristic(characteristic);
      }
    }
    
    URL detailsUrl = new URL(rootUrl + "/" + investigationId + "/sparql/investigation_details");
    JSONArray detailsJson = requestToJsonBindings(detailsUrl, null);
    for (int i = 0; i < detailsJson.length(); i++) {
      JSONObject detailJson = detailsJson.getJSONObject(i);
      AdjunctInvestigationDatum endpoint = labeledDetailFromJson(detailJson, "endpoint");
      if (endpoint != null) {
        info.addDetail(endpoint);
      }
      AdjunctInvestigationDatum technology = labeledDetailFromJson(detailJson, "technology");
      if (technology != null) {
        info.addDetail(technology);
      }
    }

    return info;
  }
  
  private boolean isDoseFactor(AdjunctInvestigationDatum factor) {
    for (String name : doseFactorNames) {
      if (name.equalsIgnoreCase(factor.getName())) {
        return true;
      }
    }
    return false;
  }

  private boolean isTimeFactor(AdjunctInvestigationDatum factor) {
    for (String name : timeFactorNames) {
      if (name.equalsIgnoreCase(factor.getName())) {
        return true;
      }
    }
    return false;
  }

  private void addSamples(InvestigationBioSamples bioSamples, JSONArray factorBindings) throws Exception {
    String bioSampleUri = null;
    String sampleUri = null;
    String compoundUri = null;
    String compoundName = null;
    Float doseValue = null;
    String doseUnits = null;
    Float timeValue = null;
    String timeUnits = null;
    JSONArray characteristicsBySampleJson = null;
    
    Map<String, JSONArray> bioSampleCharacteristics = new HashMap<String, JSONArray>();
    
    for (int i = 0; i < factorBindings.length(); i++) {
      JSONObject factorJson = factorBindings.getJSONObject(i);
      AdjunctInvestigationDatum factor = factorFromJson(factorJson);
      
      JSONArray nextCharacteristics = factorJson.getJSONArray("sampleChar");
      if (characteristicsBySampleJson == null && nextCharacteristics != null && nextCharacteristics.length() > 0) {
        characteristicsBySampleJson = nextCharacteristics;
      }
      
      if (sampleUri == null) {
        sampleUri = factor.getSampleUri();
      }
      else if (!sampleUri.equals(factor.getSampleUri())) {
        JSONArray bioSampleChars = bioSampleCharacteristics.get(bioSampleUri);
        timeUnits = getTimeUnits(timeUnits, bioSampleChars);
        timeUnits = getTimeUnits(timeUnits, characteristicsBySampleJson);
        InvestigationBioSample bioSample = bioSamples.addFactor(
            bioSampleUri,
            compoundUri,
            compoundName,
            sampleUri,
            doseValue,
            doseUnits,
            timeValue,
            timeUnits);
        addCharacteristics(bioSample, bioSampleChars);
        addCharacteristics(bioSample, characteristicsBySampleJson);
        compoundUri = null;
        compoundName = null;
        doseValue = null;
        doseUnits = null;
        timeValue = null;
        timeUnits = null;
        characteristicsBySampleJson = null;
        sampleUri = factor.getSampleUri();
      }
            
      bioSampleUri = factor.getBioSampleUri();
      JSONArray nextBioSampleCharacteristics = factorJson.getJSONArray("characteristics");
      if (nextBioSampleCharacteristics != null && nextBioSampleCharacteristics.length() > 0) {
        bioSampleCharacteristics.put(bioSampleUri, nextBioSampleCharacteristics);
      }
      
      if ("compound".equalsIgnoreCase(factor.getName())) {
        compoundName = factor.getValue();
        compoundUri = factor.getUri();
      }
      else if (isDoseFactor(factor)) {
        if (factor.getValue() != null) {
          doseValue = Float.parseFloat(factor.getValue());
          doseUnits = factor.getUnits();
        }
      }
      else if (isTimeFactor(factor)) {
        if (factor.getValue() != null) {
          timeValue = Float.parseFloat(factor.getValue());
          if (factor.getUnits() != null) {
            timeUnits = factor.getUnits();
          }
        }
      }
    }
    
    if (sampleUri != null) {
      JSONArray bioSampleChars = bioSampleCharacteristics.get(bioSampleUri);
      timeUnits = getTimeUnits(timeUnits, characteristicsBySampleJson);
      timeUnits = getTimeUnits(timeUnits, bioSampleChars);
      InvestigationBioSample bioSample = bioSamples.addFactor(
          bioSampleUri,
          compoundUri,
          compoundName,
          sampleUri,
          doseValue,
          doseUnits,
          timeValue,
          timeUnits);
      addCharacteristics(bioSample, bioSampleChars);
      addCharacteristics(bioSample, characteristicsBySampleJson);
    }
  }
  
  private String getTimeUnits(String currentTimeUnits, JSONArray sampleChars) throws Exception {
    if (sampleChars != null) {
      for (int j = 0; j < sampleChars.length(); j++) {
        JSONObject characteristicJson = sampleChars.getJSONObject(j);
        AdjunctInvestigationDatum characteristic = characteristicFromJson(characteristicJson);
        if ("sample timepointunit".equalsIgnoreCase(characteristic.getName())) {
          return characteristic.getValue();
        }
      }
    }
    return currentTimeUnits;
  }
  
  private void addCharacteristics(InvestigationBioSample bioSample, JSONArray sampleChars) throws Exception {
    if (sampleChars != null) {
      for (int j = 0; j < sampleChars.length(); j++) {
        JSONObject characteristicJson = sampleChars.getJSONObject(j);
        AdjunctInvestigationDatum characteristic = characteristicFromJson(characteristicJson);
        bioSample.addCharacteristic(characteristic);
      }
    }
  }
  
  private AdjunctInvestigationDatum factorFromJson(JSONObject jsonObj) throws Exception {
    String name = jsonObj.getJSONObject("factorname").getString("value");
    JSONObject valueObj = jsonObj.optJSONObject("value");
    String value = null;
    String units = null;
    if (valueObj != null) {
      if ("literal".equals(valueObj.getString("type"))) {
        value = valueObj.getString("value");
        JSONObject unitObj = jsonObj.optJSONObject("unit");
        if (unitObj != null) {
          units = unitObj.optString("value", null);
        }
      }
    }
    JSONObject uriObj = jsonObj.optJSONObject("ontouri");
    String uri = null;
    if (uriObj != null) {
      uri = uriObj.optString("value", null);
    }
    
    String bioSampleId = null;
    JSONObject bioSampleObj = jsonObj.optJSONObject("biosample");
    if (bioSampleObj != null) {
      bioSampleId = bioSampleObj.optString("value", null);
      if (bioSampleId != null) {
        bioSampleId = bioSampleId.substring(bioSampleId.lastIndexOf('/')+1, bioSampleId.length());
        if (bioSampleId.length() == 0) {
          bioSampleId = null;
        }
      }
    }

    String sampleId = null;
    JSONObject sampleObj = jsonObj.optJSONObject("sample");
    if (sampleObj != null) {
      sampleId = sampleObj.optString("value", null);
    }
    
    if (value == null && uriObj == null) {
      return null;
    }
    else {
      return new AdjunctInvestigationDatum(sampleId, bioSampleId, name, value, uri, units);
    }
  }

  private AdjunctInvestigationDatum characteristicFromJson(JSONObject jsonObj) throws Exception {
    String name = jsonObj.getJSONObject("propname").getString("value");
    JSONObject valueObj = jsonObj.optJSONObject("value");
    String value = null;
    if (valueObj != null) {
      if ("literal".equals(valueObj.getString("type"))) {
        value = valueObj.getString("value");
        JSONObject unitObj = jsonObj.optJSONObject("unit");
        if (unitObj != null && unitObj.optString("value", null) != null) {
          value = value + " " + unitObj.getString("value"); 
        }
      }
    }
    JSONObject uriObj = jsonObj.optJSONObject("ontouri");
    String uri = null;
    if (uriObj != null) {
      uri = uriObj.optString("value", null);
    }
    if (value == null && uriObj == null) {
      return null;
    }
    else {
      return new AdjunctInvestigationDatum(null, null, name, value, uri, null);
    }
  }
  
  private AdjunctInvestigationDatum labeledDetailFromJson(JSONObject jsonObj, String name) throws Exception {
    String label = null;
    String uri = null;
    JSONObject uriObj = jsonObj.optJSONObject(name);
    if (uriObj != null) {
      uri = uriObj.optString("value", null);
    }    
    JSONObject labelObj = jsonObj.optJSONObject(name + "Label");
    if (labelObj != null) {
      label = labelObj.optString("value", null);
    }
    
    if (label != null) {
      return new AdjunctInvestigationDatum(null, null, name, label, uri, null);
    }
    else {
      return null;
    }
  }
  
  /**
   * Gets the list of isatab entries for the given investigation url
   * @param url the url of the investigation
   * @return the list of isatab entry urls
   */
  public List<InvestigationIsaTabFile> getIsaTabEntries(String investigationUrl) throws Exception {
    List<InvestigationIsaTabFile> entries = new ArrayList<InvestigationIsaTabFile>();
    List<URL> fileUrlList = listInvestigationFileUrls(investigationUrl);
    Set<String> fileUrls = new HashSet<String>();
    for (URL fileUrl : fileUrlList) {
      fileUrls.add(fileUrl.toString());
    }
    Set<String> filenamesWithUrl = new HashSet<String>();
    
    JSONArray bindings = requestToJsonBindings(new URL(investigationUrl + "/sparql/files_by_investigation"), null);
    for (int i = 0; i < bindings.length(); i++) {
      JSONObject binding = bindings.getJSONObject(i);
      if (binding.optJSONObject("file") != null &&
          binding.optJSONObject("term") != null) {
        String filename = binding.getJSONObject("file").getString("value");
        String typeUri = binding.getJSONObject("term").getString("value");
        String downloadUri = null;
        JSONObject downloadUriObj = binding.optJSONObject("downloaduri");
        if (downloadUriObj != null) {
          downloadUri = downloadUriObj.optString("value", null);        
        }
        
        if (downloadUri == null && !filename.startsWith("ftp://")) {
          String isaTabDownloadUrl = investigationUrl + "/isatab/" + filename; 
          if (fileUrls.contains(isaTabDownloadUrl)) {
            downloadUri = isaTabDownloadUrl;
          }
        }
        
        String endpointLabel = null;
        JSONObject endpointObj = binding.optJSONObject("endpointLabel"); 
        if (endpointObj != null) {
          endpointLabel = endpointObj.optString("value");
        }
        
        String techLabel = null;
        JSONObject techObj = binding.optJSONObject("techLabel"); 
        if (techObj != null) {
          techLabel = techObj.optString("value");
        }
        
        InvestigationIsaTabFile isaTabFile =
            new InvestigationIsaTabFile(typeUri, filename, downloadUri, endpointLabel, techLabel);
        if (isaTabFile.getDownloadUri() != null) {
          filenamesWithUrl.add(filename);
        }
        entries.add(isaTabFile);
      }
    }
    
    List<InvestigationIsaTabFile> filteredEntries = new ArrayList<InvestigationIsaTabFile>();
    for (InvestigationIsaTabFile isaTabFile : entries) {
      if (isaTabFile.getDownloadUri() != null 
          || !filenamesWithUrl.contains(isaTabFile.getFilename())) {
        filteredEntries.add(isaTabFile);
      }
    }
    return filteredEntries;
  }
  
  /**
   * Lists the urls of the files contained in an investigation
   * @param url url of the investigation
   * @return the list of available investigation file urls
   * @throws Exception
   */
  public List<URL> listInvestigationFileUrls(String investigationUrl) throws Exception {
    HttpGet httpGet = new HttpGet(investigationUrl);
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
        handleError(in, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        throw new RuntimeException("handleError should have thrown exception");
      }
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (Exception e) { }
      }
    }
  }

  /**
   * Gets the list of investigation urls that match the given characteristic values
   * @param rootUrl the root url of the investigation service
   * @param value the value to search by
   * @return the list of invetigation entries
   */
  public List<TimestampedUrl> findByCharacteristicValue(URL rootUrl, String value) throws Exception {
    value = URLEncoder.encode(value, "UTF-8");
    JSONArray bindings = requestToJsonBindings(new URL(rootUrl + "/sparql/investigation_by_characteristic_value?value="+value), null);
    List<TimestampedUrl> results = new ArrayList<TimestampedUrl>();
    for (int i = 0; i < bindings.length(); i++) {
      JSONObject binding = bindings.getJSONObject(i);
      JSONObject invObj = binding.optJSONObject("investigation");
      if (invObj != null) {
        String invUrl = invObj.optString("value", null);
        if (invUrl != null) {
          results.add(new TimestampedUrl(new URL(invUrl), 0l));
        }
      }
    }
    return results;
  }

  /**
   * Gets the list of investigation urls that match the given characteristic names
   * @param rootUrl the root url of the investigation service
   * @param factorValues the list of factors to search by
   * @return the list of investigation entries
   */
  public List<TimestampedUrl> findByFactors(URL rootUrl, List<String> factorValues) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (String factorValue : factorValues) {
      String value = URLEncoder.encode(factorValue, "UTF-8");
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append(value);      
    }

    JSONArray bindings = requestToJsonBindings(new URL(rootUrl + "/sparql/investigation_by_factors?factorValues="+sb.toString()), null);
    List<TimestampedUrl> results = new ArrayList<TimestampedUrl>();
    for (int i = 0; i < bindings.length(); i++) {
      JSONObject binding = bindings.getJSONObject(i);
      JSONObject invObj = binding.optJSONObject("investigation");
      if (invObj != null) {
        String invUrl = invObj.optString("value", null);
        if (invUrl != null) {
          results.add(new TimestampedUrl(new URL(invUrl), 0l));
        }
      }
    }
    return results;
  }

  /**
   * Gets the list of investigation urls that match the given gene identifiers
   * @param rootUrl the root url of the investigation service
   * @param geneIdentifiers the list of gene identifiers to search by
   * @param value optional minimum value of the given type
   * @param valueType the type of value to search by
   * @param comparator the comparator to use
   * @return the list of investigation entries
   */
  public List<TimestampedUrl> findByGeneIdentifiers(URL rootUrl, List<String> geneIdentifiers, Float value, ValueType valueType, ComparatorType comparator) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (String gene : geneIdentifiers) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append(gene);      
    }
    String geneString = URLEncoder.encode(sb.toString(), "UTF-8");

    String valueTypeName = null;
    if (valueType != null) {
      switch(valueType) {
      case foldChange:
        valueTypeName = "FC"; break;
      case qValue:
        valueTypeName = "qvalue"; break;
      case pValue:
        valueTypeName = "pvalue"; break;
      default: 
        throw new RuntimeException("Unsupported value type: " + valueType);
      }
    }
    
    String url;
    if (value != null && !value.isNaN() && valueTypeName != null) {
      String valueString = valueTypeName + ":" + String.valueOf(value);
      valueString = URLEncoder.encode(valueString, "UTF-8");
      url = rootUrl + "/sparql/investigation_by_gene_and_value?geneIdentifiers="+geneString + 
          "&value=" + valueString + "&relOperator=" + comparator;
    }
    else {
      url = rootUrl + "/sparql/investigation_by_genes?geneIdentifiers="+geneString;
    }
    
    JSONArray bindings = requestToJsonBindings(new URL(url), null);    
    List<TimestampedUrl> results = new ArrayList<TimestampedUrl>();
    for (int i = 0; i < bindings.length(); i++) {
      JSONObject binding = bindings.getJSONObject(i);
      JSONObject invObj = binding.optJSONObject("investigation");
      if (invObj != null) {
        String invUrl = invObj.optString("value", null);
        if (invUrl != null) {
          results.add(new TimestampedUrl(new URL(invUrl), 0l));
        }
      }
    }
    return results;
  }

  /**
   * Finds all currently available genes
   * @param rootUrl the root url of the service
   */
  public Map<String, NavigableSet<String>> findAvailableGenes(URL rootUrl) throws Exception {
    Map<String, NavigableSet<String>> genesMap = new HashMap<String, NavigableSet<String>>();
    String url = rootUrl + "/sparql/genelist";
    JSONArray bindings = requestToJsonBindings(new URL(url), null);
    for (int i = 0; i < bindings.length(); i++) {
      JSONObject binding = bindings.getJSONObject(i);
      JSONObject genesObj = binding.optJSONObject("genes");
      if (genesObj != null) {
        String geneUrl = genesObj.optString("value", null);
        if (geneUrl != null) {
          int nameIdx = geneUrl.lastIndexOf('/');
          if (nameIdx > 0) {
            String prefix = mapGeneUri(geneUrl.substring(0, nameIdx+1));
            NavigableSet<String> genes = genesMap.get(prefix);
            if (genes == null) {
              genes = new TreeSet<String>();
              genesMap.put(prefix, genes);
            }
            genes.add(geneUrl.substring(nameIdx+1));
          }
        }
      }
    }
    return genesMap;
  }
    
  public static String mapGeneUri(String geneUri) {
    if ("http://onto.toxbank.net/isa/Entrez/".equals(geneUri)) {
      return "entrez";
    }
    if ("http://purl.uniprot.org/uniprot/".equals(geneUri)) {
      return "uniprot";      
    }
    if ("http://onto.toxbank.net/isa/Symbol/".equals(geneUri)) {
      return "genesymbol";
    }
    if ("http://onto.toxbank.net/isa/Unigene/".equals(geneUri)) {
      return "unigene";
    }
    if ("http://onto.toxbank.net/isa/RefSeq/".equals(geneUri)) {
      return "refseq";
    }
    return "unknown";
  }
  
  /**
   * Finds the dose responses for a given gene
   * @param rootUrl the root url of the investigation service
   * @param geneIdentifiers the gene identifiers to search by
   */
  public List<GeneDoseResponse> findGeneDoseResponse(URL rootUrl, List<String> geneIdentifiers) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (String gene : geneIdentifiers) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append(gene);      
    }
    String geneString = URLEncoder.encode(sb.toString(), "UTF-8");
    
    String url = rootUrl + "/sparql/biosearch?geneIdentifiers="+geneString;

    Map<String, GeneDoseResponse> responseMap = new HashMap<String, GeneDoseResponse>();
    
    JSONArray bindings = requestToJsonBindings(new URL(url), null);
    for (int j = 0; j < bindings.length(); j++) {
      JSONObject binding = bindings.getJSONObject(j);
      String gene = binding.optString("gene", null);
      String investigationUrl = null;
      String dataTransformName = null;      
      JSONObject invObj = binding.optJSONObject("investigation");
      if (invObj != null) {
        investigationUrl = invObj.optString("value", null);
      }
      JSONObject dataTransformObj = binding.optJSONObject("dataTransformationName");
      if (dataTransformObj != null) {
        dataTransformName = dataTransformObj.optString("value", null);
      }
      if (gene == null || investigationUrl == null || dataTransformName == null) {
        throw new RuntimeException("Missing one of gene investigation dataTransformationName");
      }
      
      String responseKey = gene + investigationUrl + dataTransformName;
      GeneDoseResponse response = responseMap.get(responseKey);
      if (response == null) {
        response = new GeneDoseResponse();
        response.setGene(gene);
        response.setInvestigationUri(investigationUrl);
        response.setDataTransformationName(dataTransformName);
        responseMap.put(responseKey, response);
      }
      
      String sampleString = binding.optString("sample", null);
      if (sampleString != null) {
        response.setSampleName(sampleString);
      }
      
      String cellString = binding.optString("cell", null);
      if (cellString != null) {
        response.setCell(cellString);
      }
        
      JSONObject titleObj = binding.optJSONObject("invTitle");
      if (titleObj != null) {
        response.setInvestigationTitle(titleObj.optString("value", null));
      }
      
      JSONArray factorValues = binding.optJSONArray("factorValues");
      if (factorValues != null) {
        for (int factorIdx = 0; factorIdx < factorValues.length(); factorIdx++) {
          JSONObject factorObj = factorValues.getJSONObject(factorIdx);
          String valueString = null;
          String units = null;
          JSONObject valueObj = factorObj.optJSONObject("value");
          if (valueObj != null) {
            valueString = valueObj.optString("value", null);
          }
          JSONObject unitsObj = factorObj.optJSONObject("unit");
          if (unitsObj != null) {
            units = unitsObj.optString("value", null);
          }
          JSONObject nameObj = factorObj.optJSONObject("factorname");
          if (valueString != null) {
            if (nameObj != null) {
              String name = nameObj.optString("value", null);
              if (name != null) {
                if ("sample TimePoint".equals(name)) {
                  response.setTimeUnits(units);
                  response.setTimeValue(Double.parseDouble(valueString));
                }
                else if ("dose".equals(name)) {
                  response.setDoseUnits(units);
                  response.setDoseValue(Double.parseDouble(valueString));
                }
                else if ("compound".equals(name)) {
                  response.setCompoundName(valueString);
                }
              }
            }
          }
        }
      }
      
      JSONObject featureTypeObj = binding.optJSONObject("featureType");
      if (featureTypeObj != null) {
        String featureTypeUri = featureTypeObj.optString("value", null);
        if (featureTypeUri != null) {
          JSONObject valueObj = binding.optJSONObject("value");
          if (valueObj != null) {
            String valueString = valueObj.optString("value", null);
            if (valueString != null) {
              double value = Double.parseDouble(valueString);
              if ("http://onto.toxbank.net/isa/pvalue".equals(featureTypeUri)) {
                response.setpValue(value);
              }
              else if ("http://onto.toxbank.net/isa/FC".equals(featureTypeUri)) {
                response.setFoldChange(value);
              }
              else if ("http://onto.toxbank.net/isa/qvalue".equals(featureTypeUri)) {
                response.setqValue(value);
              }
            }
          }
        }
      }
    }
    
    List<GeneDoseResponse> responses = new ArrayList<GeneDoseResponse>(responseMap.values());
    
    return responses;    
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
        handleError(in, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
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
   * Runs the given request. The results are returned as a json array
   * of sparql bindings
   * @param url the url where the query should be performed
   * @param user an optional user parameter
   * @return the bindings result
   */
  public JSONArray requestToJsonBindings(URL url, User user) throws Exception {
    JSONObject jsonObj = requestToJson(url, user);
    if (jsonObj != null) {
      JSONObject resultsJson = jsonObj.getJSONObject("results");
      return resultsJson.getJSONArray("bindings");
    }
    else {
      return new JSONArray();
    }
  }
  
  /**
   * Runs the given request. The results are returned as a json object
   * @param url the url where the query should be performed
   * @param user an optional user parameter
   * @return the json object with the results
   */
  public JSONObject requestToJson(URL url, User user) throws Exception {
    HttpGet httpGet = new HttpGet(url.toString());
    if (user != null) {
      httpGet.addHeader("user", user.getResourceURL().toString());
    }
    httpGet.addHeader("Accept", "application/json");
    httpGet.addHeader("Accept-Charset", "utf-8");
    
    InputStream in = null;
    try {
      HttpResponse response = getHttpClient().execute(httpGet);
      HttpEntity entity  = response.getEntity();
      in = entity.getContent();
      if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          sb.append(line);
          sb.append("\n");
        }
        if ("true".equalsIgnoreCase(System.getProperty("debug.investigation.client", "false"))) {
          System.out.println(url.toString());
          System.out.println(sb.toString());
        }
        JSONObject obj = new JSONObject(sb.toString());
        return obj;
      } else if (response.getStatusLine().getStatusCode()== HttpStatus.SC_NOT_FOUND) {
        if ("true".equalsIgnoreCase(System.getProperty("debug.investigation.client", "false"))) {
          System.out.println(url.toString() + " NOT FOUND");
        }
        return null;       
      } else {
        handleError(in, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        throw new RuntimeException("handleError should have thrown exception");
      }

    } finally {
      try {if (in !=null) in.close();} catch (Exception x) {}
    }
  }
  
  /**
   * Runs the given request. The results are returned as a json object
   * @param url the url where the query should be performed
   * @param user an optional user parameter
   * @return the json array with the results
   */
  public JSONArray requestToJsonArray(URL url, User user) throws Exception {
    HttpGet httpGet = new HttpGet(url.toString());
    if (user != null) {
      httpGet.addHeader("user", user.getResourceURL().toString());
    }
    httpGet.addHeader("Accept", "application/json");
    httpGet.addHeader("Accept-Charset", "utf-8");
    
    InputStream in = null;
    try {
      HttpResponse response = getHttpClient().execute(httpGet);
      HttpEntity entity  = response.getEntity();
      in = entity.getContent();
      if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          sb.append(line);
          sb.append("\n");
        }
        if ("true".equalsIgnoreCase(System.getProperty("debug.investigation.client", "false"))) {
          System.out.println(url.toString());
          System.out.println(sb.toString());
        }
        JSONArray array = new JSONArray(sb.toString());
        return array;
      } else if (response.getStatusLine().getStatusCode()== HttpStatus.SC_NOT_FOUND) {
        if ("true".equalsIgnoreCase(System.getProperty("debug.investigation.client", "false"))) {
          System.out.println(url.toString() + " NOT FOUND");
        }
        return null;       
      } else {
        handleError(in, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        throw new RuntimeException("handleError should have thrown exception");
      }

    } finally {
      try {if (in !=null) in.close();} catch (Exception x) {}
    }
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
        handleError(in, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
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
        handleError(in, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
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
   * @param accessRights the access rights to assign
   * @param ftpFilename name of the file on the ftp server - optional used with ftpData type
   * @return the remote task created
   */
  public RemoteTask postInvestigation(File zipFile, URL rootUrl, List<PolicyRule> accessRights, Investigation investigation,
      String ftpFilename) throws Exception {
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, utf8);
    if (investigation.getDataType() == Investigation.DataType.isaTabData ||
        investigation.getDataType() == Investigation.DataType.unformattedData) {
      entity.addPart(file_param, new FileBody(zipFile, zipFile.getName(), "application/zip", null));
    }
    if (investigation.getDataType() != Investigation.DataType.isaTabData) {
      addMetaData(entity, investigation);
    }
    if (investigation.getDataType() == Investigation.DataType.ftpData) {
      entity.addPart(ftp_file_param, new StringBody(ftpFilename));
    }
    entity.addPart(searchable_param, new StringBody(String.valueOf(investigation.isSearchable())));
    AbstractClient.addPolicyRules(entity, accessRights);
    RemoteTask task = new RemoteTask(getHttpClient(), rootUrl, "text/uri-list", entity, HttpPost.METHOD_NAME);
    return task;
  }
  
  /**
   * Updates an investigation at the given url
   * @param zipFile the new investigation zip file - null indicates that the zip file should remain unchanged
   * @param investigation the investigation object to update
   * @param accessRights the access rights to assign
   * @param ftpFilename name of the file on the ftp server - optional used with ftpData type
   * @return the remote task created
   */
  public RemoteTask updateInvestigation(File zipFile, Investigation investigation, List<PolicyRule> accessRights,
      String ftpFilename) throws Exception {
    if (investigation.getResourceURL() == null) {
      throw new IllegalArgumentException("investigation has not been assigned a resource url");
    }
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, utf8);
    if (zipFile != null) {
      entity.addPart(file_param, new FileBody(zipFile, zipFile.getName(), "application/zip", null));
    }
    if (investigation.getDataType() != Investigation.DataType.isaTabData) {
      addMetaData(entity, investigation);
    }
    if (investigation.getDataType() == Investigation.DataType.ftpData) {
      entity.addPart(ftp_file_param, new StringBody(ftpFilename));
    }
        
    addPolicyRules(entity, accessRights);

    if (investigation.isPublished() != null) {
      entity.addPart(published_param, new StringBody(investigation.isPublished().toString()));
    }
    if (investigation.isSearchable() != null) {
      entity.addPart(searchable_param, new StringBody(investigation.isSearchable().toString()));
    }
    RemoteTask task = new RemoteTask(getHttpClient(), investigation.getResourceURL(), "text/uri-list", entity, HttpPut.METHOD_NAME);
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
    
  public void addMetaData(MultipartEntity entity, Investigation investigation) throws Exception {
    entity.addPart(data_type_param, new StringBody(investigation.getDataType().name()));
    entity.addPart(title_param, new StringBody(investigation.getTitle()));
    entity.addPart(abstract_param, new StringBody(investigation.getAbstract()));
    entity.addPart(owning_org_param, new StringBody(investigation.getOrganisation().getResourceURL().toString()));
    entity.addPart(authors_param, joinUrls(investigation.getAuthors()));
    entity.addPart(projects_param, joinUrls(investigation.getProjects()));
    entity.addPart(keywords_param, joinStrings(investigation.getKeywords()));
    entity.addPart(license_param, joinStrings(investigation.getLicenses())); 
  }
  
  private static StringBody joinStrings(List<String> values) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (String value : values) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append(value);
    }
    return new StringBody(sb.toString());
  }
  
  private static StringBody joinUrls(List<? extends AbstractToxBankResource> rsrcs) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (AbstractToxBankResource rsrc : rsrcs) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append(rsrc.getResourceURL().toString());
    }
    return new StringBody(sb.toString());
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
    return updateInvestigation(null, investigation, accessRights, null);
  }
  
  /**
   * Tries to parse an opentox error from the response and throws a rest exception with
   * an appropriate message
   * @param is the input stream - assumed already opened and will be closed outside of this scope
   * @param statusCode the status code of the response
   * @param defaultMessage a default message if an error can't be parsed
   * @throws Exception
   */
  private static void handleError(InputStream in, int statusCode, String defaultMessage) throws Exception {
    String message = defaultMessage;
    try {
      StringBuilder sb = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        sb.append(line);
        sb.append("\n");
      }
      String result = sb.toString();
      Model model = ModelFactory.createDefaultModel();
      model.read(new StringReader(result), null, "TURTLE");
      
      ResIterator resIter = model.listResourcesWithProperty(OPENTOX.message);
      while (resIter.hasNext()) {
        Resource res = resIter.next();
        if (res.getProperty(OPENTOX.message) != null) {
          message = res.getProperty(OPENTOX.message).getString();
        }
      }
    }
    catch (Throwable t) { }

    throw new RestException(statusCode, message);  
  }
}
