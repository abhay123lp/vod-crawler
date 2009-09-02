package br.ufmg.dcc.vod.ncrawler.ui;

import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector.YTApiFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_videos.YTVideoApiFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles.YTHtmlFactory;

public class CrawlerPool {

	private static final Map<String, EvaluatorFactory<?,?,?>> crawlers = new HashMap<String, EvaluatorFactory<?,?,?>>();
	static {
		crawlers.put("YTAPI", new YTApiFactory());
		crawlers.put("YTVIDAPI", new YTVideoApiFactory());
		crawlers.put("YTHTML", new YTHtmlFactory());
	}
	
	public static EvaluatorFactory<?,?,?> get(String name) {
		return crawlers.get(name);
	}
}
