package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.HTMLType;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;
import br.ufmg.dcc.vod.ncrawler.stats.CompositeStatEvent;
import br.ufmg.dcc.vod.ncrawler.stats.Display;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.Tracker;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

/*
 * - Profile
 * http://www.youtube.com/profile?user=USER
 * 
 * - Friends
 * http://www.youtube.com/profile?user=USER&view=friends
 * 
 * - Subscribers
 * http://www.youtube.com/profile?user=USER&view=subscribers
 * 
 * - Subscriptions
 * http://www.youtube.com/profile?user=USER&view=subscriptions
 * 
 * - Videos
 * http://www.youtube.com/profile?user=USER&view=videos
 * 
 * - Groups
 * http://www.youtube.com/profile?user=USER&view=groups
 * 
 * - Favorites
 * http://www.youtube.com/profile?user=USER&view=favorites
 * 
 * Procurar por next em cada página!!!!!
 * <a href="/profile?user=USER&amp;view=QUE_BUSCO&amp;start=##">Next</a>
 */
public class YTUserHTMLEvaluator implements Evaluator<Pair<String, HTMLType>, InputStream> {

	private static final Logger LOG = Logger.getLogger(YTUserHTMLEvaluator.class);
	
	private final Pattern NEXT_PATTERN = Pattern.compile("(\\s+&nbsp;<a href=\")(.*?)(\"\\s*>\\s*Next.*)");
	private final Pattern VIDEO_PATTERN = Pattern.compile("(\\s+<div class=\"video-main-content\" id=\"video-main-content-)(.*?)(\".*)");
	private final Pattern RELATION_PATTERN = Pattern.compile("(\\s*<a href=\"/user/)(.*?)(\"\\s+onmousedown=\"trackEvent\\('ChannelPage'.*)");
	private final Pattern ERROR_PATTERN = Pattern.compile("\\s*<input type=\"hidden\" name=\"challenge_enc\" value=\".*");
	
	private static final String VIEW = "&view=";
	private static final String PROFILE_USER = "profile?user=";
	private static final String GL_US_HL_EN = "&gl=US&hl=en";
	private static final String BASE_URL = "http://www.youtube.com/";

	private static final String DIS_URLS = "DIS_URLS";
	private static final String DIS_VIDEOS = "DIS_VIDEOS";
	private static final String DIS_USERS = "DIS_USERS";
	
	private static final String COL_URLS = "COL_URLS";
	private static final String COL_VIDEOS = "COL_VIDEOS";
	private static final String COL_USERS = "COL_USERS";
	
	private static final String ERR_URLS = "ERR_URLS";
	private static final String ERR_VIDEOS = "ERR_VIDEOS";
	private static final String ERR_USERS = "ERR_USERS";

	private final File videosFolder;
	private final File usersFolder;
	private final HttpClient httpClient;

	private List<String> initialVideos;
	private List<String> initialUsers;

	private Tracker<String> crawledUsers;
	private Tracker<String> crawledVideos;
	
	private StatsPrinter sp;

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers, HttpClient client) {
		this(videosFolder, usersFolder, initialUsers, new LinkedList<String>(), client);
	}

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers, List<String> initialVideos, HttpClient client) {
		this.videosFolder = videosFolder;
		this.usersFolder = usersFolder;
		this.initialUsers = initialUsers;
		this.initialVideos = initialVideos;
		this.httpClient = client;
	}
	
	@Override
	public void setTrackerFactory(TrackerFactory factory) {
		this.crawledUsers = factory.createTracker();		
		this.crawledVideos = factory.createTracker();
	}
	
	@Override
	public Collection<CrawlJob> getInitialCrawl() {
		List<CrawlJob> rv = new ArrayList<CrawlJob>();
		
		Map<String, Integer> incs = new HashMap<String, Integer>();
		int urls = 0;
		int vidUrls = 0;
		int userUrls = 0;
		
		for (String v : initialVideos) {
			try {
				int generateVideoUrls = generateVideoUrls(v, rv);
				vidUrls += generateVideoUrls;
				urls += generateVideoUrls;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		for (String u : initialUsers) {
			try {
				int generateUserUrls = generateUserUrls(u, rv);
				userUrls += generateUserUrls;
				urls += generateUserUrls;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		incs.put(DIS_URLS, urls);
		incs.put(DIS_VIDEOS, vidUrls);
		incs.put(DIS_USERS, userUrls);
		
		System.out.println(incs);
		
		sp.notify(new CompositeStatEvent(incs));
		
		return rv;
	}
	
	private int generateUserUrls(String u, List<CrawlJob> rv) throws MalformedURLException {
		int i = 0;
		if (crawledUsers.add(u)) {
			for (HTMLType t : YTHTMLType.FAVORITES.enumerate()) {
				if (t.hasFollowUp()) {
					String url = BASE_URL + PROFILE_USER + u + VIEW + t.getFeatureName() + GL_US_HL_EN;
					File folder = new File(usersFolder + File.separator + u + File.separator + t.getFeatureName());
					folder.mkdirs();
					rv.add(new URLSaveCrawlJob(new URL(url), folder, t, httpClient));
					LOG.info("Found user url: " + url);
					i++;
				}
			}
		}
		
		return i;
	}

	private int generateVideoUrls(String v, List<CrawlJob> rv) throws MalformedURLException {
		int i = 0;
		if (crawledVideos.add(v)) {
			String url = BASE_URL + "watch?v=" + v + GL_US_HL_EN;
			videosFolder.mkdirs();
			rv.add(new URLSaveCrawlJob(new URL(url), videosFolder, YTHTMLType.SINGLE_VIDEO, httpClient));
			LOG.info("Found video url: " + url);
			i++;
		}
		
		return i;
	}

	@Override
	public Collection<CrawlJob> evaluteAndSave(Pair<String, HTMLType> collectID, InputStream collectContent, File savePath) {
		String nextLink = null;
		BufferedReader in = null;
		PrintStream out = null;
		
		Set<String> nextUrls = new HashSet<String>();
		
		HTMLType t = collectID.second;
		
		Map<String, Integer> incs = new HashMap<String, Integer>();
		List<CrawlJob> rv = new ArrayList<CrawlJob>();
		try {
			LOG.info("Flushing " + collectID.first + " to disk");
			
			in = new BufferedReader(new InputStreamReader(collectContent));
			out = new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(savePath))));
			
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				out.println(inputLine);
				
				Matcher matcher = NEXT_PATTERN.matcher(inputLine);
				if (matcher.matches() && inputLine.contains(t.getFeatureName())) {
					nextLink = matcher.group(2);
				}
				
				Pattern pat = null;
				if (t == YTHTMLType.FAVORITES || t == YTHTMLType.VIDEOS) {
					pat = VIDEO_PATTERN;
				} else if (t == YTHTMLType.SUBSCRIBERS || t == YTHTMLType.SUBSCRIPTIONS || t == YTHTMLType.FRIENDS) {
					pat = RELATION_PATTERN;
				} 
				
				if (pat != null) {
					matcher = pat.matcher(inputLine);
					if (matcher.matches()) {
						nextUrls.add(matcher.group(2));
					}
				}
				
				matcher = ERROR_PATTERN.matcher(inputLine);
				if (matcher.matches()) {
					throw new YTErrorPageException();
				}
			}

			int urls = 0;
			int userUrls = 0;
			int vidUrls = 0;
			
			String followUp = BASE_URL + nextLink + GL_US_HL_EN;
			if (nextLink != null && t.hasFollowUp() && !nextLink.equals(collectID.first)) {
				File folder = new File(savePath.getParent());
				rv.add(new URLSaveCrawlJob(new URL(followUp), folder, t, httpClient));
				userUrls++;
				urls++;
			}

			if (t == YTHTMLType.FAVORITES || t == YTHTMLType.VIDEOS) {
				for (String v : nextUrls) {
					int generateVideoUrls = generateVideoUrls(v, rv);
					vidUrls += generateVideoUrls;
					urls += generateVideoUrls;
				}
			} else if (t == YTHTMLType.SUBSCRIBERS || t == YTHTMLType.SUBSCRIPTIONS) {
				for (String u : nextUrls) {
					int generateUserUrls = generateUserUrls(u, rv);
					userUrls += generateUserUrls;
					urls += generateUserUrls;
				}
			}
			
			incs.put(DIS_URLS, urls);
			incs.put(DIS_VIDEOS, vidUrls);
			incs.put(DIS_USERS, userUrls);
			
			incs.put(COL_URLS, 1);
			if (t == YTHTMLType.SINGLE_VIDEO)
				incs.put(COL_VIDEOS, 1);
			else
				incs.put(COL_USERS, 1);
			
			LOG.info("File saved, found an additional " + urls + " urls to collect.");
		} catch (Exception e) {
			errorOccurred(collectID, e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
				
			if (out != null)
				out.close();
			
			if (sp != null && incs.size() > 0) sp.notify(new CompositeStatEvent(incs));
		}
		
		return rv;
	}

	@Override
	public void setStatsKeeper(StatsPrinter sp) {
		this.sp = sp;
		this.sp.setDisplay(new Display() {
			@Override
			public void print(Map<String, Integer> m) {
				Integer disU = m.get(DIS_USERS) == null ? 0 : m.get(DIS_USERS);
				Integer colU = m.get(COL_USERS) == null ? 0 : m.get(COL_USERS);
				Integer errU = m.get(ERR_USERS) == null ? 0 : m.get(ERR_USERS);
				
				Integer disV = m.get(DIS_VIDEOS) == null ? 0 : m.get(DIS_VIDEOS);
				Integer colV = m.get(COL_VIDEOS) == null ? 0 : m.get(COL_VIDEOS);
				Integer errV = m.get(ERR_VIDEOS) == null ? 0 : m.get(ERR_VIDEOS);
				
				Integer disURL = m.get(DIS_URLS) == null ? 0 : m.get(DIS_URLS);
				Integer colURL = m.get(COL_URLS) == null ? 0 : m.get(COL_URLS);
				Integer errURL = m.get(ERR_URLS) == null ? 0 : m.get(ERR_URLS);
				
				System.out.println("----" + new Date());
				System.out.println("-- users");
				System.out.println(DIS_USERS + " : " + disU);
				System.out.println(COL_USERS + " : " + colU + " ( " + (colU.doubleValue() / disU.doubleValue()) + " )");
				System.out.println(ERR_USERS + " : " + errU + " ( " + (errU.doubleValue() / disU.doubleValue()) + " )");
				System.out.println("-- vids");
				System.out.println(DIS_VIDEOS + " : " + disV);
				System.out.println(COL_VIDEOS + " : " + colV + " ( " + (colV.doubleValue() / disV.doubleValue()) + " )");
				System.out.println(ERR_VIDEOS + " : " + errV + " ( " + (errV.doubleValue() / disV.doubleValue()) + " )");
				System.out.println("-- total");
				System.out.println(DIS_URLS + " : " + disURL);
				System.out.println(COL_URLS + " : " + colURL + " ( " + (colURL.doubleValue() / disURL.doubleValue()) + " )");
				System.out.println(ERR_URLS + " : " + errURL + " ( " + (errURL.doubleValue() / disURL.doubleValue()) + " )");
				System.out.println("---");
			}}
		);
	}

	@Override
	public void errorOccurred(Pair<String, HTMLType> collectID, Exception e) {
		Map<String, Integer> incs = new HashMap<String, Integer>();
		if (collectID.second == YTHTMLType.SINGLE_VIDEO)
			incs.put(ERR_VIDEOS, 1);
		else
			incs.put(ERR_USERS, 1);
		incs.put(ERR_URLS, 1);
		sp.notify(new CompositeStatEvent(incs));
		LOG.error("Error collecting: " + collectID.first, e);
	}
}