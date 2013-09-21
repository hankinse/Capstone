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
import android.widget.NumberPicker;
import android.widget.ToggleButton;


public class SettingsActivity extends Activity implements OnClickListener {
	ToggleButton audioButton;
	NumberPicker ecDeckSizePicker, ceDeckSizePicker;
	public static SharedPreferences settings;
	public static File filesDir;
	static final String TAG = "XUE SettingsActivity";
	public static final int DECK_MIN_SIZE = 3;
	public static final int DECK_MAX_SIZE = 1000;
	public static final int DEFAULT_EC_DECK_SIZE = 50;
	public static final int DEFAULT_CE_DECK_SIZE = 50;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
        // Set up components.
    	audioButton = (ToggleButton) findViewById(R.id.audio_on_off_button);
    	audioButton.setOnClickListener(this);
    	audioButton.setChecked(audioOn());
    	ecDeckSizePicker = (NumberPicker) findViewById(R.id.deck_size_picker);
    	ecDeckSizePicker.setOnClickListener(this);
    	ecDeckSizePicker.setMaxValue(DECK_MAX_SIZE);
    	ecDeckSizePicker.setMinValue(DECK_MIN_SIZE);
    	ecDeckSizePicker.setValue(getECDeckSize());
    	ceDeckSizePicker = (NumberPicker) findViewById(R.id.ce_deck_size_picker);
    	ceDeckSizePicker.setOnClickListener(this);
    	ceDeckSizePicker.setMaxValue(DECK_MAX_SIZE);
    	ceDeckSizePicker.setMinValue(DECK_MIN_SIZE);
    	ceDeckSizePicker.setValue(getCEDeckSize());
    	File sdCard = Environment.getExternalStorageDirectory();
		filesDir = new File (sdCard.getAbsolutePath() + "/Android/data/com.MeadowEast.xue/files");
		Log.d(TAG, "xxx filesDir="+filesDir);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	ecDeckSizePicker.setValue(getECDeckSize());
    }
    
    @Override
	protected void onPause() {
    	super.onPause();
    	Log.d(TAG, "onPause()");
    	setECDeckSize(ecDeckSizePicker.getValue());
    	setAudioOnOff(audioButton.isChecked());
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    }
    
	public void onClick(View arg0) {
	   	switch (arg0.getId()){
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
	
	public int getECDeckSize() {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		int size = settings.getInt(getString(R.string.deck_size_ec_key), DEFAULT_EC_DECK_SIZE);
		Log.d(TAG, "Current deck size: " + size);
		return size;
	}
	
	public void setECDeckSize(int size) {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(getString(R.string.deck_size_ec_key), size);
		editor.commit();
		Log.d(TAG, "Set deck size to: " + size);
	}
	
	public int getCEDeckSize() {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		int size = settings.getInt(getString(R.string.deck_size_ce_key), DEFAULT_CE_DECK_SIZE);
		Log.d(TAG, "Current deck size: " + size);
		return size;
	}
	
	public void setCEDeckSize(int size) {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(getString(R.string.deck_size_ce_key), size);
		editor.commit();
		Log.d(TAG, "Set deck size to: " + size);
	}
}
