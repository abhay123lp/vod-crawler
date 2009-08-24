package br.ufmg.dcc.vod.ncrawler.jobs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.ThreadedCrawler;
import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector.YTApiFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles.YTHtmlFactory;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;
import br.ufmg.dcc.vod.ncrawler.tracker.ThreadSafeTrackerFactory;

public class Launcher {
	
	private static final Map<String, EvaluatorFactory<?,?,?>> crawlers = new HashMap<String, EvaluatorFactory<?,?,?>>();
	static {
		crawlers.put("YTAPI", new YTApiFactory());
		crawlers.put("YTHTML", new YTHtmlFactory());
	}
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		Option crawlerToDispachOpt = OptionBuilder.withArgName("crawler-name")
		.hasArg()
		.isRequired()
		.withDescription("Crawler to Dispatch Option")
		.create("crawlerToDispatch");
		
		Option nThreadsOpt = OptionBuilder.withArgName("int value")
		.hasArg()
		.isRequired()
		.withDescription("Number of Threads")
		.create("nThreads");
		
		Option sleepTimeOpt = OptionBuilder.withArgName("long value")
		.hasArg()
		.isRequired()
		.withDescription("Sleep time")
		.create("sleepTime");
		
		Option outFolderOpt = OptionBuilder.withArgName("folder")
		.hasArg()
		.isRequired()
		.withDescription("Data save folder")
		.create("outFolder");
		
		Option workQueueFolderOpt = OptionBuilder.withArgName("folder")
		.hasArg()
		.isRequired()
		.withDescription("Workqueue Folder")
		.create("workQueueFolder");
		
		Option seedFileOpt = OptionBuilder.withArgName("file")
		.hasArg()
		.isRequired()
		.withDescription("Seed File")
		.create("seedFile");
		
		Option logFileOpt = OptionBuilder.withArgName("file")
		.hasArg()
		.isRequired()
		.withDescription("Log File")
		.create("logFile");
		
		opts.addOption(crawlerToDispachOpt);
		opts.addOption(nThreadsOpt);
		opts.addOption(sleepTimeOpt);
		opts.addOption(outFolderOpt);
		opts.addOption(workQueueFolderOpt);
		opts.addOption(seedFileOpt);
		opts.addOption(logFileOpt);
		opts.addOption("overwrite", false, "Overwrite any existing data.");
		
		try {
			GnuParser parser = new GnuParser();
			parser.parse(opts, args);
			
			int nThreads = Integer.parseInt(nThreadsOpt.getValue());
			long sleepTime = Long.parseLong(sleepTimeOpt.getValue());
			File saveFolder = new File(outFolderOpt.getValue());
			File workQueueFolder = new File(workQueueFolderOpt.getValue());
			File seedFile = new File(seedFileOpt.getValue());
			EvaluatorFactory<?, ?, ?> crawlerFactory = crawlers.get(crawlerToDispachOpt.getValue());

			if (crawlerFactory == null) {
				throw new Exception("unknown crawler");
			}
			
			if (saveFolder.exists() && (!saveFolder.isDirectory() || saveFolder.list().length != 0)) {
				throw new Exception("save folder exists and is not empty");
			}
			
			if (workQueueFolder.exists() && (!workQueueFolder.isDirectory() || workQueueFolder.list().length != 0)) {
				throw new Exception("work queue folder exists and is not empty");
			}
			
			File pQueue = new File(workQueueFolder.getAbsolutePath() + File.separator + "processor");
			File eQueue = new File(workQueueFolder.getAbsolutePath() + File.separator + "eval");
			pQueue.mkdirs();
			eQueue.mkdirs();
			
			LoggerInitiator.initiateLog(logFileOpt.getValue());
			List<String> seeds = FileUtil.readFileToList(seedFile);
			
			crawlerFactory.initiate(nThreads, saveFolder, sleepTime, seeds);
			Evaluator<?, ?> evaluator = crawlerFactory.getEvaluator();
			evaluator.setTrackerFactory(new ThreadSafeTrackerFactory());
			
			Serializer<?> serializer = crawlerFactory.getSerializer();
			
			ThreadedCrawler tc = new ThreadedCrawler(nThreads, sleepTime, evaluator, pQueue, eQueue, serializer, 1024 * 1024 * 1024);
			tc.crawl();
			crawlerFactory.shutdown();
		} catch (Exception e) {
			System.out.println(e);
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("java " + Launcher.class, opts);
		}
	}
}