package com.MeadowEast.xue;

public class CardStatus {
	private int index;
	private int level;
	private int previous;
	private boolean gotCorrect;

	public CardStatus(int index, int level) {
		this.level = level;
		this.index = index;
		this.previous = level;
		this.gotCorrect = false;
	}

	public int getIndex() {
		return index;
	}

	public int getLevel() {
		return level;
	}

	public boolean wasCorrect() {
		return gotCorrect;
	}

	public boolean changedLevel() {
		return previous == level;
	}

	public int getPreviousLevel() {
		return previous;
	}

	public void wrong() {
		if (level > 0) {
			gotCorrect = false;
			previous = level;
			level -= 1;
		}
	}

	public void right() {
		if (level < 4) {
			gotCorrect = true;
			previous = level;
			level += 1;
		}
	}

	public void undo() {
		level = previous;
	}

	public String toString() {
		return "CardStatus: index=" + index + " level=" + level;
	}
}
