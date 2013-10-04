/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.core;

import aether.conf.*;

/**
 *
 * @author aniket
 */
public class AetherNode {
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void main (String[] argv) {
        
        String configFile = (argv.length == 0) ? null : argv[0];
        ConfigMgr.initConfig(configFile);
    }
}
