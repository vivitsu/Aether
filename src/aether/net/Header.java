/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.net;

/**
 *
 * @author aniket
 */
public class Header {
    
    private char messageType;       // data or control
    private int source;             // source identifier
    private int dest;               // destination identifier
    private char messageSubtype;    // for control messages
    
    
    /**
     * Header of the message. Header holds the metadata required to parse the
     * message
     * 
     * @param type Type of the message. Can be (c)ontrol or (d)ata.
     */
    public Header (char type) {
        
        messageType = type;
        messageSubtype = 'i';       // invalid
    }
    
    /**
     * Header of the message. Header holds the metadata required to parse the
     * message
     * 
     * @param type Type of the message. Can be (c)ontrol or (d)ata.
     * @param subtype Subtype of the message. This is applicable only for 
     *                control messages
     */    
    public Header (char type, char subtype) {
        
        messageType = type;
        messageSubtype = subtype;
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
}
