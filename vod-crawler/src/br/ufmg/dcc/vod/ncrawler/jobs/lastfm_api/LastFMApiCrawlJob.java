package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.Result;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.User;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

public class LastFMApiCrawlJob implements CrawlJob {

	private static final Logger LOG = Logger.getLogger(LastFMApiCrawlJob.class);
	
	private static final long serialVersionUID = 1L;

	//Patterns for tags
	private static final Pattern SONG_PATTERN_FOR_TAG = Pattern.compile("(\\s+<a href=\"/music/)(.*?)(class=\"primary\">)(.*?)(</a>.*class=\"primary\">)(.*?)(</a>.*)");
	private static final Pattern SONG_WITH_PIC_PATTERN_FOR_TAG = Pattern.compile("(\\s+<a href=\"/music/)(.*?)(/)(.*)(class=\"primary\">\\s+<span class=\"albumCover coverSmall resImage\">)(.*?)");
	private static final Pattern ARTIST_PATTERN_FOR_TAG = Pattern.compile("(\\s+<a href=\"/music/)(.*?)(class=\"primary\".*?)(</a>.*)");
	
	//Patterns for plays
    private static final Pattern ARTIST_PATTERN_FOR_PLAYS = Pattern.compile("(\\s+<a href=\"/music/)(.*?)(span class=\"pictureFrame\".*)");
	
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
		LOG.info("Collecting user: " + userID);
		boolean allFailed = true;
		
		Collection<User> friends = User.getFriends(userID, false, LIMIT, API_KEY);
		Result lastResult = Caller.getInstance().getLastResult();

		Set<String> friendNames = new HashSet<String>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			
			for (User u : friends) {
				friendNames.add(u.getName());
			}
		} else {
			LOG.warn("Unable to collect friends for user " + userID);
		}
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e1) {
		}

		Collection<Track> lovedTracks = User.getLovedTracks(userID, API_KEY);
		lastResult = Caller.getInstance().getLastResult();
		
		Set<String> loved = new HashSet<String>();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			
			for (Track t : lovedTracks) {
				String artist;
				try {
					artist = unescape(t.getArtist());
					String songName = unescape(t.getName());
					
					loved.add(createSongPair(artist, songName));
				} catch (UnsupportedEncodingException e) {
				}
			}
			
		} else {
			LOG.warn("Unable to collect loved tracks for user " + userID);
		}
		
		Collection<String> topTags = new HashSet<String>();
		lastResult = Caller.getInstance().getLastResult();
		if (lastResult.isSuccessful()) {
			allFailed = false;
			topTags = User.getTopTags(userID, LIMIT, API_KEY);
		} else {
			LOG.warn("Unable to collect tags for user " + userID);
		}
		
		Collection<LastFMTagDAO> discoverTagDAO = new HashSet<LastFMTagDAO>();
		try {
			discoverTagDAO = discoverTagDAO(topTags);
		} catch (IOException ioe) {
			LOG.warn("Unable to collected specific tag data for user " + userID, ioe);
		}
		
		if (!allFailed) {
			LastFMUserDAO lfmu = new LastFMUserDAO(userID, friendNames, loved, discoverTagDAO);
			LOG.info("Done collecting user: " + userID);
			e.evaluteAndSave(userID, lfmu);
		} else {
			LOG.warn("Unable to collect user: " + userID);
			e.error(userID, new UnableToCollectException("Unable to collect user"));
		}
	}

	private String createSongPair(String artist, String songName) {
		return artist + " - " + songName;
	}

	private Collection<String> discoverArtists() throws IOException {
		Set<String> rv = new HashSet<String>();
		
		int pNum = 1;
		boolean continueCollecting = true;
		while (continueCollecting) {
			int previousSize = rv.size();
			
			String u = "http://www.last.fm/user/"+ URLEncoder.encode(userID, "UTF8") +"/library?page=" + pNum + "&sortOrder=desc&sortBy=plays";
			URL url = new URL(u);
			
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-c86a6f99618d3dbfcf167366be991f3b");
			
			connection.connect();
			
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					Matcher artistMatcher = ARTIST_PATTERN_FOR_TAG.matcher(inputLine);
					Matcher songMatcher = SONG_PATTERN_FOR_TAG.matcher(inputLine);
					Matcher songPicMatcher = SONG_WITH_PIC_PATTERN_FOR_TAG.matcher(inputLine);
					
					if (artistMatcher.matches()) {
						String aux = artistMatcher.group(2);
						String artist = unescape(aux.substring(0, aux.length() - 2));
						
						rv.add(artist);
					}
				}
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
					}
			}
			
			int newSize = rv.size();
			continueCollecting = previousSize != newSize;
			pNum++;
		}
		
		return rv;
	}
	
	private Collection<LastFMTagDAO> discoverTagDAO(Collection<String> topTags) throws IOException {
		Set<LastFMTagDAO> rv = new HashSet<LastFMTagDAO>();
		
		for (String tag : topTags) {
			boolean continueCollecting = true;
			
			Set<String> artists = new HashSet<String>();
			Set<String> songs = new HashSet<String>();
			
			int pNum = 1;
			while (continueCollecting) {
				int previousSize = artists.size() + songs.size();
				
				String u = "http://www.last.fm/user/"+ URLEncoder.encode(userID, "UTF8") +"/library/tags?tag="+ URLEncoder.encode(tag, "UTF8") +"&page="+ pNum;
				URL url = new URL(u);
				
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-c86a6f99618d3dbfcf167366be991f3b");
				
				connection.connect();
				
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						Matcher artistMatcher = ARTIST_PATTERN_FOR_TAG.matcher(inputLine);
						Matcher songMatcher = SONG_PATTERN_FOR_TAG.matcher(inputLine);
						Matcher songPicMatcher = SONG_WITH_PIC_PATTERN_FOR_TAG.matcher(inputLine);
						
						if (songMatcher.matches()) {
							String artist = unescape(songMatcher.group(4));
							String songTitle = unescape(songMatcher.group(6));
							String artistSong = createSongPair(artist, songTitle);
							
							songs.add(artistSong);
						} else if (songPicMatcher.matches()) {
							String artist = unescape(songPicMatcher.group(2));
							String songTitle = unescape(songPicMatcher.group(4));
							
							String artistSong = createSongPair(artist, songTitle);
							
							songs.add(artistSong);
						} else if (artistMatcher.matches()) {
							String aux = artistMatcher.group(2);
							String artist = unescape(aux.substring(0, aux.length() - 2));
							
							artists.add(artist);
						}
					}
				} finally {
					if (in != null)
						try {
							in.close();
						} catch (IOException e) {
						}
				}
				
				int newSize = artists.size() + songs.size();
				continueCollecting = previousSize != newSize;
				pNum++;
			}
			
			rv.add(new LastFMTagDAO(userID, tag, artists, songs));
		}
		
		return rv;
	}

	public static String unescape(String s) throws UnsupportedEncodingException {
		return StringEscapeUtils.unescapeHtml(URLDecoder.decode(s, "UTF-8"));
	}
	
	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}

	public String getUserID() {
		return userID;
	}
	
	public static void main(String[] args) throws Exception {
		String userID = "tawhaki";
		
		Set<String> rv = new HashSet<String>();
		
		int pNum = 1;
		boolean continueCollecting = true;
		while (continueCollecting) {
			int previousSize = rv.size();
			
			String u = "http://www.last.fm/user/"+ URLEncoder.encode(userID, "UTF8") +"/library?page=" + pNum + "&sortOrder=desc&sortBy=plays";
			URL url = new URL(u);
			
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-c86a6f99618d3dbfcf167366be991f3b");
			
			connection.connect();
			
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					Matcher artistMatcher = ARTIST_PATTERN_FOR_PLAYS.matcher(inputLine);
					
					if (artistMatcher.matches()) {
						String aux = artistMatcher.group(2);
						String artist = unescape(aux.substring(0, aux.length() - 2));
						
						rv.add(artist);
						
						System.out.println(artist);
					}
				}
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
					}
			}
			
			int newSize = rv.size();
			continueCollecting = previousSize != newSize;
			pNum++;
		}
	}
}
