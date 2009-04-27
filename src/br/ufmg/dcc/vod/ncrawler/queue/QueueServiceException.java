package br.ufmg.dcc.vod.ncrawler.queue;

public class QueueServiceException extends RuntimeException {

	public QueueServiceException(String string) {
		super(string);
	}

	public QueueServiceException(InterruptedException e) {
		super(e);
	}

}
