package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_video_response;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YoutubeResponseFactory implements EvaluatorFactory<String, YoutubeVideoDAOWResponse, YoutubeAPIVidResponseJob> {

	private VideoResponseEvaluator e;
	private YoutubeResponseVideoSave serializer;

	@Override
	public Evaluator<String, YoutubeVideoDAOWResponse> getEvaluator() {
		return e;
	}

	@Override
	public Serializer<YoutubeAPIVidResponseJob> getSerializer() {
		return serializer;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime, List<String> seeds) {
		this.e = new VideoResponseEvaluator(seeds, saveFolder);
		this.serializer = new YoutubeResponseVideoSave();
	}

	@Override
	public void shutdown() {
	}
}
