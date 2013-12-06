package aether.repl;

import java.util.concurrent.LinkedBlockingQueue;


public class Replication {
	private static Replication repl;
	public static int HTBT_SND_PORT_NUMBER = 34444;
	public static int HTBT_RCV_PORT_NUMBER = 34445;
	public static int NUM_RETRIES = 4;
	public static int TIMEOUT_BEFORE_DEAD = 10000;	//milliseconds
	public static int REPL_PORT_LISTENER = 44444;	//port where all nodes listen for file chunk transfer
	public static synchronized Replication getInstance () {
		if (repl == null) {
			repl = new Replication ();
		}
		return repl;
	}
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
		/*create the required data structures*/
		HtbtBuddyMap hbm = new HtbtBuddyMap ();
		HtbtSender s = new HtbtSender (hbm);		
		HtbtReceiver r = new HtbtReceiver ();
		ReplicationListener rl = new ReplicationListener ();
		ChunkSpaceMap csm = new ChunkSpaceMap ();
		ChunkManager cm = new ChunkManager (new LinkedBlockingQueue(), csm);
		FileChunkMetadata fcm = new FileChunkMetadata ();
		new Thread(s).start();
		new Thread(r).start();
		new Thread(rl).start();

	}
}



