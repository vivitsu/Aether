package aether.app;

import aether.client.AetherClient;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class AetherApp {

    AetherClient myClient = new AetherClient();


    private void printUsage() {

        System.out.println("Usage: java AetherApp [-options] [read|write] [filename] [configfile]");
        System.out.println("where options include: ");
        System.out.println("\t-h --help: Print this help message");

    }

    private void clientInit(String filename) throws SocketTimeoutException, SocketException {

        myClient.init(filename);

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

        AetherApp myApp = new AetherApp();

        if (args[0] == "-h" || args[0] == "--help") {

            myApp.printUsage();

        } else if (args.length < 3) {

            myApp.printUsage();

        } else {

            try {

                myApp.clientInit(args[2]);

                switch (args[0]) {

                    case "read":
                        myApp.myClient.read(args[1]);
                        myApp.readFile(args[1]);
                        break;
                    case "write":
                        boolean success = myApp.myClient.write(args[1]);
                        if (success) {
                            System.out.println("File " + args[1] + " has been written to the cluster.");
                            break;
                        } else {
                            System.out.println("WRITE FAILED.");
                            break;
                        }
                    default:
                        myApp.printUsage();

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
