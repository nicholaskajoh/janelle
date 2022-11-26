package dev.terna.janelle.bplustree;

import java.util.Arrays;
import java.util.Comparator;

public class Utils {
    /**
	 * This method performs a standard linear search on a list of Node[] pointers
	 * and returns the index of the first null entry found. Otherwise, this
	 * method returns a -1. This method is primarily used in place of
	 * binarySearch() when the target t = null.
	 * @param pointers: list of Node[] pointers
	 * @return index of the target value if found, else -1
	 */
	public static int linearNullSearch(Node[] pointers) {
		for (int i = 0; i <  pointers.length; i++) {
			if (pointers[i] == null) {
                return i;
            }
		}
		return -1;
	}

    /**
	 * This method performs a standard linear search on a sorted
	 * KeyValuePair[] and returns the index of the first null entry found.
	 * Otherwise, this method returns a -1. This method is primarily used in
	 * place of binarySearch() when the target t = null.
	 * @param kvps: list of key-value pairs sorted by key within leaf node
	 * @return index of the target value if found, else -1
	 */
    public static int linearNullSearch(KeyValuePair[] kvps) {
		for (int i = 0; i <  kvps.length; i++) {
			if (kvps[i] == null) {
                return i;
            }
		}
		return -1;
	}

    /**
	 * This is a specialized sorting method used upon lists of key-value pairs
	 * that may contain interspersed null values.
	 * @param kvps: a list of key-value pair objects
	 */
	public static void sortKeyValuePairs(KeyValuePair[] kvps) {
		Arrays.sort(kvps, (a, b) -> {
            if (a == null && b == null) {
                return 0;
            }
            if (a == null) {
                return 1;
            }
            if (b == null) {
                return -1;
            }
            return a.compareTo(b);
		});
	}

    /**
	 * This method performs a standard binary search on a sorted
	 * KeyValuePair[] and returns the index of the key-value pair
	 * with target key t if found. Otherwise, this method returns a negative
	 * value.
	 * @param kvps: list of key-value pairs sorted by key within leaf node
	 * @param t: target key value of key-value pair being searched for
	 * @return index of the target value if found, else a negative value
	 */
	public static int binarySearch(KeyValuePair[] kvps, int numPairs, int t) {
		Comparator<KeyValuePair> c = new Comparator<KeyValuePair>() {
			@Override
			public int compare(KeyValuePair kvp1, KeyValuePair kvp2) {
				Integer a = Integer.valueOf(kvp1.key);
				Integer b = Integer.valueOf(kvp2.key);
				return a.compareTo(b);
			}
		};
		return Arrays.binarySearch(kvps, 0, numPairs, new KeyValuePair(t, null), c);
	}

    /**
	 * Given a list of pointers to Node objects, this method returns the index of
	 * the pointer that points to the specified 'node' LeafNode object.
	 * @param pointers: a list of pointers to Node objects
	 * @param node: a specific pointer to a LeafNode
	 * @return (int) index of pointer in list of pointers
	 */
	public static int findIndexOfPointer(Node[] pointers, LeafNode node) {
		int i;
		for (i = 0; i < pointers.length; i++) {
			if (pointers[i] == node) { break; }
		}
		return i;
	}
}
