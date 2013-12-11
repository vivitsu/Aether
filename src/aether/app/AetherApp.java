package aether.app;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class AetherApp {

    private Socket clientSocket;
    private int port;

    public AetherApp(int port) {
        this.port = port;
    }

    private static void printUsage() {

        System.out.println("Usage: java AetherApp [-options] [port] [read|write] [filename]");
        System.out.println("where options include: ");
        System.out.println("\t-h --help: Print this help message");

    }

    private void readFile(String filename) {

        File f = new File(filename);

        if (f.exists()) {
            System.out.println("File " + f.getName() + " is now available on your local machine. You can use your favorite" +
                    "program to open this file.");
        } else {
            System.out.println("ERROR.");
        }

    }

    public static void main(String[] args) {



        if (args[0] == "-h" || args[0] == "--help") {

            AetherApp.printUsage();

        } else if (args.length < 3) {

            AetherApp.printUsage();

        } else {

            try {

                AetherApp myApp = new AetherApp(Integer.parseInt(args[0]));

                myApp.clientSocket = new Socket();
                InetSocketAddress endpoint = new InetSocketAddress(myApp.port);

                myApp.clientSocket.connect(endpoint);

                ObjectOutputStream oos = new ObjectOutputStream(myApp.clientSocket.getOutputStream());

                switch (args[1]) {

                    case "read":
                        String readReq = args[1] + ":" + args[2];
                        oos.writeObject(readReq);
//                        myApp.readFile(args[1]);
                        break;
                    case "write":
                        String writeReq = args[1] + ":" + args[2];
                        oos.writeObject(writeReq);
                        break;
                    default:
                        AetherApp.printUsage();
                        break;

                }

            } catch (SocketTimeoutException e) {

                // TODO: Print useful error message
                e.printStackTrace();

            } catch (SocketException e) {

                // TODO: Print useful error message
                e.printStackTrace();

            } catch (IOException e) {

                // TODO: Print useful error message
                e.printStackTrace();

            }

        }


    }

}
