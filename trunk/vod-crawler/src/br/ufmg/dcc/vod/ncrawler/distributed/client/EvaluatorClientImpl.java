package br.ufmg.dcc.vod.ncrawler.distributed.client;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class EvaluatorClientImpl<I, C> extends UnicastRemoteObject implements EvaluatorClient<I, C>, Evaluator<I, C>  {

	private static final long serialVersionUID = 1L;
	
	// Volatile since it will not be serialized remotely
	private volatile Evaluator<I, C> e;

	public EvaluatorClientImpl(int port) throws RemoteException {
		super(port);
	}

	//Remote method
	@Override
	public void evaluteAndSave(I collectID, C collectContent, File savePath, boolean errorOcurred) {
		e.evaluteAndSave(collectID, collectContent, savePath, errorOcurred);
	}
	
	//Local methods
	public void wrap(Evaluator<I, C> e) {
		this.e = e;
	}

	public Evaluator<I, C> getWrapped() {
		return e;
	}
	
	@Override
	public Collection<CrawlJob> getInitialCrawl() {
		return e.getInitialCrawl();
	}

	@Override
	public void setProcessor(Processor processor) {
		e.setProcessor(processor);
	}

	@Override
	public void setStatsKeeper(StatsPrinter sp) {
		e.setStatsKeeper(sp);
	}

	@Override
	public void setTrackerFactory(TrackerFactory factory) {
		e.setTrackerFactory(factory);
	}
}