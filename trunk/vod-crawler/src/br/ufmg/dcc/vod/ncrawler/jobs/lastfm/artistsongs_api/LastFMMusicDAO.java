package br.ufmg.dcc.vod.ncrawler.jobs.lastfm.artistsongs_api;

import java.util.Collection;

public abstract class LastFMMusicDAO {

	private final Type t;
	private final String name;
	private final Collection<String> tags;
	private final String description;

	public enum Type {ARTIST, SONG, ALBUM}
	
	public LastFMMusicDAO(Type t, String name, Collection<String> tags,
			String description) {
				this.t = t;
				this.name = name;
				this.tags = tags;
				this.description = description;
	}
	
}
