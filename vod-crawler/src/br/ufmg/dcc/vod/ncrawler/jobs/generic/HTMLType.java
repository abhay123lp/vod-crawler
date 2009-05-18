package br.ufmg.dcc.vod.ncrawler.jobs.generic;

public interface HTMLType extends Comparable<HTMLType> {

	public String getFeatureName();

	public boolean hasFollowUp();

}