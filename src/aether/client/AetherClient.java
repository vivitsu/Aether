package aether.client;


import aether.cluster.ClusterTableRecord;
import aether.io.Chunk;
import aether.net.ControlMessage;
import aether.net.NetMgr;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.*;

public class AetherClient {

    /**
     * Out of all nodes in cluster, the client will maintain Cluster Table Records for 'K' nodes
     */
    private final int K = 5;

    /**
     * For every connection attempt, the client will try for MAX_RETRIES times. This parameter is configured from the
     * config file.
     */
    int MAX_RETRIES;

    /**
     * The client's external IP address, and two IPs (Primary & Secondary which act as a directory server for all cluster
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

    /**
     * Logger
     */
    private final static Logger logger = Logger.getLogger(AetherClient.class.getName());

    static {

        logger.setLevel(Level.INFO);
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
     * @see #getExternalIp()
     */

    public void init(String filename) throws SocketException, SocketTimeoutException {

        this.parseConfigFile(filename);
        this.setParams();
        this.netMgr = new NetMgr(this.port);
        this.myIp = getExternalIp();

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
                ipAddr = this.secondaryIp;
            }

            try {

                ControlMessage getNodeList = new ControlMessage('j', ipAddr);
                tempRecords = this.getNodeList(ipAddr, getNodeList);

                if (tempRecords != null) {

                    /* Once the node list is obtained, select 'K' nodes */
                    selectKNodes(tempRecords);

                } else {

                    /* If we didnt get the cluster table record from the cluster node, throw this */
                    throw new NullPointerException();

                }

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
    public InetAddress getExternalIp() {

        InetAddress myIp = null;

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
     * @throws NullPointerException If the message received from the cluster is null or if the {@link #getExternalIp()}
     *         method has failed.
     * @see #communicate(java.net.Socket, Object)
     * @see aether.net.ControlMessage#parseJControl()
     */
    public ClusterTableRecord[] getNodeList(InetAddress ip, ControlMessage cMsg) throws IOException {

        Socket clusterSock;

        ClusterTableRecord[] tempRecords = null;

        if (myIp !=null) {

            /* Bind a socket to the client's external IP. */
            clusterSock = new Socket(myIp, port);

            /* Endpoint is the cluster node to be contacted. */
            InetSocketAddress endpoint = new InetSocketAddress(ip, this.port);

            clusterSock.connect(endpoint, 1000);

            ControlMessage msg = (ControlMessage) communicate(clusterSock, cMsg);

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

        for (int i = 0; i < K; i++) {
            nodeList[i] = tempRecords[i];
        }

    }

    /**
     * Communicate on the given socket by writing the given object to the socket and receiving the
     * response over the socket.
     *
     * @param socket The socket on which to communicate
     * @param obj The object to send over the socket
     * @return The Object that was received over the socket
     * @throws IOException If socket communication fails
     */
    public Object communicate(Socket socket, Object obj) throws IOException {

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

        oos.writeObject(obj);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        Object resp = null;

        try {

             resp = ois.readObject();


        } catch (ClassNotFoundException e) {

            logger.log(Level.SEVERE, "Error in receiving data from cluster");
            e.printStackTrace();

        }

        socket.close();

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
        String recdFile = null;
        ClusterTableRecord[] chunkNodes = null;

        while (true) {

            /* This is the IP of the node from which to request the chunk list. */
            InetAddress ip = nodeList[i].getNodeIp();

            try {

                /* Read request. */
                ControlMessage readRequest = new ControlMessage('e', ip, filename);

                /* Get the list of nodes that have chunks of the file. */
                chunkNodes = getNodeList(ip, readRequest);

                Thread[] readers = new Thread[chunkNodes.length];

                Chunk[] chunks = new Chunk[chunkNodes.length];

                /* For each node that has the chunk, start a AetherFileReader thread. */
                for (int j = 0; j < chunkNodes.length; j++) {

                    chunks[j] = null;

                    // TODO: Chunk name and ID - What will I get along with the node list?
                    AetherFileReader fileReader = new AetherFileReader(dummyName, chunkNodes[j].getNodeIp(), port, myIp, chunks[j]);
                    readers[j] = new Thread(fileReader);

                    readers[j].start();
                }


                assembleChunks(chunks, readers, filename);

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

            }
        }
    }

    private boolean write(InetSocketAddress endpoint, String filename) throws IOException, ClassNotFoundException {

        boolean status = false;

        Socket writeSock = new Socket(myIp, port);
        writeSock.connect(endpoint, 1000);

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String input;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((input = br.readLine()) != null) {

            byte[] bytes = input.getBytes();
            outputStream.write(bytes);

        }

        byte[] bytes = outputStream.toByteArray();

        ObjectOutputStream oos = new ObjectOutputStream(writeSock.getOutputStream());
        oos.write(bytes);

        ObjectInputStream ois = new ObjectInputStream(writeSock.getInputStream());

        try {

            ControlMessage ack = (ControlMessage) ois.readObject();

            if (ack.getMessageSubtype() == 'k') {
                status = true;
            }

        } catch (ClassNotFoundException e) {
            throw e;
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