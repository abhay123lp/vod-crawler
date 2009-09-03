package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.Serializable;
import java.util.Set;

public class LastFMTagDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String tag;
	private final String user;
	
	private final Set<String> artists;
	private final Set<String> songs;

	public LastFMTagDAO(String userName, String tag, Set<String> artists, Set<String> songs) {
		this.user = userName;
		this.tag = tag;
		this.artists = artists;
		this.songs = songs;
	}

	public String getTag() {
		return tag;
	}
	
	public Set<String> getArtists() {
		return artists;
	}
	
	public Set<String> getSongs() {
		return songs;
	}

	public String getUserName() {
		return user;
	}
	
	public int getUseCount() {
		return songs.size() + artists.size();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result
				+ ((user == null) ? 0 : user.hashCode());
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
		LastFMTagDAO other = (LastFMTagDAO) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}