package com.MeadowEast.xue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.text.format.DateFormat;
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
	private String lastLine;
	private Date lastDate;

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
		String[] levels = weekLog.get(weekLog.size() - 1).split(" ");
		String first = weekLog.get(0);
		String last = weekLog.get(weekLog.size() - 1);
		if (hasLog) {
			lastDeckDateEC.setText(getString(R.string.last_deck_date_text) + lastDate.toString());
			currentLevelDescEC.setText("Level counts: L0: " + levels[0] + " L1: " + levels[1] + " L2: " + levels[2] +
										" L3: " + levels[3] + " L4: " + levels[4]);
			weeklyProgressEC.setText(extractWeeklyResults(first, last, weekLog.size()));
		}
		else {
			setTextInvalid(lastDeckDateEC, currentLevelDescEC, weeklyProgressEC);
		}
	}
	
	private void setCEStatus() {
		ArrayList<String> weekLog = getLastWeek(ECName);
		String[] levels = weekLog.get(weekLog.size() - 1).split(" ");
		String first = weekLog.get(0);
		String last = weekLog.get(weekLog.size() - 1);
		if (hasLog) {
			lastDeckDateCE.setText(getString(R.string.last_deck_date_text) + lastDate.toString());
			currentLevelDescCE.setText("Level counts: L0: " + levels[0] + " L1: " + levels[1] + " L2: " + levels[2] +
										" L3: " + levels[3] + " L4: " + levels[4]);
			weeklyProgressCE.setText(extractWeeklyResults(first, last, weekLog.size()));
		}
		else {
			setTextInvalid(lastDeckDateCE, currentLevelDescCE, weeklyProgressEC);
		}
	}
	
	private void setTextInvalid(TextView date, TextView level, TextView week) {
		date.setText(getString(R.string.invalid_log));
		level.setText(getString(R.string.invalid_log));
		week.setText(getString(R.string.invalid_log));
	}
	
	private ArrayList<String> getLastWeek(String name) {
		ArrayList<String> out = new ArrayList<String>();
		Calendar weekAgoCalendar = Calendar.getInstance();
		weekAgoCalendar.add(Calendar.DAY_OF_MONTH, -7); // Subtract seven days from right now.
		Date weekAgo = weekAgoCalendar.getTime();
		
		String currentLine = null;
		String levelCount = null;
		BufferedReader reader = null;

		try {
			File logFileHandle = new File(MainActivity.filesDir, name + ".log.txt");
			reader = new BufferedReader(new FileReader(logFileHandle));
			while ((currentLine = reader.readLine()) != null) {
				lastLine = currentLine;
				String[] rawTokens = currentLine.split(" ");
				Date date = extractDate(rawTokens[0], rawTokens[1]);
				ArrayList<String> tokens = new ArrayList<String>();
				for (int i = 2; i < rawTokens.length; ++i) {
					if (rawTokens[i] != null && rawTokens[i].length() > 0) {
						tokens.add(rawTokens[i]);
					}
				}
				if (date != null) {
					lastDate = date;
					if (weekAgo.before(date)) {
						// Add the string of level counts to the arrayList.
						levelCount = new String(tokens.get(0) + " " + tokens.get(1) + " " + 
												tokens.get(3) + " " + tokens.get(6) + " " + tokens.get(8));
						out.add(levelCount);
						Log.d(TAG, "LevelCount: " + levelCount);
					}
				}
			}
			reader.close();
			hasLog = true;
		}
		catch (Exception e) { Log.d(TAG, "Error reading log file."); hasLog = false; }
		
		return out;
	}
	
	private Date extractDate(String date, String time) {
		String dateTime = date+ " " + time;
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy kk:mm");
		Date theDate = null;
		try {
			theDate = formatter.parse(dateTime);
		} catch (ParseException e) { Log.d(TAG, "Could not parse date."); }
		return theDate;
	}
	
	private String extractWeeklyResults(String start, String end, int numDecks) {
		String log = null;
		int[] startLevels = { 0, 0, 0, 0, 0 };
		int[] endLevels = { 0, 0, 0, 0, 0 };
		String[] tokens = start.split(" ");
		for (int i = 0; i < 5; ++i) {
			try {
				startLevels[i] = Integer.parseInt(tokens[i]);
			}
			catch (Exception e) { startLevels[i] = -1; }
		}
		tokens = end.split(" ");
		for (int i = 0; i < 5; ++i) {
			try {
				endLevels[i] = Integer.parseInt(tokens[i]);
			}
			catch (Exception e) { startLevels[i] = -1; }
		}
		
		int startLearned = startLevels[2] + startLevels[3] + startLevels[4];
		int endLearned = endLevels[2] + endLevels[3] + endLevels[4];
		
		String numLearned = null;
		int learned = endLearned - startLearned;
		if (learned < 0) {
			numLearned = new String("forgotten " + learned + " cards, ");
		}
		else if (learned == 0) {
			numLearned = new String("not made any progress, ");
		}
		else if (learned > 0) {
			numLearned = new String("learned " + learned + " cards, ");
		}
		String average = String.format("%.2f", (float) learned / 7.0f);
		
		log = new String("In the past week, you have completed " + numDecks + " decks and " 
						 + numLearned + "for an average of " + average + " per day.");
		
		return log;
	}
}