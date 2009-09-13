package br.ufmg.dcc.vod.ncrawler.ui;

import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml.LFMApiFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube.user_api.YTApiFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube.video_api.YTVideoApiFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube.videoresp_api.YoutubeResponseFactory;

public class CrawlerPool {

	private static final Map<String, EvaluatorFactory<?,?,?>> crawlers = new HashMap<String, EvaluatorFactory<?,?,?>>();
	static {
		crawlers.put("YTAPI", new YTApiFactory());
		crawlers.put("YTVIDAPI", new YTVideoApiFactory());
		crawlers.put("LFM", new LFMApiFactory());
		crawlers.put("YOUTUBE_RESPONSE", new YoutubeResponseFactory());
	}
	
	public static EvaluatorFactory<?,?,?> get(String name) {
		return crawlers.get(name);
	}
}
