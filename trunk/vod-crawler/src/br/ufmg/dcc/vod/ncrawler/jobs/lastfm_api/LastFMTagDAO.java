package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.Serializable;
import java.util.Date;

public class LastFMTagDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String tag;
	private final int useCount;
	private final Date postDate;

	public LastFMTagDAO(String tag, int useCount, Date postDate) {
		this.tag = tag;
		this.useCount = useCount;
		this.postDate = postDate;
	}

	public String getTag() {
		return tag;
	}

	public int getUseCount() {
		return useCount;
	}

	public Date getPostDate() {
		return postDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((postDate == null) ? 0 : postDate.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + useCount;
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
		if (postDate == null) {
			if (other.postDate != null)
				return false;
		} else if (!postDate.equals(other.postDate))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (useCount != other.useCount)
			return false;
		return true;
	}
}