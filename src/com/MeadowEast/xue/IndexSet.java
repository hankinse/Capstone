package com.MeadowEast.xue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class IndexSet implements Serializable {
	private static final long serialVersionUID = 1L;
	private Set<Integer> is = new HashSet<Integer>();

	IndexSet() {
	}

	public void add(int i) {
		is.add(i);
	}

	public void remove(int i) {
		is.remove(i);
	}

	public int size() {
		return is.size();
	}

	// Returns a random selection of n indices from the set
	// or the whole set if there are less than n in it
	public Integer[] pick(int n) {
		Integer[] result = is.toArray(new Integer[0]);
		if (n < is.size()) {
			int[] picks = Sample.sample(n, is.size());
			Integer[] newResult = new Integer[n];
			for (int i = 0; i < n; ++i) {
				newResult[i] = result[picks[i]];
			}
			result = newResult;
		}
		for (int i : result)
			remove(i);
		return result;
	}

	public Integer[] all() {
		return is.toArray(new Integer[0]);
	}

	public Integer pickOne() {
		Integer[] indices = is.toArray(new Integer[0]);
		Random r = new Random();
		Integer index = indices[r.nextInt(indices.length)];
		remove(index);
		return index;
	}
}
