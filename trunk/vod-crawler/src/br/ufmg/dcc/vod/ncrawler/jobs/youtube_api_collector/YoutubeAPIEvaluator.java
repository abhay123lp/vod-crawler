package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles.YTErrorPageException;
import br.ufmg.dcc.vod.ncrawler.stats.CompositeStatEvent;
import br.ufmg.dcc.vod.ncrawler.stats.Display;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.Tracker;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

import com.google.gdata.client.youtube.YouTubeService;

public class YoutubeAPIEvaluator implements Evaluator<String, YoutubeUserDAO> {

	private static final Logger LOG = Logger.getLogger(YoutubeAPIEvaluator.class);
	
	private static final String DIS = "DIS";
	private static final String COL = "COL";
	private static final String ERR = "ERR";
	
	private final YouTubeService service;
	private final Collection<String> initialUsers;
	private final File savePath;
	
	private final Pattern NEXT_PATTERN = Pattern.compile("(\\s+&nbsp;<a href=\")(.*?)(\"\\s*>\\s*Next.*)");
	private final Pattern RELATION_PATTERN = Pattern.compile("(\\s*<a href=\"/user/)(.*?)(\"\\s+onmousedown=\"trackEvent\\('ChannelPage'.*)");
	private final Pattern ERROR_PATTERN = Pattern.compile("\\s*<input type=\"hidden\" name=\"challenge_enc\" value=\".*");
	
	private StatsPrinter sp;
	private Tracker<String> tracker;

	private final long sleepTime;
	
	public YoutubeAPIEvaluator(YouTubeService service, Collection<String> initialUsers, File savePath, long sleepTime) {
		this.service = service;
		this.initialUsers = initialUsers;
		this.savePath = savePath;
		this.sleepTime = sleepTime;
	}
	
	@Override
	public void setTrackerFactory(TrackerFactory factory) {
		this.tracker = factory.createTracker();
	}
	
	@Override
	public void errorOccurred(String collectID, Exception e) {
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(ERR, 1);
		sp.notify(new CompositeStatEvent(incs));
		LOG.error("Error collecting: " + collectID, e);
	}

	@Override
	public Collection<CrawlJob> evaluteAndSave(String collectID, YoutubeUserDAO collectContent, File savePath) {
		Set<String> followup = new HashSet<String>();
		
		try {
			MyXStreamer.getInstance().getStreamer().toXML(collectContent, new BufferedWriter(new FileWriter(savePath + File.separator + collectID)));
			
			Map<String, Integer> incs = new HashMap<String, Integer>();
			incs.put(COL, 1);
			
			//Subscriptions
			Set<String> subscriptions = collectContent.getSubscriptions();
			for (String s : subscriptions) {
				followup.add(s);
			}
			
			//Subscribers
			try {
				Set<String> subscribers = discoverSubscribers(collectID);
				for (String s : subscribers) {
					followup.add(s);
				}
			} catch (Exception e) {
				LOG.warn("Unable to discover every subscriber for user: " + collectID, e);
			}
			
			incs.put(DIS, followup.size());
			sp.notify(new CompositeStatEvent(incs));
		} catch (Exception e) {
			errorOccurred(collectID, e);
		}
		
		return createJobs(followup);
	}

	private Set<String> discoverSubscribers(String collectID) throws Exception {
		Set<String> rv = new HashSet<String>();
		
		String followLink = "http://www.youtube.com/profile?user=" + collectID + "&view=subscribers&gl=US&hl=en";
		String lastLink = null;
		
		do {
			lastLink = followLink;
			BufferedReader in = null;
			
			try {
				URL u = new URL(followLink);
				URLConnection connection = u.openConnection();
				connection.setRequestProperty("User-Agent", "Research-Crawler-APIDEVKEY-AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
				
				connection.connect();
				
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					Matcher matcher = NEXT_PATTERN.matcher(inputLine);
					if (matcher.matches() && inputLine.contains("subscribers")) {
						followLink = "http://www.youtube.com/" + matcher.group(2) + "&gl=US&hl=en";
					}
					
					matcher = RELATION_PATTERN.matcher(inputLine);
					if (matcher.matches()) {
						rv.add(matcher.group(2));
					}
					
					matcher = ERROR_PATTERN.matcher(inputLine);
					if (matcher.matches()) {
						throw new YTErrorPageException();
					}
				}
			} finally {
				if (in != null) in.close();
			}
			
			Thread.sleep(sleepTime);
		} while (!followLink.equals(lastLink));
		
		return rv;
	}

	@Override
	public Collection<CrawlJob> getInitialCrawl() {
		ArrayList<CrawlJob> rv = createJobs(initialUsers);
		return rv;
	}

	private ArrayList<CrawlJob> createJobs(Collection<String> users) {
		ArrayList<CrawlJob> rv = new ArrayList<CrawlJob>();
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(DIS, 0);
		for (String u : users) {
			if (!tracker.contains(u)) {
				LOG.info("Found user: " + u);
				rv.add(new YoutubeUserAPICrawlJob(service, u, savePath, sleepTime));
				tracker.add(u);
				incs.put(DIS, incs.get(DIS) + 1);
			}
		}
		sp.notify(new CompositeStatEvent(incs));
		return rv;
	}

	@Override
	public void setStatsKeeper(StatsPrinter sp) {
		this.sp = sp;
		this.sp.setDisplay(new Display() {
			@Override
			public void print(Map<String, Integer> m) {
				Integer disU = m.get(DIS) == null ? 0 : m.get(DIS);
				Integer colU = m.get(COL) == null ? 0 : m.get(COL);
				Integer errU = m.get(ERR) == null ? 0 : m.get(ERR);
				
				System.out.println("----" + new Date());
				System.out.println(DIS + " : " + disU);
				System.out.println(COL + " : " + colU + " ( " + (colU.doubleValue() / disU.doubleValue()) + " )");
				System.out.println(ERR + " : " + errU + " ( " + (errU.doubleValue() / disU.doubleValue()) + " )");
			}
			}
		);
	}
}