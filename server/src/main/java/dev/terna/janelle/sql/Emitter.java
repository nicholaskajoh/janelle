package dev.terna.janelle.sql;

import java.util.ArrayList;
import java.util.List;

public class Emitter {
    public List<Query> emit(Node ast) {
        if (ast.getNodeType() != NodeType.STATEMENTS) {
            throw new IllegalArgumentException("AST root node must be of type STATEMENTS.");
        }

        final List<Query> queries = new ArrayList<>();
        for (var statementNode : ast.getChildren()) {
            final var query = new Query();

            query.setStatement(getStatement(statementNode));
            query.setTable(getTable(statementNode));
            query.setColumns(getColumns(statementNode));
            query.setWhereClause(getWhereClause(statementNode));

            queries.add(query);
        }

        return queries;
    }

    private Statement getStatement(Node statement) {
        switch (statement.getNodeType()) {
            case SELECT -> {
                return Statement.SELECT;
            }
            case DROP -> {
                return Statement.DROP;
            }
            case DESCRIBE -> {
                return Statement.DESCRIBE;
            }
            default -> throw new IllegalStateException(statement.getNodeType() + " node type is not a statement.");
        }
    }

    private String getTable(Node statement) {
        for (var statementChild : statement.getChildren()) {
            if (statementChild.getNodeType() == NodeType.TABLE) {
                final var identifier = statementChild.getChildren().get(0);
                return identifier.getTokens().get(0).getValue();
            }
        }
        return null;
    }

    private List<String> getColumns(Node statement) {
        for (var statementChild : statement.getChildren()) {
            if (statementChild.getNodeType() == NodeType.COLUMNS) {
                return statementChild.getTokens()
                        .stream()
                        .map(Token::getValue)
                        .filter(value -> !"*".equals(value))
                        .toList();
            }
        }
        return List.of();
    }

    private List<Token> getWhereClause(Node statement) {
        for (var statementChild : statement.getChildren()) {
            if (statementChild.getNodeType() == NodeType.WHERE_CLAUSE) {
                if (statementChild.getChildren().isEmpty()) {
                    return List.of();
                }

                final var expression = statementChild.getChildren()
                        .stream()
                        .filter(node -> node.getNodeType() == NodeType.EXPRESSION)
                        .findFirst()
                        .orElseThrow();
                return expression.getTokens();
            }
        }
        return List.of();
    }
}
