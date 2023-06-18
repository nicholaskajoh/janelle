package dev.terna.janelle.database;

import dev.terna.janelle.sql.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Database {
    private final Map<String, Table> tables;
    public static final String DB_CONFIGS_TABLE_NAME = "jn_configs";

    public Database() {
        tables = new HashMap<>();
        loadTables();
    }

    private void loadTables() {
        // Get the list of tables in the db from the configs table.
        Table configsTable = Table.load(DB_CONFIGS_TABLE_NAME);
        if (configsTable == null) {
            configsTable = createConfigsTable();
        }
        final var rows = configsTable.selectAll();
        final var schema = configsTable.getSchema();
        final var tableNames = getTableNames(rows, schema);
        
        for (var tableName : tableNames) {
            tables.put(tableName, Table.load(tableName));
        }
    }

    /**
     * Get list of table names from db config table.
     */
    private String[] getTableNames(List<byte[][]> rows, Column[] schema) {
        final Column keyColumn = Stream.of(schema).filter(c -> "key".equals(c.getName())).findFirst().get();
        final var keyColumnIndex = Arrays.asList(schema).indexOf(keyColumn);

        String[] tableNames = null;
        for (var row : rows) {
            String key = "";
            try {
                key = (String) keyColumn.getDataType().getData(row[keyColumnIndex]);
            } catch (Exception e) {
                Utils.panic("Error occured while fetching config db key column field.", e);
            }
            if ("tables".equals(key)) {
                final Column valueColumn = Stream.of(schema).filter(c -> "value".equals(c.getName())).findFirst().get();
                final var valueColumnIndex = Arrays.asList(schema).indexOf(valueColumn);
                String value = "";
                try {
                    value = (String) valueColumn.getDataType().getData(row[valueColumnIndex]);
                } catch (Exception e) {
                    Utils.panic("Error occured while fetching config db value column field.", e);
                }
                tableNames = value.split(",");
                break;
            }
        }
        return tableNames;
    }

    public void createTable(String name, Column[] schema) {
        Table.create(name, schema);
        tables.put(name, Table.load(name));
    }

    /**
     * Fetch table [metadata] from memory.
     */
    public Table getTable(String name) throws Exception {
        final var table = tables.get(name);
        if (table == null) {
            throw new Exception("Table \"" + name + "\" does not exist.");
        }
        return table;
    }

    /**
     * Create table for storing db configs.
     */
    private Table createConfigsTable() {
        final var keyColumn = new Column("key", DataType.STRING, true, false, null);
        keyColumn.setSize(20);
        final var valueColumn = new Column("value", DataType.STRING);
        final var schema = new Column[] { keyColumn, valueColumn };
        createTable(DB_CONFIGS_TABLE_NAME, schema);

        final var configsTable = Table.load(DB_CONFIGS_TABLE_NAME);
 
        // Initial configs
        final var tableNames = new String[] { DB_CONFIGS_TABLE_NAME };
        saveDbConfig(configsTable, "tables", String.join(",", tableNames));

        return configsTable;
    }

    private void saveDbConfig(Table configsTable, String key, String value) {
        final Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("value", value);

        try {                         
            configsTable.insert(data);
        } catch (Exception e) {
            Utils.panic("Failed to save item in config db.", e);
        }
    }

    public Result processQuery(Query query) throws Exception {
        final var table = tables.get(query.getTable());
        if (table == null) {
            throw new Exception(String.format("Table %s does not exist.", query.getTable()));
        }

        switch (query.getStatement()) {
            case CREATE -> {
            }
            case INSERT -> {
            }
            case SELECT -> {
               return table.select(query.getColumns(), query.getWhereClause(), query.getOrderByClause());
            }
            case DESCRIBE -> {
            }
            case ALTER -> {
            }
            case UPDATE -> {
            }
            case DROP -> {
            }
            case DELETE -> {
            }
            case BEGIN -> {
            }
            case COMMIT -> {
            }
            case ROLLBACK -> {
            }
        }
        return null;
    }
}
