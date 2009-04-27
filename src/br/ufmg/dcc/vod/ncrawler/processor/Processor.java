package br.ufmg.dcc.vod.ncrawler.processor;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;

/**
 * Dispatches crawl job to be collected.
 *
 * @param <R> Result from a crawl
 * @param <T> Type of object crawled
 */
public interface Processor<R, T>  {

	public void dispatch(CrawlJob<R, T> c);
	
	public void setEvaluator(Evaluator<R, T> e);
	
}
