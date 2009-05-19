package br.ufmg.dcc.vod.ncrawler.jobs.generic;

import java.util.HashMap;


public class HTMLTypeFactory {

	private static HTMLTypeFactory instance;
	private HashMap<String, HTMLType> map;

	private HTMLTypeFactory() {
		this.map = new HashMap<String, HTMLType>();
	}
	
	public static HTMLTypeFactory getInstance() {
		if (instance == null) {
			instance = new HTMLTypeFactory();
		}
		
		return instance;
	}

	public void addMappings(HTMLType[] array, boolean overwrite) {
		for (HTMLType t : array) {
			if (map.containsKey(t.getFeatureName()) && !overwrite) {
				throw new RuntimeException();
			}
			
			map.put(t.getFeatureName(), t);
		}
	}
	
	public HTMLType resolv(String type) {
		return map.get(type);
	}
}