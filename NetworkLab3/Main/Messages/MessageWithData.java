package Main.Messages;

public interface MessageWithData extends Message {
    public String getMessageID();
    public String getData();
}
