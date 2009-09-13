package br.ufmg.dcc.vod.ncrawler.jobs.youtube.user_api;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YTApiFactory implements EvaluatorFactory<String, YoutubeUserDAO, YoutubeUserAPICrawlJob> {

	private YoutubeAPIEvaluator e;
	private YoutubeUserApiSave serializer;

	@Override
	public Serializer<YoutubeUserAPICrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int nThreads, File saveFolder, long sleepTime, List<String> seeds) {
		this.e = new YoutubeAPIEvaluator(seeds, saveFolder, sleepTime);
		this.serializer = new YoutubeUserApiSave(sleepTime);
		
		System.setProperty("http.keepAlive", "true");
		System.setProperty("http.maxConnections", ""+nThreads);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public Evaluator<String, YoutubeUserDAO> getEvaluator() {
		return e;
	}
}