package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.Serializable;
import java.util.Set;

public class LastFMPlayListDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int id;
	private final Set<LastFMTrackDAO> lastFMTrackDAO;

	public LastFMPlayListDAO(int id, Set<LastFMTrackDAO> lastFMTrackDAO) {
		this.id = id;
		this.lastFMTrackDAO = lastFMTrackDAO;
	}

	public int getId() {
		return id;
	}
	
	public Set<LastFMTrackDAO> getTrackDAO() {
		return lastFMTrackDAO;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		LastFMPlayListDAO other = (LastFMPlayListDAO) obj;
		if (id != other.id)
			return false;
		return true;
	}
}