package aether.client;


import aether.cluster.ClusterTableRecord;
import aether.net.ControlMessage;
import aether.net.NetMgr;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AetherClient {

    InetAddress primaryIp, secondaryIp, myIp;
    int port;
    HashMap<String, String> configStore = new HashMap<>();
    ClusterTableRecord[] nodeList;
    NetMgr netMgr;

    private final static Logger logger = Logger.getLogger(AetherClient.class.getName());

    static {

        logger.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

    }

    public AetherClient(String filename) throws SocketException {

       this.parseConfigFile(filename);
       this.setParams();
       this.netMgr = new NetMgr(this.port);
       this.myIp = getExternalIp();

    }

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

    public void setParams() {

        try {

            this.primaryIp = InetAddress.getByName(configStore.get("primary"));
            this.secondaryIp = InetAddress.getByName(configStore.get("secondary"));
            this.port = Integer.parseInt(configStore.get("port"));

        } catch (UnknownHostException e) {

            logger.log(Level.SEVERE, "Error in setting primary and secondary IP addresses.");
            e.printStackTrace();

        }

    }

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

    public void getNodeList(InetAddress ip) throws IOException {

        ControlMessage getNodeList = new ControlMessage('j', ip);
        Socket clusterSock;

        if (myIp !=null) {

            clusterSock = new Socket(myIp, port);
            InetSocketAddress endpoint = new InetSocketAddress(ip, this.port);

            clusterSock.connect(endpoint, 1000);

            ObjectOutputStream oos = new ObjectOutputStream(clusterSock.getOutputStream());

            oos.writeObject(getNodeList);
            oos.flush();

        } else {

            throw new NullPointerException();

        }


    }

    public static void main(String[] args) throws SocketTimeoutException {

        AetherClient myAetherClient = null;

        try {

            myAetherClient = new AetherClient(args[0]);

        } catch (SocketException e) {

            logger.log(Level.SEVERE, "Error while setting up NetMgr");
            e.printStackTrace();

        }

        boolean ip = true;
        int count = 0;
        int maxRetries = Integer.parseInt(myAetherClient.configStore.get("maxRetries"));

        while (true) {

            InetAddress ipAddr = null;

            if (ip) {
                ipAddr = myAetherClient.primaryIp;
            } else {
                ipAddr = myAetherClient.secondaryIp;
            }

            try {

                myAetherClient.getNodeList(ipAddr);

            } catch (SocketTimeoutException e) {

                ip = !ip;
                if (++count == maxRetries) throw e;

            } catch (IOException e) {

                logger.log(Level.SEVERE, "Error while getting node list from cluster");
                e.printStackTrace();

            }

        }






    }

}