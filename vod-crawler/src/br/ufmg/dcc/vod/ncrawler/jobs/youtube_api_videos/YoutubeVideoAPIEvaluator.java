package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_videos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;
import br.ufmg.dcc.vod.ncrawler.evaluator.AbstractEvaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.stats.CompositeStatEvent;
import br.ufmg.dcc.vod.ncrawler.stats.Display;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class YoutubeVideoAPIEvaluator extends AbstractEvaluator<String, YoutubeVideoDAO> {

	private static final Logger LOG = Logger.getLogger(YoutubeVideoAPIEvaluator.class);
	
	private static final String DIS = "DIS";
	private static final String COL = "COL";
	private static final String ERR = "ERR";

	private Collection<String> initialVideos;
	private File savePath;
	private StatsPrinter sp;

	public YoutubeVideoAPIEvaluator(Collection<String> initialVideos, File savePath) {
		this.initialVideos = initialVideos;
		this.savePath = savePath;
	}
	
	@Override
	public void error(String collectID, UnableToCollectException utc) {
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(ERR, 1);
		sp.notify(new CompositeStatEvent(incs));
		LOG.error("Error collecting: " + collectID, utc);
	}

	@Override
	public void evaluteAndSave(String collectID, YoutubeVideoDAO collectContent) {
		try {
			MyXStreamer.getInstance().toXML(collectContent, new File(savePath + File.separator + collectID));
			Map<String, Integer> incs = new HashMap<String, Integer>();
			incs.put(COL, 1);
			sp.notify(new CompositeStatEvent(incs));
		} catch (IOException e) {
			error(collectID, new UnableToCollectException(e.getMessage()));
		}
	}

	@Override
	public Collection<CrawlJob> getInitialCrawl() {
		List<CrawlJob> rv = new ArrayList<CrawlJob>();
		for (String v : initialVideos) {
			rv.add(new YoutubeAPIVideoCrawlJob(v));
		}
		
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(DIS, initialVideos.size());
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

	@Override
	public void setTrackerFactory(TrackerFactory factory) {
	}
}
