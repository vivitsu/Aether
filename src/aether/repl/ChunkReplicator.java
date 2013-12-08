package aether.repl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import aether.io.Chunk;
import aether.net.NetMgr;

/**
 * This runs as a separate thread. The thread performs 
 * the task of actual replication (sending
 * bytes over the wire) to the destination. A task is 
 * created and set as parameter to the executor 
 * framework instance that contains a pool of threads
 * which will execute the task as per the parameters
 * that have been set for the object.
 * */
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
		/*byte[] chunkData = new byte[chunk.getDataLength()];
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
		/*now we are done writing chunk to output stream
		
		os.close();
		//TODO add the metadata info to the data structure ChunkFileMetadata
		
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		int sourcePort = 44442;
		try {
			Socket s = new Socket (NetMgr.getLocalIp(), sourcePort);
			OutputStream os = s.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(chunk);
			oos.flush();
			oos.close();
			
			//add the dest node into the Chunk 
			//distribution data structure
			CD cd = CD.getInstance();
			cd.addChunk(chunk.getFileName(), new ChunkMetadata (null, sourcePort), new Host (destIP, destPort));
			
			//add the dest node as a chunk buddy 
			//to maintain heartbeat with that node
			HtbtBuddyMap hbm = HtbtBuddyMap.getInstance ();
			hbm.put(new Host (destIP, destPort), null);
			
		} catch (IOException e) {
			System.out.println("IOException at Chunk replicator");
			e.printStackTrace();
		}
	}
}
