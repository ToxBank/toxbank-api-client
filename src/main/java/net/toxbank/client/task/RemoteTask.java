package net.toxbank.client.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.opentox.rest.HTTPClient;
import org.opentox.rest.RestException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Convenience class to launch and poll remote POST jobs
 * @author nina
 *
 */
public class RemoteTask implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final URL url;
	protected int status = -1;
	protected URL result = null;
	protected Exception error = null;
	
	protected void setError(Exception error) {
		this.error = error;
	}
	public Exception getError() {
		return error;
	}
	public RemoteTask(URL url,
			  String acceptMIME, 
			  String[][] form,
			  String method) throws RestException, UnsupportedEncodingException {
		this(url,acceptMIME,form==null?null:HTTPClient.getForm(form),HTTPClient.mime_wwwform,method);
	}
	public RemoteTask(URL url,
			  String acceptMIME, 
			  String content,
			  String contentMIME,
			  String method) throws RestException {
		super();
		this.url = url;

		HTTPClient client = null;
		
		try {
			client = new HTTPClient(url.toString());
			client.setHeaders(new String[][] {{"Accept",acceptMIME},{"Accept-Charset", "utf-8"}});
			//client.setFollowingRedirects(true);
			//client.setRetryAttempts(1);
			//client.setRetryOnError(false);
			
			
			if (method.equals("POST")) 
				client.post(content,contentMIME);
			else if (method.equals("PUT"))
				client.put(content,contentMIME);
			else if (method.equals("DELETE"))
				client.delete();
			else if (method.equals("GET"))
				client.get();
			else throw new RestException(HttpURLConnection.HTTP_BAD_METHOD);
			this.status = client.getStatus();
			
			if (client.getInputStream()==null) {
				throw new RestException(HttpURLConnection.HTTP_BAD_GATEWAY,
						String.format("[%s] Representation not available %s",this.status,url));
			}
			
			result = handleOutput(client.getInputStream(),status,null);
		} catch (RestException x) {
			status = x.getStatus();
			try { 
				error = new RestException(HttpURLConnection.HTTP_BAD_GATEWAY,
						String.format("URL=%s [%s] ",url,x.getStatus()),
						x); 
			}	catch (Exception xx) { error = x; }
		} catch (Exception x) {
			setError(x);
			status = -1;
		} finally {
			try { client.getInputStream().close(); } catch (Exception x) { x.printStackTrace();}
			try { client.release(); } catch (Exception x) { x.printStackTrace();}
		}
	}	
	
	
	public boolean isCompletedOK() {
		return HttpURLConnection.HTTP_OK == status;
	}
	public boolean isCancelled() {
		return  HttpURLConnection.HTTP_UNAVAILABLE == status;
	}
	public boolean isAccepted() {
		return HttpURLConnection.HTTP_ACCEPTED == status;
	}	

	public boolean isERROR() {
		return error != null;
	}		
	public boolean isDone() {
		return isCompletedOK() || isERROR() || isCancelled();
	}		
	public URL getUrl() {
		return url;
	}

	public int getStatus() {
		return status;
	}

	public URL getResult() {
		return result;
	}

	@Override
	public String toString() {
		return String.format("URL: %s\tResult: %s\tStatus: %s\t%s", url,result,status,error==null?"":error.getMessage());
	}
	/**
	 * returns true if ready
	 * @return
	 */
	public boolean poll() {

			
		if (isDone()) return true;

		HttpURLConnection client = null;
		InputStream in = null;
		
		try {
			
			client = HTTPClient.getHttpURLConnection(result.toString(),"GET","text/uri-list");
			//client.setFollowRedirects(true);

	        in = client.getInputStream();
			status = client.getResponseCode();
			if (HttpURLConnection.HTTP_UNAVAILABLE == status) {
				return true;
			}
			
//			if (!r.getEntity().isAvailable()) throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,String.format("Representation not available %s",result));
			
			result = handleOutput(in,status,result);
		} catch (IOException x) {
			setError(x);

		} catch (RestException x) {
			setError(x);
			status = x.getStatus();
		} catch (Exception x) {
			setError(x);
			status = -1;
		} finally {
			try {in.close();} catch (Exception x) {}
			try {
				client.disconnect(); client = null;
			} catch (Exception x) {}

		}
		return isDone();
	}
	/**
	 * 
	 * @param in
	 * @param status
	 * @param url  the url contacted - for returning proper error only
	 * @return
	 * @throws ResourceException
	 */
	protected URL handleOutput(InputStream in,int status,URL url) throws RestException {
		URL ref = null;
		if (HttpURLConnection.HTTP_OK == status
						|| HttpURLConnection.HTTP_ACCEPTED == status 
						|| HttpURLConnection.HTTP_CREATED == status 
						//|| Status.REDIRECTION_SEE_OTHER.equals(status)
						|| HttpURLConnection.HTTP_UNAVAILABLE == status
						) {

			if (in==null) {
				if ((HttpURLConnection.HTTP_ACCEPTED == status) && (url != null)) return url;
				String msg = String.format("Error reading response from %s: %s. Status was %s", url==null?getUrl():url, "Empty content",status);
				throw new RestException(HttpURLConnection.HTTP_BAD_GATEWAY,msg);
			}
			
			int count=0;
			try {

				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = null;
				while ((line = reader.readLine())!=null) {
					if ("".equals(line.trim())) ref = null;
					else {
						ref = new URL(line.trim());
						count++;
					}
				}
			} catch (Exception x) {
				throw new RestException(HttpURLConnection.HTTP_BAD_GATEWAY,
						String.format("Error reading response from %s: %s", url==null?getUrl():url, x.getMessage()),x);
			} finally {
				try { in.close(); } catch (Exception x) {} ;
			}
			if (count == 0) 
				if (status==HttpURLConnection.HTTP_OK) return null;
				else return url==null?getUrl():url;
			/* A hack for the validation service returning empty responses on 200 OK ...
				throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
							String.format("No task status indications from %s",url==null?getUrl():url));
			*/
			return ref;
						
		} else { //everything else considered an error
			throw new RestException(status);
		}
	}

	public boolean pollRDF() {
		if (isDone()) return true;

		HttpURLConnection uc = null;
		InputStream in = null;
		
		try {
			uc = HTTPClient.getHttpURLConnection(result.toString(),"GET","application/rdf+xml");

			status =uc.getResponseCode();
//			if (!r.getEntity().isAvailable()) throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,String.format("Representation not available %s",result));
			in = uc.getInputStream();
			result = handleOutputRDF(in,status);
		} catch (IOException x) {
			
			error = new RestException(status);
			
		} catch (RestException x) {
			error = x;
			status = x.getStatus();
		} catch (Exception x) {
			error = x;
			status = -1;
		} finally {
			try {in.close();} catch (Exception x) {}
			try {uc.disconnect();} catch (Exception x) {}


		}
		return isDone();
	}	
	protected URL handleOutputRDF(InputStream in,int status) throws RestException {
		URL ref = result;
		
		if ((HttpURLConnection.HTTP_OK == status) || 
			 (HttpURLConnection.HTTP_ACCEPTED == status) || 
			 (HttpURLConnection.HTTP_CREATED == status) || 
			 (HttpURLConnection.HTTP_SEE_OTHER == status)) {
			Model jenaModel = null;
			try {
				jenaModel = ModelFactory
								.createDefaultModel(ReificationStyle.Minimal);

						jenaModel.setNsPrefix("owl", OWL.NS);
						jenaModel.setNsPrefix("dc", DC.NS);
						jenaModel.setNsPrefix("xsd", XSDDatatype.XSD + "#");
						
				jenaModel = jenaModel.read(in,"application/rdf+xml");
				Resource theTask  = jenaModel.createResource(result.toString());
				//Property hasStatus = jenaModel.createProperty("http://www.opentox.org/api/1.1#hasStatus");

				Property resultURI = jenaModel.createProperty("http://www.opentox.org/api/1.1#resultURI");
				StmtIterator i  =  jenaModel.listStatements(new SimpleSelector(theTask,resultURI,(RDFNode) null));
				Statement st = null;
				while (i.hasNext()) {
					st = i.next();
					if (st.getObject().isLiteral()) {
						ref = new URL(((Literal)st.getObject()).getString());
					} else if (st.getObject().isURIResource()) {
						ref = new URL(((Resource)st.getObject()).getURI());
					} else ref = new URL(st.getObject().toString());
					
				}	
				i.close();
			} catch (Exception x) {
				throw new RestException(HttpURLConnection.HTTP_BAD_GATEWAY,x.getMessage(),x);
			} finally {
				try { jenaModel.close(); } catch (Exception x) {} ;
				try { in.close(); } catch (Exception x) {} ;
			}
			//if (count == 0) 
				//throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,"No status indications!");
			
			return ref;
						
		} else { //everything else considered an error
			throw new RestException(status);
		}
	}	
	
	public void waitUntilCompleted(int sleepInterval) throws Exception {
		FibonacciSequence seq = new FibonacciSequence();
		while (!poll()) {
			Thread.yield();
			Thread.sleep(seq.sleepInterval(sleepInterval,true,1000 * 60 * 5)); //
			//TODO timeout
			System.out.print("poll ");
			System.out.println(this);
		}
		if (isERROR()) throw getError();
	}	
}