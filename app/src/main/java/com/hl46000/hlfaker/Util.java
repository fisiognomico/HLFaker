package com.hl46000.hlfaker;

import java.net.InetAddress;
import java.util.Random;

public class Util {
    public static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static int inetAddressToInt(InetAddress inetaddress) throws IllegalArgumentException {
        byte[] abyte0 = inetaddress.getAddress();
        if (abyte0.length == 4) {
            return ((((abyte0[3] & 0xFF) << 24) | ((abyte0[2] & 0xFF) << 16)) | ((abyte0[1] & 0xFF) << 8)) | (abyte0[0] & 0xFF);
        }
        throw new IllegalArgumentException("Not an IPv4 address");
    }
}

