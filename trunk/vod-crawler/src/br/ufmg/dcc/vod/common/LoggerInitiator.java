package br.ufmg.dcc.vod.common;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;

public class LoggerInitiator {

	public static void initiateLog() throws IOException {
		String property = System.getProperty("logfile");
		
		if (property == null) {
			throw new IOException("Property log file not defined, use -Dlogfile=file");
		}
		
		BasicConfigurator.configure(new DailyRollingFileAppender(new PatternLayout("%d [%t] %-5p %c - %m%n"), property, "'.'yyyy-MM-dd"));
	}
	
}
