package aether.client;


import aether.cluster.ClusterTableRecord;
import aether.io.Chunk;
import aether.net.ControlMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AppRequestProcessor implements Runnable {

    private int K = 1;

    private int MAX_RETRIES;

    private Socket socket = null;
    private ClusterTableRecord[] nodeList;
    private InetAddress myIp;
    private int clusterPort;


    public AppRequestProcessor(Socket socket, ClusterTableRecord[] nodeList, int port, InetAddress ip, int k, int maxRetries) {

        this.socket = socket;
        this.nodeList = nodeList;
        this.clusterPort = port;
        this.myIp = ip;
        this.K = k;
        this.MAX_RETRIES = maxRetries;

    }

    public boolean write(String filename) throws SocketTimeoutException {

        boolean status;
        int i = 0, count = 0;

        while (true) {

            InetSocketAddress endpoint = new InetSocketAddress(nodeList[i].getNodeIp(), clusterPort);

            try {

                System.out.println("Sending write request to " + endpoint.getAddress().toString() + " for file " + filename);

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

                System.out.println("Socket connection error while trying to write.");
                e.printStackTrace();

            } catch (ClassNotFoundException e) {

                System.out.println("Could not confirm write on cluster.");
                e.printStackTrace();

            }
        }
    }

    private boolean write(InetSocketAddress endpoint, String filename) throws IOException, ClassNotFoundException {

        boolean status = false;

        Socket writeSocket = new Socket();

        writeSocket.connect(endpoint, 1000);
        System.out.println("Connected to endpoint. Converting to byte array..");

        ObjectOutputStream oos = new ObjectOutputStream(writeSocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(writeSocket.getInputStream());

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String input;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((input = br.readLine()) != null) {

            byte[] bytes = input.getBytes();
            outputStream.write(bytes);

        }

        byte[] bytes = outputStream.toByteArray();


        ControlMessage cMsg = new ControlMessage('w', endpoint.getAddress(), filename + ":" + bytes.length);

        System.out.println("Sending 'w' control message to node with filename " + filename + ":" + bytes.length);

        ControlMessage recvMsg = (ControlMessage) communicate(oos, ois, cMsg);

        if (recvMsg.getMessageSubtype() == 'k') {

            System.out.println("Received ACK from node. Writing to socket..");

            oos.write(bytes);
            oos.flush();

            status = true;
        }

        return status;
    }

    public void assembleChunks(Chunk[] chunks, Thread[] readers, String filename) throws InterruptedException {

        try {

            FileWriter recdFile = new FileWriter(new File(filename));

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

            System.out.println("Error while opening file for writing");
            e.printStackTrace();

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

        System.out.println("Communicating with node " + "by sending msg " + obj.toString());

        oos.writeObject(obj);
        oos.flush();

        System.out.println("Waiting for response from node..");

        Object resp = null;

        try {

            System.out.println("Reading response..");
            resp = ois.readObject();


        } catch (ClassNotFoundException e) {

            System.out.println("Error in receiving data from cluster");
            e.printStackTrace();

        }

        System.out.println("Returning " + resp.toString() + " to caller.");

        return resp;
    }

    public void read(String filename) throws IOException {

        int i = 0, count = 0;
        HashMap<Integer, LinkedList<String>> chunkList = null;

        while (true) {

            /* This is the IP of the node from which to request the chunk list. */
            InetAddress ip = nodeList[i].getNodeIp();

            try {

                Socket readSocket = new Socket();

                /* Read request. */
                ControlMessage readRequest = new ControlMessage('e', ip, filename);

                /* Get the list of nodes that have chunks of the file. */
                InetSocketAddress endpoint = new InetSocketAddress(ip, clusterPort);

                readSocket.connect(endpoint, 1000);

                ObjectInputStream ois = new ObjectInputStream(readSocket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(readSocket.getOutputStream());

                chunkList = (HashMap<Integer, LinkedList<String>>) communicate(oos, ois, readRequest);

                Thread[] readers = new Thread[chunkList.size()];

                Chunk[] chunks = new Chunk[chunkList.size()];

                int j = 0;

                /* For each node that has the chunk, start a AetherFileReader thread. */
                for (Map.Entry<Integer, LinkedList<String>> entry : chunkList.entrySet()) {

                    chunks[j] = null;

                    AetherFileReader fileReader = new AetherFileReader(filename, entry.getKey(), clusterPort, myIp, chunks[j], entry.getValue());
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

                System.out.println("Thread was interrupted. Could not assemble chunks");
                e.printStackTrace();

            }

        }
    }

    public void processRequest() throws IOException {

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        try {

            String request = (String) ois.readObject();

            String[] s = request.split(":");
            String requestType = s[0];
            String filename = s[1];

            switch (requestType) {

                case "read":
                    read(filename);
                    break;
                case "write":
                    boolean status = write(filename);
                    /* We should actually send this status back to the application. But for now, we will just
                     * print it.
                     */
                    System.out.println("[INFO]: Write status: " + status);
                    break;
                default:
                    System.out.println("[ERROR]: Illegal request.");

            }

        } catch (ClassNotFoundException c) {

            System.out.println("[ERROR]: Illegal request from application.");
            c.printStackTrace();

        }

    }

    @Override
    public void run() {

        try {

            processRequest();

        } catch (IOException e) {

            System.out.println("[ERROR]: Error in reading request from application.");
            e.printStackTrace();

        }

    }
}
