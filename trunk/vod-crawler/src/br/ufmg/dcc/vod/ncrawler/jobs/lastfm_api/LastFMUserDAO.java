package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.Serializable;
import java.util.Collection;

public class LastFMUserDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String userID;
	private final Collection<String> friends;
	private final Collection<LastFMTagDAO> toptags;
	private final Collection<String> loved;
	private final Collection<LastFMArtistDAO> artists;

	public LastFMUserDAO(String userID, Collection<String> friendNames,
			Collection<LastFMArtistDAO> artists, Collection<String> loved, Collection<LastFMTagDAO> discoverTagDAO) {
				this.userID = userID;
				this.friends = friendNames;
				this.artists = artists;
				this.loved = loved;
				this.toptags = discoverTagDAO;
	}

	public String getUserID() {
		return userID;
	}

	public Collection<String> getFriendNames() {
		return friends;
	}

	public Collection<LastFMTagDAO> getTopTags() {
		return toptags;
	}

	public Collection<String> getLoved() {
		return loved;
	}

	public Collection<LastFMArtistDAO> getArtists() {
		return artists;
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
