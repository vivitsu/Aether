/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.net;

/**
 *
 * @author aniket
 */
public class Payload {
    
    private char[] data;
    
    
    /**
     * Payload of the message. This can be used to hold payload of character
     * array. Payload can hold actual data in data message or can hold the
     * metadata to be transfered in the control message.
     * 
     * @param data  Array of characters holding the payload to be transfered.
     */
    public Payload (char[] data) {
        
        this.data = data.clone();
    }
    
    /**
     * Get the data in the payload
     * @return  Data in the payload
     */
    public char[] getData () {
        return data;
    }
}
