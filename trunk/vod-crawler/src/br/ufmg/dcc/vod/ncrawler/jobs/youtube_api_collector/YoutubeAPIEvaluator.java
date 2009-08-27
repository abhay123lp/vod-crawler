package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.evaluator.AbstractEvaluator;
import br.ufmg.dcc.vod.ncrawler.stats.CompositeStatEvent;
import br.ufmg.dcc.vod.ncrawler.stats.Display;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.Tracker;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class YoutubeAPIEvaluator extends AbstractEvaluator<String, YoutubeUserDAO> {

	private static final Logger LOG = Logger.getLogger(YoutubeAPIEvaluator.class);
	
	private static final String DIS = "DIS";
	private static final String COL = "COL";
	private static final String ERR = "ERR";
	
	private final Collection<String> initialUsers;
	private final File savePath;
	
	private StatsPrinter sp;
	private Tracker<String> tracker;

	private final long sleepTime;
	
	public YoutubeAPIEvaluator(Collection<String> initialUsers, File savePath, long sleepTime) {
		this.initialUsers = initialUsers;
		this.savePath = savePath;
		this.sleepTime = sleepTime;
	}
	
	@Override
	public void setTrackerFactory(TrackerFactory factory) {
		this.tracker = factory.createTracker();
	}
	
	@Override
	public void evalError(String collectID, Exception e) {
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(ERR, 1);
		sp.notify(new CompositeStatEvent(incs));
		LOG.error("Error collecting: " + collectID, e);
	}

	@Override
	public boolean evalResult(String collectID, YoutubeUserDAO collectContent, File savePath) {
		try {
			MyXStreamer.getInstance().getStreamer().toXML(collectContent, new BufferedWriter(new FileWriter(savePath + File.separator + collectID)));
			
			Map<String, Integer> incs = new HashMap<String, Integer>();
			incs.put(COL, 1);
			sp.notify(new CompositeStatEvent(incs));
			
			Set<String> followup = new HashSet<String>();
			//Subscriptions
			Set<String> subscriptions = collectContent.getSubscriptions();
			for (String s : subscriptions) {
				followup.add(s);
			}
			
			//Subscribers
			Set<String> subscribers = collectContent.getSubscribers();
			for (String s : subscribers) {
				followup.add(s);
			}
			
			ArrayList<CrawlJob> createJobs = createJobs(followup);
			for (CrawlJob j : createJobs) {
				dispatch(j);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
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
				rv.add(new YoutubeUserAPICrawlJob(u, savePath, sleepTime));
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