package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.NetIFRoundRobin;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;

//This class will not work in distributed mode!

public class URLSaveCrawlJob implements CrawlJob {

	private static final long serialVersionUID = 1L;
	
	private final URL url;
	private final File savePath;
	private final HttpClient httpClient;
	private final HTMLType t;
	
	private Evaluator<Pair<String, HTMLType>, Pair<InputStream, File>> e;
	
	public URLSaveCrawlJob(URL url, File savePath, HTMLType t, HttpClient httpClient) {
		this.url = url;
		this.t = t;
		this.savePath = savePath;
		this.httpClient = httpClient;
	}
	
	@Override
	public void collect() {
		InputStream content = null;
		BufferedReader in = null;
		PrintStream out = null;
		HttpEntity entity = null;
		
		try {
			String encode = URLEncoder.encode(url.toString(), "UTF-8");
			File resultFile = new File(savePath + File.separator + encode);
			
			HttpGet request = new HttpGet(url.toString());
			
			//We create a client because we believe there is a deadlock bug
			HttpParams copy = httpClient.getParams().copy();
			InetAddress nextIF = NetIFRoundRobin.getInstance().nextIF();
			ConnRouteParams.setLocalAddress(copy, nextIF);
			
			HttpResponse execute = new DefaultHttpClient(httpClient.getConnectionManager(), copy).execute(request);
			
			entity = execute.getEntity();
			if (entity != null) {
				content = entity.getContent();
			}
			
			e.evaluteAndSave(new Pair<String, HTMLType>(url.toString(), t), new Pair<InputStream, File>(content, resultFile));
	    } catch (Exception ex) {
	    	e.error(new Pair<String, HTMLType>(url.toString(), t), new UnableToCollectException(ex.getMessage()));
	    } finally {
	    	try {
				if (in != null) in.close();
				if (out != null) out.close();
				if (content != null) content.close();
				if (entity != null) entity.consumeContent();
	    	} catch (Exception e) {}
	    }
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}
	
	public File getSavePath() {
		return savePath;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public String toString() {
		return url.toString();
	}
	
	public HTMLType getType() {
		return t;
	}
}