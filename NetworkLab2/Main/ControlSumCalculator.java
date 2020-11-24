package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ControlSumCalculator {

    public static String calculateControlSum(File file) {
        String calculatedControlSum = "";
        String fileName = file.getName();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);

            while (dis.read() != -1);//empty loop to clear the data
            md = dis.getMessageDigest();

            byte[] hashInBytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }

            calculatedControlSum = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Problem in creating control sum: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find file " + TCPConnection.DEFAULT_UPLOAD_DIRECTORY + "/" + fileName + " " +
                    e.getMessage());
        } catch (IOException e) {
            System.err.println("Problem in reading file: " + e.getMessage());
        }

        return calculatedControlSum;
    }
}
