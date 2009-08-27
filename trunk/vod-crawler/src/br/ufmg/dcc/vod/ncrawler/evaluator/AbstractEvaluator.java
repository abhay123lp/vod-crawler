package br.ufmg.dcc.vod.ncrawler.evaluator;

import java.io.File;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;

public abstract class AbstractEvaluator<I,C> implements Evaluator<I, C> {

	private Processor processor;

	@Override
	public final void evaluteAndSave(I collectID, C collectContent, File savePath, boolean errorOcurred, Exception e) {
		if (errorOcurred) {
			evalError(collectID, e);
		} else if (!evalResult(collectID, collectContent, savePath)) {
			evalError(collectID, e);
		}
	}

	public abstract boolean evalResult(I collectID, C collectContent, File savePath);

	public abstract void evalError(I collectID, Exception e);

	public final void dispatch(CrawlJob j) {
		this.processor.dispatch(j);
	}
	
	@Override
	public final void setProcessor(Processor processor) {
		this.processor = processor;
	}
}