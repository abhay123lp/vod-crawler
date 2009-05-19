package br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;

public class TestEvaluator implements Evaluator<Integer, int[]> {

	private final Map<Integer, int[]> crawled;
	private final RandomizedSyncGraph g;

	public TestEvaluator(RandomizedSyncGraph g) {
		this.g = g;
		this.crawled = Collections.synchronizedMap(new HashMap<Integer, int[]>());
	}
	
	public Map<Integer, int[]> getCrawled() {
		return crawled;
	}

	@Override
	public Collection<CrawlJob> evaluteAndSave(Integer collectID, int[] collectContent, File savePath) throws Exception {
		ArrayList<CrawlJob> rv = new ArrayList<CrawlJob>();
		
		this.crawled.put(collectID, collectContent);
		for (int i : collectContent) {
			if (!crawled.containsKey(i)) {
				rv.add(new TestCrawlJob(i, g));
			}
		}
		
		return rv;
	}

	@Override
	public Collection<CrawlJob> getInitialCrawl() throws Exception {
		return new ArrayList<CrawlJob>(Arrays.asList(new TestCrawlJob(0, g)));
	}

}
