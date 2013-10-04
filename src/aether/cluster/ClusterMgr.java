/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.cluster;

import aether.core.AetherNode;

/**
 *
 * @author aniket
 */
public class ClusterMgr implements Runnable {
    
    private int socketNum;
    private static boolean isOne = false;
    private ClusterTable clusterTable;
    
    
    /**
     * Cluster manager manages the cluster related activities for this node
     * There can be only one cluster manager per node.
     */
    public ClusterMgr () {
        
        if (isOne) {
            throw new UnsupportedOperationException();
        } else {
            socketNum = AetherNode.getServSocket();
            clusterTable = new ClusterTable();
            isOne = true;
        }
    }

    
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
}
