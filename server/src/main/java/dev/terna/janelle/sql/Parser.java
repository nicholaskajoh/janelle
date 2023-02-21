package dev.terna.janelle.sql;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Parser {
    private Node abstractSyntaxTree;

    private static final Set<TokenType> statementTypes = Set.of(
            TokenType.CREATE,
            TokenType.INSERT,
            TokenType.SELECT,
            TokenType.DESCRIBE,
            TokenType.ALTER,
            TokenType.UPDATE,
            TokenType.DROP,
            TokenType.DELETE,
            TokenType.BEGIN,
            TokenType.COMMIT,
            TokenType.ROLLBACK
    );

    private static final Set<TokenType> functions = Set.of(
            TokenType.COUNT,
            TokenType.AVERAGE,
            TokenType.SUM
    );

    private static final Set<TokenType> parenthesis = Set.of(TokenType.OPEN_PAREN, TokenType.CLOSE_PAREN);

    private static final Set<TokenType> validResultColumnTokens = new HashSet<>();
    static {
        validResultColumnTokens.addAll(functions);
        validResultColumnTokens.add(TokenType.IDENTIFIER);
        validResultColumnTokens.add(TokenType.COMMA);
        validResultColumnTokens.addAll(parenthesis);
        validResultColumnTokens.add(TokenType.ALL);
    }

    public Node parse(List<Token> tokens) throws Exception {
        if (tokens == null || tokens.isEmpty()) {
            throw new Exception("Syntax error: Empty query.");
        }

        abstractSyntaxTree = parseStatements(tokens);
        return abstractSyntaxTree;
    }

    /**
     * Split tokens into a list of statements using the semicolon token as the delimiter.
     * TOKENS = [STATEMENT_1_TOKENS, STATEMENT_2_TOKENS, ...]
     */
    private Node parseStatements(List<Token> tokens) throws Exception {
        final var children = new ArrayList<Node>();

        var statementTokens = new ArrayList<Token>();
        for (var tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
            final var token = tokens.get(tokenIndex);
            if (token.getTokenType() == TokenType.SEMICOLON || tokenIndex == tokens.size() - 1) {
                final var statement = parseStatement(statementTokens);
                children.add(statement);
                statementTokens = new ArrayList<>();
            } else {
                statementTokens.add(token);
            }
        }

        final var statements = new Node(NodeType.STATEMENTS, tokens);
        statements.setChildren(children);
        return statements;
    }

    /**
     * Determine the statement type (e.g. SELECT) and call the appropriate parse function.
     */
    private Node parseStatement(List<Token> tokens) throws Exception {
        if (tokens.isEmpty()) {
            throw new Exception("Syntax error: Empty statement.");
        }

        final var statementType = tokens.get(0).getTokenType();
        final var isValidStatement = statementTypes.contains(statementType);
        if (!isValidStatement) {
            throw new Exception("Syntax error: Invalid statement - " + statementType + ".");
        }

        switch (statementType) {
            case SELECT -> {
                return parseSelectStatement(tokens);
            }
            case DROP -> {
                return parseDropStatement(tokens);
            }
            default -> throw new Exception("Parser error: Not implemented. Statement - " + statementType + ".");
        }
    }

    private Node parseSelectStatement(List<Token> tokens) throws Exception {
        final var select = new Node(NodeType.SELECT, tokens);
        final var children = new ArrayList<Node>();

        // Get result column tokens.
        final var resultColumnTokens = new ArrayList<Token>();
        for (var token : select.getTokens()) {
            if (token.getTokenType() == TokenType.SELECT) { // Skip
                continue;
            }

            if (token.getTokenType() == TokenType.FROM) { // Done
                break;
            }

            if (!validResultColumnTokens.contains(token.getTokenType())) {
                throw new Exception("Syntax error: Invalid result column token - " + token.getValue() + ".");
            }

            resultColumnTokens.add(token);
        }
        final var columns = parseResultColumns(resultColumnTokens);
        children.add(columns);

        // Get table token.
        final var tableToken = new ArrayList<Token>();
        for (var tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
            if (tokenIndex > 0
                    && tokens.get(tokenIndex - 1).getTokenType() == TokenType.FROM
                    && tokens.get(tokenIndex).getTokenType() == TokenType.IDENTIFIER) {
                tableToken.add(tokens.get(tokenIndex));
                break;
            }
        }
        if (tableToken.isEmpty()) {
            throw new Exception("Syntax error: Could not parse table name.");
        }
        final var table = parseTable(tableToken);
        children.add(table);

        // Get where clause tokens.
        // WIP

        // Get order by tokens.
        // WIP

        // Get limit tokens.
        // Not implemented for now...

        select.setChildren(children);
        return select;
    }

    private Node parseResultColumns(List<Token> tokens) throws Exception {
        final var columns = new Node(NodeType.COLUMNS, tokens);
        final var children = new ArrayList<Node>();

        final List<List<Token>> tokenSets = new ArrayList<>();
        var tokenSet = new ArrayList<Token>();
        for (var tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
            final var token = tokens.get(tokenIndex);

            // Columns are delimited by commas.
            if (token.getTokenType() == TokenType.COMMA) {
                tokenSets.add(tokenSet);
                tokenSet = new ArrayList<>();
            } else {
                tokenSet.add(token);
            }

            if (tokenIndex == tokens.size() - 1) {
                tokenSets.add(tokenSet);
            }
        }

        for (var set : tokenSets) {
            if (set.isEmpty()) {
                // Trailing or leading comma somewhere...
                // We don't do that here!
                throw new Exception("Syntax error: Could not parse result columns.");
            }

            final var firstTokenType = set.get(0).getTokenType();
            if (firstTokenType == TokenType.IDENTIFIER) {
                if (set.size() > 1) {
                    // Missing comma somewhere...?
                    // Fix that shit!!!
                    throw new Exception("Syntax error: Could not parse result column - " + set.get(0).getValue() + ".");
                }

                final var identifier = parseIdentifier(set);
                children.add(identifier);
            } else if (functions.contains(firstTokenType)) {
                if (tokenSets.size() > 1) {
                    throw new Exception("Syntax error: A function must be the sole result column when used. Group-by queries not implemented.");
                }

                final var function = parseFunction(set);
                children.add(function);
            } else if (firstTokenType == TokenType.ALL) {
                if (tokenSets.size() > 1) {
                    throw new Exception("Syntax error: \"*\" must be the sole result column when used.");
                }

                final var identifier = parseIdentifier(set);
                children.add(identifier);
            } else {
                throw new Exception("Syntax error: Invalid result column - " + set.get(0).getValue() + ".");
            }
        }

        columns.setChildren(children);
        return columns;
    }

    private Node parseTable(List<Token> tokens) {
        final var table = new Node(NodeType.TABLE, tokens);
        final var identifier = parseIdentifier(tokens);
        table.setChildren(List.of(identifier));
        return table;
    }

    private Node parseIdentifier(List<Token> tokens) {
        return new Node(NodeType.IDENTIFIER, tokens);
    }

    private Node parseFunction(List<Token> tokens) throws Exception {
        final var function = new Node(NodeType.FUNCTION, tokens);
        final var children = new ArrayList<Node>();

        if (!(
                tokens.size() == 4
                && tokens.get(1).getTokenType() == TokenType.OPEN_PAREN
                && (tokens.get(2).getTokenType() == TokenType.IDENTIFIER || tokens.get(2).getTokenType() == TokenType.ALL)
                && tokens.get(3).getTokenType() == TokenType.CLOSE_PAREN
        )) {
            throw new Exception("Syntax error: Invalid function definition - " + tokens.get(0).getValue() + ".");
        }

        final var identifier = parseIdentifier(List.of(tokens.get(2)));
        children.add(identifier);

        function.setChildren(children);
        return function;
    }

    private Node parseDropStatement(List<Token> tokens) {
        // WIP
        return new Node(NodeType.DROP, tokens);
    }

    /**
     * Generate image of AST and write it to file.
     */
    public void visualize() {
        if (abstractSyntaxTree == null) {
            System.out.println("Abstract syntax tree empty. Nothing to visualize. :(");
            return;
        }

        final Stack<Node> stack = new Stack<>();
        stack.push(abstractSyntaxTree);

        final Map<String, MutableNode> gvNodes = new HashMap<>();
        final var startGvNode = Factory.mutNode(abstractSyntaxTree.getId())
                .add(Shape.RECTANGLE)
                .add(Label.of(abstractSyntaxTree.toString()));
        gvNodes.put(abstractSyntaxTree.getId(), startGvNode);

        while (!stack.empty()) {
            final var node = stack.pop();
            final var gvNode = gvNodes.get(node.getId());

            for (var childNode : node.getChildren()) {
                stack.push(childNode);
                final var childGvNode = Factory.mutNode(childNode.getId())
                        .add(Shape.RECTANGLE)
                        .add(Label.of(childNode.toString()));
                gvNode.addLink(childGvNode);
                gvNodes.put(childNode.getId(), childGvNode);
            }
        }

        Graph graph = Factory.graph("ast").directed().with(new ArrayList<>(gvNodes.values()));
        try {
            final var file = new File("viz/ast.png");
            Graphviz.fromGraph(graph)
                    .width(3500)
                    .height(1000)
                    .render(Format.PNG).
                    toFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
