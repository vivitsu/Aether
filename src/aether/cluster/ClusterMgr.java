package aether.cluster;

import aether.conf.ConfigMgr;
import aether.net.ControlMessage;
import aether.net.Message;
import aether.net.NetMgr;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;


/**
 * Cluster manager manages the cluster related activities for this node. 
 * For example, joining the cluster, responding to discovery messages,
 * helping other nodes to join the cluster, updating the cluster member table
 * if someone leaves the cluster.
 * There can be only one cluster manager per node.
 * 
 * @author aniket oak
 */
public class ClusterMgr implements Runnable {
    
    private static boolean isOne = false;
    private ClusterTable clusterTable;
    private int clusterPort;
    private NetMgr comm;
    private int nodeId;
    
    
    /**
     * @throws UnsupportedOperationException if one clusterMgr is already
     *          running
     * @throws SocketException if clusterMgr fails to get hold of its socket
     */
    public ClusterMgr () throws UnsupportedOperationException, SocketException {
        
        if (isOne) {
            throw new UnsupportedOperationException();
        } else {
            clusterTable = new ClusterTable();
            init();
            isOne = true;
        }
    }

    
    
    /** Initialize the things clusterMgr will need
     * @throws SocketException 
     */
    private void init () throws SocketException {
        clusterPort = ConfigMgr.getClusterPort();
        comm = new NetMgr (clusterPort);
        comm.setTimeout(5000);
    }
    
    
    
    
    /**
     * Broadcast the discovery message in the cluster for the purpose of joining
     * the cluster
     * @throws IOException 
     */
    private void broadcastDiscovery () throws IOException {
        ControlMessage discovery = new ControlMessage ('d',
                NetMgr.getBroadcastAddr());
        comm.send(discovery);
    }
    
    
    
    
    private void processDiscoveryResponse (Message r) {
        /* Not implemented yet. Here we need to processes the discovery
         * response and initiate cluster joining mechanism.
         */
        
        int contactId = r.getSourceId();
        InetAddress contactIp = r.getSourceIp();
        
        /* Send the 'I-want-to-join' message to the contact node
         * Then wait for its response
         */
        ControlMessage d2 = new ControlMessage ('j', contactIp);
        comm.send(d2);
        
        /* The second response from the contact node will have your identifier
         * as well as the list of all the nodes in the cluster.
         */
        ControlMessage r2 = (ControlMessage) comm.receive();
        
        nodeId = r2.getDestId();
        ConfigMgr.setNodeId(nodeId);
        
        
    }
    
    

    
    
    
    
    
    
    
    
    /**
     * Initialize the cluster table assuming we are the first one up in the
     * cluster.
     */
    private void initTable () {
        
        nodeId = 1;
        ClusterTableRecord myRecord = new ClusterTableRecord (nodeId, 
                ConfigMgr.getLocalIp());
        clusterTable.insertRecord(myRecord);
        ConfigMgr.setNodeId(nodeId);
    }
    
    
    
    @Override
    public void run() {
        
        /* First thing we are supposed to do is to broadcast a discovery message
         * to the cluster.
         */
        
        try {
            broadcastDiscovery();
            Message discoveryResponse = comm.receive();
            
            
            /* If we are here, that means someone is already up and running, or
             * there was another discovery message broadcasted. This will be 
             * complicated. Will need to think through the solution to implement
             * simultanious braodcasts. For now, just implement the solution 
             * where no simultanious discovery broadcast happen
             */
            
            
            processDiscoveryResponse(discoveryResponse);
            
        } catch (SocketTimeoutException to) {
            /* Looks like I am the first one up. Initialize the table.
             */
            initTable();
        } catch (IOException ex) {
            System.err.println("[ERROR]: Cluster discovery failed");
            ex.printStackTrace();
        }
        
        
        /* We are here means our table is initialized and we are up and running.
         * Now keep on listening to the socket for any incoming messages
         */
        
    }
    
    
    
    
}
