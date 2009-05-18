package br.ufmg.dcc.vod.ncrawler.stats;

import java.util.LinkedHashMap;

import br.ufmg.dcc.vod.ncrawler.queue.QueueService;

public class StatsPrinter {

	private final LinkedHashMap<EventStatsType, Integer> map;
	
	public StatsPrinter(QueueService service, EventStatsType... ts) {
		map = new LinkedHashMap<EventStatsType, Integer>();
		for (EventStatsType est : ts) {
			map.put(est, 0);
		}
	}
	
}
