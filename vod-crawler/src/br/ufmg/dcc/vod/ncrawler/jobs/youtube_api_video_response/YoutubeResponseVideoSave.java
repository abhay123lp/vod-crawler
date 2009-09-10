package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_video_response;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

public class YoutubeResponseVideoSave extends AbstractArraySerializer<YoutubeAPIVidResponseJob> {

	public YoutubeResponseVideoSave() {
		super(1);
	}

	@Override
	public byte[][] getArrays(YoutubeAPIVidResponseJob t) {
		return new byte[][]{t.getVideoID().getBytes()};
	}

	@Override
	public YoutubeAPIVidResponseJob setValueFromArrays(byte[][] bs) {
		return new YoutubeAPIVidResponseJob(new String(bs[0]));
	}

}
