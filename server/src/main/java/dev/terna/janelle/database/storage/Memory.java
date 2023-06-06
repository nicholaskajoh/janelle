package dev.terna.janelle.database.storage;

import dev.terna.janelle.database.Table;

import java.util.ArrayList;
import java.util.List;

public class Memory implements StorageHandler {
    Table metadata;
    List<Byte> data = new ArrayList<>();

    @Override
    public Table loadTable() {
        return metadata;
    }

    @Override
    public void flushMetadata(Table table) {
        // Do nothing.
    }

    @Override
    public long getEOFPointerForData() {
        return data.size();
    }

    @Override
    public byte[] readData(long seekPosition, int numberOfBytes) {
        byte[] b = new byte[numberOfBytes];

        var pointer = seekPosition;
        for (var i = 0; i < numberOfBytes; i++) {
            b[i] = data.get((int) pointer);
            pointer++;
        }

        return b;
    }

    @Override
    public void writeData(long seekPosition, byte[] newData) {
        if (seekPosition == data.size()) {
            for (var b : newData) {
                data.add(b);
            }
        } else {
            var pointer = seekPosition;
            for (var b : newData) {
                data.set((int) pointer, b);
                pointer++;
            }
        }
    }

    @Override
    public void close() {
        // Do nothing.
    }
}
