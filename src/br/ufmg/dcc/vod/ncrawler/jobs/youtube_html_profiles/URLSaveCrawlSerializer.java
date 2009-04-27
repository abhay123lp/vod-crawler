package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.http.client.HttpClient;

import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public class URLSaveCrawlSerializer implements Serializer<URLSaveCrawlJob> {

	private final HttpClient client;

	public URLSaveCrawlSerializer(HttpClient client) {
		this.client = client;
	}
	
	@Override
	public byte[] checkpointData(URLSaveCrawlJob t) {
		byte[] url = t.getID().getBytes();
		byte[] savePath = t.getSavePath().getAbsolutePath().getBytes();
		byte[] type = t.getType().name().getBytes();

		byte[] res = new byte[url.length + savePath.length + type.length + 3];
		
		//unsafe if not!!!!
		if ((url.length + savePath.length + type.length) > 2 * Byte.MAX_VALUE + 1) {
			throw new RuntimeException();
		}
		
		res[0] = (byte) url.length;
		res[1] = (byte) savePath.length;
		res[2] = (byte) type.length;
		
		System.arraycopy(url, 0, res, 3, url.length);
		System.arraycopy(savePath, 0, res, 3 + url.length, savePath.length);
		System.arraycopy(type, 0, res, 3 + url.length + savePath.length, type.length);
		
		return res;
	}

	@Override
	public URLSaveCrawlJob interpret(byte[] checkpoint) {
		int urlS = checkpoint[0];
		int savepS = checkpoint[1];
		
		String url = new String(Arrays.copyOfRange(checkpoint, 3, urlS + 3));
		String savePath = new String(Arrays.copyOfRange(checkpoint, 3 + urlS, 3 + urlS + savepS));
		String type = new String(Arrays.copyOfRange(checkpoint, 3 + urlS + savepS, checkpoint.length));
		
		try {
			return new URLSaveCrawlJob(new URL(url), new File(savePath), HTMLType.valueOf(type), client);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws MalformedURLException {
		URLSaveCrawlSerializer s = new URLSaveCrawlSerializer(null);
		
		URLSaveCrawlJob j = new URLSaveCrawlJob(new URL("http://www.vod.com.br"), new File("/tmp/disk"), HTMLType.SINGLE_VIDEO, null);
		
		byte[] checkpointData = s.checkpointData(j);
		URLSaveCrawlJob interpret = s.interpret(checkpointData);
		
		System.out.println(interpret.getID());
		System.out.println(interpret.getSavePath());
		System.out.println(interpret.getType());
	}
}

