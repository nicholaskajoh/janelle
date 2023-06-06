package dev.terna.janelle.database;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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

    public static void printQueryResult(Result result) {
        final var tt = new TextTable(result.getColumns(), result.getRows());
        tt.printTable();
    }

    public static String renderQueryResult(Result result) {
        final var tt = new TextTable(result.getColumns(), result.getRows());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            tt.printTable(ps, 0);
            return baos.toString(utf8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
