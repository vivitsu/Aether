package aether.repl;

import java.net.InetAddress;

public class Host {
	InetAddress ipAddress;
	int port;
	public Host (InetAddress ia, int p) {
		ipAddress = ia;
		port = p;
	}
	public void setIPAddress (InetAddress ia) {
		ipAddress = ia;
	}
	public void setPort (int p) {
		port = p;
	}
	public InetAddress getIPAddress () {
		return ipAddress;
	}
	public int getPort () {
		return port;
	}
}

