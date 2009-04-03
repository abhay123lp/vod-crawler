package br.ufmg.dcc.vod.queue;

public interface EventQueue<T> {

	public void put(T t);

	public T take();

	public int size();
	
}
