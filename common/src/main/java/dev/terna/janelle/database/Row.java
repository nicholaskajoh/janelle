package dev.terna.janelle.database;

public class Row {
    String data;

    public Row(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }
}
