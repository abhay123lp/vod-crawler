package br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator;

import java.util.HashMap;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;

public class TestEvaluator implements Evaluator<int[], Integer> {

	private Processor<int[], Integer> p;
	private final HashMap<Integer, int[]> crawled;
	private final RandomizedSyncGraph g;

	public TestEvaluator(RandomizedSyncGraph g) {
		this.g = g;
		this.crawled = new HashMap<Integer, int[]>();
	}
	
	@Override
	public void crawlJobConcluded(CrawlJob<int[], Integer> j) {
		this.crawled.put(j.getType(), j.getResult());
		for (int i : j.getResult()) {
			if (!crawled.containsKey(i)) {
				p.dispatch(new TestCrawlJob(i, g));
			}
		}
	}

	@Override
	public void dispatchIntialCrawl() {
		p.dispatch(new TestCrawlJob(0, g));
	}

	@Override
	public void setProcessor(Processor<int[], Integer> p) {
		this.p = p;
	}

	public HashMap<Integer, int[]> getCrawled() {
		return crawled;
	}

	@Override
	public boolean isDone() {
		return this.crawled.size() == g.getNumVertex();
	}

}
