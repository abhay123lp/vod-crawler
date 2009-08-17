package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.ThreadedCrawler;
import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;

import com.google.gdata.client.youtube.YouTubeService;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 5) {
			System.err.println("usage: <nthreads> <sleep in secs> <save folder> <seed file> <work queue folder>");
			System.exit(1);
		}
		
		int nThreads = Integer.parseInt(args[0]);
		int sleep = Integer.parseInt(args[1]);
		
		File saveFolder = new File(args[2]);
		if (saveFolder.exists() && (!saveFolder.isDirectory() || saveFolder.list().length != 0)) {
			throw new Exception("user folder exists and is not empty");
		}
		
		File seedFile = new File(args[3]);
	
		File workQueueFolder = new File(args[4]);
		if (workQueueFolder.exists() && (!workQueueFolder.isDirectory() || workQueueFolder.list().length != 0)) {
			throw new Exception("work queue folder exists and is not empty");
		}
		
		File pQueue = new File(workQueueFolder.getAbsolutePath() + File.separator + "processor");
		File eQueue = new File(workQueueFolder.getAbsolutePath() + File.separator + "eval");
		
		pQueue.mkdirs();
		eQueue.mkdirs();
		saveFolder.mkdirs();
		
		List<String> seeds = FileUtil.readFileToList(seedFile);
		
		//HTTPClient
		YouTubeService service = new YouTubeService("ytapi-FlavioVinicius-DataCollector-si5mgkd4-0", "AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
		
	    //Evaluator
		YoutubeAPIEvaluator e = new YoutubeAPIEvaluator(service, seeds, saveFolder);
	
		//Start!
		LoggerInitiator.initiateLog();
		ThreadedCrawler tc = new ThreadedCrawler(nThreads, sleep, e, pQueue, eQueue, new YoutubeUserApiSave(service), 1024 * 1024 * 1024);
		tc.crawl();
		System.exit(0);
	}
}
