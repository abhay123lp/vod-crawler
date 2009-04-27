package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

public enum HTMLType {

	//These have no following links
	SINGLE_VIDEO,
	
	//These do
	PROFILE, FRIENDS, SUBSCRIBERS, SUBSCRIPTIONS, VIDEOS, FAVORITES, GROUPS;
	
	public boolean hasFollowUp() {
		return 
			this == FRIENDS || this == SUBSCRIBERS || this == SUBSCRIPTIONS || this == VIDEOS ||
			this == FAVORITES || this == GROUPS || this == PROFILE;
	}
	
	public String getFeatureName() {
		switch (this) {
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
}