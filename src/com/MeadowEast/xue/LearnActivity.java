package com.MeadowEast.xue;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class LearnActivity extends Activity implements OnClickListener, OnLongClickListener, OnMenuItemClickListener {
	static final String TAG = "LearnActivity";
	static final String BUG_EMAIL = "brokenspicerack@gmail.com";
	static final int TIMER_UPDATE_INTERVAL = 500; // In milliseconds.

	static Handler timerHandler;
	long lastTime;
	long millis;

	SharedPreferences settings;
	LearningProject lp;
	int itemsShown;
	TextView prompt, answer, other, status, timer, promptTemp, answerTemp, otherTemp;
	EditText errorComment;
	Button advance, okay, undo;
	static Context context;

	private Animation anim;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learn);
		Log.d(TAG, "Entering onCreate");
		context = this.getApplicationContext();

		itemsShown = 0;
		prompt = (TextView) findViewById(R.id.promptTextView);
		status = (TextView) findViewById(R.id.statusTextView);
		other = (TextView) findViewById(R.id.otherTextView);
		answer = (TextView) findViewById(R.id.answerTextView);
		advance = (Button) findViewById(R.id.advanceButton);
		okay = (Button) findViewById(R.id.okayButton);
		undo = (Button) findViewById(R.id.undoButton);
		timer = (TextView) findViewById(R.id.timerTextView);

		findViewById(R.id.advanceButton).setOnClickListener(this);
		findViewById(R.id.okayButton).setOnClickListener(this);
		findViewById(R.id.undoButton).setOnClickListener(this);

		findViewById(R.id.promptTextView).setOnLongClickListener(this);
		findViewById(R.id.answerTextView).setOnLongClickListener(this);
		findViewById(R.id.otherTextView).setOnLongClickListener(this);

		if (MainActivity.mode.equals("ec")) lp = new EnglishChineseProject(getECDeckSize(), getECTarget());
		else
			lp = new ChineseEnglishProject(getCEDeckSize(), getECTarget());
		clearContent();
		doAdvance();

		millis = 0;
		lastTime = System.currentTimeMillis();
		timerHandler = new Handler();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			break;
		case R.id.menu_stats:
			i = new Intent(this, StatActivity.class);
			startActivity(i);
			break;
		}
		return true;
	}

	private void doAdvance() {
		if (itemsShown == 0) {
			if (lp.next()) {
				prompt.setText(lp.prompt());
				status.setText(lp.deckStatus());
				itemsShown++;
			} else {
				Log.d(TAG, "Error: Deck starts empty");
				throw new IllegalStateException("Error: Deck starts empty.");
			}
		} else if (itemsShown == 1) {
			Log.d(TAG, lp.other());
			answer.setText(lp.answer());
			itemsShown++;
		} else if (itemsShown == 2) {
			Log.d(TAG, lp.other());
			other.setText(lp.other());
			advance.setText("next");
			itemsShown++;
		} else if (itemsShown == 3) {
			// Got it wrong
			advance.setText("show");
			lp.wrong(audioOn());
			lp.next();
			clearContent();
			prompt.setText(lp.prompt());
			itemsShown = 1;
			status.setText(lp.deckStatus());
		}
	}

	private void clearContent() {
		prompt.setText("");
		answer.setText("");
		other.setText("");
	}

	private void doOkay() {
		if (okay.getText().equals("done")) try {
			lp.log(lp.queueStatus());
			lp.writeStatus();
			finish();
			return;
			// System.exit(0);
		} catch (IOException e) {
			Log.d(TAG, "couldn't write Status");
			return;
		}
		// Do nothing unless answer has been seen
		if (itemsShown < 2) return;
		// Got it right
		lp.right(audioOn());
		if (lp.next()) {
			advance.setText("show");
			clearContent();
			prompt.setText(lp.prompt());
			itemsShown = 1;
			status.setText(lp.deckStatus());
		} else {
			((ViewManager) advance.getParent()).removeView(advance);
			status.setText("");
			okay.setText("done");
			clearContent();
		}
	}

	public void doUndo() {
		if (lp.undo()) {
			slideCardRight();
			clearContent();
			itemsShown = 1;
			prompt.setText(lp.prompt());
			status.setText(lp.deckStatus());
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.advanceButton:
			doAdvance();
			break;
		case R.id.okayButton:
			doOkay();
			break;
		case R.id.undoButton:
			doUndo();
			break;
		// case R.id.promptTextView:
		// case R.id.answerTextView:
		// case R.id.otherTextView:
		// Toast.makeText(this, "Item index: "+lp.currentIndex(),
		// Toast.LENGTH_LONG).show();
		// break;
		}
	}

	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.promptTextView:
			handleLongPress(prompt);
			break;
		case R.id.answerTextView:
			handleLongPress(answer);
			break;
		case R.id.otherTextView:
			handleLongPress(other);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		lastTime = System.currentTimeMillis();
		timerHandler.postDelayed(timerMetronome, TIMER_UPDATE_INTERVAL);

	}

	@Override
	protected void onPause() {
		super.onPause();
		timerHandler.removeCallbacks(timerMetronome);
		long current = System.currentTimeMillis();
		long delta = current - lastTime;
		lastTime = current;
		millis += delta;

	}

	private final Runnable timerMetronome = new Runnable() {
		public void run() {
			long current = System.currentTimeMillis();
			long delta = current - lastTime;
			lastTime = current;
			millis += delta;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			int hours = minutes / 60;

			if (hours > 0) timer.setText(String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60));

			else
				timer.setText(String.format("%d:%02d", minutes, seconds % 60));

			timerHandler.postDelayed(timerMetronome, TIMER_UPDATE_INTERVAL);
		}
	};

	public void handleLongPress(TextView text) {
		String str = text.getText().toString();
		int start = text.getSelectionStart();
		int end = text.getSelectionEnd();
		if (end - start > 0) {
			str = str.substring(start, end);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mdbg.net/chindict/chindict.php?page=worddict&wdrst=0&wdqb=" + str));
			startActivity(browserIntent);
		} else {
			CreatePopupMenu(text);
		}
	}

	public void CreatePopupMenu(View v) {
		PopupMenu extraMenu = new PopupMenu(this, v);
		extraMenu.setOnMenuItemClickListener(this);
		MenuInflater inflater = extraMenu.getMenuInflater();
		inflater.inflate(R.menu.popup_extra, extraMenu.getMenu());
		extraMenu.show();
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.textToSpeech:
			TextToSpeech.englishToSpeech(lp.prompt());
			break;
		case R.id.report:
			reportError();
			break;
		default:
			break;

		}

		return true;
	}

	public void reportError() {
		LayoutInflater inflater = getLayoutInflater();
		final View errorReportDialogView = inflater.inflate(R.layout.dialog_report, null);
		new AlertDialog.Builder(this).setTitle("Error Report").setView(errorReportDialogView).setPositiveButton("Report Error", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				errorComment = (EditText) errorReportDialogView.findViewById(R.id.commentEditText);
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { BUG_EMAIL });
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Xue Error Report, ID: " + lp.currentIndex());
				emailIntent.putExtra(Intent.EXTRA_TEXT, "Xue Error Report, ID: " + lp.currentIndex() + "\n" + errorComment.getText().toString());
				emailIntent.setType("message/rfc822");

				try {
					startActivity(Intent.createChooser(emailIntent, "Choose an e-mail client to send error report"));
				} catch (android.content.ActivityNotFoundException ex) {

				}
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();

		Toast.makeText(this, "Item index: " + lp.currentIndex(), Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d(TAG, "llkj");
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.quit).setMessage(R.string.reallyQuit).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					LearnActivity.this.finish();
				}
			}).setNegativeButton(R.string.no, null).show();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public void slideCardLeft() {

		anim = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
		promptTemp.startAnimation(anim);
		otherTemp.startAnimation(anim);
		answerTemp.startAnimation(anim);
	}

	public void slideCardRight() {
		promptTemp = new TextView(context);
		answerTemp = new TextView(context);
		otherTemp = new TextView(context);
		
		promptTemp.setLayoutParams(prompt.getLayoutParams());
		answerTemp.setLayoutParams(answer.getLayoutParams());
		otherTemp.setLayoutParams(other.getLayoutParams());
		
		
		promptTemp.setText(prompt.getText());
		otherTemp.setText(other.getText());
		answerTemp.setText(answer.getText());
		
		anim = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
		prompt.startAnimation(anim);
		other.startAnimation(anim);
		answer.startAnimation(anim);

	}

	public boolean audioOn() {
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getBoolean(getString(R.string.audio_state_on_off), true);
	}

	public int getECDeckSize() {
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.deck_size_ec_key), SettingsActivity.DEFAULT_EC_DECK_SIZE);
	}

	public int getCEDeckSize() {
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.deck_size_ce_key), SettingsActivity.DEFAULT_EC_DECK_SIZE);
	}

	public int getECTarget() {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.target_ec), SettingsActivity.DEFAULT_TARGET);
	}

	public int getCETarget() {
		SharedPreferences settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.target_ce), SettingsActivity.DEFAULT_TARGET);
	}
}
