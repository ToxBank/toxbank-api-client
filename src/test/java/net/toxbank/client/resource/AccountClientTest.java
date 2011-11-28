package net.toxbank.client.resource;

import org.junit.Assert;
import org.junit.Test;

public class AccountClientTest {
	
	@Test
	public void testConstructor() {
		AccountClient account = new AccountClient();
		Assert.assertNotNull(account);
	}

}
