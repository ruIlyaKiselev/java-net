package Main;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    protected Socket socket;
    protected File file;
    protected FileInputStream fin;
    protected DataInputStream in;
    protected DataOutputStream out;
    protected long fileSize;
    protected String fileName;
    protected String calculatedControlSum;

    public Client(String filePath, String address, int port) {
        try {
            socket = new Socket(address, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Bad IP: " + e.getMessage());
        } catch (IOException e1) {
            System.err.println("Couldn't get IO: " + e1.getMessage());
        }

        process(filePath, address, port);
    }

    public void process(String filePath, String address, int port) {
        loadFile(filePath);
        calculatedControlSum = ControlSumCalculator.calculateControlSum(file);
        sendFileInfo();
        sendFile();
        receiveServerResponse();
    }

    protected void loadFile(String filePath) {
        file = new File(filePath);
        fileSize = file.length();
        fileName = file.getName();
        try {
            fin = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file for path " + filePath + " : " + e.getMessage());
        }
    }

    protected void sendFile() {
        BufferedInputStream bufferStream = new BufferedInputStream(fin, TCPConnection.DEFAULT_BUFFER_SIZE);
        long bytesSent = 0;

        try {
            int lastBytesSent = 0;
            byte[] tmp = new byte[TCPConnection.DEFAULT_BUFFER_SIZE];
            while ((lastBytesSent = bufferStream.read(tmp)) != -1) {
                out.write(tmp, 0, lastBytesSent);
                bytesSent += lastBytesSent;
            }

            bufferStream.close();
        } catch (IOException e) {
            System.err.println("Problem in reading file: " + e.getMessage());
        }
    }

    protected void sendFileInfo() {
        try {
            out.writeLong(fileSize);
            out.writeUTF(fileName);
            out.writeUTF(calculatedControlSum);
        } catch (IOException e) {
            System.err.println("Problem in sending file info: " + e.getMessage());
        }
    }

    protected void receiveServerResponse() {
        try {
            String response = in.readUTF();
            if (response.equals("ok")) {
                System.out.println("File sent successful");
            } else {
                System.out.println("File sent with damage");
            }

        } catch (IOException e) {
            System.err.println("Problem in receiving response from server: " + e.getMessage());
        }
    }
}
