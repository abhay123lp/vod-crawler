package br.ufmg.dcc.vod.ncrawler.distributed.client;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public interface EvaluatorClient<I, C> extends Remote {

	public static final String NAME = "EVAL_CLIENT";

	public void evaluteAndSave(I collectID, C collectContent, File savePath, boolean errorOcurred, UnableToCollectException utce) throws RemoteException;

}
