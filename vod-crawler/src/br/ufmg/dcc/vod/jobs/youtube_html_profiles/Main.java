package br.ufmg.dcc.vod.jobs.youtube_html_profiles;

import java.io.File;

public class Main {

	public static void main(String[] args) {
		
		int nThreads = Integer.parseInt(args[0]);
		int sleep = Integer.parseInt(args[1]);
		
		YTUserHTMLEvaluator e = null;
		if (args[2] != null && args[2].equals("-r")) {
			
			File userFolder = new File(args[3]);
			File videoFolder = new File(args[4]);
			
		} else {

			File userFolder = new File(args[2]);
			File videoFolder = new File(args[3]);
			File seedFile = new File(args[4]);
			
		}
		
	}
	
}
