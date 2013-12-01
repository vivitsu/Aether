/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.net;

import aether.conf.ConfigMgr;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author aniket
 */
public class NetMgr {
    
    
    
    private static String interfaceName = ConfigMgr.getInterfaceName();
    private static final Logger log = Logger.getLogger(NetMgr.class.getName());
    
    
    
    
    private int port;
    private DatagramSocket socket;
    
    
    static {
        log.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        log.addHandler(handler);
    }
    
    
    public NetMgr (int port) throws SocketException {
        
        this.port = port;
        socket = new DatagramSocket (port);
        
        if (ConfigMgr.getIfDebug()) {
                log.setLevel(Level.FINE);
            }
        
        socket.setBroadcast(true);
        if (socket.getBroadcast()) {
            log.fine("Enabled broadcast reception");
        }
        
    }
    
    
    
    
    
    
    /**
     * Set the timeout for the socket
     * @param timeout   int having timeout in milliseconds
     * @throws SocketException 
     */
    public void setTimeout (int timeout) throws SocketException {
        log.log(Level.FINE,"Setting timeout to {0} ms", timeout);
        socket.setSoTimeout(timeout);
    }
    
    
    
    
    
    
    /** 
     * Convert the object into series of bytes
     * @param m Object to be converted in a byte array.
     * @return  byte array containing the bytes of the object
     * @throws IOException 
     */
    private byte[] serialize (Object m) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(bOut);
        oOut.writeObject(m);
        byte[] d = bOut.toByteArray();
        log.log(Level.FINER,"Serialized the message to {0} bytes", 
                d.length);
        return d;
    }
    
    
    
    /**
     * Send a message over the network
     * @param m     Message m to be sent
     * @throws IOException 
     */
    public void send (Message m) throws IOException {
        byte[] datagramPayload = serialize(m);
        DatagramPacket packet = new DatagramPacket(datagramPayload, 
                datagramPayload.length, m.getDestIp(), port);
        
        
        log.log(Level.FINE, "Sending {0} bytes to node {1} port: {2}", 
                new Object[]{ datagramPayload.length, 
                    m.getDestIp().toString().replaceFirst(".*/", ""), 
                    port});
        socket.send(packet);
    }
    
    
    
    /**
     * Receive message from the network
     * @return  Received message. null on failure
     * @throws IOException 
     */
    public Message receive () throws IOException {
        
        DatagramPacket dResponse = null;
        for (int i = 0; i < 5; i++) {

            dResponse = new DatagramPacket(new byte[10000], 10000);
            socket.receive(dResponse);

            if (dResponse.getAddress().equals(getLocalIp())) {
                if (i == 5) {
                    throw new SocketTimeoutException();
                }
            } else {
                break;
            }
        }
        
        Message m = null;
        try {
            log.log(Level.FINE, "Recceived {0} bytes from node {1}", 
                    new Object[]{dResponse.getLength(),
                        dResponse.getSocketAddress().toString()});
            
            byte[] dataCopy = Arrays.copyOf(dResponse.getData(), 
                        dResponse.getLength());
            m = deserializeMessage(dataCopy);
        } catch (ClassNotFoundException ex) {
            log.warning("Error retrieving message from the datagram");
            ex.printStackTrace();
        }
        return m;
    }
    
    
    
    /**
     * Deserialize a message from the byte stream in a datagram
     * @param bytes     Message byte stream
     * @return          Deserialized message
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private Message deserializeMessage (byte[] bytes) throws IOException, 
            ClassNotFoundException {
        Message m;
        log.log(Level.FINE, "Deserialized {0} bytes to extract a message",
                bytes.length);
        m = (Message) deserialize (bytes);
        return m;
    }
    
    
    /**
     * Deserialize an object from a byte stream
     * @param bytes     Object bytes
     * @return          Deserialized object
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private Object deserialize (byte[] bytes) throws IOException, 
            ClassNotFoundException {
        ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
        ObjectInputStream oIn = new ObjectInputStream(bIn);
        return oIn.readObject();
    }
    
    
    
    /**
     * Return the broadcast address of the cluster interface.
     * @return  InetAddress having the broadcast address of the cluster 
     *          interface. null on failure.
     */
    public static InetAddress getBroadcastAddr () {
        
        NetworkInterface iFace;
        try {
            iFace = NetworkInterface.getByName(interfaceName);
            if (iFace.isLoopback() || (! iFace.isUp()) ) {
                return null;
            }
        } catch (SocketException e) {
            log.warning("Could not find the interface");
            return null;
        }
        
        for (InterfaceAddress addr : iFace.getInterfaceAddresses()) {
            if (addr == null) {
                continue;
            }
            InetAddress broadcast = addr.getBroadcast();
            if (broadcast == null) {
                continue;
            } else {
                return broadcast;
            }
        }
        return null;
        
    }
    
    
    /**
     * Get local IPv4 address of this machine
     * @return  InetAddress IPv4 address of the interface given in the 
     *          configuration file
     */
    public static InetAddress getLocalIp () {
        
        NetworkInterface iFace;
        try {
            iFace = NetworkInterface.getByName(interfaceName);
            if (iFace.isLoopback() || (! iFace.isUp()) ) {
                return null;
            }
        } catch (SocketException e) {
            log.warning("Could not find the interface");
            return null;
        }
        
        for (InterfaceAddress addr : iFace.getInterfaceAddresses()) {
            if (addr == null || addr.getAddress() instanceof Inet6Address) {
                continue;
            } else {
                return addr.getAddress();
            }
            
        }
        return null;
    }
}
