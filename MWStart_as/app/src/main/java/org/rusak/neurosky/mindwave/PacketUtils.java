package org.rusak.neurosky.mindwave;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PacketUtils {
    public static final byte[] UPSCALED02ALT = new byte[] {0x00, 0x7E, 0x00, 0x00, 0x00, (byte)0xF8};
    public static final byte[] UPSCALED02 = new byte[] {0x00, (byte)0xF8, 0x00, 0x00, 0x00, (byte)0xE0};

    public static void clearBuffer(InputStream is) {
        int avail;
        try {
            avail = is.available();
            is.skip(avail);
        } catch (IOException e) {
        }
    }

    /**
     * Read bytes for timeout time.
     * @param streamIn
     * @param data
     * @param timeout
     * @throws IOException
     */
    protected static void readBytesForPeriod(InputStream streamIn, byte[] data, int timeout) throws IOException {
        int pos = 0;
        long t1 = System.currentTimeMillis() + timeout;

        while (pos < data.length && System.currentTimeMillis() <= t1) {
            int n = streamIn.available();
            if(n < 0) continue;

            if (pos + n > data.length)
                n = data.length - pos;
            //Log.v(TAG, "reading "+n+" bytes");

            streamIn.read(data, pos, n);
            pos += n;
        }

        if (pos < data.length)
            new IOException("Timeout occured while reading "+data.length+" bytes at position "+pos+".");
    }
    /**
     * Read 512 bytes to check if input stream belongs to device in RAW mode
     * If true packet contains RAW data and is valid
     * @param streamIn
     * @return
     */
    public static boolean isPacketRaw(InputStream streamIn){
        clearBuffer(streamIn);
        byte[] data512 = new byte[512];
        try {
            readBytesForPeriod(streamIn, data512, 2000);
        } catch(IOException e) {
            return false;
        }

        return isRawPacketValid(data512);
    }

    /**
     * Check if received bytes are part of proper RAW mode packet
     *
     * The Header of a Packet consists of 3 bytes:
     * 	two synchronization [SYNC] bytes
     * 	 - 0xAA 0xAA,
     * 	followed by a [PLENGTH] (Payload length) byte:
     *   - 0x04 - at high speed(57600)
     *   - or 0x20 - at low speed(9600)
     *  and data:
     *   - 0x80 0x02 - RAW packet, length 0x02(56700)
     *   - or 0x02 0xXX - signal, value XX(9600);
     */
    public static boolean isRawPacketValid(byte[] data) {
//		for (int i = 0 ; i < data.length ; i += 16) {
//			String out = "";
//			for (int j = 0 ; i + j < data.length ; j++)
//				out += String.format("%02x ", data[i+j]);
//			Log.v("MWStart", out);
//		}
        for (int i = 0 ; i < data.length - 8 ; i++) {
            int len;
            if (data[i] == (byte)0xAA && data[i+1] == (byte)0xAA && (len = 0xFF&(int)data[i+2]) == 4 && data[i+3] == (byte)0x80) {
                //Log.v(TAG, "[OK] RAW packet found [AA AA 04 80 ...] at "+i);

                if (i + len + 3 >= data.length) {
                    continue;
                }
                byte sum = 0;
                for (int j = i + 3; j < i + 3 + len ; j++) {
                    sum += data[j];
                }
                //Log.v(TAG, "sum "+sum+" vs "+data[i+3+len]);

                return (0xFF&(sum ^ 0xFF)) == (0xFF&data[i + 3 + len]);
            }
        }
        return false;
    }
}
