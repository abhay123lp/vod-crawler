package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpVersion;
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

import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.HTMLType;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.HTMLTypeFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YTHtmlFactory implements EvaluatorFactory<Pair<String, HTMLType>, InputStream, URLSaveCrawlJob> {

	private YTUserHTMLEvaluator e;
	private DefaultHttpClient httpClient;
	private URLSaveCrawlSerializer serializer;

	@Override
	public void initiate(int nThreads, File saveFolder, long sleepTime, List<String> seeds) {
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
        
        this.httpClient = new DefaultHttpClient(cm, params);
        
        File userFolder = new File(saveFolder + File.separator + "users");
        File videoFolder = new File(saveFolder + File.separator + "videos");
        
        userFolder.mkdirs();
        videoFolder.mkdirs();
        
        //Hack
        HTMLTypeFactory.getInstance().addMappings(YTHTMLType.FAVORITES.enumerate(), false);
        
        //Serializer
        this.serializer = new URLSaveCrawlSerializer(httpClient);
        
        //Evaluator
		this.e = new YTUserHTMLEvaluator(videoFolder, userFolder, seeds, httpClient);		
	}

	@Override
	public void shutdown() {
		httpClient.getConnectionManager().shutdown();
	}

	@Override
	public Evaluator<Pair<String, HTMLType>, InputStream> getEvaluator() {
		return e;
	}

	@Override
	public Serializer<URLSaveCrawlJob> getSerializer() {
		return serializer;
	}
}