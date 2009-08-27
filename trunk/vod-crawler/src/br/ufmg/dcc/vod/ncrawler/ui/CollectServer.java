package br.ufmg.dcc.vod.ncrawler.ui;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.distributed.server.JobExecutorFactory;

public class CollectServer {

	private static final String PORT = "p";

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		Option portOpt = OptionBuilder.withArgName("port")
		.hasArg()
		.isRequired()
		.withDescription("Port to Bind")
		.create(PORT);
		
		opts.addOption(portOpt);
		
		int port = -1;
		try {
			GnuParser parser = new GnuParser();
			CommandLine cli = parser.parse(opts, args);
			port = Integer.parseInt(cli.getOptionValue(PORT));
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("java " + CollectServer.class, opts);
			
			System.out.println();
			System.out.println();
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
		
		try {
			JobExecutorFactory jef = new JobExecutorFactory(port);
			jef.createAndBind();
		} catch (Exception e) {
			System.out.println("Already UP!");
			System.exit(EXIT_CODES.STATE_UNCHANGED);
		}
	}
}