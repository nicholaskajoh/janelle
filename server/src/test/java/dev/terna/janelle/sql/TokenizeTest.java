package dev.terna.janelle.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenizeTest {
    @Test
    public void tokenizeSimpleSelectStatement() {
        final var query = "select * from my_table;";
        final var tokenizer = new Tokenizer(query);
        final var tokens = tokenizer.tokenize();
        final var tokenTypes = tokens.stream().map(Token::getTokenType).toArray();
        Assertions.assertArrayEquals(new TokenType[] {TokenType.SELECT, TokenType.ALL, TokenType.FROM, TokenType.IDENTIFIER, TokenType.SEMICOLON}, tokenTypes);
    }
}
