package br.ufmg.dcc.vod.ncrawler.common;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetIFRoundRobin {

	private static NetIFRoundRobin instance;
	
	private ArrayList<InetAddress> ifs;
	private int current;

	private NetIFRoundRobin() {
		this.current = 0;
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			ifs = new ArrayList<InetAddress>();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface next = networkInterfaces.nextElement();
				if (!next.isLoopback()) {
					Enumeration<InetAddress> inetAddresses = next.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress nextElement = inetAddresses.nextElement();
						if (!(nextElement instanceof Inet6Address)) {
							ifs.add(nextElement);
						}
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static synchronized NetIFRoundRobin getInstance() {
		if (instance == null) {
			instance = new NetIFRoundRobin();
		}
		
		return instance;
	}

	public synchronized InetAddress nextIF() {
		return ifs.get(current++ % ifs.size());
	}
	
	public static void main(String[] args) {
		System.out.println(NetIFRoundRobin.getInstance().ifs);
	}
}
