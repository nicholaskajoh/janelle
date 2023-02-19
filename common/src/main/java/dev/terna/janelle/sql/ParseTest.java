package dev.terna.janelle.sql;

public class ParseTest {
    public static void main(String[] args) throws Exception {
        final var query = "select * from jn_configs; drop table customers;\nselect name, age, balance from customers where age > 50 and balance < 25.0; select count(*) from users;";
        final var tokenizer = new Tokenizer(query);
        final var tokens = tokenizer.tokenize();
        final var parser = new Parser();
        final var ast = parser.parse(tokens);
        System.out.println(ast);
        parser.visualize();
    }
}
