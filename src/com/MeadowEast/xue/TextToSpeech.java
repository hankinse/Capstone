package com.MeadowEast.xue;

import android.util.Log;

public class TextToSpeech {

	private static final String TAG = null;

	public static void hanziToSpeech(String input) {

		String translateURL = "http://translate.google.com/translate_tts?tl=zh-CN&q="
				+ input;
		Log.d(TAG, "looK:;" + translateURL);
		Sound.playURL(translateURL);
	}

	public static void englishToSpeech(String input) {
		String translateURL = "http://translate.google.com/translate_tts?tl=en&q="
				+ input;
		Sound.playURL(translateURL);
	}

}
