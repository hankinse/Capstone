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
import android.widget.Toast;

public class LearnActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnMenuItemClickListener {
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
	Button doneStatsButton;
	EditText errorComment;

	int wrongCount = 0;
	int seenCount = 0;
	int learnCount = 0;
	int forgotCount = 0;

	int start_level_0;
	int start_level_1;
	int start_level_2;
	int start_level_3;
	int start_level_4;
	int end_level_0;
	int end_level_1;
	int end_level_2;
	int end_level_3;
	int end_level_4;

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

		if (MainActivity.mode.equals("ec"))
			lp = new EnglishChineseProject(getECDeckSize(), getECTarget());
		else
			lp = new ChineseEnglishProject(getCEDeckSize(), getECTarget());
		clearContent();
		doAdvance();

		start_level_0 = lp.getNumAtLevel(0);
		start_level_1 = lp.getNumAtLevel(1);
		start_level_2 = lp.getNumAtLevel(2);
		start_level_3 = lp.getNumAtLevel(3);
		start_level_4 = lp.getNumAtLevel(4);
		
		millis = 0;
		lastTime = System.currentTimeMillis();
		timerHandler = new Handler();

		anim_out_to_left = AnimationUtils.loadAnimation(this,
				R.anim.out_to_left);

		anim_out_to_right = AnimationUtils.loadAnimation(this,
				R.anim.out_to_right);

		anim_in_to_left = AnimationUtils.loadAnimation(this, R.anim.in_to_left);

		anim_in_to_right = AnimationUtils.loadAnimation(this,
				R.anim.in_to_right);

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
		seenCount++;
		wrongCount++;

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
			seenCount++;
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

	private void doDoneStats() {
		try {
			lp.log(lp.queueStatus());
			lp.writeStatus();
			Intent i = null;
			i = new Intent(this, StatActivity.class);
			startActivity(i);
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
		if (itemsShown < 2)
			return;
		// Got it right
		lp.right(audioOn());
		if (lp.next()) {
			seenCount++;
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

			end_level_0 = lp.getNumAtLevel(0);
			end_level_1 = lp.getNumAtLevel(1);
			end_level_2 = lp.getNumAtLevel(2);
			end_level_3 = lp.getNumAtLevel(3);
			end_level_4 = lp.getNumAtLevel(4);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(6, 6, 6, 6);
			Button doneButton = new Button(this);
			doneButton.setText("Finish - Main Menu");
			doneButton.setTextColor(getResources().getColor(R.color.xueWhite));
			doneButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
			doneButton.setBackgroundResource(R.drawable.buttonshape);
			doneButton.setId(50);
			doneButton.setOnClickListener((OnClickListener) this);

			Button doneStatsButton = new Button(this);
			doneStatsButton.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			doneStatsButton.setText("Finish - View Overall Statistics");
			doneStatsButton.setTextColor(getResources().getColor(
					R.color.xueWhite));
			doneStatsButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
			doneStatsButton.setBackgroundResource(R.drawable.buttonshape);
			doneStatsButton.setId(51);
			doneStatsButton.setOnClickListener((OnClickListener) this);

			prompt.setBackgroundResource(R.drawable.text_bg);
			prompt.setGravity(Gravity.CENTER);
			prompt.setPadding(6, 6, 6, 6);
			prompt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);

			answer.setBackgroundResource(R.drawable.text_bg);
			answer.setGravity(Gravity.CENTER);
			answer.setPadding(6, 6, 6, 6);
			answer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);

			other.setBackgroundResource(R.drawable.text_bg);
			other.setGravity(Gravity.CENTER);
			other.setPadding(6, 6, 6, 6);
			other.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);

			learnLayout.addView(doneButton, layoutParams);
			learnLayout.addView(doneStatsButton, layoutParams);

			prompt.setText("You have marked cards as wrong " + wrongCount
					+ " times." + "\n" + "You have seen cards " + seenCount
					+ " times.");

			float accuracy = (((float) (seenCount - wrongCount) / seenCount) * 100);
			if (accuracy >= 0 && accuracy < 30)
				answer.setText("You have an accuracy rate of:" + "\n"
						+ accuracy + "%" + "\n" + "Very Poor");
			if (accuracy >= 30 && accuracy < 50)
				answer.setText("You have an accuracy rate of:" + "\n"
						+ accuracy + "%" + "\n" + "Not Too Bad");
			if (accuracy >= 50 && accuracy < 70)
				answer.setText("You have an accuracy rate of:" + "\n"
						+ accuracy + "%" + "\n" + "Average");
			if (accuracy >= 70 && accuracy < 90)
				answer.setText("You have an accuracy rate of:" + "\n"
						+ accuracy + "%" + "\n" + "Very Good");
			if (accuracy >= 90 && accuracy < 100)
				answer.setText("You have an accuracy rate of:" + "\n"
						+ accuracy + "%" + "\n" + "Excellent");
			if (accuracy == 100)
				answer.setText("You have an accuracy rate of:" + "\n"
						+ accuracy + "%" + "\n" + "Perfect");

			// The difference of cards in level 0-1 in the end and level 0-1 in
			// the beginning, are the cards that end up in level_2, learned
			learnCount = ((end_level_0 + end_level_1) - (start_level_0 + start_level_1));
			
			// The difference of cards in level 2-4 in the start and level 2-4 in the end, are the cards that drop down to 1
			forgotCount = ((start_level_2 + start_level_3 + start_level_4) - (end_level_2 + end_level_3 + end_level_4));
			
			if (learnCount > 0)
			other.setText("You have learned " + learnCount + " card(s).");
			
			if(forgotCount > 0)
				other.setText("You have forgotten " + forgotCount + " card(s).");


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
		Toast.makeText(context, "Undo not implemented.", Toast.LENGTH_SHORT)
				.show();
		/*
		 * if (lp.undo()) { isDone = false;
		 * prompt.startAnimation(anim_out_to_right);
		 * other.startAnimation(anim_out_to_right);
		 * answer.startAnimation(anim_out_to_right);
		 * anim_out_to_right.setAnimationListener(new AnimationListener() {
		 * 
		 * public void onAnimationStart(Animation animation) { }
		 * 
		 * public void onAnimationRepeat(Animation animation) { }
		 * 
		 * public void onAnimationEnd(Animation animation) { clearContent();
		 * itemsShown = 1; prompt.setText(lp.prompt());
		 * status.setText(lp.deckStatus());
		 * prompt.startAnimation(anim_in_to_right);
		 * other.startAnimation(anim_in_to_right);
		 * answer.startAnimation(anim_in_to_right); } });
		 * 
		 * }
		 */
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case 50:
			doDone();
			break;
		case 51:
			doDoneStats();
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

			if (hours > 0)
				timer.setText(String.format("%d:%02d:%02d", hours,
						minutes % 60, seconds % 60));

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
			Intent browserIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://www.mdbg.net/chindict/chindict.php?page=worddict&wdrst=0&wdqb="
							+ str));
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
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Xue Error Report, ID: "
				+ lp.currentIndex());
		emailIntent.putExtra(
				Intent.EXTRA_TEXT,
				"You are reporting an error on the following card:" + "\n"
						+ "\n" + "\u2022" + lp.prompt() + "\n" + "\u2022"
						+ lp.answer() + "\n" + "\u2022" + lp.other() + "\n"
						+ "\n" + "Comment:" + "\n");
		emailIntent.setType("message/rfc822");

		try {
			startActivity(Intent.createChooser(emailIntent,
					"Choose an e-mail client to send error report"));
		} catch (android.content.ActivityNotFoundException ex) {

		}
		lp.currentIndex();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d(TAG, "llkj");
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.quit)
					.setMessage(R.string.reallyQuit)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									LearnActivity.this.finish();
								}
							}).setNegativeButton(R.string.no, null).show();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public boolean audioOn() {
		settings = getSharedPreferences(
				getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings
				.getBoolean(getString(R.string.audio_state_on_off), true);
	}

	public int getECDeckSize() {
		settings = getSharedPreferences(
				getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.deck_size_ec_key),
				SettingsActivity.DEFAULT_EC_DECK_SIZE);
	}

	public int getCEDeckSize() {
		settings = getSharedPreferences(
				getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.deck_size_ce_key),
				SettingsActivity.DEFAULT_CE_DECK_SIZE);
	}

	public int getECTarget() {
		SharedPreferences settings = getSharedPreferences(
				getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.target_ec),
				SettingsActivity.DEFAULT_TARGET);
	}

	public int getCETarget() {
		SharedPreferences settings = getSharedPreferences(
				getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.target_ce),
				SettingsActivity.DEFAULT_TARGET);
	}

}
