/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.conf;

import java.util.HashMap;

/**
 *
 * @author aniket
 */
public class ConfigMgr {
    
    
    private static HashMap<String,String> configDict;
    
    
    public ConfigMgr () {
        configDict = new HashMap<String,String>();
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
    }
    
    
    
    /**
     * Get the configuration parameter identifying the port on which the cluster
     * management thread of the data node runs.
     * @return  Configuration parameter identifying socket for cluster manager
     */
    public static int getServSocket () {
        
        Integer port = Integer.parseInt(configDict.get("servSock"));
        return port.intValue();
    }
}
