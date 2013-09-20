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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LearnActivity extends Activity implements OnClickListener,
		OnLongClickListener {
	static final String TAG = "LearnActivity";
	static final int ECDECKSIZE = 40;
	static final int CEDECKSIZE = 60;

	static Handler timerHandler;
	int seconds;
	long lastTime;

	SharedPreferences settings;
	LearningProject lp;
	int itemsShown;
	TextView prompt, answer, other, status, timer;
	Button advance, okay;
	static Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learn);
		Log.d(TAG, "Entering onCreate");
		context = this.getApplicationContext();

        itemsShown = 0;
        prompt  = (TextView) findViewById(R.id.promptTextView);
        status  = (TextView) findViewById(R.id.statusTextView);
        other   = (TextView) findViewById(R.id.otherTextView);
        answer  = (TextView) findViewById(R.id.answerTextView);
        advance  = (Button) findViewById(R.id.advanceButton);
        okay     = (Button) findViewById(R.id.okayButton);
        timer	= (TextView) findViewById(R.id.timerTextView);
    	   
    	findViewById(R.id.advanceButton).setOnClickListener(this);
    	findViewById(R.id.okayButton).setOnClickListener(this);
    	
    	findViewById(R.id.promptTextView).setOnLongClickListener(this);
    	findViewById(R.id.answerTextView).setOnLongClickListener(this);
    	findViewById(R.id.otherTextView).setOnLongClickListener(this);
    	
    	int deckSize = getDeckSize();
    	if (MainActivity.mode.equals("ec"))
 //   		lp = new EnglishChineseProject(ECDECKSIZE);	
    		lp = new EnglishChineseProject(deckSize);
    	else
 //   		lp = new ChineseEnglishProject(CEDECKSIZE);
    		lp = new ChineseEnglishProject(deckSize);
    	clearContent();
    	doAdvance();
    	
    	seconds = 0;
    	lastTime = System.currentTimeMillis();
		timerHandler = new Handler();
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
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
			// TextToSpeech.englishToSpeech(lp.prompt());
			answer.setText(lp.answer());
			itemsShown++;
		} else if (itemsShown == 2) {
			Log.d(TAG, lp.other());
			// TextToSpeech.hanziToSpeech((String)other.getText());
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
		if (okay.getText().equals("done"))
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
		// Do nothing unless answer has been seen
		if (itemsShown < 2)
			return;
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

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.advanceButton:
			doAdvance();
			break;
		case R.id.okayButton:
			doOkay();
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
			mdbg(prompt);
			break;

		case R.id.answerTextView:
			mdbg(answer);

			break;
		case R.id.otherTextView:
			mdbg(other);

			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		lastTime = System.currentTimeMillis();
		timerHandler.postDelayed(runnable, 1000);
	}

	@Override
	protected void onPause() {
		super.onPause();

		long current = System.currentTimeMillis();
		long delta = current - lastTime;
		lastTime = current;
		seconds += (int) (delta / 1000);
		
		timerHandler.removeCallbacks(runnable);
	}

	private final Runnable runnable = new Runnable() {
		public void run() {
			long current = System.currentTimeMillis();
			long delta = current - lastTime;
			lastTime = current;
			seconds += (int) (delta / 1000);
			int minutes = seconds / 60;
			int hours = minutes / 60;

			if (hours > 0)
				timer.setText(String.format("%d:%02d:%02d", hours,
						minutes % 60, seconds % 60));

			else
				timer.setText(String.format("%d:%02d", minutes, seconds % 60));

			timerHandler.postDelayed(runnable, 1000);
		}
	};

	public void mdbg(TextView text) {
		String str = text.getText().toString();
		int start = text.getSelectionStart();
		int end = text.getSelectionEnd();
		str = str.substring(start, end);

		Intent browserIntent = new Intent(
				Intent.ACTION_VIEW,
				Uri.parse("http://www.mdbg.net/chindict/chindict.php?page=worddict&wdrst=0&wdqb="
						+ str));
		startActivity(browserIntent);

	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Log.d(TAG, "llkj");
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.quit)
            .setMessage(R.string.reallyQuit)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LearnActivity.this.finish();    
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
            return true;
        } else {
        	return super.onKeyDown(keyCode, event);
        }
    }
    
    public boolean audioOn() {
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getBoolean(getString(R.string.audio_state_on_off), true);
    }
    
    public int getDeckSize() {
		settings = getSharedPreferences(getString(R.string.shared_settings_key), Context.MODE_PRIVATE);
		return settings.getInt(getString(R.string.deck_size_key), SettingsActivity.DEFAULT_DECK_SIZE);	
    }
}
