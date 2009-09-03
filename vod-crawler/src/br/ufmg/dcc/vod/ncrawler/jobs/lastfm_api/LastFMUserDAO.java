package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class LastFMUserDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String userID;
	private final Set<String> friendNames;
	private final Collection<LastFMTagDAO> topTags;
	private final Set<String> loved;

	public LastFMUserDAO(String userID, Set<String> friendNames,
			Set<String> loved, Collection<LastFMTagDAO> discoverTagDAO) {
				this.userID = userID;
				this.friendNames = friendNames;
				this.loved = loved;
				this.topTags = discoverTagDAO;
	}

	public String getUserID() {
		return userID;
	}

	public Set<String> getFriendNames() {
		return friendNames;
	}

	public Collection<LastFMTagDAO> getTopTags() {
		return topTags;
	}

	public Set<String> getLoved() {
		return loved;
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
