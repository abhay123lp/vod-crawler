package br.ufmg.dcc.vod.ncrawler.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class ThreadedProcessor extends AbstractProcessor {
	
	private static final Logger LOG = Logger.getLogger(ThreadedProcessor.class);
	
	public <S, I, C> ThreadedProcessor(int nThreads, long sleepTimePerExecution, QueueService service,
			Serializer<S> serializer, File queueFile, int queueSize, Evaluator<I, C> eval) 
			throws FileNotFoundException, IOException {
		super(nThreads, sleepTimePerExecution, service, serializer, queueFile, queueSize, eval);
	}
	
	public void start() {
		for (int i = 0; i < nThreads; i++) {
			service.startProcessor(myHandle, new CrawlProcessor(i));
		}
		
		Collection<CrawlJob> initialCrawl = eval.getInitialCrawl();
		for (CrawlJob j : initialCrawl) {
			dispatch(j);
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
			t.setEvaluator(eval);
			t.collect();
			
			try {
				Thread.sleep(sleepTimePerExecution);
			} catch (InterruptedException e) {
			}
		}
	}
}