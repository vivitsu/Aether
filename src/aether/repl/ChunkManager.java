package aether.repl;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import aether.io.Chunk;

/**
 * This is the main class that performs the tasks as follows:
 * 1. get the next chunk to be replicated from the queue
 * 2. send a message over the network to get the available
 * 		space at each node
 * 3. select a node on which the chunk is to be replicated
 * 4. create a task for replicating the chunk and execute it
 * 5. send a confirmation message once the chunk has been 
 * 		replicated 
 * */
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
				//calculatefreeMemory();
				//remove from the queue
				c = (Chunk)chunkQueue.take();
				csm.calculatefreeMemory(); 					//call for free memory check
				NodeSpace node = csm.getStorageNode(c.getDataLength());
				ChunkReplicator cr = new ChunkReplicator (c, node.getIPAddress(), node.getPort());
				
				//TODO add the dest node as a chunk buddy 
				//to maintain heartbeat with that node
				
				//replicate the chunk using a thread from the executor pool
				exec.execute(cr);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSpaceAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * gets the next chunk from the queue of file chunks
	 * */
	public Object getNextChunk () {
		return chunkQueue.remove();
	}
	/**
	 * adds a chunk to the queue
	 * */
	public void addChunk (Object o) {
		chunkQueue.add(o);
	}
	/**
	 * gets the chunk array from 
	 * the client component process
	 * */
	public void getChunkArray (Chunk[] chunkArr) {
		chunkList = chunkArr;
	}
	/**
	 * sets up the blocking queue of chunks 
	 * by importing chunks from the array into the queue 
	 * */
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
