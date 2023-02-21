package dev.terna.janelle.hash;

public class SimpleHash {
    public static long hash(String data) {
        long hash = 0;

        for (char c : data.toCharArray()) {
            int ascii = (int) c;
            hash += ascii;
            hash *= ascii;
        }

        return hash;
    }
}
