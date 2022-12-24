package dev.terna.janelle.bplustree;

public class BPlusTreeTest {
    public static void main(String[] args) {
        final var tree = new BPlusTree(5);

        tree.insert(1, "a");
        tree.insert(2, "b");
        tree.insert(3, "c");
        tree.insert(4, "d");
        tree.insert(5, "e");
        tree.insert(6, "f");
        tree.insert(7, "g");
        tree.insert(8, "h");
        tree.insert(9, "i");
        tree.insert(10, "j");
        tree.insert(11, "k");
        tree.insert(12, "l");
        tree.insert(13, "m");
        tree.insert(14, "n");
        tree.insert(15, "o");
        tree.insert(16, "p");
        tree.insert(17, "q");
        tree.visualize();

        System.out.println(tree.search(2)); // b
        System.out.println(tree.search(7, 11)); // g, h, i, j, k
        System.out.println(tree.search(26)); // null
 
        tree.delete(4); // d
        tree.delete(11); // k
        tree.delete(14); // n
        tree.delete(13); // m
        
        tree.insert(5, "ee");
        tree.insert(5, "eee");
        System.out.println(tree.search(5, 5)); // e, ee, eee

        tree.visualize();
    }
}
