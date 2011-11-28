package net.toxbank.client.resource;

import org.junit.Assert;
import org.junit.Test;

public class DocumentClientTest {
	
	@Test
	public void testConstructor() {
		DocumentClient clazz = new DocumentClient();
		Assert.assertNotNull(clazz);
	}

}
