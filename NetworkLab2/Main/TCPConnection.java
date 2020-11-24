package Main;

import java.io.*;
import java.net.Socket;

public class TCPConnection extends Thread {

    public final static String DEFAULT_ADDRESS = "127.0.0.1";
    public final static int DEFAULT_PORT = 8081;
    public final static int DEFAULT_BUFFER_SIZE = 1024;
    public static final String DEFAULT_UPLOAD_DIRECTORY = "uploads";
    public static final long TIME_FOR_TIME_CALCULATOR = 3000;

    protected Socket client;
    protected DataOutputStream out;
    protected DataInputStream in;
    protected long fileSize;
    protected File file;
    protected String fileName;
    protected String controlSumFromClient;
    protected String calculatedControlSum;

    public TCPConnection(Socket client) {
        this.client = client;
        try {
            out = new DataOutputStream(client.getOutputStream());
            in = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            try {
                System.err.println("Problem in creating TCP connection: " + e.getMessage());
                client.close();
            } catch (IOException e1) {
                System.err.println("Unable to set up streams " + e.getMessage());
                return;
            }
        }

        start();
    }

    public void run() {
        receiveFileInfo();
        createFile();
        receiveFile();
        calculatedControlSum = ControlSumCalculator.calculateControlSum(file);
        sendResponse();

        try {
            client.close();
        } catch (IOException e) {
            System.err.println("Cannot close client socket");
        }
    }

    protected void receiveFileInfo() {
        try {
            fileSize = in.readLong();
            fileName = in.readUTF();
            controlSumFromClient = in.readUTF();
        } catch (IOException e) {
            System.err.println("Problem in receiving file info: " + e.getMessage());
        }
    }

    protected void sendResponse() {
        try {
            if (calculatedControlSum.equals(controlSumFromClient)) {
                out.writeUTF("ok");
                System.out.println("File " + fileName + " received successful");
            } else {
                out.writeUTF("(((");
                System.out.println("File " + fileName + " received with damage");
            }
        } catch (IOException e) {
            System.err.println("Problem in sending response: " + e.getMessage());
        }
    }

    protected void createFile() {
        if(Server.containsFilename(fileName)) {
            fileName += "_";
        }

        String path = DEFAULT_UPLOAD_DIRECTORY + File.separator + fileName;

        file = new File(path);

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Unable to create new file " + DEFAULT_UPLOAD_DIRECTORY + "/" + fileName + " " +
                    e.getMessage());
        }
    }

    protected void receiveFile() {
        try {
            int lastBytesReceived = 0;
            long bytesReceivedForPeriod = 0;
            long totalBytesReceived = 0;
            byte[] tmp = new byte[TCPConnection.DEFAULT_BUFFER_SIZE];
            BufferedInputStream fin = new BufferedInputStream(in);
            FileOutputStream fout = new FileOutputStream(file);
            BufferedOutputStream bufFout = new BufferedOutputStream(fout, TCPConnection.DEFAULT_BUFFER_SIZE);

            long startPeriodTime = SpeedCalculator.getTime();
            final long startTotalTime = startPeriodTime;

            while ((lastBytesReceived = fin.read(tmp)) != -1) {
                bufFout.write(tmp, 0, lastBytesReceived);
                bufFout.flush();
                bytesReceivedForPeriod += lastBytesReceived;
                totalBytesReceived += lastBytesReceived;

                if(lastBytesReceived < TCPConnection.DEFAULT_BUFFER_SIZE) {
                    break;
                }

                if(SpeedCalculator.getTime() - startPeriodTime >= TIME_FOR_TIME_CALCULATOR) {
                    SpeedCalculator.printFormatTime(startPeriodTime, startTotalTime, bytesReceivedForPeriod,
                            totalBytesReceived);
                    startPeriodTime = SpeedCalculator.getTime();
                    bytesReceivedForPeriod = 0;
                }
            }

            SpeedCalculator.printFormatTime(startPeriodTime, startTotalTime, bytesReceivedForPeriod,
                    totalBytesReceived);

        } catch (FileNotFoundException e) {
            System.err.println("Unable to create filestream for " + DEFAULT_UPLOAD_DIRECTORY + "/" + fileName + " " +
                    e.getMessage());
        } catch (IOException e1) {
            System.err.println("Problem in receiving file: " + e1.getMessage());
        }
    }
}
