package dev.terna.janelle.sql.postfixexpression;

import dev.terna.janelle.sql.Token;
import dev.terna.janelle.sql.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class GeneratorTest {
    @Test
    public void generateBasicExpression() {
        final var tokens = new ArrayList<Token>();
        tokens.add(new Token(TokenType.OPEN_PAREN, "("));
        tokens.add(new Token(TokenType.INT_LITERAL, "5"));
        tokens.add(new Token(TokenType.MULTIPLY_OP, "*"));
        tokens.add(new Token(TokenType.INT_LITERAL, "4"));
        tokens.add(new Token(TokenType.ADD_OP, "+"));
        tokens.add(new Token(TokenType.INT_LITERAL, "3"));
        tokens.add(new Token(TokenType.MULTIPLY_OP, "*"));
        tokens.add(new Token(TokenType.INT_LITERAL, "2"));
        tokens.add(new Token(TokenType.CLOSE_PAREN, ")"));
        tokens.add(new Token(TokenType.SUBTRACT_OP, "-"));
        tokens.add(new Token(TokenType.INT_LITERAL, "1"));

        final var generator = new Generator();
        final var result = generator.generate(tokens);

        final var expression = new ArrayList<Token>();
        expression.add(new Token(TokenType.INT_LITERAL, "5"));
        expression.add(new Token(TokenType.INT_LITERAL, "4"));
        expression.add(new Token(TokenType.MULTIPLY_OP, "*"));
        expression.add(new Token(TokenType.INT_LITERAL, "3"));
        expression.add(new Token(TokenType.INT_LITERAL, "2"));
        expression.add(new Token(TokenType.MULTIPLY_OP, "*"));
        expression.add(new Token(TokenType.ADD_OP, "+"));
        expression.add(new Token(TokenType.INT_LITERAL, "1"));
        expression.add(new Token(TokenType.SUBTRACT_OP, "-"));

        Assertions.assertArrayEquals(expression.toArray(), result.toArray());
    }

    @Test
    public void generateComplexExpression() {
        final var tokens = new ArrayList<Token>();
        tokens.add(new Token(TokenType.OPEN_PAREN, "("));
        tokens.add(new Token(TokenType.IDENTIFIER, "num_orders"));
        tokens.add(new Token(TokenType.GREATER_THAN_OP, ">"));
        tokens.add(new Token(TokenType.INT_LITERAL, "0"));
        tokens.add(new Token(TokenType.AND, "and"));
        tokens.add(new Token(TokenType.IDENTIFIER, "num_orders"));
        tokens.add(new Token(TokenType.LESS_THAN_OR_EQUAL_OP, "<="));
        tokens.add(new Token(TokenType.INT_LITERAL, "5"));
        tokens.add(new Token(TokenType.CLOSE_PAREN, ")"));
        tokens.add(new Token(TokenType.OR, "or"));
        tokens.add(new Token(TokenType.IDENTIFIER, "voucher_balance"));
        tokens.add(new Token(TokenType.EQUAL_OP, "="));
        tokens.add(new Token(TokenType.FLOAT_LITERAL, "50.00"));
        tokens.add(new Token(TokenType.OR, "or"));
        tokens.add(new Token(TokenType.OPEN_PAREN, "("));
        tokens.add(new Token(TokenType.IDENTIFIER, "email"));
        tokens.add(new Token(TokenType.EQUAL_OP, "="));
        tokens.add(new Token(TokenType.STRING_LITERAL, "\"ja@nel.le\""));
        tokens.add(new Token(TokenType.AND, "and"));
        tokens.add(new Token(TokenType.IDENTIFIER, "name"));
        tokens.add(new Token(TokenType.NOT_EQUAL_OP, "!="));
        tokens.add(new Token(TokenType.NULL_LITERAL, "null"));
        tokens.add(new Token(TokenType.AND, "and"));
        tokens.add(new Token(TokenType.IDENTIFIER, "has_premium_plan"));
        tokens.add(new Token(TokenType.EQUAL_OP, "="));
        tokens.add(new Token(TokenType.BOOL_LITERAL, "true"));
        tokens.add(new Token(TokenType.CLOSE_PAREN, ")"));

        final var generator = new Generator();
        final var result = generator.generate(tokens);
        System.out.println("Result: " + result);

        final var expression = new ArrayList<Token>();
        expression.add(new Token(TokenType.IDENTIFIER, "num_orders"));
        expression.add(new Token(TokenType.INT_LITERAL, "0"));
        expression.add(new Token(TokenType.GREATER_THAN_OP, ">"));
        expression.add(new Token(TokenType.IDENTIFIER, "num_orders"));
        expression.add(new Token(TokenType.INT_LITERAL, "5"));
        expression.add(new Token(TokenType.LESS_THAN_OR_EQUAL_OP, "<="));
        expression.add(new Token(TokenType.AND, "and"));
        expression.add(new Token(TokenType.IDENTIFIER, "voucher_balance"));
        expression.add(new Token(TokenType.FLOAT_LITERAL, "50.00"));
        expression.add(new Token(TokenType.EQUAL_OP, "="));
        expression.add(new Token(TokenType.IDENTIFIER, "email"));
        expression.add(new Token(TokenType.STRING_LITERAL, "\"ja@nel.le\""));
        expression.add(new Token(TokenType.EQUAL_OP, "="));
        expression.add(new Token(TokenType.IDENTIFIER, "name"));
        expression.add(new Token(TokenType.NULL_LITERAL, "null"));
        expression.add(new Token(TokenType.NOT_EQUAL_OP, "!="));
        expression.add(new Token(TokenType.IDENTIFIER, "has_premium_plan"));
        expression.add(new Token(TokenType.BOOL_LITERAL, "true"));
        expression.add(new Token(TokenType.EQUAL_OP, "="));
        expression.add(new Token(TokenType.AND, "and"));
        expression.add(new Token(TokenType.AND, "and"));
        expression.add(new Token(TokenType.OR, "or"));
        expression.add(new Token(TokenType.OR, "or"));

        Assertions.assertArrayEquals(expression.toArray(), result.toArray());
    }
}
