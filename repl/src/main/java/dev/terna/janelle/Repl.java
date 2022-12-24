package dev.terna.janelle;

import dev.terna.janelle.database.Column;
import dev.terna.janelle.database.DataType;
import dev.terna.janelle.database.Database;

public class Repl {
    public static void main(String[] args) {
        final var db = new Database();

        final var idColumn = new Column("id", DataType.INT);
        final var schema = new Column[] { idColumn };
        db.createTable("customers", schema);
    }
}
