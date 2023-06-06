package dev.terna.janelle.database;

public class Result {
    private final String[] columns;
    private final Object[][] rows;
    private final Table source;

    public Result(String[] columns, Object[][] rows, Table source) {
        this.columns = columns;
        this.rows = rows;
        this.source = source;
    }

    public String[] getColumns() {
        return columns;
    }

    public Object[][] getRows() {
        return rows;
    }

    public Table getSource() {
        return source;
    }
}
