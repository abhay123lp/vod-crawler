package br.ufmg.dcc.vod.ncrawler;

/**
 * Collects an object and returns the result of this crawl.
 * 
 * @param <R> Result from a crawl
 * @param <T> Type of object crawled
 */
public interface CrawlJob<R, T> {

	public void collect() throws Exception;
	
	public R getResult();
	
	public T getType();
	
	public String getID();
	
	public void markWithError();
	
	public boolean success();

}
