package net.toxbank.client.policy;

import java.net.URL;

import net.toxbank.client.resource.Group;
import net.toxbank.client.resource.User;

import org.opentox.aa.policy.PolicyParser;

/**
 * Parses OpenSSO XML policies.  Should be moved to opentox-opensso library
 * <pre>http://java.net/projects/opensso/</pre>
 * @author nina

 */
public class TBPolicyParser  extends PolicyParser<User, Group, PolicyRule, AccessRights> {

	public TBPolicyParser(String content) throws Exception {
		super(content);
	}
	
	@Override
	protected AccessRights createEmptyPolicy() {
		return new AccessRights(null);
	}



	@Override
	protected void addPolicyRule(AccessRights policy, PolicyRule policyRule) {
		policy.addRule(policyRule);
	}



	@Override
	protected void setPolicyID(AccessRights policy, String policyID) {
		policy.setPolicyID(policyID);
		
	}



	@Override
	protected void setResource(AccessRights policy, URL url) {
		policy.setResource(url);
	}



	@Override
	protected User createUser(String username) {
		User user = new User();
		user.setUserName(username);
		return user;
	}



	@Override
	protected Group createGroup(String groupName) {
		Group group = new Group();
		group.setGroupName(groupName);
		return group;
	}



	@Override
	protected void setPolicyRuleMethod(PolicyRule policyRule,
			org.opentox.aa.policy.Method method, Boolean value) {
		policyRule.setAllow(method.name(),value);
		
	}



	@Override
	protected PolicyRule createPolicyRule(String name, User user, Group group) {
		PolicyRule rule = null;
		if (user != null) rule = new UserPolicyRule<User>(user);
		else if (group != null) rule = new GroupPolicyRule<Group>(group);
		rule.setName(name);
		return rule;
	}	
}
