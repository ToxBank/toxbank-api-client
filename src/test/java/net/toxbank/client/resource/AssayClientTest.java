package net.toxbank.client.resource;

import org.junit.Assert;
import org.junit.Test;

public class AssayClientTest {

	@Test
	public void testConstructor() {
		AssayClient assay = new AssayClient();
		Assert.assertNotNull(assay);
	}

}
