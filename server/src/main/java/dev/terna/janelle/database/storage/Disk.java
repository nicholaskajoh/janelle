package dev.terna.janelle.database.storage;

import dev.terna.janelle.database.Table;
import dev.terna.janelle.database.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

public class Disk implements StorageHandler {
    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_EXTENSION = ".milan";
    public enum DataFile {
        data, // table rows
        metadata, // table object
    };
    private String dataFilePath;
    private RandomAccessFile dataFile;
    private String metadataFilePath;

    public Disk(String tableName) {
        dataFilePath = getFilePath(tableName, DataFile.data);
        metadataFilePath = getFilePath(tableName, DataFile.metadata);
        setupFiles(tableName);
    }

    private String getFilePath(String tableName, DataFile dataFile) {
        return DATA_DIRECTORY + File.separatorChar + tableName + File.separatorChar + dataFile.name() + FILE_EXTENSION;
    }

    private void setupFiles(String tableName) {
        File directory = new File(DATA_DIRECTORY + File.separatorChar + tableName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {
            File file = new File(dataFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            dataFile = new RandomAccessFile(dataFilePath, "rw");
        } catch (IOException e) {
            Utils.panic("Error occurred while retrieving data file.", e);
        }

        try {
            File file = new File(metadataFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Utils.panic("Error occurred while retrieving metadata file.", e);
        }
    }

    @Override
    public Table loadTable() {
        try {
            final var metadataFIS = new FileInputStream(metadataFilePath);
            final var metadataOIS = new ObjectInputStream(metadataFIS);
            final Table table = (Table) metadataOIS.readObject();
            metadataOIS.close();
            return table;
        } catch (IOException | ClassNotFoundException e) {
            // Table not found, or something went terribly wrong.
            return null;
        }
    }

    /**
     * Serialize and write table object to file.
     */
    @Override
    public void flushMetadata(Table table) {
        try {
            final var metadataFOS = new FileOutputStream(metadataFilePath);
            final var metadataOOS = new ObjectOutputStream(metadataFOS);
            metadataOOS.writeObject(table);
            metadataOOS.flush();
            metadataOOS.close();
        } catch (IOException e) {
            Utils.panic("Error occurred while writing to metadata file.", e);
        }
    }

    @Override
    public long getEOFPointerForData() {
        try {
            return dataFile.length();
        } catch (IOException e) {
            Utils.panic("Error occurred while obtaining end of data file pointer.", e);
            return 0L;
        }
    }

    @Override
    public byte[] readData(long seekPosition, int numberOfBytes) {
        try {
            dataFile.seek(seekPosition);
            byte[] bytes = new byte[numberOfBytes];
		    dataFile.read(bytes);
            return bytes;
        } catch (IOException e) {
            Utils.panic("Error occurred while writing to data file.", e);
            return new byte[] {};
        }
    }

    @Override
    public void writeData(long seekPosition, byte[] data) {
        try {
            dataFile.seek(seekPosition);
            dataFile.write(data);
        } catch (IOException e) {
            Utils.panic("Error occurred while writing to data file.", e);
        }
    }

    @Override
    public void close() {
        try {
            dataFile.close();
        } catch (IOException e) {
            Utils.complain("Error occurred while closing db files.", e);
        }
    }
}
