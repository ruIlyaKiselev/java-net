package Main;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 3) {
            String name = args[0];
            int port = Integer.parseInt(args[1]);
            int lostProbability = Integer.parseInt(args[2]);

            User user = new User(name, port, lostProbability);
            user.process();
        } else if (args.length == 5) {
            String name = args[0];
            int port = Integer.parseInt(args[1]);
            int lostProbability = Integer.parseInt(args[2]);
            String parentAddress = args[3];
            int parentPort = Integer.parseInt(args[4]);

            User user = new User(name, port, lostProbability, parentAddress, parentPort);
            user.process();
        }

//        User user1 = new User("Ilya1", Connection.DEFAULT_PORT, 50);
//        user1.process();

//        User user2 = new User("Ilya2", Connection.DEFAULT_PORT + 1, 50,
//                "127.0.0.1", Connection.DEFAULT_PORT);
//        user2.process();

//        User user3 = new User("Ilya3", Connection.DEFAULT_PORT + 2, 50,
//                "127.0.0.1", Connection.DEFAULT_PORT + 1);
//        user3.process();

//        User user4 = new User("Ilya4", Connection.DEFAULT_PORT + 3, 50,
//                "127.0.0.1", Connection.DEFAULT_PORT + 2);
//        user4.process();
    }
}
