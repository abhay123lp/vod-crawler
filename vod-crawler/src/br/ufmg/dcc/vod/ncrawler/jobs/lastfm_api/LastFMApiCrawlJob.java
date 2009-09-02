package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.Playlist;
import net.roarsoftware.lastfm.Result;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.User;
import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class LastFMApiCrawlJob implements CrawlJob {

	private static final long serialVersionUID = 1L;

	private final Pattern SONG_PATTERN = Pattern.compile("(\\s+<a href=\"/music/)(.*?)(class=\"primary\">)(.*?)(</a>.*class=\"primary\">)(.*?)(</a>.*)");
	
	public static final String API_KEY     = "c86a6f99618d3dbfcf167366be991f3b";
	public static final String API_SECRET  = "6fb4fdae8ddcfa6d7a70024aec7a0e42";
	public static final String SESSION_KEY = "fe1acda9911e017979532610a88c0db8";
	
	private static final int LIMIT = 1000000000;
	private String userID;
	private Evaluator e;
	private final long sleepTime;
	
	public LastFMApiCrawlJob(String userID, long sleepTime) {
		this.userID = userID;
		this.sleepTime = sleepTime;
	}
	
	@Override
	public void collect() {
		boolean allFailed = true;
		
		Collection<User> friends = User.getFriends(userID, false, LIMIT, API_KEY);
		Result lastResult = Caller.getInstance().getLastResult();

		Set<String> friendNames = new HashSet<String>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			
			for (User u : friends) {
				friendNames.add(u.getName());
			}
		}
		
		Collection<Playlist> playlists = User.getPlaylists(userID, API_KEY);
		lastResult = Caller.getInstance().getLastResult();
		
		Set<LastFMPlayListDAO> playlistsDAO = new HashSet<LastFMPlayListDAO>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			
			for (Playlist p : playlists) {
				Collection<Track> tracks = p.getTracks();
				Set<LastFMTrackDAO> lastFMTrackDAO = createTrackDAO(tracks);
				LastFMPlayListDAO dao = new LastFMPlayListDAO(p.getId(), lastFMTrackDAO);
				playlistsDAO.add(dao);
			}
		}
		
		Collection<Track> lovedTracks = User.getLovedTracks(userID, API_KEY);
		lastResult = Caller.getInstance().getLastResult();
		
		Set<LastFMTrackDAO> lovedTracksDAO = new HashSet<LastFMTrackDAO>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			lovedTracksDAO = createTrackDAO(lovedTracks);
		}
		
		Collection<Track> recentTracks = User.getRecentTracks(userID, LIMIT, API_KEY);
		lastResult = Caller.getInstance().getLastResult();
		
		Set<LastFMTrackDAO> recentTracksDAO = new HashSet<LastFMTrackDAO>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			recentTracksDAO = createTrackDAO(recentTracks);	
		}
		
		Collection<String> topTags = new HashSet<String>();
		lastResult = Caller.getInstance().getLastResult();
		
		if (lastResult.isSuccessful()) {
			allFailed = false;
			topTags = User.getTopTags(userID, LIMIT, API_KEY);
		}
		
		if (!allFailed) {
			LastFMUserDAO lfmu = new LastFMUserDAO(userID, friendNames, playlistsDAO, lovedTracksDAO,
					recentTracksDAO, topTags);
			e.evaluteAndSave(userID, lfmu);
		} else {
			e.error(userID, new UnableToCollectException("Unable to collect user"));
		}
	}

	private Set<LastFMTrackDAO> createTrackDAO(Collection<Track> tracks) {
		Set<LastFMTrackDAO> rv = new HashSet<LastFMTrackDAO>();
		for (Track t : tracks) {
			String artist = t.getArtist();
			String artistMbid = t.getArtistMbid();
			String album = t.getAlbum();
			String albumMbid = t.getAlbumMbid();
			String name = t.getName();
			String mbid = t.getMbid();
			int duration = t.getDuration();
			
			rv.add(new LastFMTrackDAO(artist, artistMbid, album, albumMbid, name, mbid, duration));
		}
		
		return rv;
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}

	public String getUserID() {
		return userID;
	}
}
