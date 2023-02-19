package dev.terna.janelle.sql;

public class TokenizeTest {
    public static void main(String[] args) {
        final var query = " -- test query\n select * from jn_configs;";
        final var tokenizer = new Tokenizer(query);
        final var tokens = tokenizer.tokenize();
        System.out.println(tokens);
    }
}
