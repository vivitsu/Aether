/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.net;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author aniket
 */
public class Header implements Serializable {
    
    private char messageType;       // data or control
    private int source;             // source identifier
    private int dest;               // destination identifier
    private char messageSubtype;    // for control messages
    private InetAddress sourceIp;   // IP address of the source
    private InetAddress destIp;     // IP address of the destination
    
    
    /**
     * Header of the message. Header holds the metadata required to parse the
     * message
     * 
     * @param type Type of the message. Can be (c)ontrol or (d)ata.
     */
    public Header (char type) {
        
        messageType = type;
        messageSubtype = 'i';       // invalid
        sourceIp = NetMgr.getLocalIp();
    }
    
    /**
     * Header of the message. Header holds the metadata required to parse the
     * message
     * @param type  Type of the message
     * @param subtype   Subtype of the message
     * @param source    Source identifier
     * @param destid    Destination identifier
     * @param dest      Destination IP address
     */  
    public Header (char type, char subtype, int source, int destId, 
            InetAddress dest) {
        
        messageType = type;
        messageSubtype = subtype;
        this.source = source;
        sourceIp = NetMgr.getLocalIp();
        this.dest = destId;
        destIp = dest;
    }
    
    
    /**
     * Get the message type for this header
     * @return  Message type
     */
    public char getType () {
        return messageType;
    }
    
    
    
    /**
     * Get the message subtype for this header
     * @return  Message subtype
     */
    public char getSubtype () {
        return messageSubtype;
    }
    
    
    
    /**
     * Get the IP address of the header source
     * @return  IP address of the source
     */
    public InetAddress getSourceIp () {
        return sourceIp;
    }
    
    
    
    /**
     * Get the IP address of header destination
     * @return  IP address of the destination
     */
    public InetAddress getDestIp () {
        return destIp;
    }
    
    
    
    /**
     * Set the IP address of the destination
     * @param ip    IP address of the header destination
     */
    public void setDestIp (InetAddress ip) {
        destIp = ip;
    }
    
    
    
    /**
     * Get the identifier of the destination node
     * @return  destination identifier
     */
    public int getDest () {
        return dest;
    }
    
    
    /**
     * Get the identifier of the source node
     * @return  source identifier
     */
    public int getSource () {
        return source;
    }
    

}
