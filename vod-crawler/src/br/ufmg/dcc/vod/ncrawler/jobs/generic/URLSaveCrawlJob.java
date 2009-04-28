package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
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
import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.NetIFRoundRobin;

public class URLSaveCrawlJob<T extends HTMLType> implements CrawlJob<File, T> {

	private final URL url;
	private final File savePath;
	private final T t;
	private final HttpClient httpClient;
	
	private File resultFile;

	public URLSaveCrawlJob(URL url, File savePath, T t, HttpClient httpClient) {
		this.url = url;
		this.savePath = savePath;
		this.t = t;
		this.httpClient = httpClient;
	}
	
	@Override
	public void collect() throws Exception {
		InputStream content = null;
		BufferedReader in = null;
		PrintStream out = null;
		
		try {
			String encode = URLEncoder.encode(url.toString(), "UTF-8");
			this.resultFile = new File(savePath + File.separator + encode);
			
			HttpGet request = new HttpGet(url.toString());
			
			//We create a client because we believe there is a deadlock bug
			HttpParams copy = httpClient.getParams().copy();
			ConnRouteParams.setLocalAddress(copy, NetIFRoundRobin.getInstance().nextIF());
			HttpResponse execute = new DefaultHttpClient(httpClient.getConnectionManager(), copy).execute(request);
			
			HttpEntity entity = execute.getEntity();
			if (entity != null) {
				content = entity.getContent();
				FileUtil.saveUrlGzip(content, resultFile);
			    entity.consumeContent();
			}
	    }
	    finally
	    {
			if (in != null) in.close();
			if (out != null) out.close();
			if (content != null) content.close();
	    }
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

	@Override
	public File getResult() {
		return resultFile;
	}

	@Override
	public T getType() {
		return t;
	}

	@Override
	public String getID() {
		return url.toString();
	}
}