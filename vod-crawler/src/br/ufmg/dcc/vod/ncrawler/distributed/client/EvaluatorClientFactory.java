package br.ufmg.dcc.vod.ncrawler.distributed.client;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import sun.rmi.registry.RegistryImpl;

public class EvaluatorClientFactory<I, C> {

	private final int port;
	private Registry ri;

	public EvaluatorClientFactory(int port) throws RemoteException {
		this.port = port;
		this.ri = new RegistryImpl(port);
	}
	
	public EvaluatorClientImpl<I, C> createAndBindEvalServer() throws RemoteException, AlreadyBoundException {
		EvaluatorClientImpl<I, C> server = new EvaluatorClientImpl<I, C>(port);
		ri.bind(EvaluatorClient.NAME, server);
		return server;
	}

	public void shutdown() throws AccessException, RemoteException, NotBoundException {
		ri.unbind(EvaluatorClient.NAME);
		UnicastRemoteObject.unexportObject(ri, true);
		ri = null;
	}
}
