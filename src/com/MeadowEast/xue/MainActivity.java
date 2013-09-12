package com.MeadowEast.xue;

import java.io.File;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.MeadowEast.xue.Updater;

public class MainActivity extends Activity implements OnClickListener {

	Button ecButton, ceButton, exitButton, settingsButton;
	public static String packageName;
	public static File filesDir;
	public static String mode;
	static final String TAG = "XUE MainActivity";
	public static boolean vocabFileExists;
	public static String vocabURL = "http://www.meadoweast.com/capstone/vocabUTF8.txt";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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


		new Thread() {
			public void run() {
				Updater updater = new Updater();
				updater.checkVocabFileExists(filesDir);
				updater.downloadVocab(filesDir);
				
			}
		}.start(); 
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
}
