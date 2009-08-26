package br.ufmg.dcc.vod.ncrawler.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.ufmg.dcc.vod.ncrawler.evaluator.Evaluator;
import br.ufmg.dcc.vod.ncrawler.queue.QueueHandle;
import br.ufmg.dcc.vod.ncrawler.queue.QueueService;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;

public abstract class AbstractProcessor implements Processor {

	protected final long sleepTimePerExecution;
	protected final int nThreads;
	protected final QueueHandle myHandle;
	protected final QueueService service;
	protected final Evaluator eval;

	public <S, I, C> AbstractProcessor(int nThreads, long sleepTimePerExecution, QueueService service,
			Serializer<S> serializer, File queueFile, int queueSize, Evaluator<I, C> eval) 
			throws FileNotFoundException, IOException {
		this.nThreads = nThreads;
		this.sleepTimePerExecution = sleepTimePerExecution;
		this.service = service;
		this.eval = eval;
		this.myHandle = service.createPersistentMessageQueue("Workers", queueFile, serializer, queueSize);
	}
}
