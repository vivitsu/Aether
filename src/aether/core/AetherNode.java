/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.core;

import aether.net.*;
import java.util.HashMap;

/**
 *
 * @author aniket
 */
public class AetherNode {
    
    
    private static HashMap<String,String> configDict;
    
    
    
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
    
    
    public static void main (String[] argv) {
        
        String configFile = (argv.length == 0) ? null : argv[0];
        initConfig(configFile);
    }
}
