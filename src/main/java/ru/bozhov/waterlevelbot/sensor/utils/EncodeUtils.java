package ru.bozhov.waterlevelbot.sensor.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EncodeUtils {
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public String encodeBase52(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int mod = (int) (num % ALPHABET.length);
            sb.insert(0, ALPHABET[mod]);
            num /= ALPHABET.length;
        }

        while (sb.length() < 8) {
            sb.insert(0, 'A');
        }
        return sb.toString();
    }
}
