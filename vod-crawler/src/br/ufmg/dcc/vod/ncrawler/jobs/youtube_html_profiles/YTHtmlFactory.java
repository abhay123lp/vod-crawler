package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

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

import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;
import br.ufmg.dcc.vod.ncrawler.jobs.CrawlerFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;
import br.ufmg.dcc.vod.ncrawler.jobs.Parameters;

public class YTHtmlFactory implements CrawlerFactory<I, C> {

	public YTHtmlFactory() {

	}
	
	
	
	@Override
	public Evaluator<I, C> getEvaluator() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void initiate(Parameters p) {
		int nThreads = p.getNumberOfThreads();
		
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
	}
}