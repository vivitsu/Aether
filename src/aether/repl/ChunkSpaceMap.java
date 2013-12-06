package aether.repl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Stores the information about how much 
 * memory is available at which node in the cluster.
 * 
 * */
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
	/**
	 * adds the metadata into the data structure
	 * */
	public void put (InetAddress ipAddress, int port, long spaceAvailable) {
		freeMemory.add(new NodeSpace (ipAddress, port, spaceAvailable));
	}		
}

