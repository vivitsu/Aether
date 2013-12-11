/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.conf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author aniket
 */
public class ConfigParser {
    
    private String configFilename;
    
    /**
     * The configuration parser parses the configuration file and populates
     * the store of key and value store.
     * 
     * @param   filename    Name of the configuration file.
     */
    public ConfigParser (String filename) {
        this.configFilename = filename;
    }
    
    
    /**
     * The configuration parser parses the configuration file and populates
     * the store of key and value store.
     */
    public ConfigParser () {
        this.configFilename = "aether.conf";
    }
    
    
    /**
     * Parse the configuration file and populate a key value store.
     * @return  Returns HashMap of keys and values on successful parsing
     *          null on failure
     */
    public HashMap<String,String> populate () {
        
        HashMap<String,String> configDict = new HashMap<>();
        String line;
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(configFilename));
            
            /* This is probably the crudest way to parse a configuration file.
             * I am assuming here that all the lines have only two columns,
             * first column is a key string and second is value string. Should
             * improve it later to check for valid keys and check for valid 
             * values.
             */
            while ( (line = in.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                configDict.put(tokens[0], tokens[1]);
            }
            
            in.close();
            
            
        } catch (IOException e) {
            /* Probably we will need to log this somewhere instead of just
             * putting it on stdout or stderr.
             */
            System.err.println("[ERROR]: Error while parsing the configuration file");
            e.printStackTrace();
            configDict = null;
        }
        
        return configDict;
    }
}
