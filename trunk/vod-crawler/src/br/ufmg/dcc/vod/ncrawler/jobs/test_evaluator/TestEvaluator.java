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
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

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
	public Collection<CrawlJob> evaluteAndSave(Integer collectID, int[] collectContent, File savePath) {
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
	public Collection<CrawlJob> getInitialCrawl()  {
		return new ArrayList<CrawlJob>(Arrays.asList(new TestCrawlJob(0, g)));
	}

	@Override
	public void setStatsKeeper(StatsPrinter sp) {
	}

	@Override
	public void errorOccurred(Integer collectID, Exception e) {
	}

	@Override
	public void setTrackerFactory(TrackerFactory factory) {
	}
}