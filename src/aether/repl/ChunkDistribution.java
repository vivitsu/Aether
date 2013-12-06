package aether.repl;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import aether.io.Chunk;

/**
 * Maintains the metadata about chunk buddies.
 * We update the data structure when ever
 * a new file chunk is sent to a node and 
 * when ever a node fails 
 * */
class ChunkDistribution {
	Map<Host, ArrayList<String>> chunkDistribution;
	private static ChunkDistribution cd;
	public ChunkDistribution () {
		chunkDistribution = new ConcurrentHashMap<Host, ArrayList<String>> ();
	}
	 public static synchronized ChunkDistribution getInstance () {
		 if (cd == null) {
			 cd = new ChunkDistribution ();
		 }
		 return cd;
	 }
	 public synchronized void addChunk (Host h, String c) {
		 ArrayList<String>chunkList = chunkDistribution.get(h);
		 chunkList.add(c);
	 }
	 public synchronized ArrayList<String> getAllChunks (Host h) {
		 return chunkDistribution.get(h);
	 }
	 
}

