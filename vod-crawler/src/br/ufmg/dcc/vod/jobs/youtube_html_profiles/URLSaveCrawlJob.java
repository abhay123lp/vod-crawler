package br.ufmg.dcc.vod.jobs.youtube_html_profiles;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;

import br.ufmg.dcc.vod.CrawlJob;
import br.ufmg.dcc.vod.common.FileUtil;

public class URLSaveCrawlJob implements CrawlJob<File, HTMLType> {

	private final URL url;
	private final File savePath;
	private final HTMLType t;
	private File resultPath;

	public URLSaveCrawlJob(URL url, File savePath, HTMLType t) {
		this.url = url;
		this.savePath = savePath;
		this.t = t;
	}
	
	@Override
	public void collect() throws Exception {
		String encode = URLEncoder.encode(url.toString(), "UTF-8");
		this.resultPath = new File(savePath + File.separator + encode);
		FileUtil.saveUrlGzip(url, resultPath);
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
	
}