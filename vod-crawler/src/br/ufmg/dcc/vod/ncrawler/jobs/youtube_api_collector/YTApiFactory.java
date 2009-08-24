package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

import com.google.gdata.client.youtube.YouTubeService;

public class YTApiFactory implements EvaluatorFactory<String, YoutubeUserDAO, YoutubeUserAPICrawlJob> {

	private YouTubeService service;
	private YoutubeAPIEvaluator e;
	private YoutubeUserApiSave serializer;

	@Override
	public Serializer<YoutubeUserAPICrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime, List<String> seeds) {
		this.service = new YouTubeService("ytapi-FlavioVinicius-DataCollector-si5mgkd4-0", "AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
		this.e = new YoutubeAPIEvaluator(service, seeds, saveFolder);
		this.serializer = new YoutubeUserApiSave(service);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public Evaluator<String, YoutubeUserDAO> getEvaluator() {
		return e;
	}
}