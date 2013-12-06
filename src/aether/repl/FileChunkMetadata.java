package aether.repl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aether.io.Chunk;

/**
 * Stores the metadata of what chunks of which file 
 * are stored at the current node in the cluster. 
 * This is called when we get confirmation from 
 * destination that the chunk has been received.
 * */
class FileChunkMetadata {
	private static FileChunkMetadata fcm;
	public static synchronized FileChunkMetadata getInstance () {
		if (fcm == null) {
			fcm = new FileChunkMetadata ();
		}
		return fcm;
	}
	Map<String, ArrayList<Chunk>> fileChunkMap;
	public  FileChunkMetadata () {
		fileChunkMap = new HashMap<String, ArrayList<Chunk>> ();
	}
	
	public synchronized void addChunk (String f, Chunk c) {
		ArrayList<Chunk> chunkList = fileChunkMap.get(f);
		chunkList.add(c);
	}
	
	public synchronized void removeChunk (String f,Chunk c) {
		ArrayList<Chunk> chunkList = fileChunkMap.get(f);
		chunkList.remove(c);
	}
	
	public synchronized void removeFile (String f) {		
		fileChunkMap.remove(f);
	}	
	public synchronized boolean doesChunkExists (String f) {
		return fileChunkMap.containsKey(f);
	}
}
