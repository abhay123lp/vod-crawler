package br.ufmg.dcc.vod.ncrawler.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api.LastFMArtistDAO;
import br.ufmg.dcc.vod.ncrawler.jobs.lastfm_api.LastFMTagDAO;

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
		this.stream.alias("lf-tag", LastFMTagDAO.class);
		this.stream.alias("lf-artist", LastFMArtistDAO.class);
	}
	
	public static MyXStreamer getInstance() {
		if (instance == null) instance = new MyXStreamer();
		return instance;
	}

	public void toXML(Object o, File file) throws IOException {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(file));
			this.stream.toXML(o, w);
		} finally {
			if (w != null) w.close();
		}
	}

	public void toXML(Object o, FileDescriptor out) throws IOException {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(out));
			this.stream.toXML(o, w);
		} finally {
			if (w != null) w.close();
		}
	}
	
	public Object fromXML(File file) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(file));
			return this.stream.fromXML(r);
		} finally {
			if (r != null) r.close();
		}
	}
}