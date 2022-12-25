package dev.terna.janelle.database;
 
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import dev.terna.janelle.bplustree.BPlusTree;

public class Table implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Column[] schema;
    private int rowSequenceId;
    private int rowSizeInBytes;
    private BPlusTree data;
    private BPlusTree[] indexes;
    private transient FileHandler fileHandler;
    private List<Long> freeRowBlocks; // Pointers to deleted (thus free) row blocks in the db table file.
    private static final transient String ROW_ID_COLUMN_NAME = "row_id";
    
    private Table(String name, Column[] schema) {
       this.name = name;
       this.schema = schema;
       rowSequenceId = 0;
       rowSizeInBytes = Stream.of(schema).mapToInt(Column::getSize).sum();
       data = new BPlusTree(10);
       indexes = new BPlusTree[schema.length];
       freeRowBlocks = new ArrayList<>();
       fileHandler = new FileHandler(name);
    }

    public static void create(String name, Column[] schema) {
        final var rowId = new Column(ROW_ID_COLUMN_NAME, DataType.INT, true, false, null);
        final var fullSchema = Stream.concat(Arrays.stream(new Column[] { rowId }), Arrays.stream(schema)).toArray(Column[]::new);

        final var table = new Table(name, fullSchema);
        table.fileHandler.flushMetadata(table);
    }

    /**
     * Fetch table from disk.
     */
    public static Table load(String name) {
        final var fileHandler = new FileHandler(name);
        final var table = fileHandler.loadTable();
        if (table == null) {
            return null;
        }
        table.fileHandler = fileHandler;
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
     * Select range of rows.
     */
    public List<byte[][]> select(long fromRow, long toRow) {
        List<byte[][]> rows = new ArrayList<>();

        List<Long> rowPointers = data.search(fromRow, toRow);
        for (var rowPointer : rowPointers) {
            final var seekPosition = (long) rowPointer;
            final var bytes = fileHandler.readData(seekPosition, rowSizeInBytes);
            rows.add(bytesToRow(bytes));
        }

        return rows;
    }

    public List<byte[][]> selectAll() {
        return select(1, rowSequenceId);
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

        /**
         * Write bytes to disk.
         */
        long seekPosition;
        // Check for free row blocks.
        if (freeRowBlocks.size() > 0) {
            seekPosition = freeRowBlocks.get(0); // Take first block.
            freeRowBlocks.remove(0); // It's no longer free now.
        } else {
            // No free blocks? Go to end of file.
            seekPosition = fileHandler.getEOFPointerForData();
        }
        if (seekPosition == 0) { // File is empty.
            // First 4 bytes is for storing number of rows, so move forward by 4 bytes.
            seekPosition = 4 * 8;
        }
        fileHandler.writeData(seekPosition, outputStream.toByteArray());
        
        // Update table size.
        var numRowsBytes = fileHandler.readData(0, 4);
        var numRows = ByteBuffer.wrap(numRowsBytes).getInt();
        numRows++;
        numRowsBytes = ByteBuffer.allocate(4).putInt(numRows).array();
        fileHandler.writeData(0, numRowsBytes);

        // Update B+ tree.
        data.insert(rowId, seekPosition);
        fileHandler.flushMetadata(this);
    }
}
