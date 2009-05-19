package br.ufmg.dcc.vod.ncrawler.jobs;

import java.io.File;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.stats.StatsPrinter;

public interface Evaluator<I, C> {
	
	public void setStatsKeeper(StatsPrinter sp);
	public Collection<CrawlJob> getInitialCrawl();
	
	public Collection<CrawlJob> evaluteAndSave(I collectID, C collectContent, File savePath);
	public void errorOccurred(I collectID, Exception e);
	
	
}