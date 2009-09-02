package br.ufmg.dcc.vod.ncrawler.jobs.youtube_api_collector;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import br.ufmg.dcc.vod.ncrawler.common.FileUtil;
import br.ufmg.dcc.vod.ncrawler.common.MyXStreamer;

public class Level {

	public static void main(String[] args) throws Exception {
		File seedFile = new File(args[0]);
		File saveFolder = new File(args[1]);
		
		Set<String> check = FileUtil.readFileToSet(seedFile);
		int layer = 0;
		while (check.size() > 0) {
			layer += 1;
			
			Set<String> next = new HashSet<String>();
			double total = check.size();
			
			System.out.println(layer + " camada tem " + total + " usuarios");
			double found = 0;
			for (String u : check) {
				try {
					File f = new File(saveFolder.getAbsolutePath() + File.separator + u);
					YoutubeUserDAO udata = 
						(YoutubeUserDAO) MyXStreamer.getInstance().fromXML(f);
					
					next.addAll(udata.getSubscribers());
					next.addAll(udata.getSubscriptions());
					
					found ++;
				} catch (Exception e) {
				}
			}
			double perc = found/total;
			System.out.println(layer + " camada " + perc + " coletada (next layer has " + next.size() + " users)");
			
			check = next;
		}
	}
}