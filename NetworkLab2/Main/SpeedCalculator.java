package Main;

public class SpeedCalculator {

    public static void printFormatTime(long startPeriodTime, long startTotalTime, long bytesReceivedForPeriod,
                                       long totalBytesReceived) {
        String speedForPeriod = "Speed for period: " + calculateSpeed(bytesReceivedForPeriod,
                getTime() - startPeriodTime);
        String totalSpeed = "Total speed: " + calculateSpeed(totalBytesReceived,
                getTime() - startTotalTime);

        System.out.println(speedForPeriod + " " + totalSpeed);
    }

    public static String calculateSpeed(long bytesWasTransfer, long millsForThisBytes) {
        int bytesPerSeconds;
        try {
            bytesPerSeconds = (int)((double)bytesWasTransfer / ((double)millsForThisBytes / 1000L));
        } catch (ArithmeticException e) {
            bytesPerSeconds = 0;
        }
        String result = "";

        if (bytesPerSeconds <= 1024) {
            result += bytesPerSeconds + " B/S";
        } else if (bytesPerSeconds <= 1024 * 1024) {
            result += bytesPerSeconds + " KB/S";
        } else if (bytesPerSeconds <= 1024 * 1024 * 1024) {
            result += bytesPerSeconds + " MB/S";
        } else {
            result += bytesPerSeconds + " GB/S";
        }

        return result;
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }
}
