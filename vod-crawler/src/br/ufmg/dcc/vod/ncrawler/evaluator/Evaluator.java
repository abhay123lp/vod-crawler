package br.ufmg.dcc.vod.ncrawler.evaluator;

import br.ufmg.dcc.vod.ncrawler.CrawlResult;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;

/**
 * Initiates and manages crawls. Implementers of this class dispatches initial
 * crawl and schedules subsequent objects to be collected.
 *
 * @param <R> Result from a crawl
 * @param <T> Type of object crawled
 */
public interface Evaluator<R, T> {

	public void setProcessor(Processor<R, T> p);

	public void dispatchIntialCrawl();

	public void crawlJobConcluded(CrawlResult<R, T> j);
	
	public boolean isDone();
	
}
