package br.ufmg.dcc.vod.ncrawler;

import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;


/**
 * Collects an object and returns the result of this crawl.
 * 
 * @param <R> Result from a crawl
 * @param <T> Type of object crawled
 */
public interface CrawlJob {

	public Collection<CrawlJob> collect();

	public void setEvaluator(Evaluator e);
	
}
