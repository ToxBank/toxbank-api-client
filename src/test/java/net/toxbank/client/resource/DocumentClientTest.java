package net.toxbank.client.resource;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import net.toxbank.client.Resources;

import org.junit.Assert;
import org.opentox.rest.HTTPClient;

import com.itextpdf.text.pdf.PdfReader;


public class DocumentClientTest extends AbstractClientTest<Protocol, ProtocolClient> {
	private final static String TEST_SERVER_PROTOCOL = String.format("%s%s",AbstractClientTest.TEST_SERVER,Resources.protocol);
	
	@Override
	protected ProtocolClient getToxBackClient() {
		return new ProtocolClient();
	}

	@Override
	public void testRead() throws Exception {
		List<URL> url = ProtocolClient.listProtocols(
						new URL(String.format("%s/SEURAT-Protocol-18-1?page=0&pagesize=1",TEST_SERVER_PROTOCOL)));
		Assert.assertNotNull(url);
		Assert.assertEquals(1,url.size());

		Protocol protocol = new Protocol(url.get(0));
		URL doc = ProtocolClient.listFile(protocol);
		Assert.assertNotNull(doc);
		
		File file = ProtocolClient.downloadFile(protocol);
		
		System.out.println(file.getAbsolutePath());
		Assert.assertTrue(file.exists());
		
		FileInputStream in = new FileInputStream(file);
		PdfReader reader = new PdfReader(in);
		System.out.println(reader.getInfo());
		in.close();
		
		file.delete();
		
	}
}
