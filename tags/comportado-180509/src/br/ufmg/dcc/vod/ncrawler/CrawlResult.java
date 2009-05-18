package br.ufmg.dcc.vod.ncrawler;

public class CrawlResult<R, T> {

	private final R r;
	private final T t;
	private boolean success;
	private final String id;

	public CrawlResult(String id, R r, T t) {
		this(id, r, t, true);
	}

	public CrawlResult(String id, R r, T t, boolean success) {
		this.id = id;
		this.r = r;
		this.t = t;
		this.success = success;
	}

	public R getResult() {
		return r;
	}
	
	public T getType() {
		return t;
	}
	
	public void markWithError() {
		this.success = false;
	}	
	
	public boolean success() {
		return success;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return getId();
	}
}
