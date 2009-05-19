package br.ufmg.dcc.vod.ncrawler;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.RandomizedSyncGraph;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestEvaluator;

public class ThreadedCrawlerTest {

	@Test
	public void testCrawl1Thread() throws Exception {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler tc = new ThreadedCrawler(1, 0, te);
		
		tc.crawl();
		
		Map<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}


	@Test
	public void testCrawl2Thread() throws Exception {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler tc = new ThreadedCrawler(2, 0, te);
		
		tc.crawl();
		
		Map<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}
	
	@Test
	public void testCrawl100Thread() throws Exception {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler tc = new ThreadedCrawler(100, 0, te);
		
		tc.crawl();
		
		Map<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}

	
	private void doTheAsserts(Map<Integer, int[]> crawled, RandomizedSyncGraph g) {
		assertEquals(crawled.size(), g.getNumVertex());
		
		for (Entry<Integer, int[]> e: crawled.entrySet()) {
			
			int[] neighbours = g.getNeighbours(e.getKey());
			int[] value = e.getValue();
			
			assertEquals(neighbours.length, value.length);
			for (int i = 0; i < neighbours.length; i++) {
				assertEquals(neighbours[i], value[i]);
			}
		}
	}
}