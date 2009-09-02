package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.Serializable;

public class LastFMTrackDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String artist;
	private final String artistMbid;
	private final String album;
	private final String albumMbid;
	private final String name;
	private final String mbid;
	private final int duration;

	public LastFMTrackDAO(String artist, String artistMbid, String album,
			String albumMbid, String name, String mbid, int duration) {
				this.artist = artist;
				this.artistMbid = artistMbid;
				this.album = album;
				this.albumMbid = albumMbid;
				this.name = name;
				this.mbid = mbid;
				this.duration = duration;
	}

	public String getArtist() {
		return artist;
	}

	public String getArtistMbid() {
		return artistMbid;
	}

	public String getAlbum() {
		return album;
	}

	public String getAlbumMbid() {
		return albumMbid;
	}

	public String getName() {
		return name;
	}

	public String getMbid() {
		return mbid;
	}

	public int getDuration() {
		return duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result
				+ ((albumMbid == null) ? 0 : albumMbid.hashCode());
		result = prime * result + ((artist == null) ? 0 : artist.hashCode());
		result = prime * result
				+ ((artistMbid == null) ? 0 : artistMbid.hashCode());
		result = prime * result + duration;
		result = prime * result + ((mbid == null) ? 0 : mbid.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		LastFMTrackDAO other = (LastFMTrackDAO) obj;
		if (album == null) {
			if (other.album != null)
				return false;
		} else if (!album.equals(other.album))
			return false;
		if (albumMbid == null) {
			if (other.albumMbid != null)
				return false;
		} else if (!albumMbid.equals(other.albumMbid))
			return false;
		if (artist == null) {
			if (other.artist != null)
				return false;
		} else if (!artist.equals(other.artist))
			return false;
		if (artistMbid == null) {
			if (other.artistMbid != null)
				return false;
		} else if (!artistMbid.equals(other.artistMbid))
			return false;
		if (duration != other.duration)
			return false;
		if (mbid == null) {
			if (other.mbid != null)
				return false;
		} else if (!mbid.equals(other.mbid))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}