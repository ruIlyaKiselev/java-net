package Main;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node {
    private InetAddress address;
    private int port;

    public Node(String addressName, int port) {
        try {
            address = InetAddress.getByName(addressName);
        } catch (UnknownHostException e) {
            address = null;
        }
        this.port = port;
    }

    public Node(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
