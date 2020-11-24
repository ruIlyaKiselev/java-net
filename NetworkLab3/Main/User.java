package Main;

import Main.Messages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class User {

    public static final int DEFAULT_TTL = 10;

    private Connection connection;
    private ConcurrentHashMap<Node, Integer> babies;
    private ConcurrentHashMap<ConsoleMessage, Node> sentMessages;
    private ConcurrentHashMap<ConsoleMessage, Node> receivedMessages;

    private String thisName;
    private int thisPort;
    private int lostProbability;
    private Node parentNode;
    private int parentNodeTTL;
    private Node reserveParentNode;
    private int reserveParentNodeTTL;

    public User(String name, int port, int lostProbability) {
        connection = new Connection(port);
        babies = new ConcurrentHashMap<>();
        sentMessages = new ConcurrentHashMap<>();
        receivedMessages = new ConcurrentHashMap<>();

        this.thisName = name;
        this.thisPort = port;
        this.lostProbability = lostProbability;
        this.parentNodeTTL = -1;
        this.reserveParentNodeTTL = -1;

        parentNode = new Node("", 0);
        System.out.println(name + " " + thisPort + " " + connection.getNodeID().toString());
    }

    public User(String name, int port, int lostProbability, String nodeAddress, int nodePort) {
        this(name, port, lostProbability);

        parentNode = new Node(nodeAddress, nodePort);
    }

    public void process() {
        Integer lastNumberOfUsers = 0;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            sendIAmHereMessage();
            updateTTL();
            try {
                if (input.ready()) {
                    String message = input.readLine();
                    sendConsoleMessage(parentNode.getAddress(), parentNode.getPort(), "TOP",
                            UUID.randomUUID().toString(), message, thisName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            long startTime = getTime();
            while (getTime() - startTime < connection.DEFAULT_SOCKET_TIMEOUT) {

                DatagramPacket receivedPacket = connection.receivePacket();
                if (receivedPacket != null) {
                    if (generateLostProbability() < lostProbability) {
                        //System.out.println("Packet lost");
                        continue;
                    }
                    if (recogniseMessage(receivedPacket.getData()) == Message.MessageTypes.I_AM_HERE_MESSAGE.getInt()) {
                        receiveIAmHereMessage(receivedPacket);
                    }
                    if (recogniseMessage(receivedPacket.getData()) == Message.MessageTypes.CONSOLE_MESSAGE.getInt()) {
                        receiveConsoleMessage(receivedPacket);
                    }
                    if (recogniseMessage(receivedPacket.getData()) == Message.MessageTypes.RESPONSE_MESSAGE.getInt()) {
                        receiveResponse(receivedPacket);
                    }
                }
                resendLostMessages();
            }
        }
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

    private void sendIAmHereMessage() {
        String senderID = connection.getNodeID().toString();
        IAmHereMessage iAmHereMessage;
        DatagramPacket packet;

        for (Map.Entry<Node, Integer> entry : babies.entrySet()) {
            iAmHereMessage = new IAmHereMessage(senderID, "PARENT");
            packet = new DatagramPacket(iAmHereMessage.toString().getBytes(), iAmHereMessage.length(),
                    entry.getKey().getAddress(), entry.getKey().getPort());
            connection.sendPacket(packet);

            String reserveParentData = "";
            if (parentNode.getPort() != 0) {
                reserveParentData = parentNode.getAddress().toString().substring(1) + " " + parentNode.getPort();
            } else if (babies.size() != 0) {
                int maxTTL = -1;
                Node nodeForSending = null;
                for (Map.Entry<Node, Integer> entry1 : babies.entrySet()) {
                    if (entry1.getValue() > maxTTL) {
                        maxTTL = entry1.getValue();
                        nodeForSending = entry1.getKey();
                    }
                }
                reserveParentData = nodeForSending.getAddress().toString().substring(1) + " " + parentNode.getPort();
            } else {
                reserveParentData = ":-(";
            }
            //System.out.println(reserveParentData);
            iAmHereMessage = new IAmHereMessage(reserveParentData, "RESERVE_PARENT");
            packet = new DatagramPacket(iAmHereMessage.toString().getBytes(), iAmHereMessage.length(),
                    entry.getKey().getAddress(), entry.getKey().getPort());
            connection.sendPacket(packet);
            //System.out.println("send parent message to " + entry.getKey().getPort());
        }

        if (parentNode.getPort() != 0) {
            iAmHereMessage = new IAmHereMessage(senderID, "BABY");
            packet = new DatagramPacket(iAmHereMessage.toString().getBytes(), iAmHereMessage.length(),
                    parentNode.getAddress(), parentNode.getPort());
            connection.sendPacket(packet);
            //System.out.println("send baby message to " + connectedNode.getPort());
        }
    }

    private void sendConsoleMessage(InetAddress address, int port, String receiverID, String messageID, String message,
                                    String name) {
        if (port != 0) {
            String senderID = connection.getNodeID().toString();
            ConsoleMessage consoleMessage = new ConsoleMessage(senderID, receiverID, messageID, message, name);

            DatagramPacket packet = new DatagramPacket(consoleMessage.toString().getBytes(), consoleMessage.length(),
                    address, port);
            connection.sendPacket(packet);

            for (Map.Entry<ConsoleMessage, Node> entry : sentMessages.entrySet()) {
                if (entry.getKey().getMessageID().equals(consoleMessage.getMessageID()) &&
                        entry.getValue().getAddress().equals(address) && entry.getValue().getPort() == port) {
                    return;
                }
            }

            sentMessages.put(consoleMessage, new Node(address, port));
        } else {
            for (Map.Entry<Node, Integer> entry : babies.entrySet()) {
                sendConsoleMessage(entry.getKey().getAddress(), entry.getKey().getPort(), "ALL",
                        messageID, message, name);
            }
            System.out.println(thisName + " said: " + message);
        }
    }

    private void sendResponse(InetAddress address, int port, ConsoleMessage consoleMessage) {
        String senderID = connection.getNodeID().toString();
        ResponseMessage responseMessage = new ResponseMessage(consoleMessage.getReceiverID(),
                consoleMessage.getSenderID(), consoleMessage.getMessageID(),
                ControlSumCalculator.calculateControlSum(consoleMessage.getData().trim().getBytes()));

        DatagramPacket packet = new DatagramPacket(responseMessage.toString().getBytes(), responseMessage.length(),
                address, port);

        connection.sendPacket(packet);
        //System.out.println("response sent");
    }

    private void receiveIAmHereMessage(DatagramPacket packet) {
        IAmHereMessage message = null;
        try {
            message = new IAmHereMessage(packet);
        } catch (BadMessageArgumentsException e) {
            e.printStackTrace();
        }

        if (message.getSenderID().equals(connection.getNodeID().toString())) {
            return;
        }

        if (message.getReceiverID().trim().equals("BABY")) {
            for (Map.Entry<Node, Integer> entry : babies.entrySet()) {
                if (entry.getKey().getAddress().equals(packet.getAddress()) &&
                        entry.getKey().getPort() == packet.getPort()) {
                    return;
                }
            }
            babies.put(new Node(packet.getAddress(), packet.getPort()), DEFAULT_TTL);
            //System.out.println("My baby is " + message.getSenderID() + " from " + packet.getPort());
        }

        if (message.getReceiverID().trim().equals("PARENT")) {
            parentNodeTTL = DEFAULT_TTL;
            //System.out.println("My parent is " + message.getSenderID() + " from " + packet.getPort());
        }

        if (message.getReceiverID().trim().equals("RESERVE_PARENT")) {
            String[] parts = message.getSenderID().split(" ");
            if (parts.length == 1 && parts[0].equals(":-(")) {
                reserveParentNode = new Node("", 0);
            } else if (parts.length == 2) {
                reserveParentNode = new Node(parts[0], Integer.parseInt(parts[1].trim()));
                reserveParentNodeTTL = DEFAULT_TTL;
                //System.out.println("My reserve parent is " + reserveParentNode.getAddress().toString() +
                //        " from " + reserveParentNode.getPort());
            } else {
                //System.out.println("bad reserve parent!!!!");
            }
        }
    }

    private void receiveConsoleMessage(DatagramPacket packet) {
        ConsoleMessage message = null;
        try {
            message = new ConsoleMessage(packet);
        } catch (BadMessageArgumentsException e) {
            e.printStackTrace();
        }

        if (parentNode.getPort() == 0) {
            for (Map.Entry<Node, Integer> entry : babies.entrySet()) {
                sendConsoleMessage(entry.getKey().getAddress(), entry.getKey().getPort(), "ALL",
                        message.getMessageID(), message.getData(), message.getName().trim());
            }

            for (Map.Entry<ConsoleMessage, Node> entry : receivedMessages.entrySet()) {
                if (entry.getKey().getMessageID().equals(message.getMessageID()) &&
                        entry.getValue().getAddress().equals(packet.getAddress()) &&
                        entry.getValue().getPort() == packet.getPort()) {
                    sendResponse(packet.getAddress(), packet.getPort(), message);
                    return;
                }
            }

            System.out.println(message.getName().trim() + " said: " + message.getData());
            sendResponse(packet.getAddress(), packet.getPort(), message);
            receivedMessages.put(message, new Node(packet.getAddress(), packet.getPort()));

            return;
        }

        if (parentNode.getPort() != 0 && message.getReceiverID().equals("TOP")) {
            sendConsoleMessage(parentNode.getAddress(), parentNode.getPort(), "TOP", message.getMessageID(),
                    message.getData(), message.getName().trim());

            for (Map.Entry<ConsoleMessage, Node> entry : receivedMessages.entrySet()) {
                if (entry.getKey().getMessageID().equals(message.getMessageID()) &&
                        entry.getValue().getAddress().equals(packet.getAddress()) &&
                        entry.getValue().getPort() == packet.getPort()) {
                    sendResponse(packet.getAddress(), packet.getPort(), message);
                    return;
                }
            }

            sendResponse(packet.getAddress(), packet.getPort(), message);
            receivedMessages.put(message, new Node(packet.getAddress(), packet.getPort()));

            return;
        }

        if (message.getSenderID().equals(connection.getNodeID().toString())) {
            return;
        }

        if (message.getReceiverID().equals(connection.getNodeID().toString()) ||
                message.getReceiverID().equals("ALL")) {

            for (Map.Entry<Node, Integer> entry : babies.entrySet()) {
                sendConsoleMessage(entry.getKey().getAddress(), entry.getKey().getPort(), "ALL",
                        message.getMessageID(), message.getData(), message.getName().trim());
            }

            for (Map.Entry<ConsoleMessage, Node> entry : receivedMessages.entrySet()) {
                if (entry.getKey().getMessageID().equals(message.getMessageID()) &&
                        entry.getValue().getAddress().equals(packet.getAddress()) &&
                        entry.getValue().getPort() == packet.getPort()) {
                    sendResponse(packet.getAddress(), packet.getPort(), message);
                    return;
                }
            }

            System.out.println(message.getName().trim() + " said: " + message.getData());
            sendResponse(packet.getAddress(), packet.getPort(), message);
            receivedMessages.put(message, new Node(packet.getAddress(), packet.getPort()));
        }
    }

    private void receiveResponse(DatagramPacket packet) {
        ResponseMessage message = null;
        try {
            message = new ResponseMessage(packet);
        } catch (BadMessageArgumentsException e) {
            e.printStackTrace();
        }

        if (message.getSenderID().equals(connection.getNodeID().toString())) {
            return;
        }

        if (message.getReceiverID().equals(connection.getNodeID().toString()) ||
                message.getReceiverID().equals("ALL") || message.getReceiverID().equals("TOP")) {
            for (Map.Entry<ConsoleMessage, Node> entry: sentMessages.entrySet()) {
                if (entry.getKey().getMessageID().equals(message.getMessageID())) {
                    String controlSumFromMap =
                            ControlSumCalculator.calculateControlSum(entry.getKey().getData().getBytes());
                    String controlSumFromMessage = message.getData().trim();

                    if (controlSumFromMap.equals(controlSumFromMessage)) {
                        //System.out.println("You said: " + entry.getKey().getData());
                        sentMessages.remove(entry.getKey());
                    } else {
                        //System.out.println("Bad check sum");
                    }
                }
            }
        }
    }

    private int recogniseMessage(byte[] bytes) {
        String receivedString = new String(bytes);
        String[] parts = receivedString.split(Message.separator);

        return Integer.parseInt(parts[0]);
    }

    private int generateLostProbability() {
        return (int) ((Math.random() * 99));
    }

    private void updateTTL() {
        if (parentNodeTTL > 0) {
            parentNodeTTL--;
        }

        if (reserveParentNodeTTL > 0) {
            reserveParentNodeTTL--;
        }

        for (Map.Entry<Node, Integer> entry : babies.entrySet()) {
            babies.put(entry.getKey(), entry.getValue() - 1);
            if (entry.getValue() <= 0) {
                babies.remove(entry.getKey());
            }
        }

        if (parentNode != null || reserveParentNode != null) {
            if (parentNodeTTL == 0) {
                parentNodeTTL = -1;
                if (reserveParentNode.getPort() != 0) {
                    parentNode = reserveParentNode;
                }
            }
        }

        if (reserveParentNodeTTL == 0) {
            reserveParentNodeTTL = -1;
        }

    }

    private void resendLostMessages() {
        Iterator<Map.Entry<ConsoleMessage, Node>> iter = sentMessages.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ConsoleMessage, Node> entry = iter.next();

            sendConsoleMessage(entry.getValue().getAddress(), entry.getValue().getPort(), entry.getKey().getReceiverID(),
                    entry.getKey().getMessageID(), entry.getKey().getData(), entry.getKey().getName());
            //sentMessages.put(entry.getKey(), entry.getValue() - 1);
            // System.out.println("Message resent");

        }
    }
}