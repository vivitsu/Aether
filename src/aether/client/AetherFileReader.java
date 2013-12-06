package aether.client;


import aether.io.Chunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
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
    InetAddress myIp;

    LinkedList<String> nodeIps;

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
     * @param fname  The filename, i.e. chunk to be requested
     * @param ip     The node to request the chunk from
     * @param prt    Port at which to communicate
     * @param mIP    Client node's external IP
     * @param sChunk Shared chunk object between AetherClient and this thread
     */
    public AetherFileReader(String fname, Integer id, InetAddress ip, int prt, InetAddress mIP, Chunk sChunk, LinkedList<String> ll) {

        filename = fname + ":" + id.toString();
        nodeIps = ll;
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

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

        oos.writeObject(filename); // TODO: Chunk name and ID, most probably
        oos.flush();

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

    /**
     * Gets chunk of data by communicating with the node over the socket specified by 'socket'.
     * Internally, it will call the {@link #communicate(java.net.Socket)} method to receive the
     * object from the node over the socket.
     *
     * If the data received over the socket is null, it will throw a NullPointerException
     *
     * @param socket The socket over which to communicate
     * @throws IOException If socket communication fails
     * @throws NullPointerException If data received over socket is null
     */
    public void getChunk(Socket socket) throws IOException {

        dataChunk = (Chunk) communicate(socket);

        if (dataChunk == null) {

            throw new NullPointerException();

        }
    }


    /**
     * Creates a socket that binds to the client's external IP address and connects to the
     * cluster node. Cluster node IP is specified by {@link #nodeIps} and client's external
     * IP is specified by {@link #myIp}
     *
     * @return Socket bound to client's external IP and connected to the cluster node
     * @throws IOException If socket creation fails
     */
    public Socket connectToNode(InetAddress nodeIp) throws IOException {

        Socket clusterSoc = new Socket();
        InetSocketAddress endpoint = new InetSocketAddress(nodeIp, port);

        clusterSoc.connect(endpoint, 1000);

        return clusterSoc;
    }

    @Override
    public void run() {


        while (true) {

            try {

                InetAddress ip = InetAddress.getByName(nodeIps.pollFirst());

                if (ip != null) {

                    Socket socket = connectToNode(ip);
                    getChunk(socket);
                    sharedChunk = dataChunk;

                    return;

                } else {

                    throw new NullPointerException("Read failed for chunk " + filename + ". Could not connect to any node.");

                }

            } catch (SocketTimeoutException e) {

                logger.log(Level.SEVERE, "Connection timed out");
                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}
