
package aether.net;

import java.net.InetAddress;

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
    public ControlMessage (char subtype, InetAddress dest) {
        
        super ('c', subtype, dest);
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
