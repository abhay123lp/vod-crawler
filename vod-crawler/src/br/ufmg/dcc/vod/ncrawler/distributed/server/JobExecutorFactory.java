package br.ufmg.dcc.vod.ncrawler.distributed.server;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import sun.rmi.registry.RegistryImpl;

public class JobExecutorFactory {

	private final int port;
	private Registry ri;

	public JobExecutorFactory(int port) throws RemoteException {
		this.port = port;
		this.ri = new RegistryImpl(port);
	}
	
	public JobExecutor createAndBindExecutorServer() throws RemoteException, AlreadyBoundException {
		JobExecutorImpl server = new JobExecutorImpl(port);
		ri.bind(JobExecutor.NAME, server);
		return server;
	}

	public void shutdown() throws AccessException, RemoteException, NotBoundException {
		ri.unbind(JobExecutor.NAME);
		UnicastRemoteObject.unexportObject(ri, true);
		ri = null;
	}
}
