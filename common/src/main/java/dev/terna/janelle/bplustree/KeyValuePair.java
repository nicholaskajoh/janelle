package dev.terna.janelle.bplustree;

import dev.terna.janelle.database.Row;

public class KeyValuePair implements Comparable<KeyValuePair> {
    int key;
    Row value;

    public KeyValuePair(int key, Row value) {
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
