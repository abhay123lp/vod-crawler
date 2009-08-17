package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.common.SimpleBloomFilter;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;
import br.ufmg.dcc.vod.ncrawler.stats.CompositeStatEvent;
import br.ufmg.dcc.vod.ncrawler.stats.Display;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.BFTracker;
import br.ufmg.dcc.vod.ncrawler.tracker.ThreadSafeTracker;

import com.google.gdata.client.youtube.YouTubeService;

public class YoutubeAPIEvaluator implements Evaluator<String, YoutubeUserDAO> {

	private static final Logger LOG = Logger.getLogger(YoutubeAPIEvaluator.class);
	
	private static final String DIS = "DIS";
	private static final String COL = "COL";
	private static final String ERR = "ERR";
	
	private final YouTubeService service;
	private final Collection<String> initialUsers;
	private final File savePath;
	
	private StatsPrinter sp;
	private ThreadSafeTracker<String> bf;
	private static final int TEN_MILLION = 10000;
	
	public YoutubeAPIEvaluator(YouTubeService service, Collection<String> initialUsers, File savePath) {
		this.service = service;
		this.initialUsers = initialUsers;
		this.savePath = savePath;
		this.bf = new ThreadSafeTracker<String>(new BFTracker<String>(new SimpleBloomFilter<String>(5 * TEN_MILLION * 16, 5 * TEN_MILLION)));		
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
		try {
			MyXStreamer.getInstance().getStreamer().toXML(collectContent, new BufferedWriter(new FileWriter(savePath + File.separator + collectID)));
			
			Map<String, Integer> incs = new HashMap<String, Integer>();
			incs.put(COL, 1);
			
			//FIXME: Crawl HTML for other links!!!!
			sp.notify(new CompositeStatEvent(incs));
			return createJobs(collectContent.getSubscriptions());
		} catch (Exception e) {
			errorOccurred(collectID, e);
			return null;
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
			if (!bf.contains(u)) {
				LOG.info("Found user: " + u);
				rv.add(new YoutubeUserAPICrawlJob(service, u, savePath));
				bf.add(u);
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