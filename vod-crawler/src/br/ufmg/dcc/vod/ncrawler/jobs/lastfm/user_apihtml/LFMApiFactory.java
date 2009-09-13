package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class LFMApiFactory implements EvaluatorFactory<String, LastFMUserDAO, LastFMApiCrawlJob> {

	private LastFMAPIEvaluator e;
	private LastFMApiSave serializer;

	@Override
	public Evaluator<String, LastFMUserDAO> getEvaluator() {
		return e;
	}

	@Override
	public Serializer<LastFMApiCrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime,
			List<String> seeds) {
		this.e = new LastFMAPIEvaluator(seeds, saveFolder, sleepTime);
		this.serializer = new LastFMApiSave(sleepTime);

		System.setProperty("http.keepAlive", "true");
		System.setProperty("http.maxConnections", ""+threads);
	}

	@Override
	public void shutdown() {
	}
}