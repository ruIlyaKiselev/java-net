package Main.Messages;

import java.util.Objects;
import java.util.UUID;

public interface Message {
    final String separator = "__";

    public String getType();
    public String getReceiverID();
    public String getSenderID();
    public int getArgumentsProvide();

    enum MessageTypes {
        I_AM_HERE_MESSAGE(0),
        CONSOLE_MESSAGE(1),
        RESPONSE_MESSAGE(2),
        NODES_MESSAGE(3);

        private final int value;
        private MessageTypes(int value) {
            this.value = value;
        }

        public int getInt() {
            return value;
        }
    }
}
