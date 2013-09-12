package com.MeadowEast.xue;

import android.media.MediaPlayer;


public class Sound {
	public static MediaPlayer right = MediaPlayer.create(LearnActivity.context,
			R.raw.right);
	public static MediaPlayer wrong = MediaPlayer.create(LearnActivity.context, R.raw.wrong);
}
