package dev.terna.janelle.database;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dev.terna.janelle.bplustree.BPlusTree;
import dev.terna.janelle.database.storage.Disk;
import dev.terna.janelle.database.storage.Memory;
import dev.terna.janelle.database.storage.StorageHandler;
import dev.terna.janelle.database.storage.StorageMedium;
import dev.terna.janelle.sql.Order;
import dev.terna.janelle.sql.Query;
import dev.terna.janelle.sql.postfixexpression.Item;

public class Table implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Column[] schema;
    private int rowSequenceId;
    private final int rowSizeInBytes;
    private final BPlusTree data;
    private transient StorageHandler storageHandler;
    private final List<Long> freeRowBlocks; // Pointers to deleted (thus free) row blocks in the db table file.
    private static final String ROW_ID_COLUMN_NAME = "row_id";

    public Table(String name, Column[] schema) {
        this(name, schema, StorageMedium.DISK);
    }
    
    private Table(String name, Column[] schema, StorageMedium storageMedium) {
       this.name = name;
       this.schema = schema;
       rowSequenceId = 0;
       rowSizeInBytes = Stream.of(schema).mapToInt(Column::getSize).sum();
       data = new BPlusTree(10);
       freeRowBlocks = new ArrayList<>();
       switch (storageMedium) {
           case DISK -> storageHandler = new Disk(name);
           case MEMORY -> storageHandler = new Memory();
       }
    }

    public static void create(String name, Column[] schema) {
        final var rowId = new Column(ROW_ID_COLUMN_NAME, DataType.INT, true, false, null);
        final var fullSchema = Stream.concat(Arrays.stream(new Column[] { rowId }), Arrays.stream(schema)).toArray(Column[]::new);

        final var table = new Table(name, fullSchema);
        table.storageHandler.flushMetadata(table);
    }

    /**
     * Fetch table from disk.
     */
    public static Table load(String name) {
        final var fileHandler = new Disk(name);
        final var table = fileHandler.loadTable();
        if (table == null) {
            return null;
        }
        table.storageHandler = fileHandler;
        return table;
    }

    public String getName() {
        return name;
    }

    public int getRowSequenceId() {
        return rowSequenceId;
    }

    private int newRowSequenceId() {
        return ++rowSequenceId;
    }

    public Column[] getSchema() {
        return schema;
    }

    public BPlusTree getData() {
        return data;
    }

    /**
     * Extract fields in row.
     */
    private byte[][] bytesToRow(byte[] bytes) {
        final var row = new byte[schema.length][];

        var fieldStartIndex = 0;
        for (var fieldIndex = 0; fieldIndex < schema.length; fieldIndex++) {
            final var column = schema[fieldIndex];
            final var fieldEndIndex = fieldStartIndex + column.getSize();
            final var field = Arrays.copyOfRange(bytes, fieldStartIndex, fieldEndIndex);
            row[fieldIndex] = field;

            fieldStartIndex = fieldEndIndex;
        }

        return row;
    }

    /**
     * Fetch a range of rows.
     */
    private List<byte[][]> fetchRange(long fromRow, long toRow) {
        List<byte[][]> rows = new ArrayList<>();

        List<Long> rowPointers = data.search(fromRow, toRow);
        for (var rowPointer : rowPointers) {
            final var seekPosition = (long) rowPointer;
            final var bytes = storageHandler.readData(seekPosition, rowSizeInBytes);
            rows.add(bytesToRow(bytes));
        }

        return rows;
    }

    /**
     * Filter out fields for unspecified columns in query.
     */
    private Object[] filterColumns(Object[] row, List<String> columnNames) {
        if (columnNames.isEmpty()) {
            // No columns specified so return all fields in the row.
            return row;
        }

        List<Object> newRow = new ArrayList<>();
        for (var columnName : columnNames) {
            final var columnIndex = IntStream.range(0, schema.length)
                    .filter(i -> schema[i].getName().equals(columnName))
                    .findFirst()
                    .orElseThrow();
            newRow.add(row[columnIndex]);
        }
        return newRow.toArray();
    }

    public Object[] deserializeRow(byte[][] row, Column[] schema) {
        final var rowObject = new Object[row.length];
        for (var fieldIndex = 0; fieldIndex < row.length; fieldIndex++) {
            rowObject[fieldIndex] = schema[fieldIndex].getDataType().getData(row[fieldIndex]);
        }
        return rowObject;
    }

    public List<byte[][]> selectAll() {
        return fetchRange(1, rowSequenceId); // TODO: Use Table#select.
    }

    public Result select(List<String> columns, List<Item> whereClause, LinkedHashMap<String, Order> orderByClause, long limit) {
        List<byte[][]> serializedRows = fetchRange(1, rowSequenceId);
        List<Object[]> rows = new ArrayList<>();
        for (var serializedRow : serializedRows) {
            var row = deserializeRow(serializedRow, schema);

            // where filter

            // order by filter

            // limit filter

            // columns filter
            row = filterColumns(row, columns);

            rows.add(row);
        }

        final var resultColumns = columns.isEmpty()
                ? Arrays.stream(schema).map(Column::getName).toArray(String[]::new)
                : columns.toArray(String[]::new);
        return new Result(resultColumns, rows.toArray(Object[][]::new), this);
    }

    public void insert(Map<String, Object> newData) throws Exception {
        // Add row ID.
        final var rowId = newRowSequenceId();
        newData.put(ROW_ID_COLUMN_NAME, rowId);

        // Convert data to bytes.
        final var outputStream = new ByteArrayOutputStream();
        for (var column : schema) {
            final var fieldValue = newData.get(column.getName());
            column.validate(fieldValue);

            final var fieldValueBytes = column.getDataType().getBytes(fieldValue, column.getSize());
            outputStream.write(fieldValueBytes);
        }

         // Write bytes to disk.
        long seekPosition;
        // Check for free row blocks.
        if (freeRowBlocks.size() > 0) {
            seekPosition = freeRowBlocks.get(0); // Take first block.
            freeRowBlocks.remove(0); // It's no longer free now.
        } else {
            // No free blocks? Go to end of file.
            seekPosition = storageHandler.getEOFPointerForData();
        }
        if (seekPosition == 0) { // File is empty.
            // First 8 bytes is for storing number of rows, so move forward by 8 bytes.
            seekPosition = 8 * 8;
        }
        storageHandler.writeData(seekPosition, outputStream.toByteArray());
        
        // Update table size.
        var numRowsBytes = storageHandler.readData(0, 8);
        var numRows = ByteBuffer.wrap(numRowsBytes).getLong();
        numRows++;
        numRowsBytes = ByteBuffer.allocate(8).putLong(numRows).array();
        storageHandler.writeData(0, numRowsBytes);

        // Update B+ tree.
        data.insert(rowId, seekPosition);
        storageHandler.flushMetadata(this);
    }

    public void update(Query query) {}

    public void delete(Query query) {}

    /**
     * Get count of all records in the table.
     * This is stored in the first 8 bytes of the data file.
     */
    public long countAll() {
        final var numRowsBytes = storageHandler.readData(0, 8);
        return ByteBuffer.wrap(numRowsBytes).getLong();
    }
}
