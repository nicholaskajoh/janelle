package dev.terna.janelle.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmitTest {
    private Node getAst(String query) throws Exception {
        final var tokenizer = new Tokenizer(query);
        final var tokens = tokenizer.tokenize();
        final var parser = new Parser();
        return parser.parse(tokens);
    }

    @Test
    public void emitSimpleSelectQuery() throws Exception {
        final var ast = getAst("select * from jn_configs;");
        final var emitter = new Emitter();
        final var queries = emitter.emit(ast);

        Assertions.assertEquals(1, queries.size());
        Assertions.assertEquals(Statement.SELECT, queries.get(0).getStatement());
        Assertions.assertEquals("jn_configs", queries.get(0).getTable());
        Assertions.assertEquals(0, queries.get(0).getColumns().size());
    }
}
