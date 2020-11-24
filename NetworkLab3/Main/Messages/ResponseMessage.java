package Main.Messages;

import Main.ControlSumCalculator;

import java.net.DatagramPacket;

public class ResponseMessage implements MessageWithData{
    protected final String type = "ResponseMessage";
    protected final int argumentsProvide = 5;
    protected String senderID;
    protected String receiverID;
    protected String messageID;
    protected String data;

    public ResponseMessage(DatagramPacket packet) throws BadMessageArgumentsException {
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
    }

    public ResponseMessage(String senderID, String receiverID, String messageID, String data) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.messageID = messageID;
        this.data = data;
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

    @Override
    public String toString() {
        return String.valueOf(MessageTypes.RESPONSE_MESSAGE.getInt()) +
                separator + senderID + separator + receiverID + separator + messageID + separator + data;
    }
}
