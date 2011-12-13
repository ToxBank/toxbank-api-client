package net.toxbank.client.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.toxbank.client.io.rdf.IOClass;
import net.toxbank.client.task.RemoteTask;

import org.opentox.rest.HTTPClient;
import org.opentox.rest.RestException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public abstract class AbstractClient<T extends IToxBankResource> {
	
	public AbstractClient() {
	}
	
	protected List<T> readRDF_XML(URL url) throws Exception {
		return read(url,"application/rdf+xml");
	}
	protected List<T> readRDF_N3(URL url) throws Exception {
		return read(url,"text/n3");
	}	
	protected List<T> read(URL url,String mediaType) throws RestException, IOException {
		
		HTTPClient client = new HTTPClient(url.toString());
		client.setHeaders(new String[][] {{"Accept",mediaType},{"Accept-Charset", "utf-8"}});

		client.get();
		InputStream in = null;
		try {
			if (client.getStatus()==200) {
				in = client.getInputStream();
				Model model = ModelFactory.createDefaultModel();
				model.read(new InputStreamReader(in,"UTF-8"),"");
				return getIOClass().fromJena(model);
			} else throw new RestException(client.getStatus());
		
		} finally {
			try {in.close();} catch (Exception x) {}
			try {client.release();} catch (Exception x) {}
		}
	
	}
	
	public List<URL> readURI(URL url) throws  RestException, IOException {
		HTTPClient client = new HTTPClient(url.toString());
		client.setHeaders(new String[][] {{"Accept","text/uri-list"}});
		client.get();
		InputStream in = null;
		try {
			if (client.getStatus()==200) {
				in = client.getInputStream();
				return readURI(in);
			} else throw new RestException(client.getStatus());

		} finally {
			try {in.close();} catch (Exception x) {}
			try {client.release();} catch (Exception x) {}
		}
	}
	protected List<URL> readURI(InputStream in) throws IOException, MalformedURLException {
		List<URL> uris = new ArrayList<URL>();
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = r.readLine())!= null) {
			uris.add(new URL(line));
		}
		return uris;
	}	

	/**
	 * Delete
	 * @return
	 */
	protected void delete(URL url) throws Exception {
		RemoteTask task = new RemoteTask(url, "text/uri-list", null, HTTPClient.DELETE);
		task.waitUntilCompleted(500);
	}
	/**
	 * 
	 * @param object
	 * @throws UniformInterfaceException
	 * @throws URISyntaxException
	 */
	protected void delete(T object) throws Exception {
		delete(object.getResourceURL());
	}
	/**
	 * POST
	 * @param object
	 * @throws UniformInterfaceException
	 * @throws URISyntaxException
	 */
	/*
	protected void createWebForm(T object, URL collection) throws RestException {
		if (collection==null) throw new Exception("No URL");
		HTTPClient client = new HTTPClient(collection.toString());
		client.setHeaders(new String[][] {{"Accept","text/uri-list"}});
		client.post();
		InputStream in = null;
		try {
			if (client.getStatus()!=200) 
				throw new RestException(client.getStatus());
		} catch (Exception x) {
			throw x;
		} finally {
			try {in.close();} catch (Exception x) {}
			try {client.release();} catch (Exception x) {}
		}
		

	}
	*/
	
	/**
	 * POST to create a new object. 
	 * @param object
	 * @param collection  The URL of resource collection, e.g. /protocol or /user .
	 * The new object will be added to the collection of resources.
	 * @return  Returns {@link RemoteTask}
	 * @throws Exception in case of error.
	 */
	protected abstract RemoteTask createAsync(T object, URL collection) 
				throws RestException,UnsupportedEncodingException, IOException, URISyntaxException ;
	
	/**
	 * Creates a new object. Waits until the asynchronous tasks completes.
	 * @param object
	 * @param collection
	 * @return
	 * @throws Exception in case of error.
	 */
	protected T createSync(T object, URL collection) throws Exception {
		RemoteTask task = createAsync(object, collection);
		task.waitUntilCompleted(500);	
		if (task.isERROR()) throw task.getError();
		else object.setResourceURL(task.getResult());
		return object;
	}
	
	abstract IOClass<T> getIOClass();
	

}
