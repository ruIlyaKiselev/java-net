package Main.Messages;

import java.io.IOException;

public class BadMessageArgumentsException extends IOException {
    private int argumentsNumber;

    public int getNumber() {
        return argumentsNumber;
    }

    public BadMessageArgumentsException(String messageType, int correct, int actual) {
        super("Bad arguments number in message: for " + messageType  + " correct " + correct + ", actual " + actual);
    }
}
