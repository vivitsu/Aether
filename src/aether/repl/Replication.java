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
class ChunkManager implements Runnable {
	BlockingQueue chunkQueue;
	Chunk[] chunkList;
	ChunkSpaceMap csm;
	public static Executor exec;
	public static final int THREADS_IN_REPL_POOL = 10;		// number of threads in the thread pool used by replicator
	public ChunkManager (BlockingQueue b, ChunkSpaceMap c) {
		csm = c;
		chunkQueue = b;
		exec = Executors.newFixedThreadPool(3);
	}
	
	public void run () {
		while (!Thread.currentThread().isInterrupted()) {
			Chunk c;
			try {
				c = (Chunk)chunkQueue.take();
				NodeSpace node = csm.getStorageNode(c.getDataLength());
				ChunkReplicator cr = new ChunkReplicator (c, node.getIPAddress(), node.getPort());
				
				//replicate the chunk using a thread from the executor pool
				exec.execute(cr);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSpaceAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public Object getNextChunk () {
		return chunkQueue.remove();
	}
	public void addChunk (Object o) {
		chunkQueue.add(o);
	}
	public void getChunkArray (Chunk[] chunkArr) {
		chunkList = chunkArr;
	}
	public void setupQueue () throws InterruptedException {
		try {
			for (int i = 0; i < chunkList.length; i++) {
				chunkQueue.put(chunkList[i]);
			}
		} catch (InterruptedException e) {
			throw e;
		}
	}
}
class ChunkReplicator implements Runnable {
	Chunk chunk;	
	InetAddress destIP;
	int destPort;
	
	public ChunkReplicator (Chunk c, InetAddress ia, int p) {
		chunk = c;
		destIP = ia;
		destPort = p;
	
	}
	public void run () {
		byte[] chunkData = new byte[chunk.getDataLength()];
		int sourcePort = 55555;										//TODO change
		try {
		Socket s = new Socket (NetMgr.getLocalIp(), sourcePort);				//TODO change the port number variable
		OutputStream os = s.getOutputStream();
		
		int offset = 0;
		int read = 0;
		int BUFFER_SIZE = 2048;
		int i = 1;
		
		//write the chunk in blocks of 2048 bytes
		//in the output stream
		while ((chunk.getDataLength() - offset) > BUFFER_SIZE) {
			os.write(chunkData, offset, offset + BUFFER_SIZE);
			offset += BUFFER_SIZE;
		}
		//the last block of the chunk could contain < 2048 bytes
		//so get the remaining bytes and write to stream
		int remaining = chunk.getDataLength() - offset;
		os.write(chunkData, offset, remaining);
		/*now we are done writing chunk to output stream*/
		
		os.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
/**
 * This exception is thrown when searching for a node
 * with available space to store the file chunk. If 
 * no node in the cluster has space for the file chunk,
 * this exception is thrown. 
 * */
class NoSpaceAvailableException extends Exception {
	public NoSpaceAvailableException () {
		
	}
	public NoSpaceAvailableException (String message) {
		super(message);
	}
	
}
class ChunkSpaceMap {
	ArrayList<NodeSpace> freeMemory;
	
	/**
	 * Get the node address details in the cluster where 
	 * space is still available. The first fit algorithm 
	 * is used. If no such node exists in the cluster, 
	 * it throws an exception that should be handled by 
	 * the calling code by reporting it to the user.
	 * */
	public NodeSpace getStorageNode (long spaceRequired) throws NoSpaceAvailableException {
		
		//iterate through the list and use the  
		//first node where space is available.
		for (Iterator<NodeSpace> iter = freeMemory.iterator(); iter.hasNext() != false; ) {
			NodeSpace ns = iter.next();
			if (ns.getAvailableSpace() > spaceRequired) {
				return ns;
			}
		}
		throw new NoSpaceAvailableException ();
	}
	public void put (InetAddress ipAddress, int port, long spaceAvailable) {
		freeMemory.add(new NodeSpace (ipAddress, port, spaceAvailable));
	}	
	
}

/**
 * Data structure to store the information about 
 * how much space is available on each node of the cluster.
 * Store the node information in form of IP address,
 * port number and available space at the node
 * */
class NodeSpace {
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
class HtbtBuddyMap implements Runnable {
	Map<Host, HostDetails> clusterNodes;
	public HtbtBuddyMap () {
		clusterNodes = new HashMap<Host, HostDetails> ();
	}
	public Set<Map.Entry<Host, HostDetails>> getBuddyMapSet () {
		return clusterNodes.entrySet();
	}
	public void put (Host h, HostDetails hd) {
		clusterNodes.put(h, hd);
	}
	public HostDetails get (Host h) {
		return clusterNodes.get(h);
	}
	public void run() {
		while (true) {
			long difference;
			for (Entry<Host, HostDetails> entry : clusterNodes.entrySet()) {
				synchronized (entry) {
					difference = System.currentTimeMillis() - entry.getValue().getTime();
					if ((difference / 1000) > Replication.TIMEOUT_BEFORE_DEAD) {
						//Replace with logic of what to do when dead
						System.out.println("Port " + entry.getKey() + " dead");
					}
				}
			}
			try {
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void saveTime (Host h, HostDetails hd) {
		HostDetails oldDetail;
		if ( clusterNodes.get(h) != null ) {
			oldDetail = clusterNodes.get(h);
			System.out.println("Saving port + time: " + " " + (hd.getTime()- oldDetail.getTime()));
		}
		clusterNodes.put(h, hd);
	}
	
}
class HtbtSender implements Runnable {
	HtbtBuddyMap buddymap;
	public HtbtSender (HtbtBuddyMap hbm) {
		buddymap = hbm;
	}
	public void setBuddyMap (HtbtBuddyMap hbm) {
		buddymap = hbm;
	}
	public void run () {
		DatagramSocket ds;
		try {
			ds = new DatagramSocket(Replication.HTBT_SND_PORT_NUMBER);
			byte[] send = new byte[256];
			while (true) {

				for (Entry<Host, HostDetails> entry : buddymap.getBuddyMapSet()) {

					String mess = "from Sender";// + Integer.toString(port);
					send = mess.getBytes("UTF-8");
					DatagramPacket dps = new DatagramPacket(send, send.length,
							entry.getKey().getIPAddress(), entry.getKey().getPort());
					System.out.println(entry.getKey().getIPAddress());
					ds.send(dps);
				}
				System.out.println("Sent from sender");
				Thread.currentThread().sleep(5000);
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
}
class HtbtReceiver implements Runnable {
	HtbtBuddyMap buddyMap;
	public HtbtReceiver () {
		buddyMap = new HtbtBuddyMap();
		new Thread(buddyMap).start();
	}
	public void run () {
		try {
			DatagramSocket ds = new DatagramSocket(Replication.HTBT_RCV_PORT_NUMBER);				
			byte[] receive = new byte[256];
			DatagramPacket dpr = new DatagramPacket(receive, receive.length);
			while (!Thread.currentThread().isInterrupted()) {
				ds.receive(dpr);
				buddyMap.saveTime (new Host(dpr.getAddress(),dpr.getPort()), new HostDetails (System.currentTimeMillis()));
				String message = new String(receive, 0, dpr.getLength(),
						"UTF-8");
				System.out.println("S : got " + message);
			}			

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.out.println("Retrying ");
			//continue;
		}
	}
}
class ChunkBuddyList {
	 ArrayList<InetAddress> list;
	 public ArrayList getList () {
		 return list;
	 }
	 public void addNode (InetAddress ia) {
		 list.add(ia);
	 }
}
class HostDetails {
	long time;	
	public HostDetails (long t) {
		time = t;
	}
	public void setTime (long t) {
		time = t;
	}
	public long  getTime () {
		return time; 
	}	
}
class Host {
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
