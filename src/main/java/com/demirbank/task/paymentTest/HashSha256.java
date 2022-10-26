package com.demirbank.task.paymentTest;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashSha256 {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String getHash(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            return text;
        }
        final byte[] hashbytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashbytes);
    }

    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    
}
