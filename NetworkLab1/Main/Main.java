package Main;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        UDPMulticastUser user = new UDPMulticastUser();
        user.process();
    }
}
