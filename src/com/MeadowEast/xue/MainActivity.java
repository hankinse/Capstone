package com.MeadowEast.xue;

import java.io.File;
import java.util.Calendar;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.MeadowEast.xue.Updater;
import android.content.SharedPreferences;

public class MainActivity extends Activity implements OnClickListener {

	Button ecButton, ceButton, exitButton, settingsButton;
	public static String packageName;
	public static File filesDir;
	public static String mode;
	static final String TAG = "XUE MainActivity";
	public static boolean vocabFileExists;
	public static String vocabURL = "http://www.meadoweast.com/capstone/vocabUTF8.txt";
	public static SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		packageName = getApplicationContext().getPackageName();
		ecButton = (Button) findViewById(R.id.ecButton);
		ceButton = (Button) findViewById(R.id.ceButton);
		exitButton = (Button) findViewById(R.id.exitButton);
		settingsButton = (Button) findViewById(R.id.settings_button);
		ecButton.setOnClickListener(this);
		ceButton.setOnClickListener(this);
		exitButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
		File sdCard = Environment.getExternalStorageDirectory();
		filesDir = new File(sdCard.getAbsolutePath() + "/Android/data/com.MeadowEast.xue/files");
		Log.d(TAG, "xxx filesDir=" + filesDir);

		Log.d(TAG, "Checking for vocab file.");
		
		// DEBUG: Set update date to yesterday to force an update.  Comment forceUpdateDate() out to do real checking.
		// call forceUpdateDate() to forcibly trigger the time to update.
		// forceUpdateDate(settings);
		
		if (timeToUpdate(settings)) {
			new Thread() {
				public void run() {
					Updater updater = new Updater();
					String current = updater.getCurrentVersion();
					Log.d(TAG, "Current file version: " + current);
					if (current.equals("ERROR")) {				// Make sure we got the header correctly.
						Log.d(TAG, "Could not update vocab file.");
					}
					else {
						if (isCorrectVersion(current, settings)) {	// If we're up to date, do nothing.
							Log.d(TAG, "Local vocab file is current.");
						}
						else {
							// If not, download and replace the new file.
							Log.d(TAG, "Need to update vocab file.");
							updater.downloadVocab(filesDir);
							writeFileVersion(current, settings);
						}
					}
					setNextUpdateTime(settings);
				}
			}.start(); 
		}
	}

	public void onClick(View v) {
		Intent i;
		switch (v.getId()) {
		case R.id.ecButton:
			mode = "ec";
			i = new Intent(this, LearnActivity.class);
			startActivity(i);
			break;
		case R.id.ceButton:
			mode = "ce";
			i = new Intent(this, LearnActivity.class);
			startActivity(i);
			break;
    	case R.id.settings_button:
    		i = new Intent(this, SettingsActivity.class);    		
    		startActivity(i);
			break;
    	case R.id.exitButton:
    		finish();
			break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void displayMessage(final String msg) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
			}
		});
	}	
	
	private void writeFileVersion(String version, SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getString(R.string.file_version_id), version);
		editor.commit();
	}
	
	private boolean isCorrectVersion(String version, SharedPreferences settings) {
		String local = settings.getString(getString(R.string.file_version_id), "");
		Log.d(TAG, "Local version:" + local);
		if (local.equals(version)) {
			return true;
		}		
		else
			return false;
	}
	
	private boolean timeToUpdate(SharedPreferences settings) {
		Calendar now = Calendar.getInstance();
		
		// Get the next update time.  If there hasn't been an update yet, say
		// the next update was a year ago so we definitely want to update.
		int updateYear = settings.getInt("next_update_year", 2012);
		int updateMonth = settings.getInt("next_update_month", 1);
		int updateDay = settings.getInt("next_update_day", 1);
		Log.d(TAG, "updateYear: " + updateYear + " updateMonth: " + updateMonth + " updateDay: " + updateDay);
		Calendar toUpdate = Calendar.getInstance();
		toUpdate.set(updateYear,  updateMonth, updateDay);
		
		// If the current time is after the next update time, we want to update.
		return now.after(toUpdate);
	}
	
	private void setNextUpdateTime(SharedPreferences settings) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_MONTH, 7);	// We want to check again in one week.
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		Log.d(TAG, "Next update on: " + year + " , " + month + " , " + day + ".");
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("next_update_year", year);
		editor.putInt("next_update_month", month);
		editor.putInt("next_update_day", day);
		editor.commit();
	}
	
	@SuppressWarnings("unused")
	private void forceUpdateDate(SharedPreferences settings) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_MONTH, -1);	// We want to check again in one week.
		int day = now.get(Calendar.DAY_OF_MONTH);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("next_update_day", day);
		editor.commit();
	}
}
