package Main;

import java.io.IOException;
import java.net.*;
import java.util.UUID;

public class Connection {

    public static final int DEFAULT_SOCKET_TIMEOUT = 1000;
    public static final int DEFAULT_PORT = 6789;
    public static final String BROADCAST_ADDRESS_IPV4 = "255.255.255.255";
    public static final int DEFAULT_PACKAGE_SIZE = 1024;

    private DatagramSocket datagramSocket;
    private int port;
    private int socketTimeout;
    private final int maxPackageSize;

    private final UUID nodeID;

    public Connection(int port, int socketTimeout, int maxPackageSize) {
        this.socketTimeout = socketTimeout;
        this.maxPackageSize = maxPackageSize;
        this.port = port;
        nodeID = UUID.randomUUID();

        try {
            datagramSocket = new MulticastSocket(port);
            datagramSocket.setSoTimeout(socketTimeout);
        } catch (SocketException e) {
            System.err.println("Cannot create socket for port " + port + ": " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection(int port, int socketTimeout){
        this(port, socketTimeout, DEFAULT_PACKAGE_SIZE);
    }

    public Connection(int port){
        this(port, DEFAULT_SOCKET_TIMEOUT, DEFAULT_PACKAGE_SIZE);
    }

    public Connection(){
        this(DEFAULT_PORT, DEFAULT_SOCKET_TIMEOUT, DEFAULT_PACKAGE_SIZE);
    }

    public void sendPacket(DatagramPacket packet) {
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            System.err.println("Cannot send packet: " + e.getMessage());
        }
    }

    public DatagramPacket receivePacket() {
        DatagramPacket receivePacket = new DatagramPacket(new byte[DEFAULT_PACKAGE_SIZE], DEFAULT_PACKAGE_SIZE);
        try {
            datagramSocket.receive(receivePacket);
            return receivePacket;
        } catch (IOException e) {
            return null;
        }
    }

    public InetAddress getLocalAddress() {
        return datagramSocket.getLocalAddress();
    }

    public int getMaxPackageSize() {
        return maxPackageSize;
    }

    public UUID getNodeID() {
        return nodeID;
    }
}
