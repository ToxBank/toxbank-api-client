package net.toxbank.client.resource;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.toxbank.client.TBClient;
import net.toxbank.client.task.RemoteTask;

import org.junit.*;

public class InvestigationClientTest {
  public static final String BII_TEST_CASE_ACCESSION_ID = "BII-I-1-test";
  
  //should be configured in the .m2/settings.xml 
  protected static final String test_server_property = "toxbank.investigation.test.server";
  protected static final String aa_server_property = "toxbank.aa.opensso";
  protected static final String aa_user_property = "toxbank.aa.user";
  protected static final String aa_pass_property = "toxbank.aa.pass";
  protected static Properties properties;
  
  protected static String TEST_SERVER;
  
  public final static TBClient tbclient = new TBClient();
  @BeforeClass
  public static void setup() throws Exception {
    TEST_SERVER = config();
    
    String username = properties.getProperty(aa_user_property);
    String pass = properties.getProperty(aa_pass_property);
    //ensure maven profile properties are configured and set correctly
    Assert.assertNotNull(username);
    Assert.assertNotNull(pass);
    if (String.format("${%s}",aa_user_property).equals(username) ||
      String.format("${%s}",aa_pass_property).equals(pass)) 
      throw new Exception(String.format("The following properties are not found in the acive Maven profile ${%s} ${%s}",
          aa_user_property,aa_pass_property));
    boolean ok = tbclient.login(username,pass);
    Assert.assertTrue(ok);
  }
  

  @AfterClass
  public static void teardown() throws Exception {
    tbclient.logout();
    tbclient.close();
  }
  
  private static String config()  {
    try {
      properties = new Properties();
      properties.load(AbstractClientTest.class.getClassLoader().getResourceAsStream("net/toxbank/client/test/client.properties"));
      String testServer = properties.getProperty(test_server_property);
      if (testServer == null) {
        throw new Exception("No " + test_server_property + " was specified");
      }
      if (!testServer.startsWith("http")) {
        throw new Exception("Invalid test server url: " + testServer);
      }
      return testServer;
    } catch (Exception x) {
      throw new RuntimeException(x);
    }
  }

  protected InvestigationClient getToxBankClient() {
    return tbclient.getInvestigationClient();
  }

  @Test
  public void testList() throws Throwable {
    List<URL> urls = getToxBankClient().listInvestigationUrls(new URL(TEST_SERVER));
    Assert.assertNotNull("Should not have null list of urls", urls);
    Assert.assertNotSame("Should have a list of urls", 0, urls.size());
  }
  
  @Test
  public void testGetAll() throws Throwable {
    List<URL> urls = getToxBankClient().listInvestigationUrls(new URL(TEST_SERVER));
    Assert.assertNotNull("Should not have null list of urls", urls);
    Assert.assertNotSame("Should have a list of urls", 0, urls.size());
    
    for (URL url : urls) {
      System.out.println("Retrieving investigation for: " + url);
      Investigation investigation = getToxBankClient().getInvestigation(url);
      Assert.assertNotNull("Should have investigation for url: " + url, investigation);
      if (investigation.getAccessionId().equals(BII_TEST_CASE_ACCESSION_ID)) {
        verifyBiiInvestigation(investigation, true);
      }
    }    
  }
  
  @Test
  public void testPostAndDelete() throws Throwable {
    URL fileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1.zip");
    RemoteTask task = getToxBankClient().postInvestigation(new File(fileUrl.toURI()), new URL(TEST_SERVER));
    task.waitUntilCompleted(1000);
    URL postedURL = task.getResult();
    System.out.println("Posted new investigation: " + postedURL);
    Investigation investigation = getToxBankClient().getInvestigation(postedURL);
    verifyBiiInvestigation(investigation, false);
    
    TestCase.assertNotNull("Should have owner", investigation.getOwner());
    TestCase.assertEquals("http://toxbanktest1.opentox.org:8080/toxbank/user/U115", investigation.getOwner().getResourceURL().toString());
    
    User owner = new User();
    URL ownerUrl = new URL("http://toxbanktest1.opentox.org:8080/toxbank/user/U115");
    owner.setResourceURL(ownerUrl);
    List<URL> userUrls = getToxBankClient().listInvestigationUrls(new URL(TEST_SERVER), owner);
    Assert.assertTrue("The loaded investigation should be included in the owner's list",
        userUrls.contains(new URL(postedURL.toString() + "/")));
    
    System.out.println("Deleting investigation: " + investigation.getResourceURL());
    getToxBankClient().deleteInvestigation(investigation.getResourceURL());
    System.out.println("Deleted investigation: " + investigation.getResourceURL());
  }
  
  @Test
  public void testPostAndUpdate() throws Throwable {
    URL fileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1.zip");
    RemoteTask task = getToxBankClient().postInvestigation(new File(fileUrl.toURI()), new URL(TEST_SERVER));
    task.waitUntilCompleted(1000);
    URL postedURL = task.getResult();
    System.out.println("Posted new investigation: " + postedURL);
    Investigation investigation = getToxBankClient().getInvestigation(postedURL);
    verifyBiiInvestigation(investigation, false);
    
    URL newFileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1-smiller.zip");
    RemoteTask newTask = getToxBankClient().updateInvestigation(new File(newFileUrl.toURI()), investigation);
    newTask.waitUntilCompleted(1000);
    System.out.println("Updated investigation: " + investigation.getResourceURL());
    
    Investigation updatedInvestigation = getToxBankClient().getInvestigation(investigation.getResourceURL());
    TestCase.assertNotNull("Updated investigation should have owner", updatedInvestigation.getOwner());
    TestCase.assertEquals("http://toxbanktest1.opentox.org:8080/toxbank/user/U72", updatedInvestigation.getOwner().getResourceURL().toString());
    
    System.out.println("Deleting investigation: " + investigation.getResourceURL());
    getToxBankClient().deleteInvestigation(investigation.getResourceURL());
    System.out.println("Deleted investigation: " + investigation.getResourceURL());
  }
  
  private static void verifyBiiInvestigation(Investigation i, boolean allowMissingKeywords) throws Throwable {
    TestCase.assertEquals(BII_TEST_CASE_ACCESSION_ID, i.getAccessionId());
    TestCase.assertEquals("Growth control of the eukaryote cell: a systems biology study in yeast", i.getTitle());
    TestCase.assertEquals("Background Cell growth underlies many key cellular and developmental processes, yet a limited number of studies have been carried out on cell-growth regulation. Comprehensive studies at the transcriptional, proteomic and metabolic levels under defined controlled conditions are currently lacking. Results Metabolic control analysis is being exploited in a systems biology study of the eukaryotic cell. Using chemostat culture, we have measured the impact of changes in flux (growth rate) on the transcriptome, proteome, endometabolome and exometabolome of the yeast Saccharomyces cerevisiae. Each functional genomic level shows clear growth-rate-associated trends and discriminates between carbon-sufficient and carbon-limited conditions. Genes consistently and significantly upregulated with increasing growth rate are frequently essential and encode evolutionarily conserved proteins of known function that participate in many protein-protein interactions. In contrast, more unknown, and fewer essential, genes are downregulated with increasing growth rate; their protein products rarely interact with one another. A large proportion of yeast genes under positive growth-rate control share orthologs with other eukaryotes, including humans. Significantly, transcription of genes encoding components of the TOR complex (a major controller of eukaryotic cell growth) is not subject to growth-rate regulation. Moreover, integrative studies reveal the extent and importance of post-transcriptional control, patterns of control of metabolic fluxes at the level of enzyme synthesis, and the relevance of specific enzymatic reactions in the control of metabolic fluxes during cell growth. Conclusion This work constitutes a first comprehensive systems biology study on growth-rate control in the eukaryotic cell. The results have direct implications for advanced studies on cell growth, in vivo regulation of metabolic fluxes for comprehensive metabolic engineering, and for the design of genome-scale systems biology models of the eukaryotic cell.",
        i.getAbstract());
    TestCase.assertNotNull("Should have organisation", i.getOrganisation());
    TestCase.assertEquals("http://toxbanktest1.opentox.org:8080/toxbank/organisation/G176", i.getOrganisation().getResourceURL().toString());
    TestCase.assertNotNull("Should have project", i.getProject());
    TestCase.assertEquals("http://toxbanktest1.opentox.org:8080/toxbank/project/G2", i.getProject().getResourceURL().toString());
    TestCase.assertTrue("Should have one or more toxbank protocols", i.getProtocols().size() >= 1);
    for (Protocol protocol : i.getProtocols()) {
      TestCase.assertTrue("Should really be a toxbank protocol: " + protocol.getResourceURL(), 
          protocol.getResourceURL().toString().contains("protocol/SEURAT"));
    }
    if (!allowMissingKeywords) {
      TestCase.assertEquals("Should have 3 keywords", 3, i.getKeywords().size());
      TestCase.assertTrue("Should contain the cell migrations keyword", 
          i.getKeywords().contains("http://www.owl-ontologies.com/toxbank.owl#CellMigrationAssays"));
    }
  }
}
