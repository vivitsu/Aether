
package aether.net;

import aether.cluster.ClusterTableRecord;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
        
        ClusterTableRecord[] recArray = new ClusterTableRecord[0];
        char[] data = payload.getData();
        
        String tableString = data.toString();
        /* Separate the hashtable records by spliting on the record separator */
        String[] records = tableString.split(";");
        
        for (String record: records) {
            /* Split the record to separate key and values */
            String[] tokens = record.split("%");
            int id = Integer.parseInt(tokens[0]);
            InetAddress ip = InetAddress.getByName(tokens[1]);
            ClusterTableRecord rec = new ClusterTableRecord (id, ip);
            recArray[recArray.length] = rec;
        }
        
        return recArray;
    }
}
