package br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

public class LastFMApiSave extends AbstractArraySerializer<LastFMApiCrawlJob> {

	private final long sleepTime;

	public LastFMApiSave(long sleepTime) {
		super(1);
		this.sleepTime = sleepTime;
	}

	@Override
	public byte[][] getArrays(LastFMApiCrawlJob t) {
		byte[] id = t.getUserID().toString().getBytes();
		return new byte[][]{id};
	}

	@Override
	public LastFMApiCrawlJob setValueFromArrays(byte[][] bs) {
		String id = new String(bs[0]);
		return new LastFMApiCrawlJob(id, sleepTime);
	}

}
