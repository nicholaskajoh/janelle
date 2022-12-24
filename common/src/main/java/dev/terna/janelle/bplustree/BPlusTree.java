package dev.terna.janelle.bplustree;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;

/**
 * Sauce: https://github.com/shandysulen/B-Plus-Tree.
 */
public class BPlusTree implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int m;
	private InternalNode root;
	private LeafNode firstLeaf;
    private int vizFileSequence = 1;

    public BPlusTree(int m) {
        this.m = m;
    }

    /**
     * Algorithm: https://www.youtube.com/watch?v=DqcZLulVJ0M
     */
    public void insert(int key, Object value) {
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
				// Sort all the key-value pairs with the included pair to be inserted
				ln.kvps[ln.numPairs] = new KeyValuePair(key, value);
				ln.numPairs++;
				Utils.sortKeyValuePairs(ln.kvps);

				// Split the sorted pairs into two halves
				int midpoint = getMidpoint();
				KeyValuePair[] halfKvps = splitKvps(ln, midpoint);

				if (ln.parent == null) {
					// Flow of execution goes here when there is 1 node in tree
					// Create internal node to serve as parent, use kvps midpoint key
					Integer[] parentKeys = new Integer[this.m];
					parentKeys[0] = halfKvps[0].key;
					InternalNode parent = new InternalNode(this.m, parentKeys);
					ln.parent = parent;
					parent.appendChildPointer(ln);
				} else {
					// Flow of execution goes here when parent exists
					// Add new key to parent for proper indexing
					int newParentKey = halfKvps[0].key;
					ln.parent.keys[ln.parent.degree - 1] = newParentKey;
					Arrays.sort(ln.parent.keys, 0, ln.parent.degree);
				}

				// Create new LeafNode that holds the other half
				LeafNode newLeafNode = new LeafNode(this.m, halfKvps, ln.parent);

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
	 * This method splits kvps into two kvps where all
	 * kvps are of equal length, but each of the resulting kvps
	 * holds half of the original kvps' non-null values. This method is
	 * primarily used when splitting a node within the B+ tree. The kvps of
	 * the specified LeafNode is modified in place. The method returns the
	 * remainder of the KeyValuePairs that are no longer within ln's kvps.
	 * @param ln: list of DKeyValuePairs to be split
	 * @param split: the index at which the split occurs
	 * @return KeyValuePair[] of the two split kvps
	 */
	private KeyValuePair[] splitKvps(LeafNode ln, int split) {
		// Initialize two kvps that each hold half of the original kvps values
		KeyValuePair[] halfKvps = new KeyValuePair[this.m];

		// Copy half of the values into halfKvps
		for (int i = split; i < ln.kvps.length; i++) {
			halfKvps[i - split] = ln.kvps[i];
			ln.delete(i);
		}

		return halfKvps;
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
	 * @return the Object associated with the key within the B+ tree
	 */
	public Object search(int key) {
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
	public ArrayList<Object> search(int lowerBound, int upperBound) {
		// Instantiate Object list to hold values
		ArrayList<Object> values = new ArrayList<>();

		// Iterate through the doubly linked list of leaves
		LeafNode currNode = this.firstLeaf;
		while (currNode != null) {
			// Iterate through the kvps of each node
			for (var kvp : currNode.kvps) {
				// Stop searching kvps once a null value is encountered as this the indicates the end of non-null values
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
	 * Given a key, this method will remove the key-value pair with the
	 * corresponding key from the B+ tree.
	 * @param key: an integer key that corresponds with an existing key-value pair
	 */
	public void delete(int key) {
		if (!isEmpty()) {
			// Get leaf node and attempt to find index of key to delete
			LeafNode ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);
			int kvpIndex = Utils.binarySearch(ln.kvps, ln.numPairs, key);

			if (kvpIndex > -1) {
				// Successfully delete the key-value pair
				ln.delete(kvpIndex);

				// Check for deficiencies
				if (ln.isDeficient()) {
					LeafNode sibling;
					InternalNode parent = ln.parent;

					// Borrow: First, check the left sibling, then the right sibling
					if (ln.leftSibling != null &&
						ln.leftSibling.parent == ln.parent &&
						ln.leftSibling.isLendable()) {

						sibling = ln.leftSibling;
						KeyValuePair borrowedKvp = sibling.kvps[sibling.numPairs - 1];

						// Insert borrowed key-value pair, sort kvps, and delete key-value pair from sibling
						ln.insert(borrowedKvp);
						Utils.sortKeyValuePairs(ln.kvps);
						sibling.delete(sibling.numPairs - 1);

						// Update key in parent if necessary
						int pointerIndex = Utils.findIndexOfPointer(parent.childPointers, ln);
						if (!(borrowedKvp.key >= parent.keys[pointerIndex - 1])) {
							parent.keys[pointerIndex - 1] = ln.kvps[0].key;
						}
					} else if (ln.rightSibling != null &&
							   ln.rightSibling.parent == ln.parent &&
							   ln.rightSibling.isLendable()) {
						sibling = ln.rightSibling;
						KeyValuePair borrowedKvp = sibling.kvps[0];

						// Insert borrowed key-value pair, sort kvps, and delete key-value pair from sibling
						ln.insert(borrowedKvp);
						sibling.delete(0);
						Utils.sortKeyValuePairs(sibling.kvps);

						// Update key in parent if necessary
						int pointerIndex = Utils.findIndexOfPointer(parent.childPointers, ln);
						if (!(borrowedKvp.key < parent.keys[pointerIndex])) {
							parent.keys[pointerIndex] = sibling.kvps[0].key;
						}

                    // Merge: First, check the left sibling, then the right sibling
					} else if (ln.leftSibling != null &&
							 ln.leftSibling.parent == ln.parent &&
							 ln.leftSibling.isMergeable()) {
						sibling = ln.leftSibling;
						int pointerIndex = Utils.findIndexOfPointer(parent.childPointers, ln);

						// Remove key and child pointer from parent
						parent.removeKey(pointerIndex - 1);
						parent.removePointer(ln);

						// Update sibling pointer
						sibling.rightSibling = ln.rightSibling;

						// Check for deficiencies in parent
						if (parent.isDeficient()) {
							handleDeficiency(parent);
						}
					} else if (ln.rightSibling != null &&
							   ln.rightSibling.parent == ln.parent &&
							   ln.rightSibling.isMergeable()) {

						sibling = ln.rightSibling;
						int pointerIndex = Utils.findIndexOfPointer(parent.childPointers, ln);

						// Remove key and child pointer from parent
						parent.removeKey(pointerIndex);
						parent.removePointer(pointerIndex);

						// Update sibling pointer
						sibling.leftSibling = ln.leftSibling;
						if (sibling.leftSibling == null) {
							firstLeaf = sibling;
						}

						if (parent.isDeficient()) {
							handleDeficiency(parent);
						}
					}
				} else if (this.root == null && this.firstLeaf.numPairs == 0) {
					// Flow of execution goes here when the deleted key-value pair was the only pair within the tree
					// Set first leaf as null to indicate B+ tree is empty
					this.firstLeaf = null;
				} else {
					// The kvps of the LeafNode object may need to be sorted after a successful delete
					Utils.sortKeyValuePairs(ln.kvps);
				}
			}
		}
	}

    /**
	 * Given a deficient InternalNode in, this method remedies the deficiency
	 * through borrowing and merging.
	 * @param in: a deficient InternalNode
	 */
	private void handleDeficiency(InternalNode in) {
		InternalNode sibling;
		InternalNode parent = in.parent;

		// Remedy deficient root node
		if (this.root == in) {
			for (int i = 0; i < in.childPointers.length; i++) {
				if (in.childPointers[i] != null) {
					if (in.childPointers[i] instanceof InternalNode) {
						this.root = (InternalNode)in.childPointers[i];
						this.root.parent = null;
					} else if (in.childPointers[i] instanceof LeafNode) {
						this.root = null;
					}
				}
			}

        // Borrow:
		} else if (in.leftSibling != null && in.leftSibling.isLendable()) {
			sibling = in.leftSibling;
		} else if (in.rightSibling != null && in.rightSibling.isLendable()) {
			sibling = in.rightSibling;

			// Copy 1 key and pointer from sibling (atm just 1 key)
			int borrowedKey = sibling.keys[0];
			Node pointer = sibling.childPointers[0];

			// Copy root key and pointer into parent
			in.keys[in.degree - 1] = parent.keys[0];
			in.childPointers[in.degree] = pointer;

			// Copy borrowedKey into root
			parent.keys[0] = borrowedKey;

			// Delete key and pointer from sibling
			sibling.removePointer(0);
			Arrays.sort(sibling.keys);
			sibling.removePointer(0);
			shiftDown(in.childPointers, 1);
		}

		// Merge:
		else if (in.leftSibling != null && in.leftSibling.isMergeable()) {
            // Do nothing
		} else if (in.rightSibling != null && in.rightSibling.isMergeable()) {
			sibling = in.rightSibling;

			// Copy rightmost key in parent to beginning of sibling's keys &
			// delete key from parent
			sibling.keys[sibling.degree - 1] = parent.keys[parent.degree - 2];
			Arrays.sort(sibling.keys, 0, sibling.degree);
			parent.keys[parent.degree - 2] = null;

			// Copy in's child pointer over to sibling's list of child pointers
			for (int i = 0; i < in.childPointers.length; i++) {
				if (in.childPointers[i] != null) {
					sibling.prependChildPointer(in.childPointers[i]);
					in.childPointers[i].parent = sibling;
					in.removePointer(i);
				}
			}

			// Delete child pointer from grandparent to deficient node
			parent.removePointer(in);

			// Remove left sibling
			sibling.leftSibling = in.leftSibling;
		}

		// Handle deficiency a level up if it exists
		if (parent != null && parent.isDeficient()) {
			handleDeficiency(parent);
		}
	}

    /**
	 * This method is used to shift down a set of pointers that are prepended
	 * by null values.
	 * @param pointers: the list of pointers that are to be shifted
	 * @param amount: the amount by which the pointers are to be shifted
	 */
	private void shiftDown(Node[] pointers, int amount) {
		Node[] newPointers = new Node[this.m + 1];
		for (int i = amount; i < pointers.length; i++) {
			newPointers[i - amount] = pointers[i];
		}
		pointers = newPointers;
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

        if (root == null && firstLeaf == null) {
            System.out.println("B+ tree empty. Nothing to visualize. :(");
        }

        final Stack<NodeAndFriends> stack = new Stack<>();
        final HashMap<String, guru.nidi.graphviz.model.MutableNode> gvNodes = new HashMap<>();

        final Node startNode = root != null ? root : firstLeaf;
        stack.push(new NodeAndFriends(startNode, null, null));
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
            final var file = new File("viz/bplustree_" + vizFileSequence + ".png");
            Graphviz.fromGraph(graph)
                .width(3500)
                .height(1000)
                .render(Format.PNG).
                toFile(file);
            vizFileSequence++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
