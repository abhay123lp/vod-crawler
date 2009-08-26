package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.util.Collection;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Playlist;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.User;
import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;

public class LastFMApiCrawlJob  implements CrawlJob {

	public static final String API_KEY     = "c86a6f99618d3dbfcf167366be991f3b";
	public static final String API_SECRET  = "6fb4fdae8ddcfa6d7a70024aec7a0e42";
	public static final String SESSION_KEY = "fe1acda9911e017979532610a88c0db8";
	
	private static final int LIMIT = Integer.MAX_VALUE;
	private User user;
	
	public LastFMApiCrawlJob(User user) {
		this.user = user;
	}
	
	@Override
	public void collect() {
		String userID = user.getName();
		
		Collection<User> friends = User.getFriends(userID, API_KEY);
		Collection<User> neighbours = User.getNeighbours(userID, LIMIT, API_KEY);           
		Collection<Playlist> playlists = User.getPlaylists(userID, API_KEY);
		
		Collection<Track> lovedTracks = User.getLovedTracks(userID, API_KEY);
		Collection<Track> recentTracks = User.getRecentTracks(userID, LIMIT, API_KEY);
		Collection<Album> topAlbums = User.getTopAlbums(userID, API_KEY);
		Collection<Artist> topArtists = User.getTopArtists(userID, API_KEY);
		Collection<String> topTags = User.getTopTags(userID, LIMIT, API_KEY);
		Collection<Track> topTracks = User.getTopTracks(userID, API_KEY);
	}

	@Override
	public void setEvaluator(Evaluator e) {
		// TODO Auto-generated method stub
	}

}
