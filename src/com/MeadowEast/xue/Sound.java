package com.MeadowEast.xue;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class Sound {
	public static MediaPlayer right = MediaPlayer.create(LearnActivity.context, R.raw.right);
	public static MediaPlayer wrong = MediaPlayer.create(LearnActivity.context, R.raw.wrong);

	public static void playURL(String soundURL) {
		MediaPlayer mp = new MediaPlayer();
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {

			mp.setDataSource(soundURL);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mp.start();

	}
}
