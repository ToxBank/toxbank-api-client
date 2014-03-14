package net.toxbank.client.resource;

import java.io.InputStream;

import junit.framework.Assert;
import net.toxbank.client.TBClient;
import net.toxbank.client.io.rdf.TOXBANK;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class SPARQLTest {
	static protected Model model;
	
	
	@Test
	public void test() throws Exception {
		Assert.assertNotNull(model);
		String query = loadQuery("characteristics_by_investigation");
		System.out.println(query);
	}
	
	public String loadQuery(String query) throws Exception {
		String q = String.format("net/toxbank/client/sparql/%s.sparql", query);
		System.out.println(q);
		InputStream in = TBClient.class.getClassLoader().getResourceAsStream(q);
		Assert.assertNotNull(in);
		try {
	        //Z means: "The end of the input but for the final terminator, if any"
	     return new java.util.Scanner(in,"UTF8").useDelimiter("\\Z").next();
		} finally {
			if (in!=null) in.close();
		}
	}
	@BeforeClass
	public static void loadModel() throws Exception {	
		InputStream in = SPARQLTest.class.getClassLoader().getResourceAsStream("net/toxbank/metadata/isatab.n3");
		try {
			Assert.assertNotNull(in);
			model = ModelFactory.createDefaultModel();
			model.setNsPrefix("dcterms", DCTerms.NS);
			model.setNsPrefix("rdfs", RDFS.getURI());
			model.setNsPrefix("rdf", RDF.getURI());
			model.setNsPrefix("owl", OWL.getURI());
			model.setNsPrefix("tb", TOXBANK.URI);
			model.read(in,null,"N3");
		} catch (Exception x) {
			throw x;
		} finally {
			try { if (in!=null) in.close(); } catch (Exception x) {}
		}
	}

	@AfterClass
	public static void closeModel() throws Exception {
		if (model!=null) model.close(); 
	}
	
}
