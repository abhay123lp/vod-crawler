package br.ufmg.dcc.vod.ncrawler.evaluator;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;

public abstract class AbstractEvaluator<I,C> implements Evaluator<I, C> {

	private Processor processor;

	public final void dispatch(CrawlJob j) {
		this.processor.dispatch(j);
	}
	
	@Override
	public final void setProcessor(Processor processor) {
		this.processor = processor;
	}
}