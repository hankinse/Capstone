package com.MeadowEast.xue;

import java.awt.Color;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

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
	TextView prompt, answer, other, status, timer;
	Button doneButton;
	EditText errorComment;

	int start_level0, start_level1, start_level2, start_level3, start_level4;
	int end_level0, end_level1, end_level2, end_level3, end_level4;
	String diff_level0, diff_level1, diff_level2, diff_level3, diff_level4;

	static Context context;

	boolean isDone = false;

	private Animation anim_out_to_left;
	private Animation anim_out_to_right;
	private Animation anim_in_to_left;
	private Animation anim_in_to_right;
	private Animation anim_in_to_up;
	private Animation anim_out_to_up;

	LinearLayout learnLayout;

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
		timer = (TextView) findViewById(R.id.timerTextView);

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

		start_level0 = lp.getNumAtLevel(0);
		start_level1 = lp.getNumAtLevel(1);
		start_level2 = lp.getNumAtLevel(2);
		start_level3 = lp.getNumAtLevel(3);
		start_level4 = lp.getNumAtLevel(4);
		Log.d(TAG, "start_level0 " + start_level0);
		Log.d(TAG, "start_level1 " + start_level1);
		Log.d(TAG, "start_level2 " + start_level2);
		Log.d(TAG, "start_level3 " + start_level3);
		Log.d(TAG, "start_level4 " + start_level4);
		

		anim_out_to_left = AnimationUtils.loadAnimation(this, R.anim.out_to_left);

		anim_out_to_right = AnimationUtils.loadAnimation(this, R.anim.out_to_right);

		anim_in_to_left = AnimationUtils.loadAnimation(this, R.anim.in_to_left);

		anim_in_to_right = AnimationUtils.loadAnimation(this, R.anim.in_to_right);

		anim_in_to_up = AnimationUtils.loadAnimation(this, R.anim.in_to_up);

		anim_out_to_up = AnimationUtils.loadAnimation(this, R.anim.out_to_up);

		learnLayout = (LearnLinearLayout) findViewById(R.id.LearnLinearLayout);
		learnLayout.setOnTouchListener(new LearnSwipeTouchListener() {
			public void onDownSwipe() {
				doAdvance();
			}

			public void onUpSwipe() {
				doOkay();
			}

			public void onLeftSwipe() {
				if (isDone) {
					return;
				}
				doMarkWrong();

			}

			public void onRightSwipe() {
				doUndo();
			}
		});
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

	private void doMarkWrong() {

		if (itemsShown > 1) {
			prompt.startAnimation(anim_in_to_left);
			other.startAnimation(anim_in_to_left);
			answer.startAnimation(anim_in_to_left);

			anim_in_to_left.setAnimationListener(new AnimationListener() {

				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					// Got it wrong
					lp.wrong(audioOn());
					lp.next();
					clearContent();
					prompt.setText(lp.prompt());
					itemsShown = 1;
					status.setText(lp.deckStatus());
					prompt.startAnimation(anim_out_to_left);
					other.startAnimation(anim_out_to_left);
					answer.startAnimation(anim_out_to_left);
				}
			});

		}
	}

	private void doAdvance() {
		if (isDone) {
			return;
		}
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
			itemsShown++;
		}
	}

	private void clearContent() {
		prompt.setText("");
		answer.setText("");
		other.setText("");
	}

	private void doDone() {
		try {
			lp.log(lp.queueStatus());
			lp.writeStatus();
			finish();
			return;
			// System.exit(0);
		} catch (IOException e) {
			Log.d(TAG, "couldn't write Status");
			return;
		}

	}

	private void doOkay() {
		if (isDone) {
			return;
		}
		// Do nothing unless answer has been seen
		if (itemsShown < 2) return;
		// Got it right
		lp.right(audioOn());
		if (lp.next()) {
			prompt.startAnimation(anim_in_to_up);
			other.startAnimation(anim_in_to_up);
			answer.startAnimation(anim_in_to_up);

			anim_in_to_up.setAnimationListener(new AnimationListener() {

				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					clearContent();
					prompt.setText(lp.prompt());
					itemsShown = 1;
					status.setText(lp.deckStatus());
					prompt.startAnimation(anim_out_to_up);
					other.startAnimation(anim_out_to_up);
					answer.startAnimation(anim_out_to_up);
				}
			});

		} else {
			isDone = true;
			Button doneButton = new Button(this);
			doneButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			doneButton.setText("Done");
			doneButton.setTextColor(getResources().getColor(R.color.xueWhite));
			doneButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
			doneButton.setBackgroundResource(R.drawable.buttonshape);
			doneButton.setId(50);
			doneButton.setOnClickListener((OnClickListener) this);

			prompt.setBackgroundResource(R.drawable.text_bg);
			prompt.setGravity(Gravity.CENTER);
			prompt.setPadding(6, 6, 6, 6);

			learnLayout.addView(doneButton);

			end_level0 = lp.getNumAtLevel(0);
			end_level1 = lp.getNumAtLevel(1);
			end_level2 = lp.getNumAtLevel(2);
			end_level3 = lp.getNumAtLevel(3);
			end_level4 = lp.getNumAtLevel(4);

			if ((end_level2 - start_level2) > 0) answer.setText("Congratulations! You learned " + (end_level2 - start_level2) + " cards.");

			else if ((end_level3 - start_level3) > 0 && (end_level3 - start_level3) > 0) answer.setText("Congratulations! You haven't learned any new cards, but you have become more familiar with cards you have already learned.");
			else
				answer.setText("Sorry. You didn't make much progress this time.");
			
			Log.d(TAG, "" + end_level0 + "-" + start_level0 + "=" + (end_level0 - start_level0));
			Log.d(TAG, "" + end_level1 + "-" + start_level1 + "=" + (end_level1 - start_level1));
			Log.d(TAG, "" + end_level2 + "-" + start_level2 + "=" + (end_level2 - start_level2));
			Log.d(TAG, "" + end_level3 + "-" + start_level3 + "=" + (end_level3 - start_level3));
			Log.d(TAG, "" + end_level4 + "-" + start_level4 + "=" + (end_level4 - start_level4));
			
			

			if ((end_level0 - start_level0) > -1) diff_level0 = "+" + (end_level0 - start_level0);
			else
				diff_level0 = String.valueOf(end_level0 - start_level0);
			if ((end_level1 - start_level1) > -1) diff_level1 = "+" + (end_level1 - start_level1);
			else
				diff_level1 = String.valueOf(end_level1 - start_level1);
			if ((end_level2 - start_level2) > -1) diff_level2 = "+" + (end_level2 - start_level2);
			else
				diff_level2 = String.valueOf(end_level2 - start_level2);
			if ((end_level3 - start_level3) > -1) diff_level3 = "+" + (end_level3 - start_level3);
			else
				diff_level3 =  String.valueOf(end_level3 - start_level3);
			if ((end_level4 - start_level4) > -1) diff_level4 = "+" + (end_level4 - start_level4);
			else
				diff_level4 = String.valueOf(end_level4 - start_level4);

			prompt.setText("Level 0: " + end_level0 + " (" + diff_level0 + ")" + "\n" + "Level 1: " + end_level1 + " (" + diff_level1 + ")" + "\n" + "Level 2: " + end_level2 + " (" + diff_level2 + ")" + "\n" + "Level 3: " + end_level3 + " (" + diff_level3 + ")" + "\n" + "Level 4: " + end_level4 + " (" + diff_level4 + ")");

			other.setText("");
			status.setText("");

		}
	}

	private String getLevelStats() {
		String stats = "";

		for (int i = 0; i < 5; ++i) {
			stats = stats + "Level " + i + ": " + lp.getNumAtLevel(i) + "\n";
		}
		return stats;
	}

	public void doUndo() {
		if (lp.undo()) {
			isDone = false;
			prompt.startAnimation(anim_out_to_right);
			other.startAnimation(anim_out_to_right);
			answer.startAnimation(anim_out_to_right);
			anim_out_to_right.setAnimationListener(new AnimationListener() {

				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					clearContent();
					itemsShown = 1;
					prompt.setText(lp.prompt());
					status.setText(lp.deckStatus());
					prompt.startAnimation(anim_in_to_right);
					other.startAnimation(anim_in_to_right);
					answer.startAnimation(anim_in_to_right);
				}
			});

		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case 50:
			doDone();
			break;
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
		case R.id.report:
			reportError();
			break;
		default:
			break;

		}

		return true;
	}

	public void reportError() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { BUG_EMAIL });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Xue Error Report, ID: " + lp.currentIndex());
		emailIntent.putExtra(Intent.EXTRA_TEXT, "You are reporting an error on the following card:" + "\n" + "\n" + "\u2022" + lp.prompt() + "\n" + "\u2022" + lp.answer() + "\n" + "\u2022" + lp.other() + "\n" + "\n" + "Comment:" + "\n");
		emailIntent.setType("message/rfc822");

		try {
			startActivity(Intent.createChooser(emailIntent, "Choose an e-mail client to send error report"));
		} catch (android.content.ActivityNotFoundException ex) {

		}
		lp.currentIndex();
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
		return settings.getInt(getString(R.string.deck_size_ce_key), SettingsActivity.DEFAULT_CE_DECK_SIZE);
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
