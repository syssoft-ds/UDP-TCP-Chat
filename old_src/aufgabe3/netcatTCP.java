package aufgabe3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class netcatTCP {

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("netcatTCP usage: \"netcatTCP -s <port> \" or \"netcat -c <port>\"");

        int port =0;
        try {
            port = Integer.parseInt(args[1]);
            if(port<1 || port>65535){
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            fatal("Invalid port. Port number must be between 1 and 65535.");
        }

        switch(args[0]){
            case "-s":
                new ServerThread(port).start();
                break;
            case "-c":
                new ClientThread(port).start();
                break;
            default:
                fatal("netcatTCP usage: \"netcatTCP -s <port> \" or \"netcat -c <port>\"");
                break;
        }

    }


    // ************************************************************************
    // Client
    // ************************************************************************
//    private static void Client ( String serverHost, int serverPort ) throws IOException {
//        InetAddress serverAddress = InetAddress.getByName(serverHost);
//        Socket serverConnect = new Socket(serverAddress,serverPort);
//        PrintWriter w = new PrintWriter(serverConnect.getOutputStream(),true);
//        String line;
//        do {
//            line = readString();
//            w.println(line);
//        } while (!line.equalsIgnoreCase("stop"));
//        serverConnect.close();
//    }

}
