package net.toxbank.client.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.exceptions.InvalidInputException;
import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.task.RemoteTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.opentox.aa.opensso.AAServicesConfig;
import org.opentox.aa.opensso.OpenSSOToken;
import org.opentox.rest.RestException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * An abstract client, implementing HTTP GET, PUT, POST and DELETE.
 * @author nina
 *
 * @param <T>
 */
public abstract class AbstractClient<T extends IToxBankResource> {
	protected HttpClient httpClient;
	
	public HttpClient getHttpClient() throws IOException {
		if (httpClient==null) throw new IOException("No HttpClient!"); 
			//setHttpClient(new DefaultHttpClient());
		return httpClient;
	}
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	
	public AbstractClient(HttpClient httpclient) {
		super();
		setHttpClient(httpclient);
	}
	
	public AbstractClient() {
		this(null);
	}
	
	/**
	 * Same as {@link #getRDF_XML(URL)}
	 * @param url
	 * @return List of objects
	 * @throws Exception
	 */
	public List<T> get(URL url) throws Exception {
		return getRDF_XML(url);
	}
	/**
	 * HTTP GET with "Accept:application/rdf+xml".  Parses the RDF and creates list of objects.
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public List<T> getRDF_XML(URL url) throws Exception {
		return get(url,"application/rdf+xml");
	}
	/**
	 * HTTP GET with "Accept:text/n3".  Parses the RDF and creates list of objects.
	 * @param url
	 * @return
	 * @throws Exception
	 */
	protected List<T> getRDF_N3(URL url) throws Exception {
		return get(url,"text/n3");
	}	
	/**
	 * HTTP GET with given media type (expects one of RDF flavours). 
	 * @param url
	 * @param mediaType
	 * @return
	 * @throws RestException
	 * @throws IOException
	 */
	protected List<T> get(URL url,String mediaType) throws RestException, IOException {
		HttpGet httpGet = new HttpGet(url.toString());
		httpGet.addHeader("Accept",mediaType);
		httpGet.addHeader("Accept-Charset", "utf-8");

		InputStream in = null;
		try {
			HttpResponse response = getHttpClient().execute(httpGet);
			HttpEntity entity  = response.getEntity();
			in = entity.getContent();
			if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
				Model model = ModelFactory.createDefaultModel();
				model.read(new InputStreamReader(in,"UTF-8"),"");
				return getIOClass().fromJena(model);
			} else throw new RestException(response.getStatusLine().getStatusCode());
		
		} finally {
			try {if (in != null) in.close();} catch (Exception x) {}
		}
	
	}
	
	/**
	 * HTTP GET with "Accept:text/uri-list". 
	 * If the resource is a container, will return list URIs of contained resources.
	 * Otherwise, will return the URI of the object itself (for consistency).
	 * @param url
	 * @return
	 * @throws RestException
	 * @throws IOException
	 */
	public List<URL> listURI(URL url) throws  RestException, IOException {
		HttpGet httpGet = new HttpGet(url.toString());
		httpGet.addHeader("Accept","text/uri-list");

		InputStream in = null;
		try {
			HttpResponse response = getHttpClient().execute(httpGet);
			HttpEntity entity  = response.getEntity();
			in = entity.getContent();
			if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
				return readURI(in);
			} else throw new RestException(response.getStatusLine().getStatusCode());

		} finally {
			try {if (in !=null) in.close();} catch (Exception x) {}
		}		
	}
	/**
	 * HTTP GET with "Accept:text/uri-list". 
	 * If the resource is a container, will return list URIs of contained resources.
	 * Otherwise, will return the URI of the object itself (for consistency). 
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private List<URL> readURI(InputStream in) throws IOException, MalformedURLException {
		List<URL> uris = new ArrayList<URL>();
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = r.readLine())!= null) {
			uris.add(new URL(line));
		}
		return uris;
	}	
	
	/**
	 * HTTP POST to create a new object. Asynchronous.
	 * @param object
	 * @param collection  The URL of resource collection, e.g. /protocol or /user .
	 * The new object will be added to the collection of resources.
	 * @return  Returns {@link RemoteTask}
	 * @throws Exception if not allowed, or other error condition
	 */
	public RemoteTask postAsync(T object, URL collection) throws Exception {
		return sendAsync(collection, createPOSTEntity(object), HttpPost.METHOD_NAME);
	}	
	/**
	 * HTTP PUT to update an existing object. Asynchronous.
	 * @param object
	 * @return {@link RemoteTask}
	 * @throws Exception if not allowed, or other error condition
	 */
	protected RemoteTask putAsync(T object) throws Exception {
		return sendAsync(object.getResourceURL(), createPUTEntity(object), HttpPut.METHOD_NAME);
	}
	/**
	 * HTTP DELETE to remove an existing object. Asynchronous.
	 * @return {@link RemoteTask}
	 * @throws Exception if not allowed, or other error condition 
	 */
	protected RemoteTask deleteAsync(T object) throws Exception {
		return deleteAsync(object.getResourceURL());
	}	
	/**
	 * The same as {@link #deleteAsync(IToxBankResource)}, but accepts an URL.
	 * @param url
	 * @return {@link RemoteTask}
	 * @throws Exception
	 */
	protected RemoteTask deleteAsync(URL url) throws Exception {
		return sendAsync(url,null, HttpDelete.METHOD_NAME);
	}	
	private RemoteTask sendAsync(URL target, HttpEntity entity, String method) throws Exception {
		return new RemoteTask(getHttpClient(),target, "text/uri-list", entity, method);
	}	

	/**
	 * 
	 * @param object The object to be created
	 * @return {@link HttpEntity}
	 * @throws Exception
	 */
	protected abstract HttpEntity createPOSTEntity(T object) throws InvalidInputException,Exception;
	/**
	 * 
	 * @param object the object to be updated
	 * @return
	 * @throws Exception
	 */
	protected abstract HttpEntity createPUTEntity(T object) throws InvalidInputException,Exception;
	/**
	 * Creates a new object. Waits until the asynchronous tasks completes.
	 * @param object
	 * @param collection
	 * @return
	 * @throws Exception in case of error.
	 */
	public T post(T object, URL collection) throws Exception {
		RemoteTask task = postAsync(object, collection);
		task.waitUntilCompleted(500);	
		if (task.isERROR()) throw task.getError();
		else object.setResourceURL(task.getResult());
		return object;
	}
	/**
	 * Updates an existing object. Waits until the asynchronous tasks completes.
	 * @param object
	 * @param collection
	 * @return
	 * @throws Exception
	 */
	public T put(T object) throws Exception {
		RemoteTask task = putAsync(object);
		task.waitUntilCompleted(500);	
		if (task.isERROR()) throw task.getError();
		else object.setResourceURL(task.getResult());
		return object;
	}	
	
	/**
	 * Synchronous delete
	 * @param url
	 * @throws Exception
	 */
	public void delete(URL url) throws Exception {
		RemoteTask task = deleteAsync(url);
		task.waitUntilCompleted(500);
		if (task.isERROR()) throw task.getError();
	}
	/**
	 * 
	 * @param object
	 * @throws UniformInterfaceException
	 * @throws URISyntaxException
	 */
	public void delete(T object) throws Exception {
		delete(object.getResourceURL());
	}
	abstract IOClass<T> getIOClass();
	
	public static OpenSSOToken login(String username,String password) throws Exception {
		OpenSSOToken token = new OpenSSOToken(AAServicesConfig.getSingleton().getOpenSSOService());
		if (token.login(username,password)) {
			//AAClient.setTokenFactory();
		}
		return token;
		
	}

	
}
