package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.util.Collection;
import java.util.Date;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Track;
import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm.user_apihtml.LastFMApiCrawlJob;

public class ArtistSongCrawlJob implements CrawlJob {

	private Evaluator e;
	
	private final String toCollect;

	public ArtistSongCrawlJob(String toCollect) {
		this.toCollect = toCollect;
	}
	
	@Override
	public void collect() {
		String[] split = toCollect.split(":");
		String type = split[0];
		String collect = split[1].replaceAll("/music/", "");
		
		if (type.equals("Ar")) {
			Artist info = Artist.getInfo("", LastFMApiCrawlJob.API_KEY);
			
			String name = info.getName();
			Collection<String> tags = info.getTags();
			String wikiSummary = info.getWikiSummary();
			String wikiText = info.getWikiText();
			Date wikiLastChanged = info.getWikiLastChanged();
			int listeners = info.getListeners();
			int playcount = info.getPlaycount();
			String mbid = info.getMbid();
			boolean streamable = info.isStreamable();
			String url = info.getUrl();

			
			
		} else if (type.equals("Ab")) {
			Album info = Album.getInfo("", "", LastFMApiCrawlJob.API_KEY);
			
			String name = info.getName();
			Collection<String> tags = info.getTags();
			String wikiSummary = info.getWikiSummary();
			String wikiText = info.getWikiText();
			Date wikiLastChanged = info.getWikiLastChanged();
			int listeners = info.getListeners();
			int playcount = info.getPlaycount();
			String mbid = info.getMbid();
			boolean streamable = info.isStreamable();
			String url = info.getUrl();
			
			String artist = info.getArtist();
			Date releaseDate = info.getReleaseDate();
			
		} else if (type.equals("Sn")) {
			Track info = Track.getInfo("", "", LastFMApiCrawlJob.API_KEY);
			
			String name = info.getName();
			Collection<String> tags = info.getTags();
			String wikiSummary = info.getWikiSummary();
			String wikiText = info.getWikiText();
			Date wikiLastChanged = info.getWikiLastChanged();
			int listeners = info.getListeners();
			int playcount = info.getPlaycount();
			String mbid = info.getMbid();
			boolean streamable = info.isStreamable();
			String url = info.getUrl();
			
			String artist = info.getArtist();
			String album = info.getAlbum();
			int duration = info.getDuration();
			int position = info.getPosition();
			Date playedWhen = info.getPlayedWhen();
			
		}
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}
}
