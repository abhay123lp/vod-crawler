package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_videos;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.UnableToCollectException;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector.YoutubeAPIEvaluator;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.geo.impl.GeoRssWhere;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtStatistics;

public class YoutubeAPIVideoCrawlJob implements CrawlJob {

	private static final Logger LOG = Logger.getLogger(YoutubeAPIVideoCrawlJob.class);
	
	private static final long serialVersionUID = 1L;
	private Evaluator e;
	private String videoID;

	public YoutubeAPIVideoCrawlJob(String videoID) {
		this.videoID = videoID;
	}
	
	@Override
	public void collect() {
		LOG.info("Collecting " + videoID);
		
		YouTubeService service = new YouTubeService("ytapi-FlavioVinicius-DataCollector-si5mgkd4-0", "AI39si59eqKb2OzKrx-4EkV1HkIRJcoYDf_VSKUXZ8AYPtJp-v9abtMYg760MJOqLZs5QIQwW4BpokfNyKKqk1gi52t0qMwJBg");
		VideoEntry videoEntry;
		try {
			videoEntry = service.getEntry(new URL("http://gdata.youtube.com/feeds/api/videos/" + videoID), VideoEntry.class);
			String title = videoEntry.getTitle().getPlainText();
			String author = videoEntry.getAuthors().get(0).getName();
			
			YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
			
			MediaDescription mediaDesc = mediaGroup.getDescription();
			String description = mediaDesc == null ? null : mediaDesc.getPlainTextContent();
			String category = mediaGroup.getCategories().iterator().next().getLabel();
			long duration = mediaGroup.getDuration() == null ? -1 : mediaGroup.getDuration();
			
			MediaKeywords keywords = mediaGroup.getKeywords();
			Set<String> tags = new HashSet<String>();
			Iterator<String> iterator = keywords.getKeywords().iterator();
			while (iterator.hasNext()) {
				String tag = iterator.next();
				tags.add(tag);
			}
			
			GeoRssWhere location = videoEntry.getGeoCoordinates();
			double latitude = -1;
			double longitude = -1;
			if(location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}

			Rating rating = videoEntry.getRating();
			float avgRating = -1;
			int minRating = -1;
			int maxRating = -1;
			int ratingCount = -1;
			if(rating != null) {
				avgRating = rating.getAverage() == null ? -1 : rating.getAverage();
				minRating = rating.getMin();
				maxRating = rating.getMax();
				ratingCount = rating.getNumRaters() == null ? -1 : rating.getNumRaters();
			}

			YtStatistics stats = videoEntry.getStatistics();
			long viewCount = -1;
			long favCount = -1;
			if(stats != null ) {
				viewCount = stats.getViewCount();
				favCount = stats.getFavoriteCount();
			}
			
			YoutubeVideoDAO videoDAO = new YoutubeVideoDAO(videoID, author, title, tags, description, category, duration, latitude, longitude, avgRating, minRating, maxRating, ratingCount, viewCount, favCount);
			LOG.info("Done " + videoID);
			e.evaluteAndSave(videoID, videoDAO);
		} catch (Exception ex) {
			LOG.warn("Done with error" + videoID, ex);
			e.error(videoID, new UnableToCollectException(ex.getMessage()));
		}
	}

	@Override
	public void setEvaluator(Evaluator e) {
		this.e = e;
	}

	public String getID() {
		return videoID;
	}

}
