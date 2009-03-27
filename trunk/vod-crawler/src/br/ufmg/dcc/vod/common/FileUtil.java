package br.ufmg.dcc.vod.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPOutputStream;

/**
 * Utilities for dealing with files. 
 */
public class FileUtil {

	public static void saveUrlGzip(URL video, File filePath) throws IOException {
	    BufferedReader in = null;
	    PrintStream out = null;
	    try 
	    {
	    	URLConnection yc = video.openConnection();
	    	
		    in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		    File f = new File(filePath + ".gz");
			out = new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(f))));
		    
		    String inputLine;
		    while ((inputLine = in.readLine()) != null) {
		    	out.println(inputLine);
		    }
		    
		    out.flush();
		    out.close();
	    }
	    finally
	    {
	    	if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	if (out != null) {
	    		out.close();
	    	}
	    }
	}
	
}