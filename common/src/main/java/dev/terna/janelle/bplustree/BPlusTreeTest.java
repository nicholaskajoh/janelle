package dev.terna.janelle.bplustree;

public class BPlusTreeTest {
    public static void main(String[] args) {
        final var tree = new BPlusTree(5);

        tree.insert(1, 11);
        tree.insert(2, 22);
        tree.insert(3, 33);
        tree.insert(4, 44);
        tree.insert(5, 55);
        tree.insert(6, 66);
        tree.insert(7, 77);
        tree.insert(8, 88);
        tree.insert(9, 99);
        tree.insert(10, 1010);
        tree.insert(11, 1111);
        tree.insert(12, 1212);
        tree.insert(13, 1313);
        tree.insert(14, 1414);
        tree.insert(15, 1515);
        tree.insert(16, 1616);
        tree.insert(17, 1717);
        tree.visualize();

        System.out.println(tree.search(2)); // 22
        System.out.println(tree.search(7, 11)); // 77, 88, 99, 1010, 1111
        System.out.println(tree.search(26)); // null
 
        tree.delete(4); // 44
        tree.delete(11); // 1111
        tree.delete(14); // 1414
        tree.delete(13); // 1313
        
        tree.insert(5, 555);
        tree.insert(5, 5555);
        System.out.println(tree.search(5, 5)); // 55, 555, 5555

        tree.visualize();
    }
}
