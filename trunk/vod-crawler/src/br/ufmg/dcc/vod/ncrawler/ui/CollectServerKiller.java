package br.ufmg.dcc.vod.ncrawler.ui;

import java.net.InetAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import br.ufmg.dcc.vod.ncrawler.distributed.client.ServerID;
import br.ufmg.dcc.vod.ncrawler.distributed.server.JobExecutor;

public class CollectServerKiller {

	private static final String HOST = "h";
	private static final String PORT = "p";

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		
		Option hostOpt = OptionBuilder.withArgName("host")
		.hasArg()
		.isRequired()
		.withDescription("Host to check")
		.create(PORT);
		
		Option portOpt = OptionBuilder.withArgName("port")
		.hasArg()
		.isRequired()
		.withDescription("Port to check")
		.create(HOST);
		
		opts.addOption(hostOpt);
		opts.addOption(portOpt);
		
		InetAddress host = null;
		int port = -1;
		
		try {
			GnuParser parser = new GnuParser();
			CommandLine cli = parser.parse(opts, args);
			
			host = InetAddress.getByName(cli.getOptionValue(HOST));
			port = Integer.parseInt(cli.getOptionValue(PORT));
		} catch (Exception e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("java " + CollectServer.class, opts);
			
			System.out.println();
			System.out.println();
			e.printStackTrace();
			System.exit(EXIT_CODES.ERROR);
		}
		
		ServerID sid = new ServerID(host.getHostAddress(), port);
		JobExecutor resolve = null;
		
		try {
			resolve = sid.resolve();
			System.exit(EXIT_CODES.OK);
		} catch (Exception e) {
			System.out.println("Already offline");
			System.exit(EXIT_CODES.STATE_UNCHANGED);
		}
		
		try {
			resolve.kill();
			System.out.println("Stopped server!");
		} catch (Exception e) {
		}
		System.exit(EXIT_CODES.OK);
	}	
}