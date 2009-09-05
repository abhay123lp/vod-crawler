package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Tag;
import net.roarsoftware.lastfm.User;

public class Seed {

	public static void main(String[] args) throws IOException {
		List<Tag> topTags = Tag.getTopTags(LastFMApiCrawlJob.API_KEY);
		for (Tag t : topTags) {
			Collection<Artist> artists = Tag.getTopArtists(t.getName(), LastFMApiCrawlJob.API_KEY);
			for (Artist a : artists) {
				Collection<User> topFans = Artist.getTopFans(a.getName(), LastFMApiCrawlJob.API_KEY);
				for (User u : topFans) {
					System.out.println(u.getName());
				}
			}
		}
	}
}
