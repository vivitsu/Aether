/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.io;

import aether.conf.ConfigMgr;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author aniket
 */
public class DataMgr implements Runnable {
    
    private static boolean isOne = false;
    private int dataPort;
    private boolean dbg;
    private static final Logger log = 
            Logger.getLogger(DataMgr.class.getName());
    private ServerSocket serv;
    ConcurrentHashMap<String, Integer> fileChunkMap;
    
    
    static {
        log.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        log.addHandler(handler);
    }
    
    
    
    
    /**
     * DataMgr listens on the port dataPort for connections from client
     * @throws UnsupportedOperationException
     * @throws IOException 
     */
    public DataMgr () throws UnsupportedOperationException, IOException {
    
        if (isOne) {
            log.warning("An instance of DataMgr is already running");
            throw new UnsupportedOperationException();
        } else {
            fileChunkMap = new ConcurrentHashMap<>();
            dbg = ConfigMgr.getIfDebug();
            dataPort = ConfigMgr.getDataPort();
            init();
            isOne = true;
        }
    }
    
    
    
    /**
     * Initialize the dataMgr. Basically, just create the server socket
     * @throws IOException 
     */
    private void init () throws IOException {
        
        serv = new ServerSocket(dataPort);
    }
    
    
    
    @Override
    public void run () {
        
        log.info("Starting the DataMgr");
        
        while (true) {
            
            try {
                Socket s;
                s = serv.accept();
                
                if (s == null) {
                    continue;
                } else {
                    
                    log.log(Level.FINE, "Starting a thread to communicate with"
                            + "the client on socket {0}", s);
                    ClientConnector cli = new ClientConnector (s, fileChunkMap);
                    cli.run();
                }
                
            } catch (IOException ex) {
                
                log.log(Level.WARNING, "IOException while attempting to " +
                        "connect to the client");
            }
            
            
        }
     }
}
