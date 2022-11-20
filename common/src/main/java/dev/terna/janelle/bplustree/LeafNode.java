package dev.terna.janelle.bplustree;

import java.util.Arrays;

public class LeafNode extends Node {
    int maxNumPairs;
    int minNumPairs;
    int numPairs;
    LeafNode leftSibling;
    LeafNode rightSibling;
    KeyValuePair[] kvps;

    /**
     * Given an index, this method sets the key-value pair at that index within kvps to null.
     * @param index: the location within kvp to be set to null
     */
    public void delete(int index) {
        // Delete key-value pair from leaf
        this.kvps[index] = null;
        // Decrement numPairs
        numPairs--;
    }

    /**
     * This method attempts to insert a key-value pair within kvps
     * of the LeafNode object. If it succeeds, numPairs increments,
     * kvps is sorted, and the boolean true is returned. If the method
     * fails, the boolean false is returned.
     * @param kvp: the key-value pair to be inserted
     * @return a boolean indicating whether or not the insert was successful
     */
    public boolean insert(KeyValuePair kvp) {
        if (this.isFull()) {
            // Flow of execution goes here when numPairs == maxNumPairs
            return false;
        } else {
            // Insert key-value pair, increment numPairs, sort kvps
            this.kvps[numPairs] = kvp;
            numPairs++;
            Arrays.sort(this.kvps, 0, numPairs);
            return true;
        }
    }

    /**
     * This simple method determines if the LeafNode is deficient, i.e.
     * the numPairs within the LeafNode object is below minNumPairs.
     * @return a boolean indicating whether or not the LeafNode is deficient
     */
    public boolean isDeficient() {
        return numPairs < minNumPairs;
    }

    /**
     * This simple method determines if the LeafNode is full, i.e. the
     * numPairs within the LeafNode is equal to the maximum number of pairs.
     * @return a boolean indicating whether or not the LeafNode is full
     */
    public boolean isFull() {
        return numPairs == maxNumPairs;
    }

    /**
     * This simple method determines if the LeafNode object is capable of
     * lending a dictionary pair to a deficient leaf node. The LeafNode
     * object can lend a dictionary pair if its numPairs is greater than
     * the minimum number of pairs it can hold.
     * @return a boolean indicating whether or not the LeafNode object can
     * give a dictionary pair to a deficient leaf node
     */
    public boolean isLendable() {
        return numPairs > minNumPairs;
    }

    /**
     * This simple method determines if the LeafNode object is capable of
     * being merged with, which occurs when the number of pairs within the
     * LeafNode object is equal to the minimum number of pairs it can hold.
     * @return a boolean indicating whether or not the LeafNode object can
     * be merged with
     */
    public boolean isMergeable() {
        return numPairs == minNumPairs;
    }

    /**
     * Constructor
     * @param m: order of B+ tree that is used to calculate maxNumPairs and minNumPairs
     * @param kvp: first key-value pair inserted into new node
     */
    public LeafNode(int m, KeyValuePair kvp) {
        super();
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
        this.kvps = new KeyValuePair[m];
        this.numPairs = 0;
        this.insert(kvp);
    }

    /**
     * Constructor
     * @param kvps: list of KeyValuePair objects to be immediately inserted into new LeafNode object
     * @param m: order of B+ tree that is used to calculate maxNumPairs and minNumPairs
     * @param parent: parent of newly created child LeafNode
     */
    public LeafNode(int m, KeyValuePair[] kvps, InternalNode parent) {
        super();
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
        this.kvps = kvps;
        this.numPairs = Utils.linearNullSearch(kvps);
        this.parent = parent;
    }

    @Override
    public String toString() {
        return Arrays.toString(kvps);
    }
}
