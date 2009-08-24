package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.HttpClient;


public class URLSaveCrawlSerializer extends AbstractArraySerializer<URLSaveCrawlJob> {

	private final HttpClient client;

	public URLSaveCrawlSerializer(HttpClient client) {
		super(3);
		this.client = client;
	}
	
	@Override
	public byte[][] getArrays(URLSaveCrawlJob t) {
		byte[] url = t.getUrl().toString().getBytes();
		byte[] savePath = t.getSavePath().getAbsolutePath().getBytes();
		byte[] type = t.getType().getFeatureName().getBytes();
		
		return new byte[][]{url, savePath, type};
	}

	@Override
	public URLSaveCrawlJob setValueFromArrays(byte[][] bs) {
		String url = new String(bs[0]);
		String savePath = new String(bs[1]);
		String type = new String(bs[2]);
		
		try {
			return new URLSaveCrawlJob(new URL(url), new File(savePath), HTMLTypeFactory.getInstance().resolv(type), client);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
}