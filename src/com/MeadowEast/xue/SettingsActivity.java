package com.MeadowEast.xue;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.MeadowEast.xue.Updater;

public class SettingsActivity extends Activity implements OnClickListener {
	Button updateButton;
	ToggleButton audioButton;
	NumberPicker deckSizePicker;
	public static SharedPreferences settings;
	public static File filesDir;
	static final String TAG = "XUE SettingsActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
        // Set up components.
        updateButton = (Button) findViewById(R.id.update_button);
    	updateButton.setOnClickListener(this);
    	audioButton = (ToggleButton) findViewById(R.id.audio_on_off_button);
    	audioButton.setOnClickListener(this);
    	audioButton.setChecked(audioOn());
    	deckSizePicker = (NumberPicker) findViewById(R.id.deck_size_picker);
    	deckSizePicker.setOnClickListener(this);
    	File sdCard = Environment.getExternalStorageDirectory();
		filesDir = new File (sdCard.getAbsolutePath() + "/Android/data/com.MeadowEast.xue/files");
		Log.d(TAG, "xxx filesDir="+filesDir);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	deckSizePicker.setValue(getDeckSize());
    }
    
    @Override
	protected void onPause() {
    	super.onPause();
    	
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	Log.d(TAG, "onStop()");
    	setDeckSize(deckSizePicker.getValue());
    	setAudioOnOff(audioButton.isChecked());
    }
    
	public void onClick(View arg0) {
	   	switch (arg0.getId()){
	   	case R.id.update_button:
	   		new Thread() {
				public void run() {
					Updater updater = new Updater();
					updater.checkVocabFileExists(filesDir);
					updater.downloadVocab(filesDir);
				}
			}.start(); 
			break;
	   	case R.id.audio_on_off_button:
	   		Log.d(TAG, "Setting audio on/off: " + audioButton.isChecked());
	   		setAudioOnOff(audioButton.isChecked());
	   		break;
	   	}
	   	
	}

	public boolean audioOn() {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		boolean isOn = settings.getBoolean(getString(R.string.audio_state_on_off), true);
		Log.d(TAG, "Audio feedback is currently: " + isOn);
		return isOn;
	}
	
	public void setAudioOnOff(boolean isOn) {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(getString(R.string.audio_state_on_off), isOn);
		editor.commit();
		Log.d(TAG, "Turned audio on to: " + isOn);
	}
	
	public int getDeckSize() {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		int size = settings.getInt("deck_size", 50);
		Log.d(TAG, "Current deck size: " + size);
		return size;
	}
	
	public void setDeckSize(int size) {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("deck_size", size);
		editor.commit();
	}
}
