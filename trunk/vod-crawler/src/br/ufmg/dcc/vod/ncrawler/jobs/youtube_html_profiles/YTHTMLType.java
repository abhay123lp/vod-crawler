package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.HTMLType;

public class YTHTMLType implements HTMLType {

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
		default:
			return "no-name";
		}
	}
	
	public Type getEnum() {
		return t;
	}
	
	public enum Type {
		//These have no following links
		SINGLE_VIDEO,
		
		//These do
		PROFILE, FRIENDS, SUBSCRIBERS, SUBSCRIPTIONS, VIDEOS, FAVORITES, GROUPS;
	}

	public static YTHTMLType forEnum(Type t) {
		switch (t) {
		case FAVORITES:
			return FAVORITES;
		case FRIENDS:
			return FRIENDS;
		case GROUPS:
			return GROUPS;
		case SUBSCRIBERS:
			return SUBSCRIBERS;
		case SUBSCRIPTIONS:
			return SUBSCRIPTIONS;
		case VIDEOS:
			return VIDEOS;
		case PROFILE:
			return PROFILE;
		default:
			return null;
		}
	}
	
	@Override
	public String toString() {
		return getFeatureName();
	}
}
