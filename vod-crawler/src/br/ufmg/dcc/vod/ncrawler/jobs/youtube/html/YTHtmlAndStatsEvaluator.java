package br.ufmg.dcc.vod.ncrawler.jobs.youtube.html;

import java.io.OutputStream;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractEvaluator;

public class YTHtmlAndStatsEvaluator extends AbstractEvaluator<Pair<byte[], byte[]>> {

	private final Collection<String> initialVideos;
	private final OutputStream htmlFile;
	private final OutputStream statsFile;

	public YTHtmlAndStatsEvaluator(Collection<String> initialVideos, OutputStream htmlFile, OutputStream statsFile) {
		this.initialVideos = initialVideos;
		this.htmlFile = htmlFile;
		this.statsFile = statsFile;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new YTHtmlAndStatsCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return initialVideos;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID,	Pair<byte[], byte[]> collectContent) throws Exception {
		htmlFile.write(collectContent.first);
		statsFile.write(collectContent.second);
		return null;
	}

}
