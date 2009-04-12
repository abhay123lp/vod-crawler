package br.ufmg.dcc.vod.ncrawler.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class ThreadedProcessor<R, T> implements Processor<R, T> {
	
	private static final Logger LOG = Logger.getLogger(ThreadedProcessor.class);
	
	private final long sleepTimePerExecution;
	private final int nThreads;
	private final QueueHandle myHandle;
	private final QueueService<CrawlJob<R, T>> service;
	private Evaluator<R, T> e;

	//Uses disk queue
	public ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService<CrawlJob<R, T>> service,
			Serializer<CrawlJob<R, T>> serializer, File queueFile, int queueSize) 
			throws FileNotFoundException, IOException {
		
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.myHandle = service.createPersistentMessageQueue("Workers", queueFile, serializer, queueSize);
		this.service = service;
	}
	
	//Uses memory queue
	public ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService<CrawlJob<R, T>> service) {
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.myHandle = service.createLimitedBlockMessageQueue("Workers", nThreads * 2);
		this.service = service;
	}
	
	public void start() {
		for (int i = 0; i < nThreads; i++) {
			service.startProcessor(myHandle, new CrawlProcessor(i));
		}
	}

	@Override
	public void dispatch(CrawlJob<R, T> c) {
		try {
			service.sendObjectToQueue(myHandle, c);
		} catch (InterruptedException e) {
			LOG.error(e);
		}
	}

	@Override
	public void setEvaluator(Evaluator<R, T> e) {
		this.e = e;
	}
	
	private class CrawlProcessor implements QueueProcessor<CrawlJob<R, T>> {
		
		private final int i;

		public CrawlProcessor(int i) {
			this.i = i;
		}

		@Override
		public String getName() {
			return getClass().getName() + " " + i;
		}

		@Override
		public void process(CrawlJob<R, T> t) {
			
			try {
				LOG.info("STARTING Collecting url: url="+t.getID());
				t.collect();
				LOG.info("DONE Collected url: url="+t.getID());
			} catch (Exception e) {
				t.markWithError();
				LOG.error("ERROR Collecting: url="+t.getID(), e);
			} finally {
				e.crawlJobConcluded(t);
			}
			
			try {
				Thread.sleep(sleepTimePerExecution * 1000);
			} catch (InterruptedException e) {
			}
		}
	}
}