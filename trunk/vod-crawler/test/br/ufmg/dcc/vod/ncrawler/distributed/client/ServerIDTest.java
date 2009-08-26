package br.ufmg.dcc.vod.ncrawler.distributed.client;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.distributed.server.JobExecutor;
import br.ufmg.dcc.vod.ncrawler.distributed.server.JobExecutorFactory;

public class ServerIDTest {

	@Test
	public void testAll() throws RemoteException, AlreadyBoundException, MalformedURLException, NotBoundException {
		ServerID sid = new ServerID("localhost", 9090);
		
		JobExecutor resolve;
		try {
			resolve = sid.resolve();
			fail();
		} catch (Exception e) {
		}
		
		JobExecutorFactory f = new JobExecutorFactory(9090);
		f.createAndBind();

		resolve = sid.resolve();
		assertTrue(resolve != null);
		
		sid.reset();
		JobExecutor resolve2 = sid.resolve();
		assertTrue(resolve2 != resolve);
		
		f.shutdown();
	}
	
}
