package aether.repl;

import java.net.InetAddress;
/**
 * Data structure to store the information about 
 * how much space is available on each node of the cluster.
 * Store the node information in form of IP address,
 * port number and available space at the node
 * */


public class NodeSpace {
		InetAddress ipAddress;
		int port;
		long spaceAvailable;
		public NodeSpace (InetAddress ia, int p, long s) {
			ipAddress = ia;
			port = p;
			spaceAvailable = s;
		}
		public long getAvailableSpace () {
			return spaceAvailable;
		}
		public void setAvailableSpace (InetAddress ia, long s) {
			ipAddress = ia;
			spaceAvailable = s;
		}
		public InetAddress getIPAddress () {
			return ipAddress; 
		}
		public int getPort () {
			return port;
		}


}
