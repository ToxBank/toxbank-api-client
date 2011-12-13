package net.toxbank.client.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
	protected void delete(URL url) throws RestException, IOException {
		HTTPClient client = new HTTPClient(url.toString());
		client.setHeaders(new String[][] {{"Accept","text/uri-list"}});
		client.delete();
		try {
			if (client.getStatus()!=200) 
				throw new RestException(client.getStatus());

		} finally {
			try {client.release();} catch (Exception x) {}
		}
	}
	/**
	 * 
	 * @param object
	 * @throws UniformInterfaceException
	 * @throws URISyntaxException
	 */
	protected void delete(T object) throws RestException,IOException {
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
	
	//protected abstract Form getWebForm(T object);
	
	
	/*
	public List<T> readRDF(URL url) throws Exception {
		if (url==null) throw new Exception("No URL");
		HTTPClient client = new HTTPClient(url.toString());
		client.setHeaders(new String[][] {{"Accept","application/rdf+xml"}});
		client.get();
		InputStream in = null;
		try {
			if (client.getStatus()==200) {
				in = client.getInputStream();
				Model model = ModelFactory.createDefaultModel();
				model.read(new InputStreamReader(in),"");
				return getIOClass().fromJena(model);
			} else return null;
		} catch (Exception x) {
			throw x;
		} finally {
			try {in.close();} catch (Exception x) {}
			try {client.release();} catch (Exception x) {}
		}
	}
	*/
	
	abstract IOClass<T> getIOClass();
	
	protected String[] getMultipartWebFormRepresentation(
					String[][] form, 
					String fileFieldName, 
					File file, 
					String mediaType) throws IOException {
		String docPath = file.getAbsolutePath();
		StringBuffer str_b = new StringBuffer();
		final String bndry ="XCVBGFDS";
		String paramName = fileFieldName;
		String fileName = file.getName();
		final String type = new String(String.format("multipart/form-data; boundary=%s",bndry));
	    file = new File(docPath);
	    
	    /**
	     * WRITE THE fields
	     */
	    for (int i=0;i< form.length;i++ ) {
		    
		    String disptn = String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\n\r\n%s\r\n",
	    			bndry,form[i][0],form[i][1]);
		    str_b.append(disptn);
	    }
	      
	    /**
	     * WRITE THE FIRST/START BOUNDARY
	     */
	    String disptn = String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\nContent-Type: %s\r\n\r\n",
	    			bndry,paramName,fileName,mediaType);
	    str_b.append(disptn);
	    /**
	     * WRITE THE FILE CONTENT
	     */
	    FileInputStream is;
	    byte[] buffer = new byte[4096];
	    int bytes_read;
	    try {
	        	is = new FileInputStream(file);
				while((bytes_read = is.read(buffer)) != -1) {
				    
				    str_b.append(new String(buffer, 0, bytes_read));
				}
				is.close();
		} catch (IOException e) {
			throw e;
		}
		/**
		 * WRITE THE CLOSING BOUNDARY
		 */
	    String boundar = String.format("\r\n--%s--",bndry);
	    str_b.append(boundar); // another 2 new lines
	        //PUT
	    return new String[] {str_b.toString(),type};
        
	}	
}
