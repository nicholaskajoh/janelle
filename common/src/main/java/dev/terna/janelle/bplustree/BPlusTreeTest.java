package dev.terna.janelle.bplustree;

import dev.terna.janelle.database.Row;

public class BPlusTreeTest {
    public static void main(String[] args) {
        final var tree = new BPlusTree(5);

        tree.insert(1, new Row("a"));
        tree.insert(2, new Row("b"));
        tree.insert(3, new Row("c"));
        tree.insert(4, new Row("d"));
        tree.insert(5, new Row("e"));
        tree.insert(6, new Row("f"));
        tree.insert(7, new Row("g"));
        tree.insert(8, new Row("h"));
        tree.insert(9, new Row("i"));
        tree.insert(10, new Row("j"));
        tree.insert(11, new Row("k"));
        tree.insert(12, new Row("l"));
        tree.insert(13, new Row("m"));
        tree.insert(14, new Row("n"));
        tree.insert(15, new Row("o"));
        tree.insert(16, new Row("p"));
        tree.insert(17, new Row("q"));
        tree.visualize();

        System.out.println(tree.search(2)); // b
        System.out.println(tree.search(7, 11)); // g, h, i, j, k
        System.out.println(tree.search(26)); // null
 
        tree.delete(4); // d
        tree.delete(11); // k
        tree.delete(14); // n
        tree.delete(13); // m
        tree.visualize();
    }
}
