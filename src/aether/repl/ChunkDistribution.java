package aether.repl;

import java.util.ArrayList;
import java.util.Iterator;
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
	Map<FileChunkInfo, Host> hostFileChunks;
	private static ChunkDistribution cd;
	public ChunkDistribution () {
	}
	 public static synchronized ChunkDistribution getInstance () {
		 if (cd == null) {
			 cd = new ChunkDistribution ();
		 }
		 return cd;
	}
	public void addFileChunkInfo (Host h, String f, ChunkMetadata c) {
		
	}
	public ArrayList<Integer> getChunkIDs (String f) {
		 
	}
}
class  FileChunkInfo {
	String fileName;
	ArrayList<ChunkMetadata> chunkList;
	public FileChunkInfo (String f) {
		fileName = f;
		chunkList = new ArrayList<ChunkMetadata> ();
	}
	public void addChunk (ChunkMetadata cm) {
		chunkList.add(cm);
	}
	public ArrayList<ChunkMetadata> getChunkListInstance () {
		if (chunkList == null) {
			chunkList = new ArrayList<ChunkMetadata> ();
		}
		return chunkList;
	}
	public ArrayList<Integer> getChunkIDs (String f) {
		ArrayList<Integer> chunkID = new ArrayList<Integer> ();
		boolean flag = false;
		for (Iterator<ChunkMetadata> iter = chunkList.iterator(); iter.hasNext();) {
			ChunkMetadata cm = iter.next();
			if (cm.getFileName().compareTo(f) == 0) {
				chunkID.add(cm.getChunkId());
				flag = true;
			}
		}
		
		if (flag == true) {
			return null;
		}
		else return chunkID;
	}
}
class ChunkMetadata extends Chunk {
	public ChunkMetadata () {
		
	}
}