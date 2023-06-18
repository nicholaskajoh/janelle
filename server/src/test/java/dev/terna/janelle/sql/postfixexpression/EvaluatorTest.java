package dev.terna.janelle.sql.postfixexpression;

import dev.terna.janelle.sql.Token;
import dev.terna.janelle.sql.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EvaluatorTest {
    private List<Token> getPostfixExpression(List<Token> tokens) {
        final var generator = new Generator();
        return generator.generate(tokens);
    }

    @Test
    public void evaluateBasicExpression() {
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

        final var evaluator = new Evaluator(Map.of());
        final var result = evaluator.evaluate(getPostfixExpression(tokens));

        Assertions.assertTrue(result instanceof Number);
        Assertions.assertEquals(25, (int) Double.parseDouble(result.toString()));
    }

    private static Stream<Arguments> evaluateComplexExpressionArgs() {
        return Stream.of(
                Arguments.of(Map.of("num_orders", 4, "voucher_balance", 50, "email", "ja@nel.le", "name", "Milan", "has_premium_plan", true), true),
                Arguments.of(Map.of("num_orders", 20, "voucher_balance", 4.19, "email", "test@example.com", "name", "Milan", "has_premium_plan", false), false)
        );
    }

    @ParameterizedTest
    @MethodSource("evaluateComplexExpressionArgs")
    public void evaluateComplexExpression(Map<String, Object> variables, boolean expected) {
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

        final var evaluator = new Evaluator(variables);
        final var result = evaluator.evaluate(getPostfixExpression(tokens));

        Assertions.assertTrue(result instanceof Boolean);
        Assertions.assertEquals(expected, Boolean.parseBoolean(result.toString()));
    }
}
