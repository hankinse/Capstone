package com.MeadowEast.xue;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class StatActivity extends Activity {
	public static final String TAG = "StatActivity";
	public static final String CEName = "ChineseEnglish";
	public static final String ECName = "EnglishChinese";
	
	public TextView lastDeckDateEC, currentLevelDescEC, weeklyProgressEC;
	public TextView lastDeckDateCE, currentLevelDescCE, weeklyProgressCE;
	
	private boolean hasLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stat);
		
		lastDeckDateEC = (TextView) findViewById(R.id.last_deck_text_ec);
		currentLevelDescEC = (TextView) findViewById(R.id.level_count_text_ec);
		weeklyProgressEC = (TextView) findViewById(R.id.weekly_progress_text_ec);
		
		hasLog = false;
		setECStatus();
		
		lastDeckDateCE = (TextView) findViewById(R.id.last_deck_text_ce);
		currentLevelDescCE = (TextView) findViewById(R.id.level_count_text_ce);
		weeklyProgressCE = (TextView) findViewById(R.id.weekly_progress_text_ce);
		
		hasLog = false;
		setCEStatus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stat, menu);
		return true;
	}
	
	private void setECStatus() {
		ArrayList<String> weekLog = getLastWeek(ECName);
		if (hasLog) {
			
		}
		else {
			setTextInvalid(lastDeckDateEC, currentLevelDescEC, weeklyProgressEC);
		}
	}
	
	private void setCEStatus() {
		ArrayList<String> weekLog = getLastWeek(ECName);
		if (hasLog) {
			
		}
		else {
			setTextInvalid(lastDeckDateCE, currentLevelDescCE, weeklyProgressEC);
		}
	}
	
	private void setTextInvalid(TextView date, TextView level, TextView week) {
		
	}

	/*
	@SuppressWarnings("unchecked")
	private boolean readStatus(String name) {
		FileInputStream statusobjectFIS;
		ObjectInputStream statusobjectOIS;
		try {
			File statusobjectfile = new File(MainActivity.filesDir, name + ".status.ser");
			statusobjectFIS = new FileInputStream(statusobjectfile);
			statusobjectOIS = new ObjectInputStream(statusobjectFIS);
		} catch (Exception e) {
			Log.d(TAG, "No status file.");
			return false;
		} 
		try {
			indexSets = (List<IndexSet>) statusobjectOIS.readObject();
			statusobjectFIS.close();
			Log.d(TAG, "OBJECT status file read without problems");
		} catch (Exception e) { Log.d(TAG, "Error in readStatus"); return false;}
		return true;
	}
	*/
	
	private ArrayList<String> getLastWeek(String name) {
		ArrayList<String> out = new ArrayList<String>();
		Calendar weekAgo = Calendar.getInstance();
		weekAgo.add(Calendar.DAY_OF_MONTH, -7); // Subtract seven days from right now.

		try {
			File statusobjectfile = new File(MainActivity.filesDir, name + ".log.txt");
			hasLog = true;
		}
		catch (Exception e) { Log.d(TAG, "Error reading log file."); hasLog = false; }
		
		return out;
	}
}
