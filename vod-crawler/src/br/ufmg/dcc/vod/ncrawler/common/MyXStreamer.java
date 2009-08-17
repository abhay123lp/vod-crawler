package br.ufmg.dcc.vod.ncrawler.common;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.CGLIBEnhancedConverter;
import com.thoughtworks.xstream.mapper.CGLIBMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class MyXStreamer {

	private static MyXStreamer instance;
	private XStream stream;
	
	private MyXStreamer() {
		XStream xstream = new XStream() {
			protected MapperWrapper wrapMapper(MapperWrapper next) {
				return new CGLIBMapper(next);
			}
		};
		xstream.registerConverter(new CGLIBEnhancedConverter(xstream.getMapper(), xstream.getReflectionProvider()));
		this.stream = xstream;
	}
	
	public static MyXStreamer getInstance() {
		if (instance == null) instance = new MyXStreamer();
		return instance;
	}
	
	public XStream getStreamer() {
		return stream;
	}
	
}
