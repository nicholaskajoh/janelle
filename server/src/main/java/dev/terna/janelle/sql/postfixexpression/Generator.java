package dev.terna.janelle.sql.postfixexpression;

import dev.terna.janelle.sql.Token;
import dev.terna.janelle.sql.Parser;
import dev.terna.janelle.sql.TokenType;

import java.util.*;


/**
 * Generate postfix expression using the Shunting Yard algorithm.
 * https://www.youtube.com/watch?v=Wz85Hiwi5MY
 */
public class Generator {
    private final Stack<Token> stack = new Stack<>();
    private final Queue<Token> queue = new LinkedList<>();
    // PEMDAS
    private static final List<TokenType> OPERATOR_PRECEDENCE = new ArrayList<>();
    static {
        OPERATOR_PRECEDENCE.addAll(Parser.MULT_DIV_OPS);
        OPERATOR_PRECEDENCE.addAll(Parser.ADD_SUB_OPS);
        OPERATOR_PRECEDENCE.addAll(Parser.COMPARISON_OPS);
        OPERATOR_PRECEDENCE.addAll(Parser.LOGICAL_OPS);
    }

    private void validateTokens(List<Token> tokens) {
        // Must not be empty.
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression: No tokens.");
        }

        var previousTokenIsOperand = false;
        var previousTokenIsOperator = false;
        for (var token : tokens) {
            // Must contain only expression tokens.
            if (!Parser.EXPRESSION_TOKENS.contains(token.getTokenType())) {
                throw new IllegalArgumentException("Invalid expression: Contains non-expression token - " + token.getValue() + ".");
            }

            // Must not contain consecutive operands or operators (except for parentheses).
            var currentTokenIsOperand = Parser.OPERANDS.contains(token.getTokenType());
            var currentTokenIsOperator = Parser.OPERATORS.contains(token.getTokenType())
                    && !Parser.PARENTHESES.contains(token.getTokenType());
            if ((previousTokenIsOperand && currentTokenIsOperand)
                || (previousTokenIsOperator && currentTokenIsOperator)) {
                throw new IllegalArgumentException("Invalid expression: Contains consecutive operands/operators.");
            }
            previousTokenIsOperand = currentTokenIsOperand;
            previousTokenIsOperator = currentTokenIsOperator;
        }

        // Must not start or end with an operator (except for parentheses).
        final var firstTokenIsOperator = Parser.OPERATORS.contains(tokens.get(0).getTokenType())
                && !Parser.PARENTHESES.contains(tokens.get(0).getTokenType());
        final var lastTokenIsOperator = Parser.OPERATORS.contains(tokens.get(tokens.size() - 1).getTokenType())
                && !Parser.PARENTHESES.contains(tokens.get(tokens.size() - 1).getTokenType());
        if (firstTokenIsOperator || lastTokenIsOperator) {
            throw new IllegalArgumentException("Invalid expression: Starts/ends with an operator.");
        }
    }

    /**
     * Check if operator at the top of the stack has a higher precedence than a given operator.
     */
    private boolean opOnStackTopHasHigherPrecedence(TokenType operator) {
        final var stackTopOp = stack.peek().getTokenType();
        if (Parser.PARENTHESES.contains(stackTopOp)) {
            return false;
        }

        final var stackTopOpRank = OPERATOR_PRECEDENCE.indexOf(stackTopOp);
        final var operatorRank = OPERATOR_PRECEDENCE.indexOf(operator);
        return stackTopOpRank < operatorRank; // Lower means higher.
    }

    public List<Token> generate(List<Token> tokens) {
        validateTokens(tokens);

        for (var token : tokens) {
            if (token.getTokenType() == TokenType.OPEN_PAREN) {
                stack.push(token);

            } else if (token.getTokenType() == TokenType.CLOSE_PAREN) {
                var parenMatched = false;

                while (stack.size() > 0) {
                    if (stack.peek().getTokenType() == TokenType.OPEN_PAREN) {
                        stack.pop();
                        parenMatched = true;
                        break;
                    } else {
                        queue.add(stack.pop());
                    }
                }

                if (!parenMatched) {
                    // Each open parenthesis should have a closing one and vice versa.
                    throw new IllegalArgumentException("Invalid expression: Parentheses mismatch.");
                }

            } else if (Parser.OPERATORS.contains(token.getTokenType())) {
                if (stack.size() > 0 && opOnStackTopHasHigherPrecedence(token.getTokenType())) {
                    queue.add(stack.pop());
                }
                stack.push(token);

            } else if (Parser.OPERANDS.contains(token.getTokenType()) ) {
                queue.add(token);
            }
        }

        while (stack.size() > 0) {
            queue.add(stack.pop());
        }

        return queue.stream().toList();
    }
}
