package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
					System.out.println(ai.incrementAndGet());
					String u = "http://www.lastfm.com.br/user/wickedsalvation/library/tags?tag=jumpy&view=list";
					URL url = new URL(u);
					
					URLConnection connection = url.openConnection();
					connection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
					
					connection.connect();
					
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						
					}
				}
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
}
