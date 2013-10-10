package aether.cluster;

import aether.conf.ConfigMgr;
import aether.net.ControlMessage;
import aether.net.Message;
import aether.net.NetMgr;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;


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
     * Print cluster table in user readable format.
     */
    private void printClusterView () {
        clusterTable.printTable();
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
    
    
    
    /**
     * Process the control message of subtype 'r' indicating reception of a 
     * response to discovery message
     * @param r     Discovery response message
     */
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
        try {        
            comm.send(d2);
        } catch (IOException ex) {
            System.err.println("[ERROR]: Sending the 'join' message failed");
            ex.printStackTrace();
        }
    }
    
    

    
    /**
     * Process the control message of subtype 'j' indicating we are included
     * in the cluster.
     * @param j     Control message of subtype 'j'
     * @throws IllegalStateException when parsing the cluster table records 
     *      fails because either IP address could not be found or wrong message
     *      was parsed for the records. ClusterMgr cannot continue with bad
     *      cluster table.
     */
    private void processJoinMessage (Message j) throws IllegalStateException {
        ControlMessage join = (ControlMessage) j;
        nodeId = join.getDestId();
        ConfigMgr.setNodeId(nodeId);
        
        
        /* Now we need to update our cluster table by parsing the data in
         * the payload of the control message.
         */
        ClusterTableRecord[] recs;
        try {
            recs = join.parseJControl();
        } catch (UnknownHostException ex) {
            System.err.println("[ERROR]: Could not parse string to create IP");
            throw new IllegalStateException();
        } catch (UnsupportedOperationException ex) {
            System.err.println("[ERROR]: Tried parsing non-join message for"
                    + " cluster table records");
            throw new IllegalStateException();
        }
        
        for (ClusterTableRecord rec:recs) {
            clusterTable.insertRecord(rec);
        }
        
        printClusterView();
    }
    
    
    
    
    
    
    /**
     * Process the discovery message by some node.
     * @param d     Discovery message by the new node.
     */
    private void processDiscovery (Message d) {
        try {
            ControlMessage disc = (ControlMessage) d;
            InetAddress newNodeIp = disc.getSourceIp();
            ControlMessage r = new ControlMessage('r', newNodeIp);
            comm.send((Message)r);
        } catch (IOException ex) {
            System.err.println("[ERROR]: Could not respond to discovery "
                    + "message");
        }
    }
    
    
    
    
    
    /**
     * This is a method which takes a message and calls an appropriate method
     * to handle that kind of message.
     * @param m     Message to be processed
     * @throws IllegalStateException when parsing of critical control message 
     *      fails
     */
    private void processMessage (Message m) throws IllegalStateException {
        
        ControlMessage ctrl = (ControlMessage) m;
        char ctrlType = ctrl.getMessageSubtype();
        
        switch (ctrlType) {
            
            case 'r': /* discovery response */
                        processDiscoveryResponse(m);
                
            case 'm': /* cluster membership message */
                        processJoinMessage(m);
                
            case 'd': /* Someone wants to join the cluster */
                        processDiscovery(m);
            
            case 'j': /* We need to act as a contact node for someone */
        }
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
            
            
            processMessage (discoveryResponse);
            
        } catch (SocketTimeoutException to) {
            /* Looks like I am the first one up. Initialize the table.
             */
            initTable();
        } catch (IOException ex) {
            System.err.println("[ERROR]: Cluster discovery failed");
            ex.printStackTrace();
        } catch (IllegalStateException badState) {
            System.err.println("[ERROR]: Cluster manager exiting due to "
                    + "inconsistent state");
            return;
        }
        
        
        /* We are here means our table is initialized and we are up and running.
         * Now keep on listening to the socket for any incoming messages
         */
        
        while (true) {
            Message m;
            try {
                m = comm.receive();
                processMessage (m);
            } catch (SocketTimeoutException to) {
                // nothing needs to be done here
            } catch (IOException ex) {
                System.err.println("[ERROR]: could not listen on the socket");
                return;
            }
            
        }
    }
    
    
    
    
}
