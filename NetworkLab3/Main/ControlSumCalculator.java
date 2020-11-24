package Main;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ControlSumCalculator {

    public static String calculateControlSum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return String.valueOf(crc32.getValue());
    }
}
