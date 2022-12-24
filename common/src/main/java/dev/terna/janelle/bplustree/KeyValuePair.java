package dev.terna.janelle.bplustree;

import java.io.Serializable;

public class KeyValuePair implements Comparable<KeyValuePair>, Serializable {
    private static final long serialVersionUID = 1L;
    int key;
    Object value;

    public KeyValuePair(int key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(KeyValuePair otherKvp) {
        if (key == otherKvp.key) {
            return 0;
        } else if (key > otherKvp.key) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return key + " => " + value;
    }
}
