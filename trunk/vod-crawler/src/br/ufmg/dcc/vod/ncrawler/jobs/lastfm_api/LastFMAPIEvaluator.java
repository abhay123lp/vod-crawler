package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.AbstractEvaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.stats.CompositeStatEvent;
import br.ufmg.dcc.vod.ncrawler.stats.Display;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;
import br.ufmg.dcc.vod.ncrawler.tracker.Tracker;
import br.ufmg.dcc.vod.ncrawler.tracker.TrackerFactory;

public class LastFMAPIEvaluator extends AbstractEvaluator<String, LastFMUserDAO> {

	private static final Logger LOG = Logger.getLogger(LastFMAPIEvaluator.class);
	
	private static final String DIS = "DIS";
	private static final String COL = "COL";
	private static final String ERR = "ERR";
	
	private final List<String> seeds;
	private final File saveFolder;
	private final long sleepTime;
	private Tracker<String> tracker;
	private StatsPrinter sp;

	public LastFMAPIEvaluator(List<String> seeds, File saveFolder, long sleepTime) {
		this.seeds = seeds;
		this.saveFolder = saveFolder;
		this.sleepTime = sleepTime;
	}

	@Override
	public void error(String collectID, UnableToCollectException utc) {
		Map<String, Integer> incs = new HashMap<String, Integer>();
		incs.put(ERR, 1);
		sp.notify(new CompositeStatEvent(incs));
		LOG.error("Error collecting: " + collectID, utc);
	}

	@Override
	public void evaluteAndSave(String collectID, LastFMUserDAO collectContent) {
		
	}
	
	@Override
	public Collection<CrawlJob> getInitialCrawl() {
		// TODO Auto-generated method stub
		return null;
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
		this.tracker = factory.createTracker();
	}
}
