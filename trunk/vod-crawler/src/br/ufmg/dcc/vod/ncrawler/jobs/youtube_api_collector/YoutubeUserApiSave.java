package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.File;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

public class YoutubeUserApiSave extends AbstractArraySerializer<YoutubeUserAPICrawlJob> {

	private final long sleepTime;

	public YoutubeUserApiSave(long sleepTime) {
		super(2);
		this.sleepTime = sleepTime;
	}

	@Override
	public byte[][] getArrays(YoutubeUserAPICrawlJob t) {
		byte[] id = t.getUserID().toString().getBytes();
		byte[] savePath = t.getSavePath().getBytes();
		
		return new byte[][]{id, savePath};
	}

	@Override
	public YoutubeUserAPICrawlJob setValueFromArrays(byte[][] bs) {
		String id = new String(bs[0]);
		String savePath = new String(bs[1]);
		return new YoutubeUserAPICrawlJob(id, new File(savePath), sleepTime);
	}

}
