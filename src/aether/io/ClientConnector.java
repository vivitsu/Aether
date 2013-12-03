/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.io;

import aether.cluster.ClusterMgr;
import aether.conf.ConfigMgr;
import aether.net.ControlMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * Client Connector threads handle the communication with the client component.
 * For a read request, this thread will return a list of nodes having the 
 * chunks to the client through a ControlMessage, and receive the file for a 
 * write request
 * @author aniket
 */
public class ClientConnector implements Runnable {

  
    
    private Socket sock;
    private ConcurrentHashMap<String, Integer> map;
    private static final int SOCKET_SO_TIMEOUT = 5000;
    private static final Logger log = 
            Logger.getLogger(DataMgr.class.getName());
    
    static {
        log.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        log.addHandler(handler);
    }
    
    
    
    public ClientConnector (Socket s, ConcurrentHashMap<String, Integer> m) {
        
        sock = s;
        map = m;
        if (ConfigMgr.getIfDebug()) {
            log.setLevel(Level.FINE);
        }
    }
    
    
    
    @Override
    public void run() {
        
        /* Write the code to communicate with the client here
         * 
         */
        
        InputStream in;
        OutputStream out;
        ObjectInputStream oIn;
        ObjectOutputStream oOut;
        
        try {
            
            sock.setSoTimeout(SOCKET_SO_TIMEOUT);
            out = sock.getOutputStream();
            oOut = new ObjectOutputStream(out);
            oOut.flush();
            
            in = sock.getInputStream();
            oIn = new ObjectInputStream(in);
            
        } catch (IOException ie) {
            
            log.log(Level.WARNING, "I/O Exception on socket {0}", sock);
            ie.printStackTrace();
            return;
        }
        
        
        
        while (true) {

            try {
                ControlMessage first = (ControlMessage) oIn.readObject();

                switch (first.getMessageSubtype()) {

                    case 'e':
                        String filename = first.ParseEControl();

                        if (filename == null) {
                            /* This means that the client is requesting a node 
                             * list. That is the task for ClusterMgr
                             */
                            ClusterMgr c = ClusterMgr.getInstance();
                            String clustrInfo = c.prepareJoinMessagePayload();
                            ControlMessage nodeListMsg = new ControlMessage('j',
                                    sock.getInetAddress(), 0, clustrInfo);
                            oOut.writeObject(nodeListMsg);
                            oOut.flush();
                            return;
                        } else {

                            /* We need to query the replication manager to get 
                             * the list of nodes which have the chunks. The list 
                             * of chunks is to be retrieved from the table we 
                             * have. 
                             */
                            Integer i = map.get(filename);

                            /*
                             * TODO: Interface with replication manager and
                             * send the list of nodes to the client.
                             */
                        }

                        break;

                    case 'w':
                        /* Write request. This means the file write is to be 
                         * done. 
                         */
                        
                        /*
                         * TODO: Accept the file over the channel, split it
                         * into chunks and hand it to the replication manager.
                         */
                }

            } catch (IOException ie) {
                log.severe("I/O Exception while handling the client request");
                ie.printStackTrace();
            } catch (ClassNotFoundException c) {
                log.severe("Received unknown object from the client");
                c.printStackTrace();
            }
        }
    }
    
}
