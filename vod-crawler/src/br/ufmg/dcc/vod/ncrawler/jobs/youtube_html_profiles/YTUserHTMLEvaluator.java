package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
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
public class YTUserHTMLEvaluator implements Evaluator<Pair<String, Set<String>>, HTMLType> {

	private static final Logger LOG = Logger.getLogger(YTUserHTMLEvaluator.class);
	
	private static final String VIEW = "&view=";
	private static final String PROFILE_USER = "profile?user=";
	private static final String GL_US_HL_EN = "&gl=US&hl=en";
	private static final String BASE_URL = "http://www.youtube.com/";
	
	private final HashSet<Integer> crawledUsers;
	private final HashSet<Integer> crawledVideos;
	private final File videosFolder;
	private final File usersFolder;
	
	private Processor<Pair<String, Set<String>>, HTMLType> p;
	
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
		this.crawledUsers = new HashSet<Integer>();
		
		for (String u : crawledUsers) {
			this.crawledUsers.add(u.hashCode());
		}
		
		this.crawledVideos = new HashSet<Integer>();
		for (String v : crawledVideos) {
			this.crawledUsers.add(v.hashCode());
		}
		
		this.httpClient = client;
	}
	
	@Override
	public void setProcessor(Processor<Pair<String, Set<String>>, HTMLType> p) {
		this.p = p;
	}
	
	@Override
	public void dispatchIntialCrawl() {
		LOG.info("Dispatching initial crawl: numberOfUser="+initialUsers.size() + " , numberOfVideos="+initialVideos.size());
		try {
			for (String s : initialVideos) {
				dispatchVideo(s);
			}
			
			for (String s : initialUsers) {
				dispatchUser(s);
			}
		} catch (Exception e) {
			LOG.error("Error occurred:", e);
		}
	}
	
	@Override
	public void crawlJobConcluded(CrawlJob<Pair<String, Set<String>>, HTMLType> j) {
		URLSaveCrawlJob uscj = (URLSaveCrawlJob) j; //STA!
		
		try {
			LOG.info("Finished Crawl of: job="+uscj.getID());
			Pair<String, Set<String>> followUp = uscj.getResult();
			if (uscj.success() && followUp != null) {
				//Has something to follow, can follow, and is not equal to the last link (Youtube specific)
				String nextLink = BASE_URL + followUp.first + GL_US_HL_EN;
				if (followUp.first != null && uscj.getType().hasFollowUp() && !nextLink.equals(uscj.getID())) {
					LOG.info("Dispatching following link: link="+nextLink);
					URL next = new URL(nextLink);
					dispatch(new URLSaveCrawlJob(next, uscj.getSavePath(), uscj.getType(), httpClient));
				}

				if (uscj.getType() == HTMLType.FAVORITES || uscj.getType() == HTMLType.VIDEOS) {
					for (String v : followUp.second) {
						dispatchVideo(v);
					}
				} else if (uscj.getType() == HTMLType.SUBSCRIBERS || uscj.getType() == HTMLType.SUBSCRIPTIONS) {
					for (String u : followUp.second) {
						dispatchUser(u);	
					}
				} 
				
				if (uscj.getType() != HTMLType.SINGLE_VIDEO) {
					finishedUserUrls++;
				} else if (uscj.getType() == HTMLType.SINGLE_VIDEO) {
					finishedVideos++;
				}
				finishedUrls++;
			} else {
				LOG.error("URL url=" + uscj.getID() + " was not collected due to error!");
				errorUrls++;
				
				if (uscj.getType() != HTMLType.SINGLE_VIDEO) {
					errorUserUrls++;
				} else  {
					errorVideos++;
				}
			}
		} catch (Exception e) {
			errorUrls++;
			LOG.error("Exception occurred:", e);
			
			if (uscj.getType() != HTMLType.SINGLE_VIDEO) {
				errorUserUrls++;
			} else {
				errorVideos++;
			}
		}
		
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
		if (!crawledUsers.contains(u.hashCode())) {
			LOG.info("Dispatching user: user="+u);
			crawledUsers.add(u.hashCode());
			for (HTMLType t : HTMLType.values()) {
				if (t.hasFollowUp()) {
					String url = BASE_URL + PROFILE_USER + u + VIEW + t.getFeatureName() + GL_US_HL_EN;
					File folder = new File(usersFolder + File.separator + u + File.separator + t.getFeatureName());
					folder.mkdirs();
					dispatch(new URLSaveCrawlJob(new URL(url), folder, t, httpClient));
				}
			}
		}
	}

	private void dispatchVideo(String v) throws MalformedURLException {
		if (!crawledVideos.contains(v.hashCode())) {
			crawledVideos.add(v.hashCode());
			String url = BASE_URL + "watch?v=" + v + GL_US_HL_EN;
			LOG.info("Dispatching video: video="+v + ", url="+url);
			videosFolder.mkdirs();
			dispatch(new URLSaveCrawlJob(new URL(url), videosFolder, HTMLType.SINGLE_VIDEO, httpClient));
		}
	}
	
	private void dispatch(URLSaveCrawlJob j) {
		if (j.getType() != HTMLType.SINGLE_VIDEO) {
			this.userUrls++;
		}
		
		this.dispatchUrls++;
		p.dispatch(j);
	}

	@Override
	public boolean isDone() {
		return finishedUrls + errorUrls >= dispatchUrls;
	}
}