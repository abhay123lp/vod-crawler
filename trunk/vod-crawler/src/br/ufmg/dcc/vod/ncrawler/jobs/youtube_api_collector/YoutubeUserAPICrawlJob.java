package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.jobs.Evaluator;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Link;
import com.google.gdata.data.youtube.FriendEntry;
import com.google.gdata.data.youtube.FriendFeed;
import com.google.gdata.data.youtube.SubscriptionEntry;
import com.google.gdata.data.youtube.SubscriptionFeed;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YtUserProfileStatistics;
import com.google.gdata.util.ServiceException;

public class YoutubeUserAPICrawlJob implements CrawlJob {

	private static final Logger LOG = Logger.getLogger(YoutubeAPIEvaluator.class);
	private Evaluator e;
	private final YouTubeService service;
	private final String userID;
	private final File savePath;

	public YoutubeUserAPICrawlJob(YouTubeService service, String userID, File savePath) {
		this.service = service;
		this.userID = userID;
		this.savePath = savePath;
	}
	
	@Override
	public Collection<CrawlJob> collect() {
		try {
			UserProfileEntry profileEntry = service.getEntry(new URL("http://gdata.youtube.com/feeds/api/users/" + userID), UserProfileEntry.class);
			
			String username = profileEntry.getUsername();
			int age = profileEntry.getAge() == null ? -1 : profileEntry.getAge();
			String gender = profileEntry.getGender() == null ? null : profileEntry.getGender().getId();
			String relationship = profileEntry.getRelationship() == null ? null : profileEntry.getRelationship().name();
			String books = profileEntry.getBooks();
			String company = profileEntry.getCompany();
			String aboutMe = profileEntry.getAboutMe();
			String hobbies = profileEntry.getHobbies();
			String hometown = profileEntry.getHometown();
			String location = profileEntry.getLocation();
			String movies = profileEntry.getMovies();
			String music = profileEntry.getMusic();
			String occupation = profileEntry.getOccupation();
			String school = profileEntry.getSchool();
			String channelType = profileEntry.getChannelType();
			
			YtUserProfileStatistics stats = profileEntry.getStatistics();
			Date lastWebAccess = null;
			long videoWatchCount = -1;
			long viewCount = -1;
			if(stats != null) {
				lastWebAccess = stats.getLastWebAccess() == null ? null : new Date(stats.getLastWebAccess().getValue());
				videoWatchCount = stats.getVideoWatchCount();
				viewCount = stats.getViewCount();
			}
	
			Set<String> uploads = new HashSet<String>();
			try {
				Link uploadsFeedLink = profileEntry.getUploadsFeedLink();
				while (uploadsFeedLink != null) {
					String href = uploadsFeedLink.getHref();
					VideoFeed upsFeed = service.getFeed(new URL(href), VideoFeed.class);
					for (VideoEntry ve : upsFeed.getEntries()) {
						String[] split = ve.getId().split("\\/");
						String id = split[split.length - 1];
						uploads.add(id);
					}
					uploadsFeedLink = upsFeed.getLink("next", "application/atom+xml");
				}
			} catch (ServiceException e) {
				LOG.warn("Unable to collect uploads for user " + userID, e);
			}

			Set<String> friends = new HashSet<String>();
			try {
				Link friendsFeedLink = profileEntry.getContactsFeedLink();
				while (friendsFeedLink != null) {
					String href = friendsFeedLink.getHref();
					FriendFeed friendFeed = service.getFeed(new URL(href), FriendFeed.class);
					for (FriendEntry fe : friendFeed.getEntries()) {
						friends.add(fe.getUsername());
					}
					friendsFeedLink = friendFeed.getLink("next", "application/atom+xml");
				}
			} catch (ServiceException e) {
				LOG.warn("Unable to collect friends for user " + userID, e);
			}

			Set<String> subscriptions = new HashSet<String>();
			try {
				Link subscriptionsFeedLink = profileEntry.getSubscriptionsFeedLink();
				while (subscriptionsFeedLink != null) {
					String href = subscriptionsFeedLink.getHref();
					SubscriptionFeed subscriptionFeed = service.getFeed(new URL(href), SubscriptionFeed.class);
					for (SubscriptionEntry se : subscriptionFeed.getEntries()) {
						switch (se.getSubscriptionType()) {
							case CHANNEL:
								subscriptions.add(se.getUsername());
								break;
							default:
								continue;
						}
					}
					subscriptionsFeedLink = subscriptionFeed.getLink("next", "application/atom+xml");
				}
			} catch (ServiceException e) {
				LOG.warn("Unable to collect subs for user " + userID, e);
			}
			
			return e.evaluteAndSave(userID, new YoutubeUserDAO(userID, username, age, gender, aboutMe, relationship, books, company, hobbies, hometown, location, movies, music, occupation, school, channelType, uploads, subscriptions, friends, viewCount, videoWatchCount, lastWebAccess), savePath);
		} catch (Exception ec ) {
			e.errorOccurred(userID, ec);
			return null;
		}
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}

	public String getUserID() {
		return userID;
	}

	public String getSavePath() {
		return savePath.getAbsolutePath().toString();
	}
}