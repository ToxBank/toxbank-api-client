package net.toxbank.client.resource;

import org.junit.Assert;
import org.junit.Test;

public class AccountTest {
	
	@Test
	public void testConstructor() {
		Account account = new Account();
		Assert.assertNotNull(account);
	}

}
