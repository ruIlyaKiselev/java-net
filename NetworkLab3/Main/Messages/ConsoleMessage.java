package Main.Messages;

import java.net.DatagramPacket;
import java.util.UUID;

public class ConsoleMessage implements MessageWithData {
    protected final String type = "ConsoleMessage";
    protected final int argumentsProvide = 6;
    protected String senderID;
    protected String receiverID;
    protected String messageID;
    protected String data;
    protected String name;

    public ConsoleMessage(DatagramPacket packet) throws BadMessageArgumentsException {
        byte[] bytes = packet.getData();
        String receivedString = new String(bytes);
        String[] parts = receivedString.split(separator);

        if (parts.length != argumentsProvide) {
            throw new BadMessageArgumentsException(type, argumentsProvide, parts.length);
        }

        senderID = parts[1];
        receiverID = parts[2];
        messageID = parts[3];
        data = parts[4];
        name = parts[5];
    }

    public ConsoleMessage(String senderID, String receiverID, String messageID, String message, String name) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.messageID = messageID;
        this.data = message;
        this.name = name;
    }

    public int length() {
        return this.toString().getBytes().length;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getReceiverID() {
        return receiverID;
    }

    @Override
    public String getSenderID() {
        return senderID;
    }

    @Override
    public int getArgumentsProvide() {
        return argumentsProvide;
    }

    @Override
    public String getMessageID() {
        return messageID;
    }

    @Override
    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.valueOf(MessageTypes.CONSOLE_MESSAGE.getInt()) +
                separator + senderID + separator + receiverID + separator + messageID +
                separator + data + separator + name;
    }
}
