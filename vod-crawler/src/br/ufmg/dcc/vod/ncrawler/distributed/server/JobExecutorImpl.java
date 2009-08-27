package br.ufmg.dcc.vod.ncrawler.distributed.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.ui.EXIT_CODES;

public class JobExecutorImpl extends UnicastRemoteObject implements JobExecutor {

	private static final long serialVersionUID = 1L;

	protected JobExecutorImpl(int port) throws RemoteException {
		super(port);
	}

	@Override
	public void collect(CrawlJob c) {
		c.collect();
	}

	@Override
	public void kill() throws RemoteException {
		System.exit(EXIT_CODES.OK);
	}
}
