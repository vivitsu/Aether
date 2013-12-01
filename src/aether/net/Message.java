package aether.net;

import aether.conf.ConfigMgr;
import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author aniket
 */
public class Message implements Serializable {
    
    
    /* Every message will have a header, and optionally, a payload.
     * 
     */
    Header header;
    Payload payload;
    
    
    /**
     * This is the simple constructor where we only set the message type
     * The payload is null. This kind can be used for ping messages(?)
     * @param   messageType Type of the message
     */
    public Message (char messageType) {
        
        header = new Header(messageType);
        payload = null; // for now
    }
    
    
    
    /**
     * This type of constructor will have some payload. 
     * The subtype will be set to 'i' (invalid). If you need the subtype, better
     * use another constructor.
     * @param   messageType Type of the message
     * @param   data    Array of characters holding the data
     */
    
    public Message (char messageType, char[] data) {
        
        header = new Header(messageType);
        payload = new Payload (data);
    }
    
    
    /**
     * The third type of constructor which allows you to set a subtype along
     * with payload. This can be used for control message which need to have
     * some data.
     * @param   messageType Type of the message
     * @param   data    Array of characters holding the data
     * @param   subtype Subtype of the message (for control messages)
     */
    public Message (char messageType, char[] data, char subtype,
            InetAddress dest) {
        
        int sourceId = ConfigMgr.getNodeId();
        header = new Header(messageType, subtype, sourceId, 0, dest);
        payload = new Payload (data);
    }
    
    
    /** 
     * The forth type of constructor which is for control messages. You need
     * the message type as well as subtype.
     * @param   messageType Type of the message
     * @param   subtype Subtype of the message (for control messages)
     * @param   dest Destination IP address
     */
    public Message (char messageType, char subtype, InetAddress dest) {
        
        
        int sourceId = ConfigMgr.getNodeId();
        header = new Header(messageType, subtype, sourceId, 0, dest);
        payload = null;
    }
    
    
    
    
    /** 
     * The forth type of constructor which is for control messages. You need
     * the message type as well as subtype.
     * @param   messageType Type of the message
     * @param   subtype Subtype of the message (for control messages)
     * @param   destId Destination id
     * @param   dest Destination IP address
     */
    public Message (char messageType, char[] payload, char subtype, int destId, 
            InetAddress dest) {
        
        
        int sourceId = ConfigMgr.getNodeId();
        header = new Header(messageType, subtype, sourceId, destId, dest);
        this.payload = new Payload(payload);
    }
    
    
    
    /**
     * Create a message with messageType, subType, destination id, and 
     * destination IP address.
     * @param messageType
     * @param subtype
     * @param destId
     * @param dest 
     */
    public Message (char messageType, char subtype, int destId, 
            InetAddress dest) {
        
        int sourceId = ConfigMgr.getNodeId();
        header = new Header(messageType, subtype, sourceId, destId, dest);
        payload = null;
    }
    
    
    
    
    
    
    /**
     * Return the type of the message
     * @return  Message type character
     */
    public char getMessageType () {
        return header.getType();
    }
    
    
    
    
    /**
     * Get the IP address of the message source
     * @return  Source IP address
     */
    public InetAddress getSourceIp ()  {
        return header.getSourceIp();
    }
    
    
    
    
    /**
     * Set the IP address of the message destination
     * @param ipAddr    Destination IP address
     */
    public void setDestIp (InetAddress ipAddr) {
        header.setDestIp(ipAddr);
    }
    
    
    
    
    /**
     * Get the IP address of the message destination
     * @return  IP address of the message destination
     */
    public InetAddress getDestIp () {
        return header.getDestIp();
    }
    
    
    /**
     * Get the identifier of the destination node
     * @return  destination identifier
     */
    public int getDestId () {
        return header.getDest();
    }
    
    
    /**
     * Get the identifier of the source node
     * @return  source identifier
     */
    public int getSourceId () {
        return header.getSource();
    }
}
