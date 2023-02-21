package dev.terna.janelle.bplustree;

import java.io.Serializable;
import java.util.UUID;

public abstract class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    String id; 
    InternalNode parent;

    public Node() {
        id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
