package br.ufmg.dcc.vod.ncrawler.jobs;

public interface CrawlerFactory<I, C> {

	public void initiate(Parameters p);
	
	public Evaluator<I, C> getEvaluator();
	
}
