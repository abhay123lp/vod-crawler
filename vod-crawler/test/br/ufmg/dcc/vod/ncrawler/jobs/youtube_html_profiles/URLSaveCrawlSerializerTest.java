package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.HTMLTypeFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlSerializer;


public class URLSaveCrawlSerializerTest {

	@Test
	public void testSave() throws MalformedURLException {
		HTMLTypeFactory.getInstance().addMappings(YTHTMLType.FAVORITES.enumerate(), false);
		
		URLSaveCrawlSerializer s = new URLSaveCrawlSerializer(null);
		
		URLSaveCrawlJob j = new URLSaveCrawlJob(new URL("http://www.vod.com.br"), new File("/tmp/disk"), YTHTMLType.FRIENDS, null);
		
		byte[] checkpointData = s.checkpointData(j);
		URLSaveCrawlJob interpret = s.interpret(checkpointData);
		
		assertEquals(interpret.getUrl(), new URL("http://www.vod.com.br"));
		assertEquals(interpret.getSavePath(), new File("/tmp/disk"));
		assertEquals(interpret.getType(), YTHTMLType.FRIENDS);
	}
	
}