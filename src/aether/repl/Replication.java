package aether.repl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import aether.net.Message;
import aether.net.NetMgr;


public class Replication {
	private static Replication repl;
	public static int HTBT_SND_PORT_NUMBER = 34444;
	public static int HTBT_RCV_PORT_NUMBER = 34445;
	public static int NUM_RETRIES = 4;
	public static int TIMEOUT_BEFORE_DEAD = 10000;	//milliseconds
	public static int REPL_PORT_LISTENER = 44444;	//port where all nodes listen for file chunk transfer	
	public static int REPL_MAIN_LISTENER = 34343;	//port where all the read requests arrive from other cluster nodes and clients
	public static synchronized Replication getInstance () {
		if (repl == null) {
			repl = new Replication ();
		}
		return repl;
	}
	public Replication () {
		HtbtBuddyMap hbm = new HtbtBuddyMap ();
		//hbm.put(new Host(), null);
		HtbtSender s = new HtbtSender (hbm);		
		HtbtReceiver r = new HtbtReceiver ();
		ReplicationListener rl = ReplicationListener.getInstance();
		ChunkSpaceMap csm = new ChunkSpaceMap ();
		ChunkManager cm = new ChunkManager (new LinkedBlockingQueue(), csm);
		FileChunkMetadata fcm = new FileChunkMetadata ();
		new Thread(s).start();
		new Thread(r).start();
		new Thread(rl).start();
	}
	public void run () {
		try {
			DatagramSocket s = new DatagramSocket (Replication.REPL_MAIN_LISTENER, NetMgr.getLocalIp());
			CD cd = CD.getInstance();
			while (!Thread.currentThread().isInterrupted()) {
				byte[] buffer = new byte[1024];
				DatagramPacket dpr = new DatagramPacket (buffer, buffer.length);
				NetMgr mgr = new NetMgr (29298);				
				Message m = mgr.receive();
				String fileName = m.toString();
				ArrayList<Integer> chunkIds = cd.getChunkIDForFile(fileName);
				byte[] sendbuf = new byte[2048];
				DatagramPacket dps = new DatagramPacket (chunkIds.toString());
				s.send(dps);
			}
		} catch (SocketException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}	
	}
	/**
	 * @param args
	 */
	//public static void main(String[] args) {

		/*HtbtBuddyMap hbm = new HtbtBuddyMap ();
		try {
			//hbm.put(new Host (InetAddress.getLocalHost(), Replication.HTBT_RCV_PORT_NUMBER), null);
			hbm.put(new Host (InetAddress.getByName("192.168.0.10"), Replication.HTBT_RCV_PORT_NUMBER), null);
			System.out.println(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		
		HtbtSender s = new HtbtSender (hbm);
		new Thread(s).start();
		
		HtbtReceiver r = new HtbtReceiver();
		new Thread(r).start();

		*/
		/*create the required data structures*/
		
	//}
}



