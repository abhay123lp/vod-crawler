package br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;

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
	public void collect() {
		e.evaluteAndSave(vertex, g.getNeighbours(vertex), null, false);
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}

	public int vertex() {
		return vertex;
	}
}
