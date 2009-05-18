package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.Evaluator;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.common.SimpleBloomFilter;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.HTMLType;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles.YTHTMLType.Type;
import br.ufmg.dcc.vod.ncrawler.tracker.BFTracker;
import br.ufmg.dcc.vod.ncrawler.tracker.ThreadSafeTracker;

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
	
	private static final int TEN_MILLION = 10000000;

	private static final String VIEW = "&view=";
	private static final String PROFILE_USER = "profile?user=";
	private static final String GL_US_HL_EN = "&gl=US&hl=en";
	private static final String BASE_URL = "http://www.youtube.com/";
	
	private final ThreadSafeTracker<String> crawledUsers;
	private final ThreadSafeTracker<String> crawledVideos;
	
	private final File videosFolder;
	private final File usersFolder;
	private final HttpClient httpClient;

	private List<String> initialVideos;
	private List<String> initialUsers;

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers, HttpClient client) {
		this(videosFolder, usersFolder, initialUsers, new LinkedList<String>(), new HashSet<String>(), new HashSet<String>(), client);
	}

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers, List<String> initialVideos, HashSet<String> crawledVideos, HashSet<String> crawledUsers, HttpClient client) {
		this.videosFolder = videosFolder;
		this.usersFolder = usersFolder;
		this.initialUsers = initialUsers;
		this.initialVideos = initialVideos;
		
		this.crawledUsers = new ThreadSafeTracker<String>(new BFTracker<String>(new SimpleBloomFilter<String>(5 * TEN_MILLION * 16, 5 * TEN_MILLION)));		
		for (String u : crawledUsers) {
			this.crawledUsers.add(u);
		}
		
		this.crawledVideos = new ThreadSafeTracker<String>(new BFTracker<String>(new SimpleBloomFilter<String>(5 * TEN_MILLION * 16, 5 * TEN_MILLION)));
		for (String v : crawledVideos) {
			this.crawledVideos.add(v);
		}
		
		this.httpClient = client;
	}
	
//	private void printStats() {
//		System.out.println("---");
//		System.out.println("Stats: " + new Date());
//		System.out.println("-- in users");
//		System.out.println("Discovered Users = " + crawledUsers.size());
//		System.out.println("In URLs = " + userUrls);
//		System.out.println("Collected user URLs = " + finishedUserUrls + " (" + ((double)finishedUserUrls/userUrls) + ")");
//		System.out.println("User URLs with error = " + errorUserUrls + " (" + ((double)errorUserUrls/userUrls) + ")");
//		System.out.println("-- in videos (each video is one url only)");
//		System.out.println("Discovered Videos = " + crawledVideos.size());
//		System.out.println("Collected Videos = " + finishedVideos + " (" + ((double)finishedVideos/crawledVideos.size()) + ")");
//		System.out.println("Videos with error = " + errorVideos + " (" + ((double)errorVideos/crawledVideos.size()) + ")");
//		System.out.println("-- in total urls");
//		System.out.println("Discovered  URL = " + dispatchUrls);
//		System.out.println("URLs collected = " + finishedUrls + " (" + ((double)finishedUrls/dispatchUrls) + ")");
//		System.out.println("URLs with error = " + errorUrls + " (" + ((double)errorUrls/dispatchUrls) + ")");
//		System.out.println("---");
//		System.out.println();
//	}

	
	@Override
	public Collection<CrawlJob> getInitialCrawl() throws Exception {
		List<CrawlJob> rv = new ArrayList<CrawlJob>();
		
		for (String v : initialVideos) {
			generateVideoUrls(v, rv);
		}
		
		for (String u : initialUsers) {
			generateUserUrls(u, rv);
		}
		
		return rv;
	}
	
	private void generateUserUrls(String u, List<CrawlJob> rv) throws MalformedURLException {
		if (crawledUsers.add(u)) {
			for (Type t : YTHTMLType.Type.values()) {
				YTHTMLType ytt = YTHTMLType.forEnum(t);
				
				if (ytt.hasFollowUp()) {
					String url = BASE_URL + PROFILE_USER + u + VIEW + ytt.getFeatureName() + GL_US_HL_EN;
					File folder = new File(usersFolder + File.separator + u + File.separator + ytt.getFeatureName());
					folder.mkdirs();
					rv.add(new URLSaveCrawlJob(new URL(url), folder, ytt, httpClient));
				}
			}
		}
	}

	private void generateVideoUrls(String v, List<CrawlJob> rv) throws MalformedURLException {
		if (crawledVideos.add(v)) {
			String url = BASE_URL + "watch?v=" + v + GL_US_HL_EN;
			LOG.info("Dispatching video: video="+v + ", url="+url);
			videosFolder.mkdirs();
			rv.add(new URLSaveCrawlJob(new URL(url), videosFolder, YTHTMLType.SINGLE_VIDEO, httpClient));
		}
	}

	@Override
	public Collection<CrawlJob> evaluteAndSave(Pair<String, HTMLType> collectID, InputStream collectContent, File savePath) throws Exception {
		String nextLink = null;
		BufferedReader in = null;
		Set<String> nextUrls = new HashSet<String>();
		
		HTMLType t = collectID.second;
		
		try {
			in = new BufferedReader(new InputStreamReader(collectContent));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
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
				
				matcher = pat.matcher(inputLine);
				if (matcher.matches()) {
					nextUrls.add(matcher.group(2));
				}
				
				matcher = ERROR_PATTERN.matcher(inputLine);
				if (matcher.matches()) {
					throw new YTErrorPageException();
				}
			}

			List<CrawlJob> rv = new ArrayList<CrawlJob>();
			
			//Has something to follow, can follow, and is not equal to the last link (Youtube specific)
			String followUp = BASE_URL + nextLink + GL_US_HL_EN;
			if (nextLink != null && t.hasFollowUp() && !nextLink.equals(collectID.first)) {
				File folder = new File(usersFolder + File.separator + savePath.getParentFile() + File.separator + t.getFeatureName());
				rv.add(new URLSaveCrawlJob(new URL(followUp), folder, t, httpClient));
			}

			if (t == YTHTMLType.FAVORITES || t == YTHTMLType.VIDEOS) {
				for (String v : nextUrls) {
					generateVideoUrls(v, rv);
				}
			} else if (t == YTHTMLType.SUBSCRIBERS || t == YTHTMLType.SUBSCRIPTIONS) {
				for (String u : nextUrls) {
					generateUserUrls(u, rv);	
				}
			}
			
			return rv;
		} finally {
			if (in != null) in.close();
		}
	}
}