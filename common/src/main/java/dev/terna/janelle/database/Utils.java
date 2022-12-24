package dev.terna.janelle.database;

import java.util.stream.Stream;

import dnl.utils.text.table.TextTable;

public class Utils {
    public static void complain(String message, Exception e) {
        System.out.println(message);
        e.printStackTrace();
    }

    public static void panic(String message, Exception e) {
        complain(message, e);
        System.exit(1);
    }

    public static void printTable(Table table) {
        final var schema = table.getSchema();
        final var rows = table.selectAll();
        final String[] columnNames = Stream.of(schema).map(c -> c.getName()).toArray(String[]::new);
        final Object[][] data = new Object[rows.size()][];

        for (var rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            final var row = rows.get(rowIndex);
            data[rowIndex] = new Object[row.length];
            for (var fieldIndex = 0; fieldIndex < row.length; fieldIndex++) {
                try {
                    data[rowIndex][fieldIndex] = schema[fieldIndex].getDataType().getData(row[fieldIndex]);
                } catch (Exception e) {
                    panic("Error occured while printing table.", e);
                }
            }
        }
        TextTable tt = new TextTable(columnNames, data);

        System.out.println("***** " + table.getName() + " table *****");
        tt.printTable();
    }
}
