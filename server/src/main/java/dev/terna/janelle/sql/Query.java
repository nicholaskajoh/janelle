package dev.terna.janelle.sql;

import dev.terna.janelle.database.Column;
import dev.terna.janelle.sql.postfixexpression.Item;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Query {
    private Statement statement;
    private String table;
    private List<String> columns;
    private List<Object[]> rows;
    private HashMap<Column, Column> schema; // new column -> old column (or null if there's no old column)
    private List<Item> whereClause; // Postfix expression.
    private LinkedHashMap<String, Order> orderByClause; // column -> order
    private long limit; // Zero means no limit.

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Object[]> getRows() {
        return rows;
    }

    public void setRows(List<Object[]> rows) {
        this.rows = rows;
    }

    public HashMap<Column, Column> getSchema() {
        return schema;
    }

    public void setSchema(HashMap<Column, Column> schema) {
        this.schema = schema;
    }

    public List<Item> getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(List<Item> whereClause) {
        this.whereClause = whereClause;
    }

    public LinkedHashMap<String, Order> getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(LinkedHashMap<String, Order> orderByClause) {
        this.orderByClause = orderByClause;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
