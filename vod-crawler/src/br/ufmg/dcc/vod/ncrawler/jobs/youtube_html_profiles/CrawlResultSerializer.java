package br.ufmg.dcc.vod.ncrawler.jobs.youtube_html_profiles;

import java.io.File;

import br.ufmg.dcc.vod.ncrawler.CrawlResult;
import br.ufmg.dcc.vod.ncrawler.jobs.generic.AbstractArraySerializer;

public class CrawlResultSerializer extends AbstractArraySerializer<CrawlResult<File, YTHTMLType>> {

	public CrawlResultSerializer() {
		super(4);
	}

	@Override
	public byte[][] getArrays(CrawlResult<File, YTHTMLType> t) {
		byte[] id = t.getId().toString().getBytes();
		byte[] bytes = t.getResult() == null ? new byte[]{-1} : t.getResult().getAbsolutePath().getBytes();
		byte[] type = t.getType().getEnum().toString().getBytes();
		byte[] suc = new byte[]{(byte) (t.success() ? 1 : 0)};		
		
		return new byte[][]{id, bytes, type, suc};
	}

	@Override
	public CrawlResult<File, YTHTMLType> setValueFromArrays(byte[][] bs) {
		String id = new String(bs[0]);
		File r = (bs[1][0] == -1 && bs[1].length == 1) ?  null : new File(new String(bs[1]));
		YTHTMLType e = YTHTMLType.forEnum(YTHTMLType.Type.valueOf(new String(bs[2])));
		boolean success = bs[3][0] == 1;
		
		return new CrawlResult<File, YTHTMLType>(id, r, e, success);
		
	}
}