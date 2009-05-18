package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.CrawlResult;


public class CrawlResultSerializerTest {

	@Test
	public void testSave() throws MalformedURLException {
		
		CrawlResultSerializer s = new CrawlResultSerializer();
		CrawlResult<File, YTHTMLType> j = new CrawlResult<File, YTHTMLType>("oi", new File("/tmp/disk"), YTHTMLType.FAVORITES, true);
		
		byte[] checkpointData = s.checkpointData(j);
		CrawlResult<File, YTHTMLType> interpret = s.interpret(checkpointData);
		
		assertEquals(interpret.getId(), "oi");
		assertEquals(interpret.getResult(), new File("/tmp/disk"));
		assertEquals(interpret.getType(), YTHTMLType.FAVORITES);
		assertEquals(interpret.success(), true);
	}
	
	@Test
	public void testSave2() throws MalformedURLException {
		
		CrawlResultSerializer s = new CrawlResultSerializer();
		CrawlResult<File, YTHTMLType> j = new CrawlResult<File, YTHTMLType>("oi", null, YTHTMLType.FAVORITES, false);
		
		byte[] checkpointData = s.checkpointData(j);
		CrawlResult<File, YTHTMLType> interpret = s.interpret(checkpointData);
		
		assertEquals(interpret.getId(), "oi");
		assertEquals(interpret.getResult(), null);
		assertEquals(interpret.getType(), YTHTMLType.FAVORITES);
		assertEquals(interpret.success(), false);
	}
}
