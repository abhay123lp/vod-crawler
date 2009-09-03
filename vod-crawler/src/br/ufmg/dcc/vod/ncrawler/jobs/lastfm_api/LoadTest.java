package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

	private static final AtomicInteger ai = new AtomicInteger(0);
	
	public static void main(String[] args) throws IOException {
		for (int i = 0; i < 600; i++) {
			new Thread(new Runa()).start();
		}
	}
	
	private static class Runa implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					
				}
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
}
