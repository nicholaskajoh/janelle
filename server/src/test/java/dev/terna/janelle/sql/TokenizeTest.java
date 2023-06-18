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

    @Test
    public void tokenizeSelectStatementWithWhereClause() {
        final var query = "select * from my_table where key = \"tables\" and 1=1;";
        final var tokenizer = new Tokenizer(query);
        final var tokens = tokenizer.tokenize();
        final var tokenTypes = tokens.stream().map(Token::getTokenType).toArray();

        final var expected = new TokenType[] {
                TokenType.SELECT,
                TokenType.ALL,
                TokenType.FROM,
                TokenType.IDENTIFIER,
                TokenType.WHERE,
                TokenType.IDENTIFIER,
                TokenType.EQUAL_OP,
                TokenType.STRING_LITERAL,
                TokenType.AND,
                TokenType.INT_LITERAL,
                TokenType.EQUAL_OP,
                TokenType.INT_LITERAL,
                TokenType.SEMICOLON
        };
        Assertions.assertArrayEquals(expected, tokenTypes);
    }
}
