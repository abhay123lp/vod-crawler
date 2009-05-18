package br.ufmg.dcc.vod.ncrawler.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.Evaluator;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class ThreadedProcessor implements Processor {
	
	private static final Logger LOG = Logger.getLogger(ThreadedProcessor.class);
	
	private final long sleepTimePerExecution;
	private final int nThreads;
	private final QueueHandle myHandle;
	private final QueueService service;
	private final Evaluator e;

	public <S, I, C> ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService service,
			Serializer<S> serializer, File queueFile, int queueSize, Evaluator<I, C> e) 
			throws FileNotFoundException, IOException {
		
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.service = service;
		this.e = e;
		this.myHandle = service.createPersistentMessageQueue("Workers", queueFile, serializer, queueSize);
	}
	
	public <I, C> ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService service, Evaluator<I, C> e) {
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.service = service;
		this.e = e;
		this.myHandle = service.createMessageQueue("Workers");
	}

	public void start() {
		for (int i = 0; i < nThreads; i++) {
			service.startProcessor(myHandle, new CrawlProcessor(i));
		}
	}

	@Override
	public void dispatch(CrawlJob c) {
		try {
			service.sendObjectToQueue(myHandle, c);
		} catch (InterruptedException e) {
			LOG.error(e);
		}
	}

	private class CrawlProcessor implements QueueProcessor<CrawlJob> {
		
		private final int i;

		public CrawlProcessor(int i) {
			this.i = i;
		}

		@Override
		public String getName() {
			return getClass().getName() + " " + i;
		}

		@Override
		public void process(CrawlJob t) {
			try {
				t.setvaluator(e);
				Collection<CrawlJob> collect = t.collect();
				for (CrawlJob j : collect) {
					ThreadedProcessor.this.dispatch(j);
				}
			} catch (Exception e) {
				//FIXME!!
			}
			
			try {
				Thread.sleep(sleepTimePerExecution);
			} catch (InterruptedException e) {
			}
		}
	}
}