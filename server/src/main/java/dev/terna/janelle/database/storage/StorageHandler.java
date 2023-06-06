package dev.terna.janelle.database.storage;

import dev.terna.janelle.database.Table;

public interface StorageHandler {
    Table loadTable();

    void flushMetadata(Table table);

    long getEOFPointerForData();

    byte[] readData(long seekPosition, int numberOfBytes);

    void writeData(long seekPosition, byte[] data);

    void close();
}
