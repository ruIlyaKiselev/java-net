package Main.Messages;

import java.net.DatagramPacket;

public class IAmHereMessage implements Message {
    protected final String type = "IAmHereMessage";
    protected final int argumentsProvide = 3;
    protected String senderID;
    protected String senderType;

    public IAmHereMessage(DatagramPacket packet) throws BadMessageArgumentsException {
        byte[] data = packet.getData();
        String receivedString = new String(data);
        String[] parts = receivedString.split(separator);

        if (parts.length != argumentsProvide) {
            throw new BadMessageArgumentsException(type, argumentsProvide, parts.length);
        }

        senderID = parts[1];
        senderType = parts[2];
    }

    public IAmHereMessage(String senderID, String senderType) {
        this.senderID = senderID;
        this.senderType = senderType;
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
        return senderType;
    } //TODO: FIX IT

    @Override
    public String getSenderID() {
        return senderID;
    }

    @Override
    public int getArgumentsProvide() {
        return argumentsProvide;
    }

    @Override
    public String toString() {
        return String.valueOf(MessageTypes.I_AM_HERE_MESSAGE.getInt()) +
                separator + senderID + separator + senderType;
    }
}
