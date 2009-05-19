package br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator;

import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;

public class TestCrawlJob implements CrawlJob {

	private final int vertex;
	private final RandomizedSyncGraph g;
	private int[] neighbours;
	private Evaluator e;

	public TestCrawlJob(int vertex, RandomizedSyncGraph g) {
		this.vertex = vertex;
		this.g = g;
	}

	@Override
	public Collection<CrawlJob> collect() throws Exception {
		return e.evaluteAndSave(vertex, g.getNeighbours(vertex), null);
	}

	@Override
	public void setvaluator(Evaluator e) {
		this.e = e;
	}
}
