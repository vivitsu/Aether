/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.core;

import aether.cluster.ClusterMgr;
import aether.conf.ConfigMgr;
import aether.io.DataMgr;
import aether.repl.Replication;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author aniket
 */
public class AetherNode {
    
    
    private static boolean clusterUp = false;
    private static boolean dataUp = false;
    private static final Logger log = 
            Logger.getLogger(AetherNode.class.getName());
    
    
    
    static {
        log.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        log.addHandler(handler);
    }
    
    
    
    /**
     * Method to indicate that the cluster manager is up and running
     */
    public static void setClusterRunning () {
        AetherNode.clusterUp = true;
    }
    
    
    /**
     * Method to indicate that the data manager is up and running
     */
    public static void setDataRunning () {
        AetherNode.dataUp = true;
    }
    
    
    
    
    
    
    
    public static void main (String[] argv) {
        
        String configFile = (argv.length == 0) ? null : argv[0];
        log.info("Starting the AetherNode");
        ConfigMgr.initConfig(configFile);
        
        
        if (ConfigMgr.getIfDebug()) {
            log.setLevel(Level.FINE);
        }
            
        try {
            ClusterMgr clusterManager = ClusterMgr.getInstance();
            DataMgr dataManager = new DataMgr();
            Thread clusterMgrThread = new Thread(clusterManager);
            Thread dataMgrThread = new Thread(dataManager);
            
            log.fine("Launching cluster manager");
            clusterMgrThread.start();
            
            while (true) {
                /* Wait for cluster manager to come up. We can then procceed
                 * to start data manager
                 */

                try {
                    if (clusterUp) {
                        log.fine("Launching data manager");
                        dataMgrThread.start();
                        break;
                    } else {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException ex) {
                    // nothing to do here
                }
            }
            
            Replication repl = Replication.getInstance();
            
        } catch (UnsupportedOperationException ex) {
            // This means cluster manager is already running. No worries then.
        } catch (SocketException ex) {
            log.severe("Could not start cluster manager");
        } catch (IOException ex) {
            log.severe("Could not start data manager");
        }
    }
}
