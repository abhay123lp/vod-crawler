package br.ufmg.dcc.vod.ncrawler.jobs.youtube.html;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.common.Pair;
import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.evaluator.EvaluatorFactory;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.CrawlJobStringSerializer;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class YTHtmlStatsFactory implements EvaluatorFactory<String, Pair<byte[], byte[]>, CrawlJob> {

	private YTHtmlAndStatsEvaluator eval;
	private CrawlJobStringSerializer serial;
	
	private BufferedOutputStream htmlFile;
	private BufferedOutputStream statsFile;

	@Override
	public Evaluator<String, Pair<byte[], byte[]>> getEvaluator() {
		return eval;
	}

	@Override
	public Serializer<CrawlJob> getSerializer() {
		return serial;
	}

	@Override
	public void initiate(int threads, File saveFolder, long sleepTime,	List<String> seeds) throws FileNotFoundException, IOException {
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy_H-mm-ss");
		String htmlFileName = "videoinfo-"+formatter.format(now);
		String statsFileName = "videostats-"+formatter.format(now);
		
		
		this.htmlFile = new BufferedOutputStream(
				new GZIPOutputStream(
						new FileOutputStream(
								new File(saveFolder + File.separator + htmlFileName))));
		
		this.statsFile = new BufferedOutputStream(
				new GZIPOutputStream(
						new FileOutputStream(
								new File(saveFolder + File.separator + statsFileName))));
		
		this.eval = new YTHtmlAndStatsEvaluator(seeds, htmlFile, statsFile);
		this.serial = new CrawlJobStringSerializer(this.eval);
	}

	@Override
	public void shutdown() throws IOException {
		htmlFile.close();
		statsFile.close();
	}
}
