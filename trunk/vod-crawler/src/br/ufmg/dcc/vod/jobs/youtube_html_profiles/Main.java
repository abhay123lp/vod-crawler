package br.ufmg.dcc.vod.jobs.youtube_html_profiles;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ThreadedCrawler;
import br.ufmg.dcc.vod.common.FileUtil;
import br.ufmg.dcc.vod.common.LoggerInitiator;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 5) {
			System.err.println("usage: <nthreads> <sleep in secs> <user save folder> <videos save folder> <seed file>");
			System.exit(1);
		}
		
		int nThreads = Integer.parseInt(args[0]);
		int sleep = Integer.parseInt(args[1]);
		
		File userFolder = new File(args[2]);
		if (userFolder.exists() && (!userFolder.isDirectory() || userFolder.list().length != 0)) {
			throw new Exception("user folder exists and is not empty");
		}
		
		File videoFolder = new File(args[3]);
		if (videoFolder.exists() && (!videoFolder.isDirectory() || videoFolder.list().length != 0)) {
			throw new Exception("video folder exists and is not empty");
		}
		
		File seedFile = new File(args[4]);

		List<String> seeds = FileUtil.readFileToList(seedFile);
		YTUserHTMLEvaluator e = new YTUserHTMLEvaluator(videoFolder, userFolder, seeds);
		
		LoggerInitiator.initiateLog();
		
		ThreadedCrawler<File, HTMLType> tc = new ThreadedCrawler<File, HTMLType>(nThreads, sleep, e);
		tc.crawl();
	}
	
}