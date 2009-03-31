package br.ufmg.dcc.vod.jobs.youtube_html_profiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateSeed {

	public static void main(String[] args) throws IOException {
		Pattern compile = Pattern.compile("(\\s+<a href=\"/user/)(.*?)(\" title=\"\\2\" rel=\"nofollow\">\\2</a>)");

		HashSet<String> set = new HashSet<String>();
		for (int i = 1; i <= 5; i++) {
			String s = "http://www.youtube.com/members?gl=US&hl=en&s=ms&t=a&g=0&c=0&to=0&nb=0&p="+i;
			URL u = new URL(s);
			
			URLConnection openConnection = u.openConnection();
			
			BufferedReader bis = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
			String line = null;
			while ((line = bis.readLine()) != null) {
				Matcher matcher = compile.matcher(line);
				if (matcher.matches()) {
					set.add(matcher.group(2));
				}
			}
			bis.close();
		}
		
		for (String s : set) {
			System.out.println(s);
		}
	}
}
