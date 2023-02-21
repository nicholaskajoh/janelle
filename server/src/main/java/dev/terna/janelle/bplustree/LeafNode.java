package dev.terna.janelle.bplustree;

import java.util.Arrays;

public class LeafNode extends Node {
    int maxNumPairs;
    int minNumPairs;
    int numPairs;
    LeafNode leftSibling;
    LeafNode rightSibling;
    Entry[] entries;

    /**
     * Given an index, this method sets the entry at that index within entries to null.
     * @param index: the location within entry to be set to null
     */
    public void delete(int index) {
        // Delete entry from leaf
        this.entries[index] = null;
        // Decrement numPairs
        numPairs--;
    }

    /**
     * This method attempts to insert an entry within entries
     * of the LeafNode object. If it succeeds, numPairs increments,
     * entries is sorted, and the boolean true is returned. If the method
     * fails, the boolean false is returned.
     * @param entry: the entry to be inserted
     * @return a boolean indicating whether or not the insert was successful
     */
    public boolean insert(Entry entry) {
        if (this.isFull()) {
            // Flow of execution goes here when numPairs == maxNumPairs
            return false;
        } else {
            // Insert entry, increment numPairs, sort entries
            this.entries[numPairs] = entry;
            numPairs++;
            Arrays.sort(this.entries, 0, numPairs);
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
     * lending an entry to a deficient leaf node. The LeafNode
     * object can lend an entry if its numPairs is greater than
     * the minimum number of pairs it can hold.
     * @return a boolean indicating whether or not the LeafNode object can
     * give an entry to a deficient leaf node
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
     * @param entry: first entry inserted into new node
     */
    public LeafNode(int m, Entry entry) {
        super();
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
        this.entries = new Entry[m];
        this.numPairs = 0;
        this.insert(entry);
    }

    /**
     * Constructor
     * @param entries: list of Entry objects to be immediately inserted into new LeafNode object
     * @param m: order of B+ tree that is used to calculate maxNumPairs and minNumPairs
     * @param parent: parent of newly created child LeafNode
     */
    public LeafNode(int m, Entry[] entries, InternalNode parent) {
        super();
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
        this.entries = entries;
        this.numPairs = Utils.linearNullSearch(entries);
        this.parent = parent;
    }

    @Override
    public String toString() {
        return Arrays.toString(entries);
    }
}
