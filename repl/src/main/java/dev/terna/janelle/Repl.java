package dev.terna.janelle;

import dev.terna.janelle.bplustree.BPlusTree;
import dev.terna.janelle.pager.Page;

public class Repl {
    public static void main(String[] args) {
        final var tree = new BPlusTree(5);

        tree.insert(1, new Page("a"));
        tree.insert(2, new Page("b"));
        tree.insert(3, new Page("c"));
        tree.insert(4, new Page("d"));
        tree.insert(5, new Page("e"));
        tree.insert(6, new Page("f"));
        tree.insert(7, new Page("g"));
        tree.insert(8, new Page("h"));
        tree.insert(9, new Page("i"));
        tree.insert(10, new Page("j"));
        tree.insert(11, new Page("k"));
        tree.insert(12, new Page("l"));
        tree.insert(13, new Page("m"));
        tree.insert(14, new Page("n"));
        tree.insert(15, new Page("o"));
        tree.insert(16, new Page("p"));
        tree.insert(17, new Page("q"));
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
