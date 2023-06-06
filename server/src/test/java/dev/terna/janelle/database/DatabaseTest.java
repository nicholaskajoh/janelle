package dev.terna.janelle.database;

import dev.terna.janelle.sql.Emitter;
import dev.terna.janelle.sql.Parser;
import dev.terna.janelle.sql.Query;
import dev.terna.janelle.sql.Tokenizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DatabaseTest {
    private Query getQuery(String queryString) throws Exception {
        final var tokenizer = new Tokenizer(queryString);
        final var tokens = tokenizer.tokenize();
        final var parser = new Parser();
        final var ast = parser.parse(tokens);
        final var emitter = new Emitter();
        return emitter.emit(ast).get(0);
    }

    @Test
    public void loadConfigTableWithQuery() throws Exception {
        final var db = new Database();
        final var query = getQuery(String.format("select * from %s;", Database.DB_CONFIGS_TABLE_NAME));
        final var result = db.processQuery(query);

        Assertions.assertTrue(result.getRows().length > 0);

        Utils.printQueryResult(result);
        result.getSource().getData().visualize();
    }
}
