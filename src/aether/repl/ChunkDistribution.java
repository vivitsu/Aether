package aether.repl;
import aether.io.*;
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

class CD {
	private static CD chunkDist;
	String[] fileNames;
	ChunkMetadata[] chunks;
	Host[] hosts;
	boolean[] deleted;
	int curPointer;
	private static int MAX_SIZE = 100;
	public static synchronized CD getInstance () {
		if (chunkDist == null)
			chunkDist = new CD ();
		return chunkDist;
	}
	public CD () {
		fileNames = new String[MAX_SIZE];
		chunks = new ChunkMetadata[MAX_SIZE];
		hosts = new Host[MAX_SIZE];
		curPointer = 0;
	}
	public synchronized void addChunk (String f, ChunkMetadata cm, Host h) {
		fileNames[curPointer] = f;
		chunks[curPointer] = cm;
		hosts[curPointer] = h;
	}
	public synchronized ArrayList<Integer> getChunkIDForFile (String f) {
		ArrayList<Integer> chunkIDs = new ArrayList<Integer>();
		for (int i = 0; fileNames[i] != null; i++) {
			if (fileNames[i].compareTo(f) == 0 && deleted[i] == false) {
				chunkIDs.add(chunks[i].getChunkId());
			}			
		}
		return chunkIDs;
	}
	public synchronized void removeChunk (String f, String cn) {
		for (int i = 0; fileNames[i] != null; i++) {
			if (fileNames[i].compareTo(f) == 0 
				&& chunks[i].getChunkName().compareTo(cn) == 0) {
					deleted[i] = true;
			}
		}
	}
	public synchronized void removeChunk (Host h) {
		for (int i = 0; fileNames[i] != null; i++) {
			if (h.getIPAddress() == hosts[i].getIPAddress())
				deleted[i] = true;
		}
	}
}
/*class  FileChunkInfo {
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
}*/

/**
 * This class stores the metadata about the chunks in 
 * a class. This object is further used in different places
 * instead of storing the entire chunk in the data
 * structure.
 * */
class ChunkMetadata  {
	String chunkName;
	String fileName;
	int chunkId;
	public ChunkMetadata(Chunk c) {
		chunkName = c.getChunkName();
		fileName = c.getFileName();
		chunkId = c.getChunkId();
	}
	public String getChunkName () {
		return chunkName;
	}
	public String getFileName () {
		return fileName;
	}
	public int getChunkId () {
		return chunkId;
	}
}