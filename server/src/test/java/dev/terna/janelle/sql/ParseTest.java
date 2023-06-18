package dev.terna.janelle.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ParseTest {
    private List<Token> getTokens(String query) {
        final var tokenizer = new Tokenizer(query);
        return tokenizer.tokenize();
    }

    @Test
    public void parseQueryWithMultipleStatements() throws Exception {
        final var query = "select * from jn_configs; drop table customers;\nselect name, age, balance from customers where age > 50 and balance < 25.0;";
        final var parser = new Parser();
        final var ast = parser.parse(getTokens(query));

        System.out.println(ast);
        parser.visualize();

        Assertions.assertEquals(NodeType.STATEMENTS, ast.getNodeType());
        Assertions.assertEquals(3, ast.getChildren().size());
    }

    @Test
    public void parseQueryWithWhereClause() throws Exception {
        final var query = """
                select name, email, num_orders
                from customers
                where (num_orders > 0 and num_orders <= 5) or voucher_balance = 50.00 or (email = "ja@nel.le" and name != null and has_premium_plan = true);
                """;
        final var parser = new Parser();
        final var ast = parser.parse(getTokens(query));
        parser.visualize();

        Assertions.assertEquals(NodeType.STATEMENTS, ast.getNodeType());
    }
}
