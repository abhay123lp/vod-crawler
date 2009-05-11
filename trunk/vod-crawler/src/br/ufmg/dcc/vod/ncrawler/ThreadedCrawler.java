package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.QueueServiceBasedEvaluator;
import br.ufmg.dcc.vod.ncrawler.processor.ThreadedProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class ThreadedCrawler<R, T> {

	private static final Logger LOG = Logger.getLogger(ThreadedCrawler.class);
	
	private final ThreadedProcessor<R, T> processor;
	private final QueueServiceBasedEvaluator<R, T> evaluator;
	private final QueueService service;
	
	private final int nThreads;
	private final long sleep;

	public ThreadedCrawler(int nThreads, long sleep, Evaluator<R, T> evaluator) {
		this.nThreads = nThreads;
		this.sleep = sleep;
		this.service = new QueueService();
		this.evaluator = new QueueServiceBasedEvaluator<R, T>(evaluator, service);
		this.processor = new ThreadedProcessor<R, T>(nThreads, sleep, service);
	}

	public <S> ThreadedCrawler(int nThreads, long sleep, Evaluator<R, T> evaluator, File pQueueDir, File eQueueDir, Serializer<S> s, int fileSize) 
		throws FileNotFoundException, IOException {
		
		this.nThreads = nThreads;
		this.sleep = sleep;
		this.service = new QueueService();
		this.evaluator = new QueueServiceBasedEvaluator<R, T>(evaluator, service, eQueueDir, fileSize);
		this.processor = new ThreadedProcessor<R, T>(nThreads, sleep, service, s, pQueueDir, fileSize);
	}
	
	public void crawl() throws Exception {
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

		LOG.info("Done! Stopping");
		System.out.println("Done! Stopping");
		
		//Safety loop? uncessary?
		while(!evaluator.isDone()) {
			try {
				Thread.sleep(wi * 1000);
			} catch (InterruptedException e) {
			}
		}
		
		LOG.info("Crawl done!");
	}
}