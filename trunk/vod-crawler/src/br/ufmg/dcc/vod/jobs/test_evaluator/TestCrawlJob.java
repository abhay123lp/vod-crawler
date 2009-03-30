package br.ufmg.dcc.vod.jobs.test_evaluator;

import br.ufmg.dcc.vod.CrawlJob;

public class TestCrawlJob implements CrawlJob<int[], Integer> {

	private final int vertex;
	private final RandomizedSyncGraph g;
	private int[] neighbours;

	public TestCrawlJob(int vertex, RandomizedSyncGraph g) {
		this.vertex = vertex;
		this.g = g;
	}
	
	@Override
	public void collect() throws Exception {
		this.neighbours = g.getNeighbours(vertex);
	}

	@Override
	public String getID() {
		return getType()+"";
	}

	@Override
	public int[] getResult() {
		return neighbours;
	}

	@Override
	public Integer getType() {
		return vertex;
	}
}
