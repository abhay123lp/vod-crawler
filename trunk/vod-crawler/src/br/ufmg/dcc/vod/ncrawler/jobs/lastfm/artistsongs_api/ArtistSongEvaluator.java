package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.io.File;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.evaluator.AbstractEvaluator;

public class ArtistSongEvaluator extends AbstractEvaluator<String, LastFMMusicDAO> {

	private Collection<String> toCollect;
	private File savePath;

	public ArtistSongEvaluator(Collection<String> toCollect, File savePath) {
		this.toCollect = toCollect;
		this.savePath = savePath;
	}
	
	@Override
	public CrawlJob createJob(String next) {
		return new ArtistSongCrawlJob(next);
	}

	@Override
	public Collection<String> getSeeds() {
		return toCollect;
	}

	@Override
	public Collection<String> realEvaluateAndSave(String collectID,	LastFMMusicDAO collectContent) throws Exception {
		MyXStreamer.getInstance().toXML(collectContent, new File(savePath + File.separator + collectID));
		return null;
	}

}
