package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.HTMLType;

public class YTHTMLType implements HTMLType {

	private enum Type {
		//These have no following links
		SINGLE_VIDEO,
		
		//These do
		PROFILE, FRIENDS, SUBSCRIBERS, SUBSCRIPTIONS, VIDEOS, FAVORITES, GROUPS;
	}
	
	public static final YTHTMLType SINGLE_VIDEO = new YTHTMLType(Type.SINGLE_VIDEO);
	public static final YTHTMLType PROFILE = new YTHTMLType(Type.PROFILE);
	public static final YTHTMLType SUBSCRIBERS = new YTHTMLType(Type.SUBSCRIBERS);
	public static final YTHTMLType SUBSCRIPTIONS = new YTHTMLType(Type.SUBSCRIPTIONS);
	public static final YTHTMLType VIDEOS = new YTHTMLType(Type.VIDEOS);
	public static final YTHTMLType FAVORITES = new YTHTMLType(Type.FAVORITES);
	public static final YTHTMLType GROUPS = new YTHTMLType(Type.GROUPS);
	public static final YTHTMLType FRIENDS = new YTHTMLType(Type.FRIENDS);
	
	private final Type t;

	private YTHTMLType(Type t) {
		this.t = t;
	}

	@Override
	public int compareTo(HTMLType o) {
		if (o instanceof YTHTMLType) {
			YTHTMLType yts = (YTHTMLType) o;
			return t.compareTo(yts.t);
		}
		
		return -1;
	}

	public boolean hasFollowUp() {
		return 
			t == Type.FRIENDS || t == Type.SUBSCRIBERS || t == Type.SUBSCRIPTIONS || t == Type.VIDEOS ||
			t == Type.FAVORITES || t == Type.GROUPS || t == Type.PROFILE;
	}
	
	@Override
	public String getFeatureName() {
		switch (t) {
		case FAVORITES:
			return "favorites";
		case FRIENDS:
			return "friends";
		case GROUPS:
			return "groups";
		case SUBSCRIBERS:
			return "subscribers";
		case SUBSCRIPTIONS:
			return "subscriptions";
		case VIDEOS:
			return "videos";
		case PROFILE:
			return "profile";
		case SINGLE_VIDEO:
			return "single-vid";
		default:
			return "no-name";
		}
	}
	
	@Override
	public String toString() {
		return getFeatureName();
	}

	@Override
	public HTMLType[] enumerate() {
		return new HTMLType[]{SINGLE_VIDEO,  PROFILE, SUBSCRIBERS, SUBSCRIPTIONS,
			VIDEOS, FAVORITES , GROUPS, FRIENDS};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((t == null) ? 0 : t.hashCode());
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
		YTHTMLType other = (YTHTMLType) obj;
		if (t == null) {
			if (other.t != null)
				return false;
		} else if (!t.equals(other.t))
			return false;
		return true;
	}
}