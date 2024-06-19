package oxoo2a;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN (Multithreading is Used everywhere to call non-static operations)
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l")) {
            System.out.println("Server offen");
            Servers server = new Servers(port);
            server.run();
        } else{
            Clients client = new Clients(args[0],port);
            client.run();
        }

    }
}
    // ******************************************************************************
    // Server (Used as a Middleman for every User to connect to and forward messages)
    // ******************************************************************************
  class Servers implements Runnable {
        int port;

        public Servers(int port) {
            this.port = port;
        }

        private ArrayList<ClientServer> Nutzer;

     void Server ( int port ) throws IOException {
        ServerSocket s = new ServerSocket(port);
        Nutzer = new ArrayList<>();
        while (true) {
            Socket client = s.accept();
            ClientServer handler = new ClientServer(client);
            handler.nickname = "\" Still Unspecified/new \"";
            Nutzer.add(handler);
            Thread go = new Thread(handler);
            go.start();
        }
    }
        //MultiThreading for Server
        @Override
        public void run() {
            try {
                Server(port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        class ClientServer implements Runnable{

      private final Socket client;


      private String nickname;


      public ClientServer(Socket client) throws IOException {
          this.client = client;
      }

    private void serveClient(Socket clientConnection) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            PrintWriter w = new PrintWriter(clientConnection.getOutputStream(),true);
            String line;

            w.println("Enter Username: ");
            nickname = r.readLine();
            while(nickname.isEmpty() || nickname.equals("/quit") || nickname.equals("/User")){
                w.println("invalid");
                w.println("Try again: ");
                nickname = r.readLine();
            }
            System.out.println("New User " + nickname);
            send("Welcome! Type /User to find out who is online and available! and /quit to close\n" +
                    "To reach a User type in this form: [Username]:[Message you want to send]\n" +
                    "\n");
            do {
                line = r.readLine();
                if(line.equals("/User")){
                    for(ClientServer i : Nutzer){
                        if(nickname != null){send(i.nickname);}
                    }
                }else{
                   if(!line.equalsIgnoreCase("/stop")){ forward(line);}
                }
            } while (!line.equalsIgnoreCase("/stop"));
            System.out.println(nickname + " disconnected.");
            Nutzer.remove(this.client);
            clientConnection.close();
        }
        catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
      }

      public void send(String message) throws IOException {
          PrintWriter w = new PrintWriter(client.getOutputStream(),true);
          w.println(message);
      }
            //MultiThreading for Serving
            @Override
            public void run() {
                serveClient(client);
            }
            //Send message to the specified user
            public void forward(String command) throws IOException {
             //regexsplit for predetermined syntax
              String[] Text = command.split(":",2);
              //Check for user
              for (ClientServer g: Nutzer){
                  if(Text[0].equals(g.nickname)){
                      PrintWriter w = new PrintWriter(g.client.getOutputStream(),true);
                      w.println(this.nickname + " says: " + Text[1]);
                      return;
                  }
              }
                send("User not Found or invalid command!\n" +
                        "Valid Syntax: [Username]:[Message you want to send]");
            }
        }

  }

    // ************************************************************************
    // Client (The actual user who connects to the Server)
    // ************************************************************************


   class Clients implements Runnable{

    private final String Host;
    private final int port;

       public Clients(String host, int port) {
           this.Host = host;
           this.port = port;
       }

       void Client(String serverHost, int serverPort) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverConnect = new Socket(serverAddress,serverPort);
        PrintWriter w = new PrintWriter(serverConnect.getOutputStream(),true);

        Thread c = new Thread(() -> {

            try {
                Invoice(serverConnect);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        c.start();

        String outline;
        do {
            outline = readString();
            w.println(outline);
        } while (!outline.equalsIgnoreCase("/stop"));
        serverConnect.close();
    }

    private  String readString () {
        BufferedReader br = null;
         boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            }
            catch (Exception e) {
                System.out.printf("Exception: %s\n",e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    //Recieve message as Client User
    private void Invoice(Socket serverConnects) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(serverConnects.getInputStream()));
        String inline;
        boolean does = true;
        do {
            try{
            inline = r.readLine();
            System.out.println(inline);
            }catch(IOException e){
                does = false;
            }
        } while (does);

      }
       //MultiThreading for Client
       @Override
       public void run() {
           try {
               Client(Host, port);
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
       }
   }

