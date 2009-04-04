package br.ufmg.dcc.vod.ncrawler;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.QueueServiceBasedEvaluator;
import br.ufmg.dcc.vod.ncrawler.processor.ThreadedProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

public class ThreadedCrawler<R, T> {

	private static final Logger LOG = Logger.getLogger(ThreadedCrawler.class);
	
	private final ThreadedProcessor<R, T> processor;
	private final QueueServiceBasedEvaluator<R, T> evaluator;
	private final QueueService<CrawlJob<R, T>> service;
	private final int nThreads;
	private final long sleep;

	public ThreadedCrawler(int nThreads, long sleep, Evaluator<R, T> evaluator) {
		this.nThreads = nThreads;
		this.sleep = sleep;
		this.service = new QueueService<CrawlJob<R, T>>();
		this.evaluator = new QueueServiceBasedEvaluator<R, T>(evaluator, service);
		this.processor = new ThreadedProcessor<R, T>(nThreads, sleep, service);
	}
	
	public void crawl() {
		LOG.info("Starting ThreadedCrawler: nThreads="+nThreads + " , sleepTime="+sleep+"s");
		
		//Configuring
		evaluator.setProcessor(processor);
		processor.setEvaluator(evaluator);
		
		//Starting Threads
		evaluator.dispatchIntialCrawl();
		evaluator.start();
		processor.start();

		//Waiting until crawl ends
		int wi = 10;
		LOG.info("Waiting until crawl ends: waitInterval="+wi+"s");
		this.service.waitUntilWorkIsDoneAndStop(wi);
	}
}