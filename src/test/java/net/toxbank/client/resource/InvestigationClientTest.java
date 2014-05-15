package net.toxbank.client.resource;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.toxbank.client.TBClient;
import net.toxbank.client.policy.AccessRights;
import net.toxbank.client.policy.GroupPolicyRule;
import net.toxbank.client.policy.PolicyRule;
import net.toxbank.client.policy.UserPolicyRule;
import net.toxbank.client.task.RemoteTask;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class InvestigationClientTest {
  public static final String BII_TEST_CASE_ACCESSION_ID = "BII-I-1-test";
  
  //should be configured in the .m2/settings.xml 
  protected static final String test_user_server_property = "toxbank.test.server"; 
  protected static final String test_server_property = "toxbank.investigation.test.server";
  protected static final String aa_server_property = "toxbank.aa.opensso";
  protected static final String aa_user_property = "toxbank.aa.user";
  protected static final String aa_pass_property = "toxbank.aa.pass";
  protected static Properties properties;
  
  protected static String TEST_SERVER;
  protected static String INVESTIGATION_ROOT;
  protected static String USER_SERVICE_ROOT;
  protected static String ORGANISATION_SERVICE_ROOT;
  protected static String PROJECT_SERVICE_ROOT;
  
  public final static TBClient tbclient = new TBClient();
  
  private static InvestigationClient investigationClient;
  
  @BeforeClass
  public static void setup() throws Exception {
    config();
    
    String username = properties.getProperty(aa_user_property);
    String pass = properties.getProperty(aa_pass_property);
    //ensure maven profile properties are configured and set corgrectly
    Assert.assertNotNull(username);
    Assert.assertNotNull(pass);
    if (String.format("${%s}",aa_user_property).equals(username) ||
      String.format("${%s}",aa_pass_property).equals(pass)) 
      throw new Exception(String.format("The following properties are not found in the acive Maven profile ${%s} ${%s}",
          aa_user_property,aa_pass_property));
    boolean ok = tbclient.login(username,pass);
    // FileWriter fw = new FileWriter(new File("InvestigationClientTest-queries.txt"));
    investigationClient = tbclient.getInvestigationClient();
    // investigationClient.setQueryDebuggingWriter(fw);
    Assert.assertTrue(ok);
  }
  

  @AfterClass
  public static void teardown() throws Exception {
    try {
      if (investigationClient.getQueryDebuggingWriter() != null) {
        investigationClient.getQueryDebuggingWriter().close();
      }
    } catch (Exception e) { }
    tbclient.logout();
    tbclient.close();
  }
  
  private static String getPropertyUrl(Properties props, String prop) throws Exception {
    String value = properties.getProperty(prop);
    if (value == null) {
      throw new Exception("No " + prop + " was specified");
    }
    if (!value.startsWith("http")) {
      throw new Exception("Invalid " + prop + ": " + value);
    }
    if (value.endsWith("/")) {
      value = value.substring(0, value.length()-1);
    }
    return value;
  }
  
  private static void config()  {
    try {
      properties = new Properties();
      properties.load(AbstractClientTest.class.getClassLoader().getResourceAsStream("net/toxbank/client/test/client.properties"));
      TEST_SERVER = getPropertyUrl(properties, test_server_property);
      INVESTIGATION_ROOT = TEST_SERVER + "/investigation";
      USER_SERVICE_ROOT = getPropertyUrl(properties, test_user_server_property) + "/user";
      PROJECT_SERVICE_ROOT = getPropertyUrl(properties, test_user_server_property) + "/project";
      ORGANISATION_SERVICE_ROOT = getPropertyUrl(properties, test_user_server_property) + "/organisation";
    } catch (Exception x) {
      throw new RuntimeException(x);
    }
  }

  private String getUserUrl() throws Exception {
    User user = tbclient.getLoggedInUser(USER_SERVICE_ROOT);
    if (user == null) {
      throw new RuntimeException("Logged in user could not be found in user service");
    }
    return user.getResourceURL().toString();
  }
  
  protected InvestigationClient getToxBankClient() {
    return investigationClient;
  }

  @Test
  public void testList() throws Throwable {
    List<URL> urls = getToxBankClient().listInvestigationUrls(new URL(INVESTIGATION_ROOT));
    Assert.assertNotNull("Should not have null list of urls", urls);
    Assert.assertNotSame("Should have a list of urls", 0, urls.size());
  }
  
  @Test
  public void testGetAll() throws Throwable {
    List<URL> urls = getToxBankClient().listInvestigationUrls(new URL(INVESTIGATION_ROOT));
    Assert.assertNotNull("Should not have null list of urls", urls);
    Assert.assertNotSame("Should have a list of urls", 0, urls.size());
    
    int maxUrls = 5;

    for (int i = 0; i < maxUrls && i < urls.size(); i++) {
      URL url = urls.get(i);
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
    Investigation newInvestigation = new Investigation();
    newInvestigation.setSearchable(false);
    RemoteTask task = getToxBankClient().postInvestigation(
        new File(fileUrl.toURI()), new URL(INVESTIGATION_ROOT), new ArrayList<PolicyRule>(0), newInvestigation, null);
    task.waitUntilCompleted(1000);
    URL postedURL = task.getResult();
    System.out.println("Posted new investigation: " + postedURL);
    Investigation investigation = getToxBankClient().getInvestigation(postedURL);
    verifyBiiInvestigation(investigation, false);
    String userUrl = getUserUrl();
        
    TestCase.assertNotNull("Should have owner", investigation.getOwner());
    TestCase.assertEquals(userUrl, investigation.getOwner().getResourceURL().toString());
    
    User owner = new User();
    URL ownerUrl = new URL(userUrl);
    owner.setResourceURL(ownerUrl);
    List<TimestampedUrl> userUrls = getToxBankClient().listTimestampedInvestigations(new URL(INVESTIGATION_ROOT), owner);
    boolean hasPostedUrl = false;
    for (TimestampedUrl url : userUrls) {
      if ((postedURL.toString() + "/").equals(url.getUrl().toString())) {
        hasPostedUrl = true;
      }
    }
    Assert.assertTrue("The loaded investigation should be included in the owner's list",
        hasPostedUrl);
    
    System.out.println("Deleting investigation: " + investigation.getResourceURL());
    getToxBankClient().deleteInvestigation(investigation.getResourceURL());
    System.out.println("Deleted investigation: " + investigation.getResourceURL());
  }

  @Test
  public void testPostAndDeleteWithPermissions() throws Throwable {
    URL fileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1.zip");    
    List<PolicyRule> accessRights = createPolicyRules();     
    Investigation newInvestigation = new Investigation();
    newInvestigation.setSearchable(false);
    RemoteTask task = getToxBankClient().postInvestigation(new File(fileUrl.toURI()), new URL(INVESTIGATION_ROOT), accessRights, newInvestigation, null);
    task.waitUntilCompleted(1000);
    URL postedURL = task.getResult();
    
    System.out.println("Posted new investigation: " + postedURL);
    
    Investigation investigation = getToxBankClient().getInvestigation(postedURL);
    verifyBiiInvestigation(investigation, false);
    verifyPolicyRules(investigation);
    String userUrl = getUserUrl();
        
    TestCase.assertNotNull("Should have owner", investigation.getOwner());
    TestCase.assertEquals(userUrl, investigation.getOwner().getResourceURL().toString());
    
    User owner = new User();
    URL ownerUrl = new URL(userUrl);
    owner.setResourceURL(ownerUrl);
    List<TimestampedUrl> userUrls = getToxBankClient().listTimestampedInvestigations(new URL(INVESTIGATION_ROOT), owner);
    boolean hasPostedUrl = false;
    for (TimestampedUrl url : userUrls) {
      if ((postedURL.toString() + "/").equals(url.getUrl().toString())) {
        hasPostedUrl = true;
      }
    }
    Assert.assertTrue("The loaded investigation should be included in the owner's list",
        hasPostedUrl);
    
    System.out.println("Deleting investigation: " + investigation.getResourceURL());
    getToxBankClient().deleteInvestigation(investigation.getResourceURL());
    System.out.println("Deleted investigation: " + investigation.getResourceURL());
  }

  @Test
  public void testPostPublishAndDelete() throws Throwable {
    URL fileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1.zip");
    Investigation newInvestigation = new Investigation();
    newInvestigation.setSearchable(false);
    RemoteTask task = getToxBankClient().postInvestigation(new File(fileUrl.toURI()), new URL(INVESTIGATION_ROOT), new ArrayList<PolicyRule>(0), newInvestigation, null);
    task.waitUntilCompleted(1000);
    URL postedURL = task.getResult();
    System.out.println("Posted new investigation: " + postedURL);
    Investigation investigation = getToxBankClient().getInvestigation(postedURL);
    verifyBiiInvestigation(investigation, false);
    
    TestCase.assertNotNull("Should have owner", investigation.getOwner());
    TestCase.assertEquals(getUserUrl(), investigation.getOwner().getResourceURL().toString());
    TestCase.assertNotNull("Should have a published status set", investigation.isPublished());
    TestCase.assertEquals("Should not be published", Boolean.FALSE, investigation.isPublished());

    task = getToxBankClient().publishInvestigation(investigation, true);
    task.waitUntilCompleted(1000);
    Investigation updatedInvestigation = getToxBankClient().getInvestigation(postedURL);
    TestCase.assertEquals(Boolean.TRUE, updatedInvestigation.isPublished());
    
    System.out.println("Deleting investigation: " + investigation.getResourceURL());
    getToxBankClient().deleteInvestigation(investigation.getResourceURL());
    System.out.println("Deleted investigation: " + investigation.getResourceURL());
  }

  @Test
  public void testPostAndUpdate() throws Throwable {
    URL fileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1.zip");
    Investigation newInvestigation = new Investigation();
    newInvestigation.setSearchable(false);
    RemoteTask task = getToxBankClient().postInvestigation(new File(fileUrl.toURI()), new URL(INVESTIGATION_ROOT), new ArrayList<PolicyRule>(0), newInvestigation, null);
    task.waitUntilCompleted(1000);
    URL postedURL = task.getResult();
    System.out.println("Posted new investigation: " + postedURL);
    Investigation investigation = getToxBankClient().getInvestigation(postedURL);
    verifyBiiInvestigation(investigation, false);
    
    URL newFileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1-smiller.zip");
    RemoteTask newTask = getToxBankClient().updateInvestigation(new File(newFileUrl.toURI()), investigation, new ArrayList<PolicyRule>(0), null);
    newTask.waitUntilCompleted(1000);
    URL postedUpdateURL = task.getResult();
    Assert.assertEquals("Should have same update url as original posted url", postedURL, postedUpdateURL);
    System.out.println("Updated investigation: " + investigation.getResourceURL());
    
    Investigation updatedInvestigation = getToxBankClient().getInvestigation(investigation.getResourceURL());
    TestCase.assertEquals("Should have new title in updated investigation", 
        "Growth control of the eukaryote cell: a systems biology study in yeast - updated", updatedInvestigation.getTitle());
    TestCase.assertNotNull("Updated investigation should have owner", updatedInvestigation.getOwner());
    TestCase.assertEquals("Updated investigation should have same owner", getUserUrl(), 
        updatedInvestigation.getOwner().getResourceURL().toString());
    
    System.out.println("Deleting investigation: " + investigation.getResourceURL());
    getToxBankClient().deleteInvestigation(investigation.getResourceURL());
    System.out.println("Deleted investigation: " + investigation.getResourceURL());
  }

  @Test
  public void testPostAndUpdateRemovingPermissions() throws Throwable {
    URL fileUrl = getClass().getClassLoader().getResource("net/toxbank/client/test/BII-I-1.zip");
    List<PolicyRule> accessRights = createPolicyRules();
    Investigation newInvestigation = new Investigation();
    newInvestigation.setSearchable(false);
    RemoteTask task = getToxBankClient().postInvestigation(new File(fileUrl.toURI()), new URL(INVESTIGATION_ROOT), accessRights, newInvestigation, null);
    task.waitUntilCompleted(1000);
    URL postedURL = task.getResult();
    System.out.println("Posted new investigation: " + postedURL);
    Investigation investigation = getToxBankClient().getInvestigation(postedURL);
    verifyBiiInvestigation(investigation, false);
    verifyPolicyRules(investigation);
    
    RemoteTask newTask = getToxBankClient().updateInvestigation(investigation, new ArrayList<PolicyRule>(0));
    newTask.waitUntilCompleted(1000);
    URL postedUpdateURL = task.getResult();
    Assert.assertEquals("Should have same update url as original posted url", postedURL, postedUpdateURL);
    System.out.println("Updated investigation: " + investigation.getResourceURL());

    Investigation updatedInvestigation = getToxBankClient().getInvestigation(investigation.getResourceURL());
    verifyBiiInvestigation(updatedInvestigation, false);
    
    TestCase.assertNotNull("Updated investigation should have owner", updatedInvestigation.getOwner());
    TestCase.assertEquals("Updated investigation should have same owner", getUserUrl(), 
        updatedInvestigation.getOwner().getResourceURL().toString());
    
    verifyOwnerOnlyPolicyRules(updatedInvestigation);
    
    System.out.println("Deleting investigation: " + investigation.getResourceURL());
    getToxBankClient().deleteInvestigation(investigation.getResourceURL());
    System.out.println("Deleted investigation: " + investigation.getResourceURL());
  }

  private static List<PolicyRule> createPolicyRules() throws Exception {
    List<PolicyRule> policyRules = new ArrayList<PolicyRule>();
    
    User otherUser = new User(new URL("http://toxbanktest1.opentox.org:8080/toxbank/user/U115"));
    UserPolicyRule otherUserRule = new UserPolicyRule(otherUser, true, false, false, false);
    policyRules.add(otherUserRule);

    Project otherProject = new Project(new URL("http://toxbanktest1.opentox.org:8080/toxbank/project/G2"));
    GroupPolicyRule otherProjectRule = new GroupPolicyRule(otherProject, true, false, false, false);
    policyRules.add(otherProjectRule);

    return policyRules;
  }
    
  private static void verifyPolicyRules(Investigation i) throws Throwable {
    List<PolicyRule> testRules = createPolicyRules();
    PolicyRule ownerRule = new PolicyRule(tbclient.getLoggedInUser(USER_SERVICE_ROOT), true, true, true, true);
    testRules.add(ownerRule);    
    verifyPolicyRules(i, testRules);
  }

  private static void verifyOwnerOnlyPolicyRules(Investigation i) throws Throwable {
    List<PolicyRule> testRules = new ArrayList<PolicyRule>();
    PolicyRule ownerRule = new PolicyRule(tbclient.getLoggedInUser(USER_SERVICE_ROOT), true, true, true, true);
    testRules.add(ownerRule);    
    verifyPolicyRules(i, testRules);
  }

  private static void verifyPolicyRules(Investigation i, List<PolicyRule> testRules) throws Throwable {
    List<AccessRights> rights = tbclient.readPolicy(i.getResourceURL());
    List<PolicyRule> rules = new ArrayList<PolicyRule>();
    for (AccessRights right : rights) {
      rules.addAll(right.getRules());
    }

    for (PolicyRule testRule : testRules) {
      PolicyRule matchingRule = null;
      for (PolicyRule rule : rules) {
        fillInSubject(rule);
        IToxBankResource subject = rule.getSubject();
        TestCase.assertNotNull("PolicyRule subject is null", subject.getResourceURL());
        IToxBankResource testSubject = testRule.getSubject();
        if (subject.getResourceURL().equals(testSubject.getResourceURL())) {
          TestCase.assertEquals("Should have same get for " + testSubject.getResourceURL(), testRule.allowsGET(), rule.allowsGET());
          TestCase.assertEquals("Should have same put for " + testSubject.getResourceURL(), testRule.allowsPUT(), rule.allowsPUT());
          TestCase.assertEquals("Should have same post for " + testSubject.getResourceURL(), testRule.allowsPUT(), rule.allowsPOST());
          TestCase.assertEquals("Should have same delete for " + testSubject.getResourceURL(), testRule.allowsPUT(), rule.allowsDELETE());
          matchingRule = rule;
        }
      }
      if (matchingRule == null) {
        TestCase.fail("Did not have a rule for: " + testRule.getSubject().getResourceURL());
      }
      else {
        rules.remove(matchingRule);
      }
    }
    
    if (rules.size() > 0) {
      StringBuilder sb = new StringBuilder();
      for (PolicyRule rule : rules) {
        sb.append(rule.getSubject().getResourceURL());
        sb.append("\n");
      }
      TestCase.fail("Had extraneous rules: " + rules.size() + "\n  " + sb.toString());
    }
  }

  
  private static void fillInSubject(PolicyRule rule) throws Throwable {
    if (rule instanceof UserPolicyRule) {
      User user = ((UserPolicyRule<User>)rule).getSubject();
      User filledInUser = tbclient.getUserByUsername(USER_SERVICE_ROOT, user.getUserName());
      if (filledInUser == null) {
        throw new RuntimeException("Could not find real user for: " + user.getUserName());
      }
      rule.setSubject(filledInUser);
    }
    else if (rule instanceof GroupPolicyRule) {
      Group group = ((GroupPolicyRule<? extends Group>)rule).getSubject();
      Project filledInProject = getProjectByGroupName(group.getGroupName());
      if (filledInProject != null) {
        rule.setSubject(filledInProject);
      }
      else {
        Organisation filledInOrg = getOrganisationByGroupName(group.getGroupName());
        if (filledInOrg != null) {
          rule.setSubject(filledInOrg);
        }
        else {
          throw new RuntimeException("Could not find project or organisation for group name: " + group.getGroupName());
        }
      }
    }
  }
  
  private static Project getProjectByGroupName(String groupName) throws Exception {
    List<Project> allProjects = tbclient.getProjectClient().get(new URL(PROJECT_SERVICE_ROOT));
    for (Project project : allProjects) {
      if (groupName.equals(project.getGroupName())) {
        return project;
      }
    }
    return null;
  }
  
  private static Organisation getOrganisationByGroupName(String groupName) throws Exception {
    List<Organisation> allOrgs = tbclient.getOrganisationClient().get(new URL(ORGANISATION_SERVICE_ROOT));
    for (Organisation org : allOrgs) {
      if (groupName.equals(org.getGroupName())) {
        return org;
      }
    }
    return null;
  }

  private static void verifyBiiInvestigation(Investigation i, boolean allowMissingKeywords) throws Throwable {
    TestCase.assertEquals(BII_TEST_CASE_ACCESSION_ID, i.getAccessionId());
    TestCase.assertEquals("Growth control of the eukaryote cell: a systems biology study in yeast", i.getTitle());
    TestCase.assertEquals("Background Cell growth underlies many key cellular and developmental processes, yet a limited number of studies have been carried out on cell-growth regulation. Comprehensive studies at the transcriptional, proteomic and metabolic levels under defined controlled conditions are currently lacking. Results Metabolic control analysis is being exploited in a systems biology study of the eukaryotic cell. Using chemostat culture, we have measured the impact of changes in flux (growth rate) on the transcriptome, proteome, endometabolome and exometabolome of the yeast Saccharomyces cerevisiae. Each functional genomic level shows clear growth-rate-associated trends and discriminates between carbon-sufficient and carbon-limited conditions. Genes consistently and significantly upregulated with increasing growth rate are frequently essential and encode evolutionarily conserved proteins of known function that participate in many protein-protein interactions. In contrast, more unknown, and fewer essential, genes are downregulated with increasing growth rate; their protein products rarely interact with one another. A large proportion of yeast genes under positive growth-rate control share orthologs with other eukaryotes, including humans. Significantly, transcription of genes encoding components of the TOR complex (a major controller of eukaryotic cell growth) is not subject to growth-rate regulation. Moreover, integrative studies reveal the extent and importance of post-transcriptional control, patterns of control of metabolic fluxes at the level of enzyme synthesis, and the relevance of specific enzymatic reactions in the control of metabolic fluxes during cell growth. Conclusion This work constitutes a first comprehensive systems biology study on growth-rate control in the eukaryotic cell. The results have direct implications for advanced studies on cell growth, in vivo regulation of metabolic fluxes for comprehensive metabolic engineering, and for the design of genome-scale systems biology models of the eukaryotic cell.",
        i.getAbstract());
    TestCase.assertNotNull("Should have organisation", i.getOrganisation());
    TestCase.assertEquals("Should have project", 1, i.getProjects().size());
    if (!allowMissingKeywords) {
      TestCase.assertEquals("Should have 3 keywords", 3, i.getKeywords().size());
      TestCase.assertTrue("Should contain the cell migrations keyword", 
          i.getKeywords().contains("http://www.owl-ontologies.com/toxbank.owl#CellMigrationAssays"));
    }
  }
}
