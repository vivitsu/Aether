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
	Map<String, ArrayList<Chunk>> fileChunkMap;
	public FileChunkMetadata () {
		fileChunkMap = new HashMap<String, ArrayList<Chunk>> ();
	}
	
	public void addChunk (String f, Chunk c) {
		ArrayList<Chunk> chunkList = fileChunkMap.get(f);
		chunkList.add(c);
	}
	
	public void removeChunk (String f,Chunk c) {
		ArrayList<Chunk> chunkList = fileChunkMap.get(f);
		chunkList.remove(c);
	}
	
	public void removeFile (String f) {		
		fileChunkMap.remove(f);
	}	
	public boolean doesChunkExists (String f) {
		return fileChunkMap.containsKey(f);
	}
}
