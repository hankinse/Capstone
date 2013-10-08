package com.MeadowEast.xue;

import java.util.*;

public class EnglishChineseProject extends LearningProject {
	EnglishChineseProject(int n, int target) {
		super("EnglishChinese", n, target);
	}

	protected String prompt() {
		return card.getEnglish();
	}

	protected String answer() {
		return card.getPinyin();
	}

	protected String other() {
		return card.getHanzi();
	}

	public void addNewItems() {
		addNewItems(0);
	}

	public void addNewItems(int n) {
		// get the highest index so far
		// add the next n items, if they exist; n = 0 indicates all remaining
		// items
		int newindex = -1;
		for (int i = 0; i < 5; ++i)
			for (int index : indexSets.get(i).all())
				newindex = Math.max(newindex, index);
		newindex++;
		int added = 0;
		Date old = new Date(0);
		while (newindex < AllCards.length() && (added < n || n == 0)) {
			// Examine card here to decide if you want it
			Card c = AllCards.getCard(newindex);
			if (c.getEnglish().charAt(0) != '*') {
				indexSets.get(0).add(newindex);
				timestamps.put(newindex, old);
				added++;
			}
			newindex++;
		}
	}

}