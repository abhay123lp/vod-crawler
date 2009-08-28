package br.ufmg.dcc.vod.ncrawler.distributed.client;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class EvaluatorClientImpl<I, C> extends UnicastRemoteObject implements EvaluatorClient<I, C>  {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(EvaluatorClientImpl.class);
	
	// Volatile since it will not be serialized remotely
	private volatile Evaluator<I, C> e;

	public EvaluatorClientImpl(int port) throws RemoteException {
		super(port);
	}

	//Remote method
	@Override
	public void evaluteAndSave(I collectID, C collectContent, File savePath, boolean errorOcurred, UnableToCollectException utce) {
		LOG.info("Result received: "+ collectID);
		e.evaluteAndSave(collectID, collectContent, savePath, errorOcurred, utce);
	}
	
	//Local methods
	public void wrap(Evaluator<I, C> e) {
		this.e = e;
	}
}