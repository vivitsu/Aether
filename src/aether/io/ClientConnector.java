/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.io;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author aniket
 */
public class ClientConnector implements Runnable {

  
    
    private Socket sock;
    private ConcurrentHashMap<String, Integer> map;
    
    
    
    public ClientConnector (Socket s, ConcurrentHashMap<String, Integer> m) {
        
        sock = s;
        map = m;
    }
    
    
    
    @Override
    public void run() {
        
        /* Write the code to communicate with the client here
         * 
         */
        
        
        return;
    }
    
}
