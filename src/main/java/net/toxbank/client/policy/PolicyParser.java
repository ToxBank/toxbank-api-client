package net.toxbank.client.policy;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.toxbank.client.policy.PolicyRule.Method;
import net.toxbank.client.resource.Group;
import net.toxbank.client.resource.IToxBankResource;
import net.toxbank.client.resource.User;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses OpenSSO XML policies.  Should be moved to opentox-opensso library
 * <pre>http://java.net/projects/opensso/</pre>
 * @author nina

 */
public class PolicyParser {
	protected Document doc;
	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}
	public enum type {
		cn {
			@Override
			public String toString() {
				return "User";
			}
		},
		member {
			@Override
			public String toString() {
				return "members";
			}
		},
		LDAPUsers {
			@Override
			public String toString() {
				return "Applies to user ";
			}
		},
		LDAPGroups {
			@Override
			public String toString() {
				return "Applies to group ";
			}
		};
	}
	public enum tags {
		ResourceName,
		Policies,
		Policy,
		Rule,
		Subjects,
		Subject,
		AttributeValuePair,
		Attribute,
		Value
		
	}
	public PolicyParser(String content) throws Exception {
		doc = parse(content);
	}
	
	public Document parse(String content) throws IOException,  SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
       
        StringReader reader = new StringReader(content);
        Document doc = builder.parse(new InputSource(reader));
        doc.normalize();
        return doc;
	}
	
	public AccessRights getAccessRights() throws Exception {
		AccessRights accessRights = new AccessRights(null);
		
		Element top = doc.getDocumentElement();
		org.w3c.dom.NodeList policies = top.getElementsByTagName(tags.Policy.toString());
		for (int i=0; i < policies.getLength(); i++) {
			Element policy = (Element) policies.item(i);
			accessRights.setPolicyID(policy.getAttribute("name"));
			//subjects
			IToxBankResource user_or_group = null;
			org.w3c.dom.NodeList subjects = policy.getElementsByTagName(tags.Subjects.toString());
			for (int j=0; j < subjects.getLength(); j++) {
				Element subject = ((Element) subjects.item(j));
				org.w3c.dom.NodeList ss = subject.getElementsByTagName(tags.Subject.toString());
				for (int l=0; l < ss.getLength(); l++) {
					Element s = ((Element) ss.item(j));
					String name = s.getAttribute("name");
					if (name==null) continue;
					
					type t = null;
					try {
						t = type.valueOf(s.getAttribute("type"));
						switch (t) {
						case LDAPGroups: {
							Group group = new Group();
							group.setGroupName(name);
							user_or_group = group;
							break;
						}
						case LDAPUsers: {
							User user = new User();
							user.setUserName(name);
							user_or_group = user;
							break;
						}
						}
					} catch (Exception x) {
						
					}
					
					/*
					b.append(String.format("&nbsp;&nbsp;&nbsp;%s <b>%s</b> [%s]<br>\n",
							t,
							name,
							s.getAttribute("includeType")));
					*/
				}

			}
			//rules
			org.w3c.dom.NodeList rules = policy.getElementsByTagName(tags.Rule.toString());
			for (int j=0; j < rules.getLength(); j++) {
				Element rule = ((Element) rules.item(j));
				
				PolicyRule policyRule = user_or_group instanceof User?
							new UserPolicyRule((User)user_or_group):
							new GroupPolicyRule((Group)user_or_group);
				policyRule.setName(rule.getAttribute("name"));
				accessRights.addRule(policyRule);
				org.w3c.dom.NodeList ResourceName = rule.getElementsByTagName(tags.ResourceName.toString());
				for (int k=0; k < ResourceName.getLength(); k++) {
					Element attr = ((Element) ResourceName.item(k));
					accessRights.setResource(new URL(attr.getAttribute("name")));
				}	
				
				org.w3c.dom.NodeList attrs = rule.getElementsByTagName(tags.AttributeValuePair.toString());
				for (int k=0; k < attrs.getLength(); k++) {
					processAttrValuePair(((Element) attrs.item(k)),policyRule);
				}

			}
			
		}
		return accessRights;
	}
	
	public void processAttrValuePair(Element vp, PolicyRule policyRule) {
		org.w3c.dom.NodeList attr = vp.getElementsByTagName(tags.Attribute.toString());
		Method method = null;
		for (int k=0; k < attr.getLength(); k++) try {
			method = PolicyRule.Method.valueOf(((Element)attr.item(k)).getAttribute("name"));
		} catch (Exception x) {method = null;}
		if (method==null) return;
		org.w3c.dom.NodeList val = vp.getElementsByTagName(tags.Value.toString());
		for (int k=0; k < val.getLength(); k++) {
			String v = ((Element)val.item(k)).getTextContent();
			policyRule.setAllow(method,"allow".equals(v));
		}	
	}	
}
