
package aether.conf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 *
 * @author aniket
 */
public class ConfigMgr {
    
    
    private static HashMap<String,String> configDict = 
            new HashMap<String,String>();

    
    
    
    public ConfigMgr () {
        
    }
    
    
    /**
     * Initialize the configuration key value store by parsing the configuration
     * file.
     * @param file Name of the configuration file. Can be null for default
     * @return void. Exits if parsing fails.
     */
    public static void initConfig(String file) {
        /* We can do nothing till the time we parse the configuration file,
         * so do that first.
         */
        ConfigParser parser;
        
        if (file != null) {
            parser = new ConfigParser(file);
        } else {
            parser = new ConfigParser();
        }
        
        configDict = parser.populate();
        
        if (configDict == null) {
            System.exit(1);
        }
        
        
        /*
         * We need to put the local IP address on which we are running in config
         * dict so that anyone who needs it in the program can get that from 
         * here 
         */
        try {
            InetAddress myIp = InetAddress.getLocalHost();
            configDict.put("localIp", myIp.toString());
            
        } catch (UnknownHostException ex) {
            System.err.println("[ERROR]: ConfigMgr could not find local "
                    + "IP address");
            ex.printStackTrace();
        }
    }
    
    
    
    /**
     * Get the configuration parameter identifying the port on which the commMgr
     * receiver thread waits.
     * @return  Configuration parameter identifying socket for communication
     *          manager receiver
     */
    public static int getReceiverSock () {
        
        Integer port = Integer.parseInt(configDict.get("receiverSock"));
        return port.intValue();
    }
    
    
    
    
    
    /**
     * Get the configuration parameter identifying the port on which the commMgr
     * sender thread runs.
     * @return  Configuration parameter identifying socket for communication
     *          manager sender
     */
    public static int getSenderPort() {
        
        Integer port = Integer.parseInt(configDict.get("senderSock"));
        return port.intValue();
    }
    
    
    
    
    /**
     * Get the configuration parameter identifying the port on which the 
     * clusterMgr sends and receives messages.
     * @return  Configuration parameter identifying socket for cluster manager
     */
    public static int getClusterPort() {
        
        Integer port = Integer.parseInt(configDict.get("clusterPort"));
        return port.intValue();
    }
    
    
    
    
    
    /**
     * Get the name of the interface we used for connecting to the cluster
     * @return  String containing the name of the network interface
     */
    public static String getInterfaceName () {
        return configDict.get("interface");
    }
    
    
    
    
    /**
     * Get the configuration parameter local IP address.
     * @return  Local IP address
     */
    public static InetAddress getLocalIp () {
       
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(configDict.get("localIp"));
        } catch (UnknownHostException ex) {
            System.err.println("[ERROR]: Could not get local IP");
        }
        return ip;
    }
    
    
    /**
     * Set the identifier for this node. This should be called by clusterMgr
     * after node is accepted in the cluster
     * @param id    identifier of this node assigned by clusterMgr
     */
    public static void setNodeId (int id) {
        Integer i = id; 
        configDict.put("nodeId", i.toString());
    }
    
    
    /**
     * Get the identifier of this node.
     * @return  int having the identifier of this node in the cluster
     */
    public static int getNodeId () {
        
        String s = configDict.get("nodeId");
        if (s == null) {
            return 0;
        }
        Integer i = Integer.parseInt(s);
        return i.intValue();
                
    }
}
