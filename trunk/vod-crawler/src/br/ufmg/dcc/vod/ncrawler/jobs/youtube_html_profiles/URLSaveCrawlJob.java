package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;

public class URLSaveCrawlJob implements CrawlJob<Pair<String, Set<String>>, HTMLType> {

	private static final Pattern NEXT_PATTERN = Pattern.compile("(\\s+&nbsp;<a href=\")(.*?)(\"\\s*>\\s*Next.*)");
	private static final Pattern VIDEO_PATTERN = Pattern.compile("(\\s+<div class=\"video-main-content\" id=\"video-main-content-)(.*?)(\".*)");
	private static final Pattern RELATION_PATTERN = Pattern.compile("(\\s*<a href=\"/user/)(.*?)(\"\\s+onmousedown=\"trackEvent\\('ChannelPage'.*)");
	private static final Pattern ERROR_PATTERN = Pattern.compile("\\s*<input type=\"hidden\" name=\"challenge_enc\" value=\".*");
	
	private final URL url;
	private final File savePath;
	private final HTMLType t;
	private final HttpClient httpClient;
	
	private File resultPath;
	private boolean success;
	private Pair<String, Set<String>> result;

	public URLSaveCrawlJob(URL url, File savePath, HTMLType t, HttpClient httpClient) {
		this.url = url;
		this.savePath = savePath;
		this.t = t;
		this.httpClient = httpClient;
		this.success = true;
	}
	
	@Override
	public void collect() throws Exception {
		Set<String> returnValue = new HashSet<String>();
		String nextLink = null;
		
		InputStream content = null;
		BufferedReader in = null;
		PrintStream out = null;
		
		try {
			String encode = URLEncoder.encode(url.toString(), "UTF-8");
			this.resultPath = new File(savePath + File.separator + encode);
			
			HttpGet request = new HttpGet(url.toString());
			HttpResponse execute = httpClient.execute(request);
			content = execute.getEntity().getContent();
			
		    in = new BufferedReader(new InputStreamReader(content));
			out = new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(resultPath))));
			
			String inputLine;
		    while ((inputLine = in.readLine()) != null) {
		    	Matcher matcher = NEXT_PATTERN.matcher(inputLine);
		    	if (matcher.matches() && inputLine.contains(t.getFeatureName())) {
		    		nextLink = matcher.group(2);
		    	}
		    	
				Pattern pat = null;
				if (t == HTMLType.FAVORITES || t == HTMLType.VIDEOS) {
					pat = VIDEO_PATTERN;
				} else if (t == HTMLType.SUBSCRIBERS || t == HTMLType.SUBSCRIPTIONS || t == HTMLType.FRIENDS) {
					pat = RELATION_PATTERN;
				} 
		    	
		    	if (pat != null) {
			    	matcher = pat.matcher(inputLine);
			    	if (matcher.matches()) {
			    		returnValue.add(matcher.group(2));
			    	}
		    	}
		    	
		    	matcher = ERROR_PATTERN.matcher(inputLine);
		    	if (matcher.matches()) {
		    		throw new ErrorPageException();
		    	}
		    }
	    }
	    finally
	    {
			if (in != null) in.close();
			if (out != null) out.close();
			if (content != null) content.close();
	    }
	    
	    this.result = new Pair<String, Set<String>>(nextLink, returnValue);
	}
	
	@Override
	public String getID() {
		return url.toString();
	}

	@Override
	public Pair<String, Set<String>> getResult() {
		return result;
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
	
	public File getSavePath() {
		return savePath;
	}
	
	public URL getUrl() {
		return url;
	}
	
	@Override
	public String toString() {
		return getID();
	}
}