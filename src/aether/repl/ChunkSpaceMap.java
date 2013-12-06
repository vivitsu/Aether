package aether.repl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.FileStore;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import aether.cluster.ClusterMgr;
import aether.conf.ConfigMgr;
import aether.net.ControlMessage;
import aether.net.Message;
import aether.net.NetMgr;

/**
 * Stores the information about how much 
 * memory is available at which node in the cluster.
 * 
 * */
class ChunkSpaceMap implements Runnable{
	ArrayList<NodeSpace> freeMemory;
	int replPort;
	NetMgr repl;
	private static final Logger repllog = Logger.getLogger(ClusterMgr.class.getName());
	/**
	 * Get the node address details in the cluster where 
	 * space is still available. The first fit algorithm 
	 * is used. If no such node exists in the cluster, 
	 * it throws an exception that should be handled by 
	 * the calling code by reporting it to the user.
	 * */
	
	private void init () throws SocketException {
		 	replPort = ConfigMgr.getReplFactor();
	        repl = new NetMgr (replPort);
	        repl.setTimeout(5000);
	        repllog.fine("Initialized ChunckSpaceMap");
	    }
		
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


	public void calculatefreeMemory()throws IOException {
		
		InetAddress bAddr = NetMgr.getBroadcastAddr();
		if (bAddr != null) {
		
			ControlMessage freespacesearch = new ControlMessage('f',bAddr);
			repl.send(freespacesearch);
			
		}	
		
	}
	

private void processMemorySpaceRequired(Message m) throws IOException
{
	long totalspace = 0;
	for (Path root : FileSystems.getDefault().getRootDirectories())
	{
	    try{
	       
	    	FileStore store = Files.getFileStore(root);
	        totalspace += store.getUsableSpace();
	        
	    }
	    catch (FileSystemException e)
	    {
	        repllog.warning("error querying space");
	    }
	}
	
	 try {
         
		 ControlMessage freespace = (ControlMessage) m;
         InetAddress newNodeIp = freespace.getSourceIp();
         ControlMessage space = new ControlMessage('s', newNodeIp,String.valueOf(totalspace));
         repllog.fine("Sending freespace response 's'");
         repl.send((Message)space);
         
     } catch (IOException ex) {
         repllog.warning("Could not send freespace");
     }
}


private void UpdateFreespace(Message m){
	
		ControlMessage freespace = (ControlMessage)m;
		InetAddress nodeInContext = freespace.getSourceIp();
		long spaceAvailable = Long.parseLong(freespace.parseAControl());
		this.put(m.getSourceIp(), Replication.REPL_PORT_LISTENER, spaceAvailable);		
		
}

private void processspacemessage(Message m) throws IOException{
	
	 repllog.fine("Processing space requirement message");
     ControlMessage ctrl = (ControlMessage) m;
     char ctrlType = ctrl.getMessageSubtype();
     
     
     switch (ctrlType) {
         
         case 'f': /*call for free memory space available  */
        	 		processMemorySpaceRequired(m);
                    break;
             
         case 's': /*updating the chunk space map */
        	 		UpdateFreespace(m);
                    break;
     }
	
}
	
	@Override
	public void run() {
		try {
			
			Message spacerequest = repl.receive();
			processspacemessage(spacerequest);
		} catch (IOException e) {
			
			repllog.warning("Did not receive the space Map messages");
		}
	
		
	}		
}
