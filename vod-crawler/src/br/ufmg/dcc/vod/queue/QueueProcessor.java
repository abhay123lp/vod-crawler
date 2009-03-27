package br.ufmg.dcc.vod.queue;

public interface QueueProcessor<T> {

	public void process(T t);

	public String getName();
	
}
