package br.ufmg.dcc.vod.ncrawler.stats;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueProcessor;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

public class StatsPrinter implements QueueProcessor<StatEvent> {

	public static final String SEP = "iu34ry8919uxc328urbn981";
	
	private final LinkedHashMap<String, Integer> map;
	private QueueHandle h;
	private final QueueService service;
	
	public StatsPrinter(QueueService service, String... ts) {
		map = new LinkedHashMap<String, Integer>();
		for (String est : ts) {
			map.put(est, 0);
		}
		
		this.h = service.createMessageQueue("Stats");
		this.service = service;
	}

	public void start() {
		service.startProcessor(h, this);
	}
	
	@Override
	public String getName() {
		return "StatsPrinter";
	}

	@Override
	public void process(StatEvent se) {
		if (map.containsKey(se.t)) {
			map.put(se.t, map.get(se) + se.value);
		}
		
		printStats();
	}

	private void printStats() {
		for (Entry<String, Integer> e : map.entrySet()) {
			if (e.getKey() == SEP) {
				System.out.println("--");
			} else {
				System.out.println(e.getKey() + " : " + e.getValue());
			}
		}
	}
}
