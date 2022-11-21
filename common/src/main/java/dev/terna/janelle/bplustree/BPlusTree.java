package dev.terna.janelle.bplustree;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import dev.terna.janelle.pager.Page;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;

/**
 * Sauce: https://github.com/shandysulen/B-Plus-Tree.
 */
public class BPlusTree {
    private final int m;
	private InternalNode root;
	private LeafNode firstLeaf;

    public BPlusTree(int m) {
        this.m = m;
    }

    /**
     * Algorithm: https://www.youtube.com/watch?v=DqcZLulVJ0M
     */
    public void insert(int key, Page value) {
        if (isEmpty()) {
			// Flow of execution goes here only when first insert takes place
			// Create leaf node as first node in B plus tree (root is null)
			LeafNode ln = new LeafNode(this.m, new KeyValuePair(key, value));
			// Set as first leaf node (can be used later for in-order leaf traversal)
			this.firstLeaf = ln;
		} else {
			// Find leaf node to insert into
			LeafNode ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

			// Insert into leaf node fails if node becomes overfull
			if (!ln.insert(new KeyValuePair(key, value))) {
				// Sort all the dictionary pairs with the included pair to be inserted
				ln.kvps[ln.numPairs] = new KeyValuePair(key, value);
				ln.numPairs++;
				Utils.sortKeyValuePairs(ln.kvps);

				// Split the sorted pairs into two halves
				int midpoint = getMidpoint();
				KeyValuePair[] halfDict = splitDictionary(ln, midpoint);

				if (ln.parent == null) {
					// Flow of execution goes here when there is 1 node in tree
					// Create internal node to serve as parent, use kvps midpoint key
					Integer[] parentKeys = new Integer[this.m];
					parentKeys[0] = halfDict[0].key;
					InternalNode parent = new InternalNode(this.m, parentKeys);
					ln.parent = parent;
					parent.appendChildPointer(ln);
				} else {
					// Flow of execution goes here when parent exists
					// Add new key to parent for proper indexing
					int newParentKey = halfDict[0].key;
					ln.parent.keys[ln.parent.degree - 1] = newParentKey;
					Arrays.sort(ln.parent.keys, 0, ln.parent.degree);
				}

				// Create new LeafNode that holds the other half
				LeafNode newLeafNode = new LeafNode(this.m, halfDict, ln.parent);

				// Update child pointers of parent node
				int pointerIndex = ln.parent.findIndexOfPointer(ln) + 1;
				ln.parent.insertChildPointer(newLeafNode, pointerIndex);

				// Make leaf nodes siblings of one another
				newLeafNode.rightSibling = ln.rightSibling;
				if (newLeafNode.rightSibling != null) {
					newLeafNode.rightSibling.leftSibling = newLeafNode;
				}
				ln.rightSibling = newLeafNode;
				newLeafNode.leftSibling = ln;

				if (this.root == null) {
					// Set the root of B+ tree to be the parent
					this.root = ln.parent;
				} else {
					// If parent is overfull, repeat the process up the tree, until no deficiencies are found
					InternalNode in = ln.parent;
					while (in != null) {
						if (in.isOverfull()) {
							splitInternalNode(in);
						} else {
							break;
						}
						in = in.parent;
					}
				}
			}
		}
    }

    /**
	 * This is a simple method that determines if the B+ tree is empty or not.
	 * @return a boolean indicating if the B+ tree is empty or not
	 */
	private boolean isEmpty() {
		return firstLeaf == null;
	}

    /**
	 * This method starts at the root of the B+ tree and traverses down the
	 * tree via key comparisons to the corresponding leaf node that holds 'key' within its kvps.
	 * @param key: the unique key that lies within the kvps of a LeafNode object
	 * @return the LeafNode object that contains the key within its kvps
	 */
	private LeafNode findLeafNode(int key) {
		// Initialize keys and index variable
		Integer[] keys = root.keys;
		int i;

		// Find next node on path to appropriate leaf node
		for (i = 0; i < root.degree - 1; i++) {
			if (key < keys[i]) {
                break;
            }
		}

		// Return node if it is a LeafNode object, otherwise repeat the search function a level down
		Node child = root.childPointers[i];
		if (child instanceof LeafNode) {
			return (LeafNode) child;
		} else {
			return findLeafNode((InternalNode) child, key);
		}
	}

    private LeafNode findLeafNode(InternalNode node, int key) {
		// Initialize keys and index variable
		Integer[] keys = node.keys;
		int i;

		// Find next node on path to appropriate leaf node
		for (i = 0; i < node.degree - 1; i++) {
			if (key < keys[i]) {
                break;
            }
		}

		// Return node if it is a LeafNode object, otherwise repeat the search function a level down
		Node childNode = node.childPointers[i];
		if (childNode instanceof LeafNode) {
			return (LeafNode) childNode;
		} else {
			return findLeafNode((InternalNode)node.childPointers[i], key);
		}
	}

    /**
	 * This is a simple method that returns the midpoint (or lower bound
	 * depending on the context of the method invocation) of the max degree m of
	 * the B+ tree.
	 * @return (int) midpoint / lower bound
	 */
	private int getMidpoint() {
		return (int) Math.ceil((m + 1) / 2.0) - 1;
	}

    /**
	 * This method splits a single dictionary into two dictionaries where all
	 * dictionaries are of equal length, but each of the resulting dictionaries
	 * holds half of the original dictionary's non-null values. This method is
	 * primarily used when splitting a node within the B+ tree. The dictionary of
	 * the specified LeafNode is modified in place. The method returns the
	 * remainder of the KeyValuePairs that are no longer within ln's dictionary.
	 * @param ln: list of DKeyValuePairs to be split
	 * @param split: the index at which the split occurs
	 * @retuKeyValuePair[] of the two split dictionaries
	 */
	private KeyValuePair[] splitDictionary(LeafNode ln, int split) {
		// Initialize two dictionaries that each hold half of the original dictionary values
		KeyValuePair[] halfDict = new KeyValuePair[this.m];

		// Copy half of the values into halfDict
		for (int i = split; i < ln.kvps.length; i++) {
			halfDict[i - split] = ln.kvps[i];
			ln.delete(i);
		}

		return halfDict;
	}

    private void splitInternalNode(InternalNode in) {
		// Acquire parent
		InternalNode parent = in.parent;

		// Split keys and pointers in half
		int midpoint = getMidpoint();
		int newParentKey = in.keys[midpoint];
		Integer[] halfKeys = splitKeys(in.keys, midpoint);
		Node[] halfPointers = splitChildPointers(in, midpoint);

		// Change degree of original InternalNode in
		in.degree = Utils.linearNullSearch(in.childPointers);

		// Create new sibling internal node and add half of keys and pointers
		InternalNode sibling = new InternalNode(this.m, halfKeys, halfPointers);
		for (Node pointer : halfPointers) {
			if (pointer != null) { pointer.parent = sibling; }
		}

		// Make internal nodes siblings of one another
		sibling.rightSibling = in.rightSibling;
		if (sibling.rightSibling != null) {
			sibling.rightSibling.leftSibling = sibling;
		}
		in.rightSibling = sibling;
		sibling.leftSibling = in;

		if (parent == null) {
			// Create new root node and add midpoint key and pointers
			Integer[] keys = new Integer[this.m];
			keys[0] = newParentKey;
			InternalNode newRoot = new InternalNode(this.m, keys);
			newRoot.appendChildPointer(in);
			newRoot.appendChildPointer(sibling);
			this.root = newRoot;

			// Add pointers from children to parent
			in.parent = newRoot;
			sibling.parent = newRoot;
		} else {
			// Add key to parent
			parent.keys[parent.degree - 1] = newParentKey;
			Arrays.sort(parent.keys, 0, parent.degree);

			// Set up pointer to new sibling
			int pointerIndex = parent.findIndexOfPointer(in) + 1;
			parent.insertChildPointer(sibling, pointerIndex);
			sibling.parent = parent;
		}
	}

    /**
	 * This method modifies a list of Integer-typed objects that represent keys
	 * by removing half of the keys and returning them in a separate Integer[].
	 * This method is used when splitting an InternalNode object.
	 * @param keys: a list of Integer objects
	 * @param split: the index where the split is to occur
	 * @return Integer[] of removed keys
	 */
	private Integer[] splitKeys(Integer[] keys, int split) {
		Integer[] halfKeys = new Integer[this.m];

		// Remove split-indexed value from keys
		keys[split] = null;

		// Copy half of the values into halfKeys while updating original keys
		for (int i = split + 1; i < keys.length; i++) {
			halfKeys[i - split - 1] = keys[i];
			keys[i] = null;
		}

		return halfKeys;
	}

    /**
	 * This method modifies the InternalNode 'in' by removing all pointers within
	 * the childPointers after the specified split. The method returns the removed
	 * pointers in a list of their own to be used when constructing a new
	 * InternalNode sibling.
	 * @param in: an InternalNode whose childPointers will be split
	 * @param split: the index at which the split in the childPointers begins
	 * @return a Node[] of the removed pointers
	 */
	private Node[] splitChildPointers(InternalNode in, int split) {
		Node[] pointers = in.childPointers;
		Node[] halfPointers = new Node[this.m + 1];

		// Copy half of the values into halfPointers while updating original keys
		for (int i = split + 1; i < pointers.length; i++) {
			halfPointers[i - split - 1] = pointers[i];
			in.removePointer(i);
		}

		return halfPointers;
	}

    /**
	 * Given a key, this method returns the value associated with the key
	 * within a key-value pair that exists inside the B+ tree.
	 * @param key: the key to be searched within the B+ tree
	 * @return the Page associated with the key within the B+ tree
	 */
	public Page search(int key) {
		// If B+ tree is completely empty, simply return null
		if (isEmpty()) {
            return null;
        }

		// Find leaf node that holds the key
		LeafNode ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

		// Perform binary search to find index of key within kvps
		int index = Utils.binarySearch(ln.kvps, ln.numPairs, key);

		// If index negative, the key doesn't exist in B+ tree
		if (index < 0) {
			return null;
		} else {
			return ln.kvps[index].value;
		}
	}

	/**
	 * This method traverses the doubly linked list of the B+ tree and records
	 * all values whose associated keys are within the range specified by
	 * lowerBound and upperBound.
	 * @param lowerBound: (int) the lower bound of the range
	 * @param upperBound: (int) the upper bound of the range
	 * @return an list that holds all values of key-value pairs
	 * whose keys are within the specified range
	 */
	public ArrayList<Page> search(int lowerBound, int upperBound) {
		// Instantiate Page list to hold values
		ArrayList<Page> values = new ArrayList<>();

		// Iterate through the doubly linked list of leaves
		LeafNode currNode = this.firstLeaf;
		while (currNode != null) {
			// Iterate through the kvps of each node
			for (var kvp : currNode.kvps) {
				// Stop searching the dictionary once a null value is encountered as this the indicates the end of non-null values
				if (kvp == null) {
                    break;
                }

				// Include value if its key fits within the provided range
				if (lowerBound <= kvp.key && kvp.key <= upperBound) {
					values.add(kvp.value);
				}
			}

			// Update the current node to be the right sibling, leaf traversal is from left to right
			currNode = currNode.rightSibling;
		}

		return values;
	}

    /**
     * Generate image of tree and write it to file.
     */
    public void visualize() {
        class NodeAndFriends {
            Node node;
            String parentId;
            Node[] parentChildren;

            NodeAndFriends(Node node, String parentId, Node[] parentChildren) {
                this.node = node;
                this.parentId = parentId;
                this.parentChildren = parentChildren;
            }
        }

        final Stack<NodeAndFriends> stack = new Stack<>();
        final HashMap<String, guru.nidi.graphviz.model.MutableNode> gvNodes = new HashMap<>();

        stack.push(new NodeAndFriends(root, null, null));
        while (!stack.empty()) {
            final var nodeaf = stack.pop();
            final var gvNodeLabel = nodeaf.node.toString();
            final guru.nidi.graphviz.model.MutableNode gvNode = Factory.mutNode(gvNodeLabel).add(Shape.RECTANGLE);
            gvNodes.put(nodeaf.node.id, gvNode);

            if (nodeaf.parentId != null && nodeaf.parentChildren != null) {
                final var gvParent = gvNodes.get(nodeaf.parentId);
                var nodeIndexInParent = -1;
                for (var i = 0; i < nodeaf.parentChildren.length; i++) {
                    var child = nodeaf.parentChildren[i];
                    if (child != null && child.id.equals(nodeaf.node.id)) {
                        nodeIndexInParent = i;
                    }
                }
                final var linkLabel = String.valueOf(nodeIndexInParent);
                gvParent.addLink(Factory.to(gvNode).with(Label.of(linkLabel)));
            }

            if (nodeaf.node instanceof InternalNode) {
                var internalNode = (InternalNode) nodeaf.node;
                for (var child : internalNode.childPointers) {
                    if (child != null) {
                        stack.push(new NodeAndFriends(child, internalNode.id, internalNode.childPointers));
                    }
                }
            }
        }

        Graph graph = Factory.graph("bplustree").directed().with(gvNodes.values().stream().collect(Collectors.toList()));
        try {
            Graphviz.fromGraph(graph).width(3500).height(1000).render(Format.PNG).toFile(new File("viz/bplustree.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
