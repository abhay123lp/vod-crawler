package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.File;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;

import br.ufmg.dcc.vod.ncrawler.ThreadedCrawler;
import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;

public class Main {
	
	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 6) {
			System.err.println("usage: <nthreads> <sleep in secs> <user save folder> <videos save folder> <seed file> <work queue folder>");
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

		File workQueueFolder = new File(args[5]);
		if (workQueueFolder.exists() && (!workQueueFolder.isDirectory() || workQueueFolder.list().length != 0)) {
			throw new Exception("work queue folder exists and is not empty");
		}
		
		File pQueue = new File(workQueueFolder.getAbsolutePath() + File.separator + "processor");
		File eQueue = new File(workQueueFolder.getAbsolutePath() + File.separator + "eval");
		
		List<String> seeds = FileUtil.readFileToList(seedFile);
		
		//HTTPClient
		BasicHttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUserAgent(params, "Social Networks research crawler, author: Flavio Figueiredo - hp: http://www.dcc.ufmg.br/~flaviov - email: flaviov@dcc.ufmg.br - resume at: http://flaviovdf.googlepages.com/flaviov.d.defigueiredo-resume");
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		
		params.setParameter("Accept-Language", "en-us");
		
		//Totals
		ConnManagerParams.setTimeout(params, 10 * 1000 * 60);
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(nThreads));
		ConnManagerParams.setMaxTotalConnections(params, nThreads);
		
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        
        HttpClient httpClient = new DefaultHttpClient(cm, params);
        
        //Evaluator
		YTUserHTMLEvaluator e = new YTUserHTMLEvaluator(videoFolder, userFolder, seeds, httpClient);

		//Start!
		LoggerInitiator.initiateLog();
		ThreadedCrawler<File, YTHTMLType> tc = new ThreadedCrawler<File, YTHTMLType>(nThreads, sleep, e, pQueue, eQueue, new URLSaveCrawlSerializer(httpClient), 512 * 1024 * 1024);
		tc.crawl();
		httpClient.getConnectionManager().shutdown();
	}
}