package dev.terna.janelle.bplustree;

import java.util.Arrays;

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
	 * @param dictionary: a list of key-value pair objects
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
}
