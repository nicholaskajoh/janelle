package dev.terna.janelle.bplustree;

import java.io.Serializable;

public class Entry implements Comparable<Entry>, Serializable {
    private static final long serialVersionUID = 1L;
    long key;
    long value;

    public Entry(long key, long value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(Entry otherEntry) {
        if (key == otherEntry.key) {
            return 0;
        } else if (key > otherEntry.key) {
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
