package br.ufmg.dcc.vod.ncrawler.evaluator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlResult;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles.CrawlResultSerializer;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

public class QueueServiceBasedEvaluator<R, T> implements Evaluator<R, T>, QueueProcessor<CrawlResult<R, T>> {

	private static final Logger LOG = Logger.getLogger(QueueServiceBasedEvaluator.class);
	
	private final Evaluator<R, T> e;
	private final QueueHandle myHandle;
	private final QueueService service;
	
	public QueueServiceBasedEvaluator(Evaluator<R, T> e, QueueService service, File queueDir, int bytes) 
		throws FileNotFoundException, IOException {
		
		this.e = e;
		this.service = service;
		this.myHandle = service.createPersistentMessageQueue("Evaluator", queueDir, new CrawlResultSerializer(), bytes);
	}
	
	public QueueServiceBasedEvaluator(Evaluator<R, T> e, QueueService service) {
		this.e = e;
		this.service = service;
		this.myHandle = service.createMessageQueue("Evaluator");
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
	public void crawlJobConcluded(CrawlResult<R, T> r) {
		try {
			service.sendObjectToQueue(myHandle, r);
		} catch (InterruptedException e) {
			LOG.error(e);
		}
	}

	public void start() {
		service.startProcessor(myHandle, this);
	}

	@Override
	public String getName() {
		return "ThreadSafeEval";
	}

	@Override
	public void process(CrawlResult<R, T> r) {
		e.crawlJobConcluded(r);
	}

	@Override
	public boolean isDone() {
		return e.isDone();
	}
}