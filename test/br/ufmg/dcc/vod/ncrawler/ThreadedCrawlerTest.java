package br.ufmg.dcc.vod.ncrawler;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.ThreadedCrawler;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.RandomizedSyncGraph;
import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestEvaluator;

public class ThreadedCrawlerTest {

	@Test
	public void testCrawl1Thread() {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler<int[], Integer> tc = new ThreadedCrawler<int[], Integer>(1, 0, te);
		
		tc.crawl();
		
		HashMap<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}


	@Test
	public void testCrawl2Thread() {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler<int[], Integer> tc = new ThreadedCrawler<int[], Integer>(2, 0, te);
		
		tc.crawl();
		
		HashMap<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}
	
	@Test
	public void testCrawl100Thread() {
		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
		
		TestEvaluator te = new TestEvaluator(g);
		ThreadedCrawler<int[], Integer> tc = new ThreadedCrawler<int[], Integer>(100, 0, te);
		
		tc.crawl();
		
		HashMap<Integer, int[]> crawled = te.getCrawled();
		doTheAsserts(crawled, g);
	}

	
	private void doTheAsserts(HashMap<Integer, int[]> crawled, RandomizedSyncGraph g) {
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