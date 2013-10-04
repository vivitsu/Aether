/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.cluster;

import java.net.InetAddress;

/**
 *
 * @author aniket
 */
public class ClusterTableRecord {
    
    
    private int nodeIdentifier;
    private InetAddress nodeIp;
    
    /**
     * ClusterTableRecord is an entry in the cluster table storing information
     * about a node in the cluster.
     * 
     * @param id    node identifier integer.
     * @param ip    IP address of the node
     */
    public ClusterTableRecord (int id, InetAddress ip) {
        
        nodeIdentifier = id;
        nodeIp = ip;
    }
    
    
    /**
     * Get the identifier for the node in this record.
     * @return Node identifier (int)
     */
    public int getNodeId () {
        
        return nodeIdentifier;
    }
    
    /**
     * Get the IP address of the node in this record
     * @return Node IP address
     */
    public InetAddress getNodeIp () {
        
        return nodeIp;
    }
    
    
    
}
