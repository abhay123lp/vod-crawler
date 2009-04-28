package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.HttpClient;

import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.URLSaveCrawlJob;

public class URLSaveCrawlSerializer extends AbstractArraySerializer<URLSaveCrawlJob<YTHTMLType>> {

	private final HttpClient client;

	public URLSaveCrawlSerializer(HttpClient client) {
		super(3);
		this.client = client;
	}
	
	@Override
	public byte[][] getArrays(URLSaveCrawlJob<YTHTMLType> t) {
		byte[] url = t.getUrl().toString().getBytes();
		byte[] savePath = t.getSavePath().getAbsolutePath().getBytes();
		byte[] type = t.getType().getEnum().toString().getBytes();
		
		return new byte[][]{url, savePath, type};
	}

	@Override
	public URLSaveCrawlJob<YTHTMLType> setValueFromArrays(byte[][] bs) {
		String url = new String(bs[0]);
		String savePath = new String(bs[1]);
		String type = new String(bs[2]);
		
		try {
			return new URLSaveCrawlJob<YTHTMLType>(new URL(url), new File(savePath), YTHTMLType.forEnum(YTHTMLType.Type.valueOf(type)), client);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws MalformedURLException {
		URLSaveCrawlSerializer s = new URLSaveCrawlSerializer(null);
		
		URLSaveCrawlJob j = new URLSaveCrawlJob(new URL("http://www.vod.com.br"), new File("/tmp/disk"), YTHTMLType.FRIENDS, null);
		
		byte[] checkpointData = s.checkpointData(j);
		URLSaveCrawlJob interpret = s.interpret(checkpointData);
		
		System.out.println(interpret.getUrl());
		System.out.println(interpret.getSavePath());
		System.out.println(interpret.getType());
	}
}