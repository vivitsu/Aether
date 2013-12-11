package aether.client;


import aether.cluster.ClusterTableRecord;
import aether.conf.ConfigMgr;
import aether.net.ControlMessage;
import aether.net.NetMgr;

import java.io.*;
import java.net.*;
import java.util.logging.*;

//TODO: Not all sockets are being closed. This can be a problem later.

public class AetherClient {

    /**
     * Out of all nodes in cluster, the client will maintain Cluster Table Records for 'K' nodes
     */
    private final int K = 1;

    /**
     * For every connection attempt, the client will try for MAX_RETRIES times. This parameter is configured from the
     * config file.
     */
    int MAX_RETRIES;

    /**
     * The client's external IP address, and two IPs (Primary & Secondary) which act as a directory server for all cluster
     * IPs.
     */
    InetAddress primaryIp, secondaryIp, myIp;

    /**
     * Configuration dictionary. Holds parameters read from the config file.
     */
//    HashMap<String, String> configStore = new HashMap<>();

    /**
     * Holds list of 'K' cluster table records
     */
    ClusterTableRecord[] nodeList = new ClusterTableRecord[K];

    /**
     * The port at which to communicate with the cluster
     */
    int clusterPort, clientPort;

    ServerSocket serverSocket;

    /**
     * Logger
     */
    private final static Logger logger = Logger.getLogger(AetherClient.class.getName());

    static {

        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

    }

    /**
     * Initializes the Aether client. Will parse the configuration file and set the class params.
     * Will try to get the list of nodes in the cluster from the primary and secondary IPs as set
     * in the config file. No of attempts made to get the node list are defined by {@link #MAX_RETRIES}
     *
     * If the node list is received, it will select 'K' nodes from the received list and set it
     * as the class node list param. Will also set the client node's external IP address.
     *
     * @param filename The filename of the config file
     * @see #selectKNodes(aether.cluster.ClusterTableRecord[])
     * @see #getNodeList(java.net.InetAddress, aether.net.ControlMessage)
     */

    public void init(String filename) {

        ConfigMgr.initConfig(filename);
        setParams();

//        printParams();

        try {

            populateNodeList();

        } catch (SocketTimeoutException e) {

            logger.log(Level.SEVERE, "Could not get node list from cluster.");
            e.printStackTrace();

        }

    }

    /**
     * Gets values from the configStore hashmap and sets the class params.
     */
    public void setParams() {

        primaryIp = ConfigMgr.getPrimaryIP();
        secondaryIp = ConfigMgr.getSecondaryIP();
        myIp = ConfigMgr.getLocalIp();
        clusterPort = ConfigMgr.getClusterPort();
        clientPort = ConfigMgr.getClientPort();
        MAX_RETRIES = ConfigMgr.getMaxRetries();

    }

    private void printParams() {

        System.out.println("Primary IP =  " + primaryIp);
        System.out.println("Secondary IP =  " + secondaryIp);
        System.out.println("Cluster Port =  " + clusterPort);
        System.out.println("Client Port =  " + clientPort);
        System.out.println("MAX_RETRIES =  " + MAX_RETRIES);
        System.out.println("Local IP =  " + myIp);

        System.exit(0);
    }

    /**
     * Gets the client node's external IP address for communication with the cluster.
     * This is the IP at which all sockets will bind and which will be used to communicate with the cluster
     *
     * This method will contact a canonical URL, and parse the response from that URL to get the node's
     * external IP.
     *
     * @return InetAddress object containing the node's external IP.
     */
/*    public void getExternalIp() {

        try {

            URL whatIsMyIp = new URL("http://checkip.amazonaws.com");

            BufferedReader br = new BufferedReader(new InputStreamReader(whatIsMyIp.openStream()));
            String ip = br.readLine();

            myIp = InetAddress.getByName(ip);

        } catch (IOException e) {

            logger.log(Level.SEVERE, "Could not get external IP address");
            e.printStackTrace();

        }

        return myIp;
    } */


/*    private void setLocalIp() {

        try {

            Socket s = new Socket("google.com", 80);
            myIp = InetAddress.getByName(s.getLocalAddress().getHostAddress());

        } catch (UnknownHostException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

    } */

    public void populateNodeList() throws SocketTimeoutException {

        /* This flag decides whether the node list is obtained from primary or secondary IP. */
        boolean ip = true;
        /* Once this flag reaches MAX_RETRIES, a SocketTimeoutException will be thrown. */
        int count = 0;

        ClusterTableRecord[] tempRecords;

        /* Get the cluster member record table from the primary node or
         * secondary node.
         */
        while (true) {

            InetAddress ipAddr = null;

            /* Switch to primary or secondary IP at every retry. */
            if (ip) {
                ipAddr = this.primaryIp;
            } else {

                logger.log(Level.FINE, "Selecting secondary node IP");
                ipAddr = this.secondaryIp;
            }

            try {

                logger.log(Level.INFO, "ipAddr = " + ipAddr);

                ControlMessage getNodeList = new ControlMessage('e', ipAddr);
                tempRecords = getNodeList(ipAddr, getNodeList);

                if (tempRecords != null) {

                    /* Once the node list is obtained, select 'K' nodes */
                    logger.log(Level.FINE, "Got the node list. Now selecting nodes..");
                    selectKNodes(tempRecords);

                } else {

                    /* If we didnt get the cluster table record from the cluster node, throw this */
                    logger.log(Level.INFO, "Did not get cluster table record from cluster node.");
                    throw new NullPointerException();

                }

                break;

            } catch (SocketTimeoutException e) {

                /* Could not connect to the selected node. Try again with the alternate node. */
                ip = !ip;
                if (++count == MAX_RETRIES) throw e;

            } catch (IOException e) {

                logger.log(Level.SEVERE, "[ERROR]: Error while getting node list from cluster");
                e.printStackTrace();

                /* Could not connect to the selected node. Try again with the alternate node. */
                ip = !ip;
                if (++count == MAX_RETRIES) System.exit(1);

            }
        }

    }

    /**
     * Gets the node list from the node specified by the 'ip' param. Node list will be in a form
     * of array of ClusterTableRecord objects. This method is used while initializing and while reading
     * a file, so it accepts a {@link aether.net.ControlMessage} object that specifies what kind of
     * request it needs to send.
     *
     * @param ip The IP address of the node to be contacted
     * @param cMsg The ControlMessage to send to the cluster node
     * @return Array of {@link aether.cluster.ClusterTableRecord} objects
     * @throws IOException If the socket creation fails
     * @throws NullPointerException If the message received from the cluster is null
     * @see aether.net.ControlMessage#parseJControl()
     */
    private ClusterTableRecord[] getNodeList(InetAddress ip, ControlMessage cMsg) throws IOException {

        ClusterTableRecord[] tempRecords = null;

        /* Bind a socket to the client's external IP. */
        Socket clusterSock = new Socket();

        /* Endpoint is the cluster node to be contacted. */
        InetSocketAddress endpoint = new InetSocketAddress(ip, clusterPort);

        clusterSock.connect(endpoint, 1000);

        ObjectOutputStream oos = new ObjectOutputStream(clusterSock.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(clusterSock.getInputStream());

        ControlMessage msg = (ControlMessage) communicate(oos, ois, cMsg);

        if (msg != null) {
            tempRecords = msg.parseJControl();
        } else {
            throw new NullPointerException();
        }

        return tempRecords;
    }

    /**
     * Select {@link #K} nodes out of the node list received from the cluster node.
     * Runs a simple iterative loop to select the first K records from the node list.
     *
     * @param tempRecords The record list from which to select the K nodes.
     */
    private void selectKNodes(ClusterTableRecord[] tempRecords) {

        logger.log(Level.FINE, "Selecting K Nodes..");

        for (int i = 0; i < K; i++) {
            nodeList[i] = tempRecords[i];
            logger.log(Level.FINE, "nodeList[" + i + "] = " + nodeList[i]);
        }

    }

    /**
     * Communicate on the given socket by writing the given object to the socket and receiving the
     * response over the socket.
     *
     * @param obj The object to send over the socket
     * @return The Object that was received over the socket
     * @throws IOException If socket communication fails
     */
    public Object communicate(ObjectOutputStream oos, ObjectInputStream ois, Object obj) throws IOException {

        logger.log(Level.FINE, "Communicating with node " + "by sending msg " + obj.toString());

        oos.writeObject(obj);
        oos.flush();

        logger.log(Level.FINE, "Waiting for response from node..");

        Object resp = null;

        try {

             logger.log(Level.FINE, "Reading response..");
             resp = ois.readObject();


        } catch (ClassNotFoundException e) {

            logger.log(Level.SEVERE, "Error in receiving data from cluster");
            e.printStackTrace();

        }

//        socket.close();

        logger.log(Level.FINE, "Returning " + resp.toString() + " to caller.");

        return resp;
    }

    public void processRequest() {

        try {

            serverSocket = new ServerSocket(clientPort);

            while (true) {

                AppRequestProcessor rp = new AppRequestProcessor(serverSocket.accept(), nodeList, clusterPort, myIp, K, MAX_RETRIES);
                Thread requestProcessor = new Thread(rp);
                requestProcessor.start();

            }

        } catch (IOException e) {

            logger.log(Level.FINE, "Could not listen on port " + clientPort);

        }
    }

    /**
     * Initialize an AetherClient object and initialize it.
     *
     * @param args Contains the config file name.
     * @throws SocketTimeoutException If initialization fails while getting node list from cluster.
     */
    public static void main(String[] args) throws SocketTimeoutException {

        AetherClient myAetherClient = new AetherClient();
        myAetherClient.init(args[0]);
        myAetherClient.processRequest();

    }

}