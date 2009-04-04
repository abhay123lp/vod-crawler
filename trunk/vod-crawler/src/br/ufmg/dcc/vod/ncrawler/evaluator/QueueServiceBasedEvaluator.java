package br.ufmg.dcc.vod.ncrawler.evaluator;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

public class QueueServiceBasedEvaluator<R, T> implements Evaluator<R, T>, QueueProcessor<CrawlJob<R, T>> {

	private final Evaluator<R, T> e;
	private final QueueHandle myHandle;
	protected final QueueService<CrawlJob<R, T>> service;
	
	public QueueServiceBasedEvaluator(Evaluator<R, T> e, QueueService<CrawlJob<R, T>> service) {
		this.e = e;
		this.myHandle = service.createMessageQueue("Evaluator");
		this.service = service;
	}
	
	@Override
	public void setProcessor(Processor<R, T> p) {
		e.setProcessor(p);
	}
	
	@Override
	public void dispatchIntialCrawl() {
		e.dispatchIntialCrawl();
	}

	@Override
	public void crawlJobConcluded(CrawlJob<R, T> j) {
		service.sendObjectToQueue(myHandle, j);
	}

	public void start() {
		service.startProcessor(myHandle, this);
	}

	@Override
	public String getName() {
		return "ThreadSafeEval";
	}

	@Override
	public void process(CrawlJob<R, T> t) {
		e.crawlJobConcluded(t);
	}

}