package aether.repl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import aether.io.Chunk;
import aether.net.NetMgr;


public class Replication {
	public static int HTBT_SND_PORT_NUMBER = 34444;
	public static int HTBT_RCV_PORT_NUMBER = 34445;
	public static int NUM_RETRIES = 4;
	public static int TIMEOUT_BEFORE_DEAD = 10000;	//milliseconds
	public static int REPL_PORT_LISTENER = 44444;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*HtbtBuddyMap hbm = new HtbtBuddyMap ();
		try {
			//hbm.put(new Host (InetAddress.getLocalHost(), Replication.HTBT_RCV_PORT_NUMBER), null);
			hbm.put(new Host (InetAddress.getByName("192.168.0.10"), Replication.HTBT_RCV_PORT_NUMBER), null);
			System.out.println(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HtbtSender s = new HtbtSender (hbm);
		new Thread(s).start();
		
		HtbtReceiver r = new HtbtReceiver();
		new Thread(r).start();

		*/
	}
}



