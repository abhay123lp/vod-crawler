package br.ufmg.dcc.vod.ncrawler.evaluator;

import java.io.File;

import br.ufmg.dcc.vod.ncrawler.CrawlJob;
import br.ufmg.dcc.vod.ncrawler.processor.Processor;

public abstract class AbstractEvaluator<I,C> implements Evaluator<I, C> {

	private Processor processor;

	@Override
	public final void evaluteAndSave(I collectID, C collectContent, File savePath, boolean errorOcurred) {
		if (errorOcurred) {
			evalError(collectID);
		} else if (!evalResult(collectID, collectContent, savePath)) {
			evalError(collectID);
		}
	}

	public abstract boolean evalResult(I collectID, C collectContent, File savePath);

	public abstract void evalError(I collectID);

	public final void dispatch(CrawlJob j) {
		this.processor.dispatch(j);
	}
	
	@Override
	public final void setProcessor(Processor processor) {
		this.processor = processor;
	}
}