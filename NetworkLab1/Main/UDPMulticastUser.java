package Main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UDPMulticastUser {

    public static final int DEFAULT_TTL = 10;

    private UDPMulticastConnection connection;
    private ConcurrentHashMap<String, Integer> users;
    private ConcurrentHashMap<String, String> addresses;

    public UDPMulticastUser() {
        connection = new UDPMulticastConnection();
        users = new ConcurrentHashMap<>();
        addresses = new ConcurrentHashMap<>();
    }

    public void process() {
        int lastNumberOfUsers = 0;

        while (true) {
            connection.sendPacket(connection.getLocalAddress().getHostAddress());

            for (Map.Entry<String, Integer> entry : users.entrySet()) {
                entry.setValue(new Integer(entry.getValue() - 1)); // TODO: replace deprecated method
                if (entry.getValue().intValue() <= 0) {
                    users.remove(entry.getKey());
                    lastNumberOfUsers--;
                    printUsers();
                }
            }

            if (lastNumberOfUsers != users.size()) {
                printUsers();
                lastNumberOfUsers = users.size();
            }

            long startTime = getTime();
            while (getTime() - startTime < connection.DEFAULT_SOCKET_TIMEOUT) {

                try {
                    String message = new String(connection.receivePacket().getData());
                    String key = message.substring(message.lastIndexOf(" ") + 1).trim();
                    String IP = message.substring(0, message.indexOf(' '));

                    users.put(key, new Integer(DEFAULT_TTL));
                    addresses.put(key, IP);
                } catch (NullPointerException e) {
                    continue;
                }
            }
        }
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

    private void printUsers() {
        System.out.println("\n========================== UPDATE ==========================");
        System.out.println("Number of users: " + users.size());
        System.out.println("============================================================");
        for (String key: users.keySet()) {
            if (key.equals(connection.nodeID.toString())) {
                System.out.println("User: " + key + " IP: " + addresses.get(key) + " (You)");
            } else {
                System.out.println("User: " + key + " IP: " + addresses.get(key) + " (other)");
            }
        }
    }
}
