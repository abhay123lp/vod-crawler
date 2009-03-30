package br.ufmg.dcc.vod;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.evaluator.Evaluator;
import br.ufmg.dcc.vod.evaluator.QueueServiceBasedEvaluator;
import br.ufmg.dcc.vod.processor.ThreadedProcessor;
import br.ufmg.dcc.vod.queue.QueueService;

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
		LOG.info("Starting ThreadedCrawler: nThreads="+nThreads + " , sleepTime="+sleep+"ms");
		
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