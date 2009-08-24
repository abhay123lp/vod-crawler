package br.ufmg.dcc.vod.ncrawler.jobs;

import java.io.File;
import java.util.List;

import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public interface EvaluatorFactory<I, C, T> {

	public void initiate(int threads, File saveFolder, long sleepTime, List<String> seeds);
	
	public Evaluator<I, C> getEvaluator();

	public Serializer<T> getSerializer();

	public void shutdown();
	
}
