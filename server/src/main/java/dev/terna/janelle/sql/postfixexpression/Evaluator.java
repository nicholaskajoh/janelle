package dev.terna.janelle.sql.postfixexpression;

import dev.terna.janelle.sql.Parser;
import dev.terna.janelle.sql.Token;
import dev.terna.janelle.sql.TokenType;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Evaluate postfix expression.
 * https://www.youtube.com/watch?v=bebqXO8H4eA
 */
public class Evaluator {
    private final TreeMap<String, Object> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Stack<Object> stack = new Stack<>();

    public Evaluator(Map<String, Object> variables) {
        this.variables.putAll(variables);
    }

    public Object evaluate(List<Token> expression) {
        for (var token : expression) {
            if (Parser.OPERANDS.contains(token.getTokenType())) {
                final var nativeValue = getOperandNativeValue(token.getTokenType(), token.getValue());
                stack.push(nativeValue);

            } else {
                final var secondOperand = stack.pop();
                final var firstOperand = stack.pop();
                final var result = compute(token.getTokenType(), firstOperand, secondOperand);
                stack.push(result);
            }
        }

        return stack.pop();
    }

    private Object getOperandNativeValue(TokenType name, String value) {
        switch (name) {
            case INT_LITERAL -> {
                return Integer.parseInt(value);
            }
            case FLOAT_LITERAL -> {
                return Float.parseFloat(value);
            }
            case STRING_LITERAL -> {
                // Remove quotes.
                return value.substring(1, value.length() - 1);
            }
            case BOOL_LITERAL -> {
                return Boolean.parseBoolean(value);
            }
            case NULL_LITERAL -> {
                return null;
            }
            case IDENTIFIER -> {
                return variables.get(value);
            }
            default -> throw new IllegalStateException(String.format("Cannot get native value of operand %s of type %s", value, name));
        }
    }

    private Object compute(TokenType operator, Object firstOperand, Object secondOperand) {
        switch (operator) {
            case ADD_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) + Double.parseDouble(secondOperand.toString());
                }
            }

            case SUBTRACT_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) - Double.parseDouble(secondOperand.toString());
                }
            }

            case MULTIPLY_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) * Double.parseDouble(secondOperand.toString());
                }
            }

            case DIVIDE_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) / Double.parseDouble(secondOperand.toString());
                }
            }

            case EQUAL_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) == Double.parseDouble(secondOperand.toString());
                } else if ((firstOperand instanceof String && secondOperand instanceof String)
                        || (firstOperand instanceof Boolean && secondOperand instanceof Boolean)) {
                    return firstOperand.equals(secondOperand);
                } else if (firstOperand == null && secondOperand == null) {
                    return true;
                } else {
                    return false;
                }
            }

            case NOT_EQUAL_OP -> {
                final var result = compute(TokenType.EQUAL_OP, firstOperand, secondOperand);
                return !Boolean.parseBoolean(result.toString());
            }

            case GREATER_THAN_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) > Double.parseDouble(secondOperand.toString());
                }
            }

            case GREATER_THAN_OR_EQUAL_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) >= Double.parseDouble(secondOperand.toString());
                }
            }

            case LESS_THAN_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) < Double.parseDouble(secondOperand.toString());
                }
            }

            case LESS_THAN_OR_EQUAL_OP -> {
                if (firstOperand instanceof Number && secondOperand instanceof Number) {
                    return Double.parseDouble(firstOperand.toString()) <= Double.parseDouble(secondOperand.toString());
                }
            }

            case AND -> {
                if (firstOperand instanceof Boolean && secondOperand instanceof Boolean) {
                    return Boolean.parseBoolean(firstOperand.toString()) && Boolean.parseBoolean(secondOperand.toString());
                }
            }

            case OR -> {
                if (firstOperand instanceof Boolean && secondOperand instanceof Boolean) {
                    return Boolean.parseBoolean(firstOperand.toString()) || Boolean.parseBoolean(secondOperand.toString());
                }
            }
        }

        throw new IllegalStateException(String.format("Cannot compute %s %s %s.", firstOperand, operator, secondOperand));
    }
}
