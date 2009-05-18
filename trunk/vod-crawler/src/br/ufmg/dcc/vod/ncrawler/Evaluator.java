package br.ufmg.dcc.vod.ncrawler;

import java.io.File;
import java.util.Collection;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;

public interface Evaluator<I, C> {

	public Collection<CrawlJob> evaluteAndSave(I collectID, C collectContent, File savePath) throws Exception;

	public Collection<CrawlJob> getInitialCrawl() throws Exception;
	
}