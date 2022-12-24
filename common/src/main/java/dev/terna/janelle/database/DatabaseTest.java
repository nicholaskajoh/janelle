package dev.terna.janelle.database;

public class DatabaseTest {
    public static void main(String[] args) {
        final var db = new Database();

        try {
            final var configsTable = db.getTable(Database.DB_CONFIGS_TABLE_NAME);
            Utils.printTable(configsTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
