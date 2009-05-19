package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlSerializer;


public class URLSaveCrawlSerializerTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testSave() throws MalformedURLException {
		
		URLSaveCrawlSerializer s = new URLSaveCrawlSerializer(null);
		
		URLSaveCrawlJob j = new URLSaveCrawlJob(new URL("http://www.vod.com.br"), new File("/tmp/disk"), YTHTMLType.FRIENDS, null);
		
		byte[] checkpointData = s.checkpointData(j);
		URLSaveCrawlJob interpret = s.interpret(checkpointData);
		
		assertEquals(interpret.getUrl(), new URL("http://www.vod.com.br"));
		assertEquals(interpret.getSavePath(), new File("/tmp/disk"));
		assertEquals(interpret.getType(), YTHTMLType.FRIENDS);
	}
	
}
