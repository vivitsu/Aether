package aether.client;


import aether.cluster.ClusterTableRecord;
import aether.conf.ConfigMgr;
import aether.io.Chunk;
import aether.net.ControlMessage;
import aether.net.NetMgr;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.*;

//TODO: Make this a permanently running thread accepting requests on a socket.
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
    HashMap<String, String> configStore = new HashMap<>();

    /**
     * Holds list of 'K' cluster table records
     */
    ClusterTableRecord[] nodeList = new ClusterTableRecord[K];

    /**
     * Need this to serialize, deserialize messages.
     */
    NetMgr netMgr;

    /**
     * The port at which to communicate with the cluster
     */
    int port;

    Socket clientSocket = new Socket();
    ObjectInputStream ois;
    ObjectOutputStream oos;

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
     * @throws SocketException If a SocketException is received while receving the node list
     * @throws SocketTimeoutException If a timeout occurs while connecting to a node
     * @see #selectKNodes(aether.cluster.ClusterTableRecord[])
     * @see #getNodeList(java.net.InetAddress, aether.net.ControlMessage)
     * @see #parseConfigFile(String)
     */

    public void init(String filename) throws SocketException, SocketTimeoutException {

        ConfigMgr.initConfig(filename);  // TODO: Remove redundancies

        this.parseConfigFile(filename);
        this.setParams();
//        this.netMgr = new NetMgr(this.port);
//        this.myIp = getExternalIp();

        getLocalIp();

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
                tempRecords = this.getNodeList(ipAddr, getNodeList);

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

                logger.log(Level.SEVERE, "Error while getting node list from cluster");
                e.printStackTrace();

            }
        }
    }

    /**
     * Parses the configuration file and sets the class parameters like primary IP, secondary IP, port, MAX_RETRIES, etc.
     * Values are stored in configStore, the configuration dictionary, which is a hashmap mapping String keys to String
     * values
     *
     * @param filename
     */

    public void parseConfigFile(String filename) {


        try {

            BufferedReader br = new BufferedReader(new FileReader(filename));
            String input;

            while ((input = br.readLine()) != null) {

                String[] s = input.split("\\s+");
                configStore.put(s[0], s[1]);

            }

            br.close();

        } catch (FileNotFoundException e) {

            logger.log(Level.SEVERE, "Could not find configuration file. Unable to initialize client");
            e.printStackTrace();
            System.exit(1);

        } catch (IOException e) {

            logger.log(Level.SEVERE, "Error while reading configuration file");
            e.printStackTrace();

        }

    }

    /**
     * Gets values from the configStore hashmap and sets the class params.
     */
    public void setParams() {

        try {

            this.primaryIp = InetAddress.getByName(configStore.get("primary"));
            this.secondaryIp = InetAddress.getByName(configStore.get("secondary"));
            this.port = Integer.parseInt(configStore.get("port"));
            this.MAX_RETRIES = Integer.parseInt(configStore.get("maxRetries"));

        } catch (UnknownHostException e) {

            logger.log(Level.SEVERE, "Error in setting primary and secondary IP addresses.");
            e.printStackTrace();

        }

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


    private void getLocalIp() {

        try {

            Socket s = new Socket("google.com", 80);
            myIp = InetAddress.getByName(s.getLocalAddress().getHostAddress());

        } catch (UnknownHostException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
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
    public ClusterTableRecord[] getNodeList(InetAddress ip, ControlMessage cMsg) throws IOException {

        ClusterTableRecord[] tempRecords = null;

        if (myIp !=null) {

            /* Bind a socket to the client's external IP. */
            //clusterSock = new Socket(myIp, port);

            /* Endpoint is the cluster node to be contacted. */
            InetSocketAddress endpoint = new InetSocketAddress(ip, this.port);

            clientSocket.connect(endpoint, 1000);

            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            ControlMessage msg = (ControlMessage) communicate(cMsg);

            if (msg != null) {
                tempRecords = msg.parseJControl();
            } else {
                throw new NullPointerException();
            }

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
    public Object communicate(Object obj) throws IOException {

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

    /**
     * Reads the file specified by the filename from the cluster. Will contact one node from the node list with the
     * request. If that node is not able to fulfill the request, it will try with another node in the node list and so
     * on, until it has tried contacting all the nodes in the node list at least {@link #MAX_RETRIES} no. of times.
     *
     * After that, it throws a SocketTimeoutException. If the request is successful, it will receive a array of
     * ClusterTableRecord objects. Each object contains the ID & IP address of a node having a chunk of the file.
     * For each node in the ClusterTableRecord array, it will start a new AetherFileReader thread which will contact the
     * node to request the chunk from that node.
     *
     * @param filename The filename to be requested from the cluster
     * @throws IOException If Socket communication fails
     */
    public void read(String filename) throws IOException {

        int i = 0, count = 0;
        ClusterTableRecord[] chunkNodes = null;

        HashMap<Integer, LinkedList<String>> chunkList = null;

        while (true) {

            /* This is the IP of the node from which to request the chunk list. */
            InetAddress ip = nodeList[i].getNodeIp();

            try {

                /* Read request. */
                ControlMessage readRequest = new ControlMessage('e', ip, filename);

                /* Get the list of nodes that have chunks of the file. */
                InetSocketAddress endpoint = new InetSocketAddress(ip, port);

                clientSocket.connect(endpoint, 1000);

                Object obj = communicate(readRequest);

                if (obj instanceof HashMap) {

                    // TODO: Fix unchecked cast warning
                    chunkList = (HashMap<Integer, LinkedList<String>>) obj;

                }

                Thread[] readers = new Thread[chunkList.size()];

                Chunk[] chunks = new Chunk[chunkList.size()];

                int j = 0;

                /* For each node that has the chunk, start a AetherFileReader thread. */

                for (Map.Entry<Integer, LinkedList<String>> entry : chunkList.entrySet()) {

                    chunks[j] = null;

                    // TODO: Chunk name and ID - What will I get along with the node list?
                    AetherFileReader fileReader = new AetherFileReader(filename, entry.getKey(), chunkNodes[j].getNodeIp(), port, myIp, chunks[j], entry.getValue());
                    readers[j] = new Thread(fileReader);

                    readers[j].start();

                    j++;

                }

                assembleChunks(chunks, readers, filename);
                break;

            } catch (SocketTimeoutException e) {

                if (i == K && ++count == MAX_RETRIES) {
                    throw e;
                } else {
                    i++;
                }

                e.printStackTrace();

            } catch (IOException e) {

                throw e;

            } catch (InterruptedException e) {

                logger.log(Level.SEVERE, "Thread was interrupted. Could not assemble chunks");
                e.printStackTrace();

            }

        }
    }

    public void assembleChunks(Chunk[] chunks, Thread[] readers, String filename) throws InterruptedException {

        try {

            FileWriter recdFile = new FileWriter(filename);

            /* Wait for all threads to finish execution so that we have all the chunks. */
            for (int i = 0; i < chunks.length; i++) {
                readers[i].join();
            }

            Arrays.sort(chunks, new ChunkComparator());

            for (int i = 0; i < chunks.length; i++) {

                String str = new String(chunks[i].getData());
                recdFile.append(str);

            }

        } catch (IOException e) {

            logger.log(Level.SEVERE, "Error while opening file for writing");
            e.printStackTrace();

        }
    }

    public boolean write(String filename) throws SocketTimeoutException {

        boolean status;
        int i = 0, count = 0;

        while (true) {

            InetSocketAddress endpoint = new InetSocketAddress(nodeList[i].getNodeIp(), port);

            try {

                logger.log(Level.FINE, "Sending write request to " + endpoint.getAddress().toString() + " for file " + filename);

                status = write(endpoint, filename);
                return status;

            } catch (SocketTimeoutException e) {

                if (i == K && ++count == MAX_RETRIES) {
                    throw e;
                } else {
                    i++;
                }

                e.printStackTrace();

            } catch (IOException e) {

                logger.log(Level.SEVERE, "Socket connection error while trying to write.");
                e.printStackTrace();

            } catch (ClassNotFoundException e) {

                logger.log(Level.SEVERE, "Could not confirm write on cluster.");
                e.printStackTrace();

            }
        }
    }

    private boolean write(InetSocketAddress endpoint, String filename) throws IOException, ClassNotFoundException {

        boolean status = false;

        clientSocket.connect(endpoint, 1000);
        logger.log(Level.FINE, "Connected to endpoint. Converting to byte array..");

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String input;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((input = br.readLine()) != null) {

            byte[] bytes = input.getBytes();
            outputStream.write(bytes);

        }

        byte[] bytes = outputStream.toByteArray();


        ControlMessage cMsg = new ControlMessage('w', endpoint.getAddress(), filename + ":" + bytes.length);

        logger.log(Level.FINE, "Sending 'w' control message to node with filename " + filename + ":" + bytes.length);

        ControlMessage recvMsg = (ControlMessage) communicate(cMsg);

        if (recvMsg.getMessageSubtype() == 'k') {

            logger.log(Level.FINE, "Received ACK from node. Writing to socket..");

            oos.write(bytes);
            oos.flush();

            status = true;

 /*           ObjectInputStream ois = new ObjectInputStream(writeSock.getInputStream());

            try {

                ControlMessage ack = (ControlMessage) ois.readObject();

                if (ack.getMessageSubtype() == 'k') {
                    status = true;
                }

                return status;

            } catch (ClassNotFoundException e) {
                throw e;
            }

        } else {
            return status;
        } */

        }

        return status;
    }


    /**
     * Initialize an AetherClient object and initialize it.
     *
     * @param args Contains the config file name.
     * @throws SocketTimeoutException If initialization fails while getting node list from cluster.
     */
    public static void main(String[] args) throws SocketTimeoutException {

        AetherClient myAetherClient;

        try {

            myAetherClient = new AetherClient();
            myAetherClient.init(args[0]);

        } catch (SocketException e) {

            logger.log(Level.SEVERE, "Error while setting up NetMgr");
            e.printStackTrace();

        }
    }

}