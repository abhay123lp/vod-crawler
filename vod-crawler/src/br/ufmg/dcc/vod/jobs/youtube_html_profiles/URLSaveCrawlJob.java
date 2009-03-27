package br.ufmg.dcc.vod.jobs.youtube_html_profiles;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import br.ufmg.dcc.vod.CrawlJob;
import br.ufmg.dcc.vod.common.FileUtil;

public class URLSaveCrawlJob implements CrawlJob<File, HTMLType> {

	private final URL url;
	private final File savePath;
	private final HTMLType t;

	public URLSaveCrawlJob(URL url, File savePath, HTMLType t) {
		this.url = url;
		this.savePath = savePath;
		this.t = t;
	}
	
	@Override
	public void collect() throws Exception {
		FileUtil.saveUrlGzip(url, new File(savePath + File.separator + getID()));
	}

	@Override
	public String getID() {
		try {
			return URLEncoder.encode(url.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	@Override
	public File getResult() {
		return savePath;
	}

	@Override
	public HTMLType getType() {
		return t;
	}
	
}