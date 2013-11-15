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
import java.util.HashMap;
import java.util.LinkedList;


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
    private int nodeIdCounter;
    private HashMap<Integer,ClusterTableRecord> tempRecs = new HashMap<>();
    
    
    /**
     * @throws UnsupportedOperationException if one clusterMgr is already
     *          running
     * @throws SocketException if clusterMgr fails to get hold of its socket
     */
    public ClusterMgr () throws UnsupportedOperationException, SocketException {
        
        if (isOne) {
            throw new UnsupportedOperationException();
        } else {
            nodeIdCounter = 0;
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
        ControlMessage d2 = new ControlMessage ('m', contactIp);
        try {        
            comm.send(d2);
        } catch (IOException ex) {
            System.err.println("[ERROR]: Sending the 'membership request' "
                    + "message failed");
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
     * Process join request response from the other nodes in the cluster.
     * @param m Control message- reply to the join request from contact node
     * @return  true if reply is 'accept'
     */
    private boolean processJoinResponseMessage (Message m) {
        
        ControlMessage reply = (ControlMessage) m;
        if (reply == null || reply.getMessageSubtype() != 'a') {
            return false;
        }
        if (reply.parseAControl().equalsIgnoreCase("accept")) {
            return true;
        }
        return false;
    }
    
    
    
    
    
    
    
    
    /**
     * Process the membership request from a new node and respond to it with
     * a join message if successful
     * @param d     Membership request message
     */
    private void processMembershipRequest (Message d) {
  
        ControlMessage mReq = (ControlMessage) d;
        // find total nodes in the cluster
        int numNodes = clusterTable.getNumRecords() - 1;
        int numAttempts = ConfigMgr.getNumJoinAttempts();
        
        boolean success = false;
        ClusterTableRecord tempNodeRec = null;
        Integer tempNodeId = null;
        
        try {

            while (numAttempts > 0 && success == false) {

                tempNodeId = ++nodeIdCounter;
                
                tempNodeRec = new ClusterTableRecord (
                        tempNodeId, mReq.getSourceIp());
                
                char[] payload = tempNodeRec.toDelimitedString().toCharArray();

                ControlMessage joinInfo = new ControlMessage('p',
                        NetMgr.getBroadcastAddr(), payload);

                comm.send((Message) joinInfo);

                int replyCount = 0;
                LinkedList<Message> replyList = new LinkedList<>();

                for (int i = 0; i < numNodes; i++) {

                    try {
                        Message reply = comm.receive();
                        if (processJoinResponseMessage(reply)) {
                            replyList.add(reply);
                        }
                        
                    } catch (SocketTimeoutException soe) {
                        // nothing to do here. This means we missed a reply
                    }

                    if (replyList.size() == numNodes) {
                        success = true;
                    }
                }

                numAttempts--;
            }

            if (success) {
                /* First we need to tell all the nodes that they can commit
                 * the new node in the cluster table
                 */
                if (tempNodeRec != null && tempNodeId != null) {
                    
                    char[] load = tempNodeId.toString().toCharArray();
                    ControlMessage commit = new ControlMessage('c',
                        NetMgr.getBroadcastAddr(), load);
                    comm.send(commit);
                    clusterTable.insertRecord(tempNodeRec);
                }
                
                
                
                /* Now we need to tell the new node that it has been admitted
                 * in the cluster
                 */
                char[] joinPayload = prepareJoinMessagePayload();
                ControlMessage joinMessage = new ControlMessage('j',
                        mReq.getSourceIp(), joinPayload);
                comm.send(joinMessage);
            }
            
        } catch (IOException ioe) {
            System.err.println("[ERROR]: Could not process membership "
                    + "request");
            ioe.printStackTrace();
        }
        

    }
    
    
    
    
    
    
    /**
     * Process the membership proposal from a node in the cluster
     * @param m     Membership proposal control message
     */
    private void processMembershipProposal (Message m) {
        
        ControlMessage p = (ControlMessage) m;
        String response;
        ClusterTableRecord tempRec = null;
        
        try {
            tempRec = p.parsePControl();
            if (tempRec == null) {
                response = "deny";
            } else if (clusterTable.exists(tempRec.getNodeId())) {
                response = "deny";
            } else {
                response = "accept";
            }
            
        } catch (UnknownHostException e) {
            response = "deny";
        }
        
        ControlMessage reply = new ControlMessage ('a', p.getSourceIp(),
                response.toCharArray());
        
        try {
            comm.send(reply);
        } catch (IOException e) {
            // do nothing. The contact node will timeout and try again.
            return;
        }
        
        if (tempRec != null && response.equalsIgnoreCase("accept")) {
            tempRecs.put(tempRec.getNodeId(), tempRec);
        }
        
    }
    
    
    
    
    
    /**
     * Process the membership commit message
     * @param m     Membership commit control message
     */
    private void processMembershipCommit (Message m) {
        
        ControlMessage c = (ControlMessage) m;
        Integer newNodeId = c.parseCControl();
        
        ClusterTableRecord toInsert = tempRecs.get(newNodeId);
        if (toInsert == null) {
            // something went wrong
            System.err.println("[ERROR]: Could not find matching temp record"
                    + " for the commit message. tempNodeId: " + newNodeId);
        } else {
            clusterTable.insertRecord(toInsert);
            tempRecs.remove(newNodeId);
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
                        break;
                
            case 'j': /* cluster membership message */
                        processJoinMessage(m);
                        break;
                
            case 'd': /* Someone wants to join the cluster */
                        processDiscovery(m);
                        break;
            
            case 'm': /* We need to act as a contact node for someone */
                        processMembershipRequest(m);
                        break;
            
            case 'a': /* This should not be called from here */
                        break;
            
            case 'p': /* A membership proposal was received */
                        processMembershipProposal(m);
                        break;
           
            case 'c': /* A membership commit */
                        processMembershipCommit(m);
                        break;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Prepare the payload for join message having all the records in the 
     * cluster table in character form.
     * @return  char array having all the records in cluster table
     */
    private char[] prepareJoinMessagePayload () {
        
        ClusterTableRecord[] tableRecs = clusterTable.getAllRecords();
        String[] recStrings = new String[tableRecs.length];
        
        int i=0;
        for (ClusterTableRecord r: tableRecs) {
            recStrings[i++] = r.toDelimitedString();
        }
        
        String s = "";
        for (String recStr: recStrings) {
            s = recStr + ";" + s;
        }
        
        return s.toCharArray();
    }
    
    
    
    
    /**
     * Initialize the cluster table assuming we are the first one up in the
     * cluster.
     */
    private void initTable () {
        
        nodeId = ++nodeIdCounter;
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
