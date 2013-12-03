/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.core;

import aether.cluster.ClusterMgr;
import aether.conf.ConfigMgr;
import java.net.SocketException;

/**
 *
 * @author aniket
 */
public class AetherNode {
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void main (String[] argv) {
        
        String configFile = (argv.length == 0) ? null : argv[0];
        ConfigMgr.initConfig(configFile);
            
        try {
            ClusterMgr clusterManager = ClusterMgr.getInstance();
            Thread clusterMgrThread = new Thread(clusterManager);
            clusterMgrThread.start();
            
        } catch (UnsupportedOperationException ex) {
            // This means cluster manager is already running. No worries then.
        } catch (SocketException ex) {
            System.err.println("[ERROR]: Could not start cluster manager");
        }
    }
}
