package dev.terna.janelle.bplustree;

import dev.terna.janelle.pager.Page;

public class KeyValuePair implements Comparable<KeyValuePair> {
    int key;
    Page value;

    public KeyValuePair(int key, Page value) {
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
