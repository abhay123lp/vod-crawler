package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import java.io.File;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;
import br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector.YoutubeUserAPICrawlJob;

public class LastFMApiSave extends AbstractArraySerializer<LastFMApiCrawlJob> {

	private final long sleepTime;

	public LastFMApiSave(long sleepTime) {
		super(2);
		this.sleepTime = sleepTime;
	}

	@Override
	public byte[][] getArrays(LastFMApiCrawlJob t) {
		byte[] id = t.getUserID().toString().getBytes();
		byte[] savePath = t.getSavePath().getBytes();
		
		return new byte[][]{id, savePath};
	}

	@Override
	public LastFMApiCrawlJob setValueFromArrays(byte[][] bs) {
		String id = new String(bs[0]);
		String savePath = new String(bs[1]);
		return new LastFMApiCrawlJob(id, new File(savePath), sleepTime);
	}

}
