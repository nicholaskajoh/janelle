package dev.terna.janelle.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Node {
    private final String id;
    private NodeType nodeType;
    private List<Node> children = new ArrayList<>();
    private List<Token> tokens;

    public Node(NodeType nodeType, List<Token> tokens) {
        id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.nodeType = nodeType;
        this.tokens = tokens;
    }

    public String getId() {
        return id;
    }

    List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    List<Token> getTokens() {
        return tokens;
    }

    @Override
    public String toString() {
        return nodeType.name() + "\n" + tokens.stream().map(Token::getValue).collect(Collectors.joining(" "));
    }
}
