package net.toxbank.client.resource;

import org.junit.Assert;
import org.junit.Test;

public class AlertClientTest {
	
	@Test
	public void testConstructor() {
		AlertClient alert = new AlertClient();
		Assert.assertNotNull(alert);
	}

}
