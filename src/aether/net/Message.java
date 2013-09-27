/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.net;

import java.io.Serializable;

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
    public Message (char messageType, char[] data, char subtype) {
        
        header = new Header(messageType, subtype);
        payload = new Payload (data);
    }
    
    
    /** 
     * The forth type of constructor which is for control messages. You need
     * the message type as well as subtype.
     * @param   messageType Type of the message
     * @param   subtype Subtype of the message (for control messages)
     */
    public Message (char messageType, char subtype) {
        
        header = new Header(messageType, subtype);
        payload = null;
    }
    
    
    
    
    
    /**
     * Return the type of the message
     * @return  Message type character
     */
    public char getMessageType () {
        return header.getType();
    }
}
