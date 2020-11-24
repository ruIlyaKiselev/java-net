package Main;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server {

    private ServerSocket serverSocket;
    private static Set<String> fileNames = Collections.synchronizedSet(new HashSet<>());
    private static Set<TCPConnection> connections = Collections.synchronizedSet(new HashSet<>());

    public Server() {
        try {
            serverSocket = new ServerSocket(TCPConnection.DEFAULT_PORT);
            while (true) {
                Socket client = serverSocket.accept();
                connections.add(new TCPConnection(client));
            }
        } catch (IOException e) {
            System.err.println("Unable to set up server " + e.getMessage());
        }
    }

    public static boolean containsFilename(String filename) {
        return fileNames.contains(filename);
    }
}