package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_videos;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YTVideoApiFactory implements EvaluatorFactory<String, YoutubeVideoDAO, YoutubeAPIVideoCrawlJob> {

	private YoutubeVideoAPIEvaluator e;
	private YoutubeVideoApiSave serializer;

	@Override
	public Evaluator<String, YoutubeVideoDAO> getEvaluator() {
		return e;
	}

	@Override
	public Serializer<YoutubeAPIVideoCrawlJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime,
			List<String> seeds) {
		this.e = new YoutubeVideoAPIEvaluator(seeds, saveFolder);
		this.serializer = new YoutubeVideoApiSave();		
	}

	@Override
	public void shutdown() {
	}

}
