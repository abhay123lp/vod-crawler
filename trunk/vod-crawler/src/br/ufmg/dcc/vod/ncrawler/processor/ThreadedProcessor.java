package br.ufmg.dcc.vod.ncrawler.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.CrawlResult;
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
	private final QueueService service;
	private Evaluator<R, T> e;

	public <S> ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService service,
			Serializer<S> serializer, File queueFile, int queueSize) 
			throws FileNotFoundException, IOException {
		
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.service = service;
		this.myHandle = service.createPersistentMessageQueue("Workers", queueFile, serializer, queueSize);
	}
	
	public ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService service) {
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.service = service;
		this.myHandle = service.createMessageQueue("Workers");
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
			R r = null;
			
			try {
				LOG.info("STARTING Collecting url: url="+t);
				t.collect();
				r = t.getResult();
				LOG.info("DONE Collected url: url="+t);
			} catch (Exception e) {
				LOG.error("ERROR Collecting: url="+t, e);
			}
			
			//R will be null if error occurred!
			e.crawlJobConcluded(new CrawlResult<R, T>(t.getID(), r, t.getType(), r == null));
			
			try {
				Thread.sleep(sleepTimePerExecution * 1000);
			} catch (InterruptedException e) {
			}
		}
	}
}