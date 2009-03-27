package br.ufmg.dcc.vod.queue;

public class QueueServiceException extends RuntimeException {

	public QueueServiceException(String string) {
		super(string);
	}

	public QueueServiceException(InterruptedException e) {
		super(e);
	}

}
