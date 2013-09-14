package com.MeadowEast.xue;


public class TextToSpeech {
	
	public static void hanziToSpeech(String input)
	{
		String translateURL = "http://translate.google.com/translate_tts?tl=zh-cn&q=" + input;
		Sound.playURL(translateURL);	
	}
	
	public static void englishToSpeech(String input)
	{
		String translateURL = "http://translate.google.com/translate_tts?tl=en&q=" + input;
		Sound.playURL(translateURL);	
	}

}
