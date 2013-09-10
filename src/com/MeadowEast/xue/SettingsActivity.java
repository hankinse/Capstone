package com.MeadowEast.xue;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.MeadowEast.xue.Updater;

public class SettingsActivity extends Activity implements OnClickListener {
	Button updateButton;
	public static File filesDir;
	static final String TAG = "XUE SettingsActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
        updateButton   = (Button) findViewById(R.id.update_button);
    	updateButton.setOnClickListener(this);
        File sdCard = Environment.getExternalStorageDirectory();
		filesDir = new File (sdCard.getAbsolutePath() + "/Android/data/com.MeadowEast.xue/files");
		Log.d(TAG, "xxx filesDir="+filesDir);
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
	    	}
	    }

}
