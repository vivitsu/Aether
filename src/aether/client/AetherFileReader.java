package aether.client;


import aether.io.Chunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AetherFileReader implements Runnable {

    /**
     * The file i.e. chunk, to be read from the cluster.
     */
    String filename;

    /**
     * The node from where to request the chunk and the client node's external IP.
     */
    InetAddress nodeIp, myIp;

    /**
     * Port
     */
    int port;

    /**
     * dataChunk is what will catch the received chunk, sharedChunk is the shared chunk object between AetherClient and
     * this thread.
     */
    Chunk dataChunk, sharedChunk;

    private final static Logger logger = Logger.getLogger(AetherClient.class.getName());

    static {

        logger.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

    }


    /**
     * Sole constructor.
     *
     * @param fname  The filename, i.e. chunk ID to be requested
     * @param ip     The node to request the chunk from
     * @param prt    Port at which to communicate
     * @param mIP    Client node's external IP
     * @param sChunk Shared chunk object between AetherClient and this thread
     */
    public AetherFileReader(String fname, InetAddress ip, int prt, InetAddress mIP, Chunk sChunk) {

        filename = fname;
        nodeIp = ip;
        myIp = mIP;
        port = prt;
        sharedChunk = sChunk;
    }

    /**
     * Communicates over the socket specified by 'socket' param.
     *
     * Receives object over the socket and returns it to the callee.
     *
     * @param socket The socket over which to communicate
     * @return Object that is received over the socket
     * @throws IOException If socket communication fails.
     */
    public Object communicate(Socket socket) throws IOException {

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        Object resp = null;

        try {

            resp = ois.readObject();

        } catch (ClassNotFoundException e) {

            logger.log(Level.SEVERE, "Error in receiving chunk from cluster");
            e.printStackTrace();

        }

        return resp;

    }

    public void getChunk(Socket socket) throws IOException {

        dataChunk = (Chunk) communicate(socket);

        if (dataChunk == null) {

            throw new NullPointerException();

        }
    }

    public Socket connectToNode() throws IOException {

        Socket clusterSoc = new Socket(myIp, port);
        InetSocketAddress endpoint = new InetSocketAddress(nodeIp, port);

        clusterSoc.connect(endpoint, 1000);

        return clusterSoc;
    }

    @Override
    public void run() {

        try {

            Socket socket = connectToNode();
            getChunk(socket);
            sharedChunk = dataChunk;

        } catch (SocketTimeoutException e) {

            logger.log(Level.SEVERE, "Connection timed out");
            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}
