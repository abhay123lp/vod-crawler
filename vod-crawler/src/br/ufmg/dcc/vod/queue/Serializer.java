package br.ufmg.dcc.vod.queue;

public interface Serializer<T> {

	public byte[] checkpointData(T t);
	
	public T interpret(byte[] checkpoint);
	
}
