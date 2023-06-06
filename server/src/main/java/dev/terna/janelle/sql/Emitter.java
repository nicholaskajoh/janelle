package dev.terna.janelle.sql;

import java.util.ArrayList;
import java.util.List;

public class Emitter {
    public List<Query> emit(Node ast) {
        if (ast.getNodeType() != NodeType.STATEMENTS) {
            throw new IllegalArgumentException("AST root node must be of type STATEMENTS.");
        }

        final List<Query> queries = new ArrayList<>();
        for (var child : ast.getChildren()) {
            final var query = new Query();

            query.setStatement(getStatement(child));
            query.setTable(getTable(child));
            query.setColumns(getColumns(child));

            queries.add(query);
        }

        return queries;
    }

    private Statement getStatement(Node node) {
        switch (node.getNodeType()) {
            case SELECT -> {
                return Statement.SELECT;
            }
            case DROP -> {
                return Statement.DROP;
            }
            default -> throw new IllegalStateException(node.getNodeType() + " node type is not a statement.");
        }
    }

    private String getTable(Node node) {
        for (var child : node.getChildren()) {
            if (child.getNodeType() == NodeType.TABLE) {
                return child.getTokens().get(0).getValue();
            }
        }
        return null;
    }

    private List<String> getColumns(Node node) {
        for (var child : node.getChildren()) {
            if (child.getNodeType() == NodeType.COLUMNS) {
                return child.getTokens()
                        .stream()
                        .map(Token::getValue)
                        .filter(value -> !"*".equals(value))
                        .toList();
            }
        }
        return List.of();
    }
}
