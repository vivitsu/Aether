
package aether.net;

import aether.cluster.ClusterTableRecord;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * Control messages are sent from one node in the cluster to the other. The
 * purpose of the control message is communicating the cluster management and 
 * other meta data between the nodes of the cluster. Control messages have 
 * different subtypes which signify different purpose of the control message.
 * Control message inherits from class Message which has header and payload.
 * 
 * An important field in header of the control message is its subtype which
 * can be one of the following:
 * 
 * 'd': Discovery message:
 *      This message is sent by a new node who wants to join the cluster. All
 *      the nodes in the cluster should respond to this message by discovery
 *      response message of type 'r'.
 * 'r': Discovery response message:
 *      This message is sent by each node in the cluster to the new node who
 *      sent a discovery message.
 * 'm': Membership request message:
 *      This message is sent by the new node wishing the membership of the 
 *      cluster to one of the nodes that sent it the discovery response 'r'.
 * 'p': Membership proposal message:
 *      The node in the cluster who receives the membership request 'm', sends 
 *      a membership proposal to all the nodes in the cluster. In the payload,
 *      it has the proposed id of the new node
 * 'a': Proposal response message:
 *      Other nodes in the cluster respond to the proposal with proposal 
 *      response message. In the payload, it has 'accept' or 'deny' reply.
 * 'c': Membership commit message:
 *      Once the contact node receives acceptance from all the nodes, it sends
 *      the membership commit message telling all the nodes to commit the  new
 *      node to their cluster tables.
 * 'j': Join message:
 *      After sending the commit message, the contact node sends a join message
 *      to the new node. In the payload, it has the list of nodes already 
 *      present in the cluster.
 * 'e': Read request
 *      Control message coming from the client requesting a particular file
 *      mentioned in the payload.
 * 'w': Write request
 *      Control message coming from the client for the write request. This 
 *      message should be responded with an acknowledgment, after which the
 *      file will be received from client. Payload will have the filename.
 * 'k': Acknowledgment message
 *      Control message for acknowledgment purpose. It might have a payload.
 * 'b': Chunk read request
 *      Control message from client requesting a particular chunk
 * 'l': Chunk Node list message
 *      List of nodes and the chunks they are holding
 * 
 * 
 * @author aniket
 */
public class ControlMessage extends Message {
    
    
    /** 
     * Control message is sent for management of the cluster and handling
     * the cluster administration.
     * Every control message must have a subtype.
     * 
     * @param subtype Subtype of the control message
     * @see Message
     */
    public ControlMessage (char subtype, InetAddress dest) {
        
        super ('c', subtype, dest);
    }
    
    
    
    public ControlMessage (char subtype, InetAddress dest, String payload) {
        
        super('c', payload, subtype, dest);
    }
    
    
    public ControlMessage (char subtype, InetAddress dest, int destId,
            String payload) {
        super('c', payload, subtype, destId, dest);
    }
    
    
    
    
    /**
     * Get the subtype of the control message
     * 
     * @return  Subtype of the control message
     */
    public char getMessageSubtype () {
        return header.getSubtype();
    }
    
    
    
    /**
     * Parse the control message with subtype 'j' to create an array of cluster
     * table records from the payload.
     * @return  Array of cluster table records
     * @throws UnknownHostException if we fail to create IP address from string
     * @throws UnsupportedOperationException If the message type is not 'j'
     */
    public ClusterTableRecord[] parseJControl () throws UnknownHostException,
            UnsupportedOperationException {
        
        if (header.getSubtype() != 'j') {
            throw new UnsupportedOperationException();
        }
        
        
        String data = payload.getData();
        
        String tableString = data;
        /* Separate the hashtable records by spliting on the record separator */
        String[] records = tableString.split(";");
        ClusterTableRecord[] recArray = new ClusterTableRecord[records.length];
        
        //for (String record: records) {
        for (int i=0; i < recArray.length; i++) {
            /* Split the record to separate key and values */
            String record = records[i];
            String[] tokens = record.split("%");
            int id = Integer.parseInt(tokens[0]);
            InetAddress ip = InetAddress.getByName(tokens[1]);
            ClusterTableRecord rec = new ClusterTableRecord (id, ip);
            recArray[i] = rec;
        }
        
        return recArray;
    }
    
    
    
    
    /**
     * Parse the control message with subtype 'a' to return the string in its 
     * payload
     * @return  Payload in string form
     */
    public String parseAControl () {
        
        return payload.getData();
    }
    
    
    
    /**
     * Parse the control message with subtype 'p' to return the cluster table
     * record for the new node
     * @return  Cluster table record for new node
     * @throws UnknownHostException
     */
    public ClusterTableRecord parsePControl () throws UnknownHostException {
        
        String newNodeRecString = payload.getData();
        String[] tokens = newNodeRecString.split("%");
        int id = Integer.parseInt(tokens[0]);
        InetAddress ip = InetAddress.getByName(tokens[1]);
        ClusterTableRecord rec = new ClusterTableRecord (id, ip);
        return rec;    
    }
    
    
    
    /**
     * Parse the control message with subtype 'c' to return the node id for
     * which commit confirm is received
     * @return  Integer new node id
     */
    public Integer parseCControl () {
        
        return Integer.parseInt(payload.getData());
    }
    
    
    /**
     * Parse the control message with subtype 'e' to return the file name in the
     * read request
     * @return  file name for the read request
     */
    public String ParseEControl () {
        
        if (payload == null) {
            return null;
        }
        return payload.getData();
    }
    
    
    
    /**
     * Parse the control message with subtype 'w' to return the file name that
     * is to be written.
     * @return  filename
     */
    public String parseWControl () {
        
        if (payload == null) {
            return null;
        }
        return payload.getData();
    }
    
    
    /**
     * Parse the control message with subtype 'b' to return the file name that
     * is to be written.
     * @return String having chunkId and the filename
     */
    public String parseBControl() {

        if (payload == null) {
            return null;
        }
        return payload.getData();
    }
    
    
    /**
     * Parse the control message with subtype 'k' to return the data in the  
     * payload. Parsing of the data is left to the caller
     * @return Ack payload
     */
    public String parseKControl() {

        if (payload == null) {
            return null;
        }
        return payload.getData();
    }
}
