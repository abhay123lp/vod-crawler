package br.ufmg.dcc.vod.evaluator;

import br.ufmg.dcc.vod.CrawlJob;
import br.ufmg.dcc.vod.processor.Processor;

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

	public void crawlJobConcluded(CrawlJob<R, T> j);
	
}
