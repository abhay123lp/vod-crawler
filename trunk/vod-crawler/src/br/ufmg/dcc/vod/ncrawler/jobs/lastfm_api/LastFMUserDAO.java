package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class LastFMUserDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String userID;
	private final Set<String> friendNames;
	private final Set<LastFMPlayListDAO> playlistsDAO;
	private final Set<LastFMTrackDAO> lovedTracksDAO;
	private final Set<LastFMTrackDAO> recentTracksDAO;
	private final Collection<String> topTags;

	public LastFMUserDAO(String userID, Set<String> friendNames,
			Set<LastFMPlayListDAO> playlistsDAO, Set<LastFMTrackDAO> lovedTracksDAO,
			Set<LastFMTrackDAO> recentTracksDAO, Collection<String> topTags) {
				this.userID = userID;
				this.friendNames = friendNames;
				this.playlistsDAO = playlistsDAO;
				this.lovedTracksDAO = lovedTracksDAO;
				this.recentTracksDAO = recentTracksDAO;
				this.topTags = topTags;
	}

	public String getUserID() {
		return userID;
	}

	public Set<String> getFriendNames() {
		return friendNames;
	}

	public Set<LastFMPlayListDAO> getPlaylistsDAO() {
		return playlistsDAO;
	}

	public Set<LastFMTrackDAO> getLovedTracksDAO() {
		return lovedTracksDAO;
	}

	public Set<LastFMTrackDAO> getRecentTracksDAO() {
		return recentTracksDAO;
	}

	public Collection<String> getTopTags() {
		return topTags;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LastFMUserDAO other = (LastFMUserDAO) obj;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		return true;
	}

}
