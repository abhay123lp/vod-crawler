package br.ufmg.dcc.vod.ncrawler.jobs.youtube.video_api;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

public class YoutubeVideoApiSave extends AbstractArraySerializer<YoutubeAPIVideoCrawlJob> {

	public YoutubeVideoApiSave() {
		super(1);
	}

	@Override
	public byte[][] getArrays(YoutubeAPIVideoCrawlJob t) {
		return new byte[][]{t.getID().getBytes()};
	}

	@Override
	public YoutubeAPIVideoCrawlJob setValueFromArrays(byte[][] bs) {
		return new YoutubeAPIVideoCrawlJob(new String(bs[0]));
	}

}
