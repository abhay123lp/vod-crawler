package br.ufmg.dcc.vod.jobs.youtube_html_profiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.CrawlJob;
import br.ufmg.dcc.vod.evaluator.Evaluator;
import br.ufmg.dcc.vod.processor.Processor;

public class YTUserHTMLEvaluator implements Evaluator<File, HTMLType> {

	private static final Logger LOG = Logger.getLogger(YTUserHTMLEvaluator.class);
	
	private static final String VIEW = "&view=";
	private static final String PROFILE_USER = "profile?user=";
	private static final String GL_US_HL_EN = "&gl=US&hl=en";
	private static final String BASE_URL = "http://www.youtube.com/";
	
	private static final Pattern NEXT_PATTERN = Pattern.compile("(\\s+&nbsp;<a href=\")(.*?)(\".*)");
	private static final Pattern VIDEO_PATTERN = Pattern.compile("(\\s+<div class=\"video-main-content\" id=\"video-main-content-)(.*?)(\".*)");
	private static final Pattern RELATION_PATTERN = Pattern.compile("(\\s*<a href=\"/user/)(.*?)(\"\\s+onmousedown=\"trackEvent\\('ChannelPage'.*)");
	
	private final HashSet<String> crawledUsers;
	private final HashSet<String> crawledVideos;
	private final File videosFolder;
	private final File usersFolder;
	
	private Processor<File, HTMLType> p;
	
	private final List<String> initialUsers;
	private final List<String> initialVideos;

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers) {
		this(videosFolder, usersFolder, initialUsers, new LinkedList<String>(), new HashSet<String>(), new HashSet<String>());
	}

	public YTUserHTMLEvaluator(File videosFolder, File usersFolder, List<String> initialUsers, List<String> initialVideos, HashSet<String> crawledVideos, HashSet<String> crawledUsers) {
		this.videosFolder = videosFolder;
		this.usersFolder = usersFolder;
		this.initialUsers = initialUsers;
		this.initialVideos = initialVideos;
		this.crawledUsers = crawledUsers;
		this.crawledVideos = crawledVideos;
	}
	
	@Override
	public void setProcessor(Processor<File, HTMLType> p) {
		this.p = p;
	}
	
	@Override
	public void dispatchIntialCrawl() {
		LOG.info("Dispatching initial crawl: numberOfUser="+initialUsers.size() + " , numberOfVideos="+initialVideos.size());
		try {
			for (String s : initialVideos) {
				dispatchVideo(s);
			}
			
			for (String s : initialUsers) {
				dispatchUser(s);
			}
		} catch (Exception e) {
			LOG.error("Error occurred:", e);
		}
	}
	
	@Override
	public void crawlJobConcluded(CrawlJob<File, HTMLType> j) {
		try {
			LOG.info("Finished Crawl of: job="+j.getID());
			
			File result = j.getResult();
			if (result != null && j.getType().hasFollowUp()) {
				URL next;
				try {
					next = new URL(nextLink(result));
					LOG.info("Dispatching following link: link="+next);
					p.dispatch(new URLSaveCrawlJob(next, j.getResult().getParentFile(), j.getType()));
				} catch (MalformedURLException e) {
					LOG.error("Error occurred:", e);
				} catch (IOException e) {
					LOG.error("Error occurred:", e);
				}
			}
			
			Set<String> relations = new LinkedHashSet<String>();
			Set<String> videos = new LinkedHashSet<String>();
			
			switch (j.getType()) {
			case SUBSCRIBERS:
				relations.addAll(readRelations(j.getResult()));
				break;
			case SUBSCRIPTIONS:
				relations.addAll(readRelations(j.getResult()));
				break;
			case FAVORITES:
				videos.addAll(readVideos(j.getResult()));
				break;
			case VIDEOS:
				videos.addAll(readRelations(j.getResult()));
				break;
			}
			
			//Adding videos for collection
			for (String v : videos) {
				dispatchVideo(v);
			}

			//Adding new users
			for (String u : relations) {
				dispatchUser(u);	
			}
		} catch (Exception e) {
			LOG.error("Error occurred:", e);
		}
	}

	private void dispatchUser(String u) throws MalformedURLException {
		if (!crawledUsers.contains(u)) {
			LOG.info("Dispatching user: user="+u);
			crawledUsers.add(u);
			for (HTMLType t : HTMLType.values()) {
				if (t.hasFollowUp()) {
					String url = BASE_URL + PROFILE_USER + u + VIEW + t.getFeatureName() + GL_US_HL_EN;
					File folder = new File(usersFolder + File.separator + u + File.separator + t.getFeatureName());
					folder.mkdirs();
					p.dispatch(new URLSaveCrawlJob(new URL(url), folder, t));
				}
			}
		}
	}

	private void dispatchVideo(String v) throws MalformedURLException {
		if (!crawledVideos.contains(v)) {
			LOG.info("Dispatching video: video="+v);
			crawledVideos.add(v);
			String url = BASE_URL + "watch?v=" + v + GL_US_HL_EN;
			videosFolder.mkdirs();
			p.dispatch(new URLSaveCrawlJob(new URL(url), videosFolder, HTMLType.SINGLE_VIDEO));
		}
	}

	private Set<String> readRelations(File file) throws IOException {
		Set<String> users = new LinkedHashSet<String>();
		
		String inputLine;
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
		    while ((inputLine = r.readLine()) != null) {
		    	Matcher matcher = RELATION_PATTERN.matcher(inputLine);
		    	if (matcher.matches()) {
		    		users.add(matcher.group(2));
		    	}
		    }
		} finally {
			r.close();
		}
		
		return users;	
	}

	private Set<String> readVideos(File f) throws FileNotFoundException, IOException {
		Set<String> videos = new HashSet<String>();
		
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
			String inputLine;
		    while ((inputLine = r.readLine()) != null) {
		    	Matcher matcher = VIDEO_PATTERN.matcher(inputLine);
		    	if (matcher.matches()) {
		    		videos.add(matcher.group(2));
		    	}
		    }
		} finally {
			r.close();
		}
		
		return videos;
	}
	
	private String nextLink(File filePath) throws IOException {
	    BufferedReader in = null;
	    try 
	    {
	    	in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath))));
			String inputLine;
		    while ((inputLine = in.readLine()) != null) {
		    	Matcher matcher = NEXT_PATTERN.matcher(inputLine);
		    	if (matcher.matches()) {
		    		return matcher.group(2);
		    	}
		    }
		    return null;
	    }
	    finally
	    {
			in.close();
	    }
	}
}