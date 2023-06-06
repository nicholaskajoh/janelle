package dev.terna.janelle.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParseTest {
    @Test
    public void parseQueryStringWithMultipleStatements() throws Exception {
        final var query = "select * from jn_configs; drop table customers;\nselect name, age, balance from customers where age > 50 and balance < 25.0;";
        final var tokenizer = new Tokenizer(query);
        final var tokens = tokenizer.tokenize();
        final var parser = new Parser();
        final var ast = parser.parse(tokens);

        System.out.println(ast);
        parser.visualize();

        Assertions.assertEquals(NodeType.STATEMENTS, ast.getNodeType());
    }
}
