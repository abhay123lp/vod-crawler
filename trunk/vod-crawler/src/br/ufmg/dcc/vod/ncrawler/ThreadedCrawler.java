package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.processor.ThreadedProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class ThreadedCrawler<R, T> {

	private static final Logger LOG = Logger.getLogger(ThreadedCrawler.class);
	
	private final ThreadedProcessor processor;
	private final QueueService service;
	
	private final int nThreads;
	private final long sleep;

	public ThreadedCrawler(int nThreads, long sleep, Evaluator<R, T> evaluator) {
		this.nThreads = nThreads;
		this.sleep = sleep;
		this.service = new QueueService();
		this.processor = new ThreadedProcessor(nThreads, sleep, service, evaluator);
	}

	public <S> ThreadedCrawler(int nThreads, long sleep, Evaluator<R, T> evaluator, File pQueueDir, File eQueueDir, Serializer<S> s, int fileSize) 
		throws FileNotFoundException, IOException {
		
		this.nThreads = nThreads;
		this.sleep = sleep;
		this.service = new QueueService();
		this.processor = new ThreadedProcessor(nThreads, sleep, service, s, pQueueDir, fileSize, evaluator);
	}
	
	public void crawl() throws Exception {
		LOG.info("Starting ThreadedCrawler: nThreads="+nThreads + " , sleepTime="+sleep+"s");

		//Starting
		processor.start();

		//Waiting until crawl ends
		int wi = 10;
		LOG.info("Waiting until crawl ends: waitInterval="+wi+"s");
		this.service.waitUntilWorkIsDoneAndStop(wi);

		LOG.info("Done! Stopping");
		System.out.println("Done! Stopping");
		LOG.info("Crawl done!");
	}
}