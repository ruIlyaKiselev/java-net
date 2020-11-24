package Main;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.UUID;

public class UDPMulticastConnection {

    public static final int DEFAULT_SOCKET_TIMEOUT = 1000;
    public static final int DEFAULT_PORT = 6789;
    public static final String DEFAULT_ADDRESS_IPV4 = "228.5.6.7";
    public static final int DEFAULT_PACKAGE_SIZE = 256;
    public final UUID nodeID = UUID.randomUUID();

    private MulticastSocket multicastSocket;
    private InetAddress groupAddress;
    private int port;
    private int socketTimeout;
    private int maxPackageSize;

    public UDPMulticastConnection(String groupAddressName, int port, int socketTimeout, int maxPackageSize) {
        this.socketTimeout = socketTimeout;
        this.maxPackageSize = maxPackageSize;
        this.port = port;

        try {
            groupAddress = InetAddress.getByName(groupAddressName);
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(groupAddress); // TODO: replace deprecated method
            multicastSocket.setSoTimeout(socketTimeout);
        } catch (UnknownHostException e) {
            System.err.println("Cannot get address for " + groupAddressName + ": " + e.getMessage());
        } catch (SocketException e) {
            System.err.println("Cannot create multicast socket for port " + port + ": " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UDPMulticastConnection(String groupAddressName, int port, int socketTimeout){
        this(groupAddressName, port, socketTimeout, DEFAULT_PACKAGE_SIZE);
    }

    public UDPMulticastConnection(String groupAddressName, int port){
        this(groupAddressName, port, DEFAULT_SOCKET_TIMEOUT, DEFAULT_PACKAGE_SIZE);
    }

    public UDPMulticastConnection(){
        this(DEFAULT_ADDRESS_IPV4, DEFAULT_PORT, DEFAULT_SOCKET_TIMEOUT, DEFAULT_PACKAGE_SIZE);
    }

    public void sendPacket(String message) {
        String messageWithUUID = message.concat(" ").concat(nodeID.toString());
        int messageSizeWithUUID = messageWithUUID.length();

        if (messageSizeWithUUID <= maxPackageSize) {
            try {
                multicastSocket.send(new DatagramPacket(messageWithUUID.getBytes(), messageSizeWithUUID,
                        groupAddress, port));
            } catch (IOException e) {
                System.err.println("Cannot send packet: " + e.getMessage());
            }
        } else {
            System.err.println("Too big UDP packet. Max size = " + maxPackageSize +
                    ". Your packet size = " + messageSizeWithUUID);
        }
    }

    public DatagramPacket receivePacket() {
        DatagramPacket receivePacket = new DatagramPacket(new byte[DEFAULT_PACKAGE_SIZE], DEFAULT_PACKAGE_SIZE);
        try {
            multicastSocket.receive(receivePacket);
            return receivePacket;
        } catch (IOException e) {
            //System.err.println("Cannot receive packet: " + e.getMessage());
            return null;
        }
    }

    public InetAddress getLocalAddress() {
        return multicastSocket.getLocalAddress();
    }
}
