package br.ufmg.dcc.vod.ncrawler.evaluator;

import java.io.File;
import java.util.Collection;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class ThreadSafeEvaluator<I, C> implements Evaluator<I, C> {

	private static final Logger LOG = Logger.getLogger(ThreadSafeEvaluator.class);
	
	private final Evaluator<I, C> e;
	private final QueueHandle myHandle;
	private final QueueService service;
	private final int numEvaluatorThreads;
	
	public ThreadSafeEvaluator(int numEvaluatorThreads, Evaluator<I, C> e, QueueService service) {
		this.numEvaluatorThreads = numEvaluatorThreads;
		this.e = e;
		this.service = service;
		this.myHandle = service.createMessageQueue("Evaluator");
	}

	@Override
	public void setProcessor(Processor p) {
		e.setProcessor(p);
	}
	
	@Override
	public Collection<CrawlJob> getInitialCrawl() {
		return e.getInitialCrawl();
	}

	@Override
	public void evaluteAndSave(I collectID, C collectContent, File savePath, boolean error, UnableToCollectException utce) {
		try {
			service.sendObjectToQueue(myHandle, new QueueObj(collectID, collectContent, savePath, error, utce));
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	@Override
	public void setStatsKeeper(StatsPrinter sp) {
		e.setStatsKeeper(sp);
	}

	@Override
	public void setTrackerFactory(TrackerFactory factory) {
		e.setTrackerFactory(factory);
	}
	
	public void start() {
		for (int i = 0; i < numEvaluatorThreads; i++) {
			this.service.startProcessor(myHandle, new EvalT());
		}
	}
	
	private class EvalT implements QueueProcessor<QueueObj> {

		@Override
		public String getName() {
			return "EvalT";
		}

		@Override
		public void process(QueueObj qo) {
			e.evaluteAndSave(qo.id, qo.content, qo.path, qo.error, qo.utce);
		}
	}
	
	private class QueueObj {
		private final I id;
		private final C content;
		private final File path;
		private final boolean error;
		private final UnableToCollectException utce;
		
		public QueueObj(I id, C content, File path, boolean error, UnableToCollectException utce) {
			this.id = id;
			this.content = content;
			this.path = path;
			this.error = error;
			this.utce = utce;
		}
	}
}