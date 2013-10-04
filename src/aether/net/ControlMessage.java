/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.net;

/**
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
    public ControlMessage (char subtype) {
        
        super ('c', subtype);
    }
    
    
    /**
     * Get the subtype of the control message
     * 
     * @return  Subtype of the control message
     */
    public char getMessageSubtype () {
        return header.getSubtype();
    }
    
    
}
