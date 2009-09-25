package br.ufmg.dcc.vod.ncrawler.csv_conv;

public interface Converter {

	public String[] convert(Object o);
	
	public String[] header();
	
}
