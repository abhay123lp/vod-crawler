package br.ufmg.dcc.vod;

import br.ufmg.dcc.vod.evaluator.Evaluator;
import br.ufmg.dcc.vod.evaluator.QueueServiceBasedEvaluator;
import br.ufmg.dcc.vod.processor.ThreadedProcessor;
import br.ufmg.dcc.vod.queue.QueueService;

public class ThreadedCrawler<R, T> {

	private final ThreadedProcessor<R, T> processor;
	private final QueueServiceBasedEvaluator<R, T> evaluator;
	private final QueueService<CrawlJob<R, T>> service;

	public ThreadedCrawler(int nThreads, long sleep, Evaluator<R, T> evaluator) {
		this.service = new QueueService<CrawlJob<R, T>>();
		this.evaluator = new QueueServiceBasedEvaluator<R, T>(evaluator, service);
		this.processor = new ThreadedProcessor<R, T>(nThreads, sleep, service);
	}
	
	public void crawl() {
		//Configuring
		evaluator.setProcessor(processor);
		processor.setEvaluator(evaluator);
		
		//Starting Threads
		evaluator.dispatchIntialCrawl();
		evaluator.start();
		processor.start();

		//Waiting until crawl ends
		this.service.waitUntilWorkIsDoneAndStop(10);
	}
}