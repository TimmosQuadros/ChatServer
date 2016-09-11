package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoServer {

    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private String ip;
    private int port;
    public static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void stopServer() {
        keepRunning = false;
    }

    private static void handleClient(Socket socket) throws IOException {
        ClientHandler client = new ClientHandler(socket,clients);
        client.start();
    }
    
    private void runServer(String ip, int port) {
        this.port = port;
        this.ip = ip;

        //System.out.println("Server started. Listening on: " + port + ", bound to: " + ip);
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO,"Server started. Listening on: " + port + ", bound to: " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                //System.out.println("Connected to a client");
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO,"Connected to a client");
                handleClient(socket);
            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(Log.LOG_NAME).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                throw new IllegalArgumentException("Error: Use like: java -jar EchoServer.jar <ip> <port>");
            }
            String ip = args[0];
            int port = Integer.parseInt(args[1]);
            Log.setLogFile("logFile.txt", "Serverlog");
            try {
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Starting the Server");
                new EchoServer().runServer(ip, port);
            } finally {
                Log.closeLogger();
            }
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO,e.getMessage());
            Logger.getLogger(Log.LOG_NAME).log(Level.SEVERE, null, e);
        }
    }
}
