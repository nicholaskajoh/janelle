package dev.terna.janelle.bplustree;

import java.util.UUID;

public abstract class Node {
    String id; 
    InternalNode parent;

    public Node() {
        id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
