package br.ufmg.dcc.vod.ncrawler.distributed.client;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;

public class EvaluatorClientImpl<I, C> extends UnicastRemoteObject implements EvaluatorClient<I, C>  {

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
}