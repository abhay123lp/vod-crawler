package br.ufmg.dcc.vod.processor;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.CrawlJob;
import br.ufmg.dcc.vod.evaluator.Evaluator;
import br.ufmg.dcc.vod.queue.QueueHandle;
import br.ufmg.dcc.vod.queue.QueueProcessor;
import br.ufmg.dcc.vod.queue.QueueService;

public class ThreadedProcessor<R, T> implements Processor<R, T> {
	
	private static final Logger LOG = Logger.getLogger(ThreadedProcessor.class);
	
	private final long sleepTimePerExecution;
	private final int nThreads;
	private final QueueHandle myHandle;
	private final QueueService<CrawlJob<R, T>> service;
	private Evaluator<R, T> e;
	
	public ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService<CrawlJob<R, T>> service) {
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.myHandle = service.createMessageQueue();
		this.service = service;
	}
	
	public void start() {
		for (int i = 0; i < nThreads; i++) {
			service.startProcessor(myHandle, new CrawlProcessor(i));
		}
	}

	@Override
	public void dispatch(CrawlJob<R, T> c) {
		service.sendObjectToQueue(myHandle, c);
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
				e.crawlJobConcluded(t);
				LOG.info("DONE Collected url: url="+t.getID());
			} catch (Exception e) {
				LOG.error("ERROR Collecting: url="+t.getID(), e);
			}
			
			try {
				Thread.sleep(sleepTimePerExecution);
			} catch (InterruptedException e) {
			}
		}
	}
}