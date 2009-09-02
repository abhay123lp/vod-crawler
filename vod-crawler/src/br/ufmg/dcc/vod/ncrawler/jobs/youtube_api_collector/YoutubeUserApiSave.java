package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

public class YoutubeUserApiSave extends AbstractArraySerializer<YoutubeUserAPICrawlJob> {

	private final long sleepTime;

	public YoutubeUserApiSave(long sleepTime) {
		super(1);
		this.sleepTime = sleepTime;
	}

	@Override
	public byte[][] getArrays(YoutubeUserAPICrawlJob t) {
		byte[] id = t.getUserID().toString().getBytes();
		return new byte[][]{id};
	}

	@Override
	public YoutubeUserAPICrawlJob setValueFromArrays(byte[][] bs) {
		String id = new String(bs[0]);
		return new YoutubeUserAPICrawlJob(id, sleepTime);
	}

}
