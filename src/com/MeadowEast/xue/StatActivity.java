package com.MeadowEast.xue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

/*	Usage statistics are calculated as the activity is being loaded.
 * 	The lastLine member variable contains the last line of the log
 * 	file, but is currently unused because statistics are being 
 * 	calculated and displayed in a slightly more user-friendly
 * 	format. 
 */

public class StatActivity extends Activity {
	public static final String TAG = "StatActivity";
	public static final String CEName = "ChineseEnglish";
	public static final String ECName = "EnglishChinese";
	public static final int TIME_HORIZON_DAYS = 7; // The number of days we want to calculate statistics over.
	public static final int NUM_DECKS_UNTIL_REPORT = 20;
	
	public TextView lastDeckDateEC, currentLevelDescEC, weeklyProgressEC;
	public TextView lastDeckDateCE, currentLevelDescCE, weeklyProgressCE;
	
	private boolean hasLog;
	private String lastLine;
	private Date lastDate;
	
	private int numCompletedDecks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stat);
		
		lastDeckDateEC = (TextView) findViewById(R.id.last_deck_text_ec);
		currentLevelDescEC = (TextView) findViewById(R.id.level_count_text_ec);
		weeklyProgressEC = (TextView) findViewById(R.id.weekly_progress_text_ec);
		
		numCompletedDecks = 0;
		hasLog = false;
		setECStatus();
		
		lastDeckDateCE = (TextView) findViewById(R.id.last_deck_text_ce);
		currentLevelDescCE = (TextView) findViewById(R.id.level_count_text_ce);
		weeklyProgressCE = (TextView) findViewById(R.id.weekly_progress_text_ce);

		numCompletedDecks = 0;
		hasLog = false;
		setCEStatus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stat, menu);
		return true;
	}
	
	private void setStatus(String name, TextView date, TextView level, TextView week) {
		ArrayList<String> weekLog = getRelevantEntries(name);
		String[] levels = null;
		String first = null;
		String last = null;
		
		// Check to make sure there's something there.
		if (weekLog.size() < 1) {
			hasLog = false;
		}
		else {
			levels = weekLog.get(weekLog.size() - 1).split(" "); // The last line contains the most recent.
			first = weekLog.get(0);
			last = weekLog.get(weekLog.size() - 1);
		}
		if (hasLog) {
			date.setText(lastDate.toString());
			level.setText("Level 0: " + levels[0] + "\n" + "Level 1: " + levels[1] + "\n" + "Level 2: " + levels[2] + "\n"+
										"Level 3: " + levels[3] + "\n" + "Level 4: " + levels[4]);
			// We want to check to see if it's still too early to extract meaningful weekly results
			if (numCompletedDecks < NUM_DECKS_UNTIL_REPORT) {
				String progress = "Here you will see a report of your learning progress after you have completed at least " +
									NUM_DECKS_UNTIL_REPORT + " decks.  We consider a card learned after it reaches level 2.  " +
									"You have currently completed " + numCompletedDecks + " decks.";
				week.setText(progress);
			}
			week.setText(extractWeeklyResults(first, last, weekLog.size()));
		}
		else {
			setTextInvalid(date, level, week);
		}
		
		
	}
	
	private void setECStatus() {
		setStatus(ECName, lastDeckDateEC, currentLevelDescEC, weeklyProgressEC);
	}
	
	private void setCEStatus() {
		setStatus(CEName, lastDeckDateCE, currentLevelDescCE, weeklyProgressCE);
	}
	
	private void setTextInvalid(TextView date, TextView level, TextView week) {
		date.setText(getString(R.string.invalid_log));
		level.setText(getString(R.string.invalid_log));
		week.setText(getString(R.string.invalid_log));
	}
	
	// Returns the last TIME_HORIZON_DAYS worth of entries as an ArrayList of strings
	// in the following format:
	// "L0 L1 L2 L3 L4"  where LX is the count of items at level X in that entry.
	@SuppressLint("SimpleDateFormat")
	private ArrayList<String> getRelevantEntries(String name) {
		ArrayList<String> out = new ArrayList<String>();
		Calendar weekAgoCalendar = Calendar.getInstance();
		weekAgoCalendar.add(Calendar.DAY_OF_MONTH, -TIME_HORIZON_DAYS); // Subtract time_horizon days from right now.
		Date weekAgo = weekAgoCalendar.getTime();
		
		String currentLine = null;
		String levelCount = null;
		BufferedReader reader = null;

		try {
			File logFileHandle = new File(MainActivity.filesDir, name + ".log.txt");
			reader = new BufferedReader(new FileReader(logFileHandle));
			while ((currentLine = reader.readLine()) != null) {
				++numCompletedDecks;
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
						// This is highly tailored to the exact file we're working with.
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
	
	/*	Calculates weekly statistics from the oldest entry of the week (start)
	 *  and the newest entry of the week (end) only.  The numDecks is the number
	 *  of entries in the log during the given timeframe.
	 */
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
			numLearned = new String("forgotten " + (-learned) + " cards, ");
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
