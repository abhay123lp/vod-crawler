package br.ufmg.dcc.vod.jobs.youtube_html_profiles;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import br.ufmg.dcc.vod.CrawlJob;
import br.ufmg.dcc.vod.common.FileUtil;

public class URLSaveCrawlJob implements CrawlJob<File, HTMLType> {

	private final URL url;
	private final File savePath;
	private final HTMLType t;
	private final HttpClient httpClient;
	
	private File resultPath;
	private boolean success;

	public URLSaveCrawlJob(URL url, File savePath, HTMLType t, HttpClient httpClient) {
		this.url = url;
		this.savePath = savePath;
		this.t = t;
		this.httpClient = httpClient;
		this.success = true;
	}
	
	@Override
	public void collect() throws Exception {
		InputStream content = null;
		try {
			HttpGet request = new HttpGet(url.toString());
			String encode = URLEncoder.encode(url.toString(), "UTF-8");
			this.resultPath = new File(savePath + File.separator + encode);
			HttpResponse execute = httpClient.execute(request);
			content = execute.getEntity().getContent();
			FileUtil.saveUrlGzip(content, resultPath);
		} finally {
			if (content != null)  content.close();
		}
	}

	@Override
	public String getID() {
		return url.toString();
	}

	@Override
	public File getResult() {
		return resultPath;
	}

	@Override
	public HTMLType getType() {
		return t;
	}

	@Override
	public void markWithError() {
		this.success = false;
	}
	
	@Override
	public boolean success() {
		return success;
	}
	
}