package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.File;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

import com.google.gdata.client.youtube.YouTubeService;

public class YoutubeUserApiSave extends AbstractArraySerializer<YoutubeUserAPICrawlJob> {

	private final YouTubeService service;

	public YoutubeUserApiSave(YouTubeService service) {
		super(2);
		this.service = service;
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
		return new YoutubeUserAPICrawlJob(service, id, new File(savePath));
	}

}
