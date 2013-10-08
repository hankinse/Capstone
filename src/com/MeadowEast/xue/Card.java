package com.MeadowEast.xue;

public class Card {
	private String english, pinyin, hanzi;

	public Card(String eng, String pin, String han) {
		english = eng;
		pinyin = pin;
		hanzi = han;
	}

	public String getEnglish() {
		return english;
	}

	public String getPinyin() {
		return pinyin;
	}

	public String getHanzi() {
		return hanzi;
	}

	public String toString() {
		return english + "\n" + pinyin + "\n" + hanzi;
	}
}
