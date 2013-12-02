/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.net;

/**
 *
 * @author aniket
 */
public class DataMessage extends Message {
    
    
    /**
     * Data messages are sent with the data payload for transfering the
     * data from client to nodes or nodes to nodes
     * 
     * @param data Array of characters holding the data to be transfered
     * @see Message
     */
    public DataMessage (String data) {
        
        super('d', data);       
    }
    
    
    /**
     * Get the data in the data message
     * 
     * @return  The array of characters holding the data
     */
    public String getDataPayload () {
        return payload.getData();
    }
}
