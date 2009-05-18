package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlResult;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.common.SimpleBloomFilter;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles.YTHTMLType.Type;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;

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
public class YTUserHTMLEvaluator implements Evaluator<File, YTHTMLType> {

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
	
	private final Set<String> crawledUsers;
	private final Set<String> crawledVideos;
	private final File videosFolder;
	private final File usersFolder;
	
	private Processor<File, YTHTMLType> p;
	
	private final List<String> initialUsers;
	private final List<String> initialVideos;
	private final HttpClient httpClient;

	private int dispatchUrls = 0;
	private int finishedUrls = 0;
	private int errorUrls = 0;
	private int finishedVideos = 0;
	private int userUrls = 0;
	private int finishedUserUrls = 0;
	private int errorUserUrls = 0;
	private int errorVideos = 0;

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers, HttpClient client) {
		this(videosFolder, usersFolder, initialUsers, new LinkedList<String>(), new HashSet<String>(), new HashSet<String>(), client);
	}

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers, List<String> initialVideos, HashSet<String> crawledVideos, HashSet<String> crawledUsers, HttpClient client) {
		this.videosFolder = videosFolder;
		this.usersFolder = usersFolder;
		this.initialUsers = initialUsers;
		this.initialVideos = initialVideos;
		this.crawledUsers = new SimpleBloomFilter<String>(5 * TEN_MILLION * 16, 5 * TEN_MILLION);
		
		for (String u : crawledUsers) {
			this.crawledUsers.add(u);
		}
		
		this.crawledVideos = new SimpleBloomFilter<String>(5 * TEN_MILLION * 16, 5 * TEN_MILLION);
		for (String v : crawledVideos) {
			this.crawledVideos.add(v);
		}
		
		this.httpClient = client;
	}
	
	@Override
	public void setProcessor(Processor<File, YTHTMLType> p) {
		this.p = p;
	}
	
	@Override
	public void dispatchIntialCrawl() throws Exception {
		LOG.info("Dispatching initial crawl: numberOfUser="+initialUsers.size() + " , numberOfVideos="+initialVideos.size());
		for (String s : initialVideos) {
			dispatchVideo(s);
		}
		
		for (String s : initialUsers) {
			dispatchUser(s);
		}
		
		initialUsers.clear();
		initialVideos.clear();
	}
	
	@Override
	public void crawlJobConcluded(CrawlResult<File, YTHTMLType> j) {
		try {
			LOG.info("Finished Crawl of: job= "+j.getId());
			Pair<String, Set<String>> followUp = null;
			if (j != null && j.getResult() != null) {
				followUp = eval(j.getResult(), j.getType());
			}
			
			if (j.success() && followUp != null) {
				//Has something to follow, can follow, and is not equal to the last link (Youtube specific)
				String nextLink = BASE_URL + followUp.first + GL_US_HL_EN;
				if (followUp.first != null && j.getType().hasFollowUp() && !nextLink.equals(j.getId())) {
					LOG.info("Dispatching following link: link="+nextLink);
					URL next = new URL(nextLink);
					dispatch(new URLSaveCrawlJob<YTHTMLType>(next, j.getResult().getParentFile(), j.getType(), httpClient));
				}

				if (j.getType() == YTHTMLType.FAVORITES || j.getType() == YTHTMLType.VIDEOS) {
					for (String v : followUp.second) {
						dispatchVideo(v);
					}
				} else if (j.getType() == YTHTMLType.SUBSCRIBERS || j.getType() == YTHTMLType.SUBSCRIPTIONS) {
					for (String u : followUp.second) {
						dispatchUser(u);	
					}
				} 
				
				if (j.getType() != YTHTMLType.SINGLE_VIDEO) {
					finishedUserUrls++;
				} else if (j.getType() == YTHTMLType.SINGLE_VIDEO) {
					finishedVideos++;
				}
				finishedUrls++;
				LOG.info("URL url=" + j.getId() + " collected ok!");
			} else {
				LOG.error("URL url=" + j.getId() + " was not collected due to error!");
				errorUrls++;
				
				if (j.getType() != YTHTMLType.SINGLE_VIDEO) {
					errorUserUrls++;
				} else  {
					errorVideos++;
				}
			}
		} catch (Exception e) {
			errorUrls++;
			LOG.error("URL url=" + j.getId() + " was not collected due to error!");
			LOG.error("Exception occurred:", e);
			
			if (j.getType() != YTHTMLType.SINGLE_VIDEO) {
				errorUserUrls++;
			} else {
				errorVideos++;
			}
		}
		
		printStats();
	}

	private void printStats() {
		System.out.println("---");
		System.out.println("Stats: " + new Date());
		System.out.println("-- in users");
		System.out.println("Discovered Users = " + crawledUsers.size());
		System.out.println("In URLs = " + userUrls);
		System.out.println("Collected user URLs = " + finishedUserUrls + " (" + ((double)finishedUserUrls/userUrls) + ")");
		System.out.println("User URLs with error = " + errorUserUrls + " (" + ((double)errorUserUrls/userUrls) + ")");
		System.out.println("-- in videos (each video is one url only)");
		System.out.println("Discovered Videos = " + crawledVideos.size());
		System.out.println("Collected Videos = " + finishedVideos + " (" + ((double)finishedVideos/crawledVideos.size()) + ")");
		System.out.println("Videos with error = " + errorVideos + " (" + ((double)errorVideos/crawledVideos.size()) + ")");
		System.out.println("-- in total urls");
		System.out.println("Discovered  URL = " + dispatchUrls);
		System.out.println("URLs collected = " + finishedUrls + " (" + ((double)finishedUrls/dispatchUrls) + ")");
		System.out.println("URLs with error = " + errorUrls + " (" + ((double)errorUrls/dispatchUrls) + ")");
		System.out.println("---");
		System.out.println();
	}

	private void dispatchUser(String u) throws MalformedURLException {
		if (!crawledUsers.contains(u)) {
			LOG.info("Dispatching user: user="+u);
			crawledUsers.add(u);
			for (Type t : YTHTMLType.Type.values()) {
				YTHTMLType ytt = YTHTMLType.forEnum(t);
				
				if (ytt.hasFollowUp()) {
					String url = BASE_URL + PROFILE_USER + u + VIEW + ytt.getFeatureName() + GL_US_HL_EN;
					File folder = new File(usersFolder + File.separator + u + File.separator + ytt.getFeatureName());
					folder.mkdirs();
					dispatch(new URLSaveCrawlJob<YTHTMLType>(new URL(url), folder, ytt, httpClient));
				}
			}
		}
	}

	private void dispatchVideo(String v) throws MalformedURLException {
		if (!crawledVideos.contains(v)) {
			crawledVideos.add(v);
			String url = BASE_URL + "watch?v=" + v + GL_US_HL_EN;
			LOG.info("Dispatching video: video="+v + ", url="+url);
			videosFolder.mkdirs();
			dispatch(new URLSaveCrawlJob<YTHTMLType>(new URL(url), videosFolder, YTHTMLType.SINGLE_VIDEO, httpClient));
		}
	}
	
	private void dispatch(URLSaveCrawlJob<YTHTMLType> j) {
		if (j.getType() != YTHTMLType.SINGLE_VIDEO) {
			this.userUrls++;
		}
		
		this.dispatchUrls++;
		p.dispatch(j);
	}

	@Override
	public boolean isDone() {
		return (finishedUrls + errorUrls) >= dispatchUrls;
	}
	
	public Pair<String, Set<String>> eval(File f, YTHTMLType t) throws Exception {
		String nextLink = null;
		BufferedReader in = null;
		Set<String> returnValue = new HashSet<String>();
		
		try {
		    in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
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
		    	
		    	if (pat != null) {
			    	matcher = pat.matcher(inputLine);
			    	if (matcher.matches()) {
			    		returnValue.add(matcher.group(2));
			    	}
		    	}
		    	
		    	matcher = ERROR_PATTERN.matcher(inputLine);
		    	if (matcher.matches()) {
		    		throw new YTErrorPageException();
		    	}
		    }
		    
		    return new Pair<String, Set<String>>(nextLink, returnValue);
		} finally {
			if (in != null) in.close();
		}
	}
}