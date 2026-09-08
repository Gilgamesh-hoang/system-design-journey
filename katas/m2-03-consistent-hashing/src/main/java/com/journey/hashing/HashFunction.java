package com.journey.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFunction {
    private final MessageDigest md;

    public HashFunction() {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }

    public long hash(String key) {
        md.reset();
        md.update(key.getBytes());
        byte[] digest = md.digest();

        // Convert the first 8 bytes of MD5 digest to a 64-bit long
        long h = 0;
        for (int i = 0; i < 8; i++) {
            h <<= 8;
            h |= (digest[i] & 0xFF);
        }
        return h;
    }
}
