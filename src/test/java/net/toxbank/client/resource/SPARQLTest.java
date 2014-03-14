package net.toxbank.client.resource;

import java.io.InputStream;

import junit.framework.Assert;
import net.toxbank.client.TBClient;
import net.toxbank.client.io.rdf.TOXBANK;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class SPARQLTest {
	static protected Model model;
	
	
	@Test
	public void test_characteristics_by_investigation() throws Exception {
		Assert.assertNotNull(model);
		String sparqlQuery = String.format(
				loadQuery("characteristics_by_investigation","{investigation_uri}"),
				"https://services.toxbank.net/investigation/6c81b6f9-1684-41e6-ae02-e1b45ef60741/I2");
		Assert.assertTrue(execQuery(sparqlQuery)>0);
	}
	
	@Test
	public void test_characteristics_by_sample() throws Exception {
		Assert.assertNotNull(model);
		String sparqlQuery = String.format(
				loadQuery("characteristics_by_sample","{sample_uri}"),
				"https://services.toxbank.net/investigation/6c81b6f9-1684-41e6-ae02-e1b45ef60741/source417");
		Assert.assertTrue(execQuery(sparqlQuery)>0);
		
		sparqlQuery = String.format(
				loadQuery("characteristics_by_sample","{sample_uri}"),
				"https://services.toxbank.net/investigation/6c81b6f9-1684-41e6-ae02-e1b45ef60741/source405");

	
		Assert.assertTrue(execQuery(sparqlQuery)>0);
		
	}
	
	@Test
	public void test_factorvalues_by_investigation() throws Exception {
		Assert.assertNotNull(model);
		String sparqlQuery = String.format(
				loadQuery("factorvalues_by_investigation","{investigation_uri}"),
				"https://services.toxbank.net/investigation/6c81b6f9-1684-41e6-ae02-e1b45ef60741/I2");
	
		
		Assert.assertTrue(
				execQuery(sparqlQuery, new ProcessSolution() {
					void process(ResultSet rs, QuerySolution qs) {
						String biosample = qs.getResource("biosample").getURI();
						try {
							String subSelect = String.format(
									loadQuery("characteristics_by_sample","{sample_uri}"),
									biosample);
							Assert.assertTrue(execQuery(subSelect)>0);
						} catch (Exception x) {
							x.printStackTrace();
						}
						super.process(rs, qs);						
					};
				}
				)>0);		
	}
	
	protected int execQuery(String sparqlQuery) {
		return execQuery(sparqlQuery,new ProcessSolution());
	}
	protected int execQuery(String sparqlQuery,ProcessSolution processor) {
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		int records = 0;
		try {
			ResultSet rs = qe.execSelect();
			records = processor.process(rs);
		} finally {
			qe.close();	
		}
		return records;
	}
	public String loadQuery(String sparqlQuery,String param) throws Exception {
		String q = String.format("net/toxbank/client/sparql/%s.sparql", sparqlQuery);
		InputStream in = TBClient.class.getClassLoader().getResourceAsStream(q);
		Assert.assertNotNull(in);
		try {
	        //Z means: "The end of the input but for the final terminator, if any"
	     return new java.util.Scanner(in,"UTF8").useDelimiter("\\Z").next().replace(param,"s");
		} finally {
			if (in!=null) in.close();
		}
	}
	@BeforeClass
	public static void loadModel() throws Exception {	
		InputStream in = SPARQLTest.class.getClassLoader().getResourceAsStream("net/toxbank/metadata/tggates/isatab.n3");
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

class ProcessSolution {
	public int process(ResultSet rs) {
		int records = 0;
		System.out.println();
		while (rs.hasNext()) {
			records++;
			QuerySolution qs = rs.next();
			process(rs,qs);
		}
		return records;
	}
	void processHeader(ResultSet rs) {
		for (String name : rs.getResultVars()) {
			System.out.print(name);
			System.out.print("\t");
		}		
	}
	void process(ResultSet rs,QuerySolution qs) {
		for (String name : rs.getResultVars()) {
			RDFNode node = qs.get(name);
			if (node ==null) ;
			else if (node.isLiteral()) System.out.print(node.asLiteral().getString());
			else if (node.isResource()) System.out.print(node.asResource().getURI());
			else System.out.print(node.asNode().getName());
			 System.out.print("\t");
		}
		System.out.println();
	}
}
