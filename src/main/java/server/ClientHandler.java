/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

/**
 *
 * @author TimmosQuadros
 */
public class ClientHandler extends Thread {

    private Socket socket;
    private Scanner input;
    private PrintWriter writer;
    private ConcurrentHashMap<String, ClientHandler> clients;
    private String username;

    public Socket getSocket() {
        return socket;
    }

    public ClientHandler(Socket socket, ConcurrentHashMap<String, ClientHandler> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
        input = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);

    }

    @Override
    public void run() {
        try {
            String message = input.nextLine(); //IMPORTANT blocking call
            //System.out.println(String.format("Received the message: %1$S ", message));
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO, String.format("Received the message: %1$S ", message));
            while (!message.equals(ProtocolStrings.LOGOUT)) {
                parseMessage(message);
                //System.out.println(String.format("Received the message: %1$S ", message.toUpperCase()));
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, String.format("Received the message: %1$S ", message.toUpperCase()));
                message = input.nextLine(); //IMPORTANT blocking call
            }
            writer.println(ProtocolStrings.LOGOUT);//Echo the stop message back to the client for a nice closedown
            socket.close();
            //System.out.println("Closed a Connection");
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Closed a Connection");
            super.run(); //To change body of generated methods, choose Tools | Templates.
        } catch (NoSuchElementException | IOException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void handleClient(Socket socket) throws IOException {

    }

    private synchronized void parseMessage(String message) {
        String[] splitColon = message.split(":");
        String messageArgument = splitColon[0];
        String response;
        if (messageArgument.equalsIgnoreCase(ProtocolStrings.LOGIN)) {
            username = splitColon[1];
            clients.putIfAbsent(username, this);
            response = ProtocolStrings.CLIENTLIST + ":";
            Enumeration<String> usernames = clients.keys();

            while (usernames.hasMoreElements()) {
                response += usernames.nextElement() + ",";
            }

            response = response.substring(0, response.length() - 1);

            Enumeration<ClientHandler> clientHandlers = clients.elements();

            while (clientHandlers.hasMoreElements()) {
                ClientHandler nextElement = clientHandlers.nextElement();
                nextElement.sendMessage(response);
            }
        } else if (messageArgument.equalsIgnoreCase(ProtocolStrings.SENDMESSAGE)) {
            if (splitColon[1].equalsIgnoreCase("")) {
                response = ProtocolStrings.MESSAGERESPONSE + ":";
                
                response+=username+ ":" + splitColon[2];

                Enumeration<ClientHandler> clientHandlers = clients.elements();
                
                while (clientHandlers.hasMoreElements()) {
                    ClientHandler client = clientHandlers.nextElement();
                    client.sendMessage(response);
                }
            } else {
                response = ProtocolStrings.MESSAGERESPONSE + ":";
                
                response+=username+ ":" + splitColon[2];
                
                String[] userNames = splitColon[1].split(",");
                
                Enumeration<ClientHandler> clientHandlers = clients.elements();
                
                for (String userName : userNames) {
                    ClientHandler client = clients.get(userName);
                    client.sendMessage(response);
                }
            }
        } else if (messageArgument.equalsIgnoreCase(ProtocolStrings.LOGOUT)) {
            clients.get(username).sendMessage(ProtocolStrings.LOGOUT);
            clients.remove(username);
            response = ProtocolStrings.CLIENTLIST + ":";
            Enumeration<String> usernames = clients.keys();

            while (usernames.hasMoreElements()) {
                response += usernames.nextElement() + ",";
            }
            
            response = response.substring(0, response.length() - 1);
            
            Enumeration<ClientHandler> clientHandlers = clients.elements();

            while (clientHandlers.hasMoreElements()) {
                ClientHandler nextElement = clientHandlers.nextElement();
                nextElement.sendMessage(response);
            }
        }
    }

    private void sendMessage(String msg) {
        writer.println(msg);
    }
}
