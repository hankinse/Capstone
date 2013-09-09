package com.MeadowEast.xue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

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

public class MainActivity extends Activity implements OnClickListener {

	Button ecButton, ceButton, exitButton, settingsButton;
	public static File filesDir;
	public static boolean vocabFileExists;
	public static String vocabURL = "http://www.meadoweast.com/capstone/vocabUTF8.txt";
	public static String mode;
	static final String TAG = "XUE MainActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
				checkVocabFileExists();
				downloadVocab();
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

	public void checkVocabFileExists() {
		File vocabFile = new File(filesDir + "/vocabUTF8.txt");

		Log.d(TAG, "Checking if vocabulary file exists.");
		//Check whether the vocab file exists at given directory.
		if (!vocabFile.exists()) {
			Log.d(TAG, "Vocabulary file does not exist.");
			vocabFileExists = false;
		} else {
			Log.d(TAG, "Vocab file exists");
			vocabFileExists = true;
		}

	}

	public void downloadVocab() {
		File vocabFile = new File(filesDir + "/vocabUTF8.txt");

		// If the vocab file is found, check its filesize, open an http connection to get the filesize of the remote file.
		// Compare the filesize of the existing file with the filesize of the remote file.  If the sizes differ, download the remote file.
		if (vocabFileExists) {
			try {
				displayMessage("Checking For Updates");
				// Determine size of the local file.
				int vocabFileSize = (int) vocabFile.length();
				Log.d(TAG, "Existing vocab file size " + vocabFileSize + " bytes");

				// Open an http connection and determine size of the remote file.
				URL url = new URL(vocabURL);
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				int remoteFileSize = httpConnection.getContentLength();
				Log.d(TAG, "Remote vocab file size " + remoteFileSize + " bytes");

				// Compare filesizes.  Set flag so that we 'pretend' the vocab file does not exist, so that a new one is downloaded.
				if (vocabFileSize != remoteFileSize) {
					displayMessage("Update Found");
					vocabFileExists = false;
					Log.d(TAG, "Existing vocab and remote file have a size mismatch.");
				}

				// Delete later. Debug message purposes only.
				else {
					displayMessage("No Updates Found");
					Log.d(TAG, "Existing vocab and remote file have the same size. No need to update.");
				}
			}

			catch (MalformedURLException e) {
				e.printStackTrace();
				displayMessage("Malformed URL Exception");
			} catch (ProtocolException e) {
				e.printStackTrace();
				displayMessage("Protocol Exception");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				displayMessage("File Not Found Exception");
			} catch (IOException e) {
				e.printStackTrace();
				displayMessage("IO Exception");
			}

		}

		// Either no vocab file existed in the first place, or there is a size mismatch so a new one is downloaded.
		if (!vocabFileExists) {
			try {
				displayMessage("Downloading Update");
				// URL of remote vocabulary file
				URL url = new URL(vocabURL);

				//  Open http connection to get the vocab file.
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				Log.d(TAG, "Opening connection to download vocab file.");
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				FileOutputStream fos = new FileOutputStream(vocabFile);
				InputStream is = httpConnection.getInputStream();

				// Total size of remote vocab file
				int fileSize = httpConnection.getContentLength();
				Log.d(TAG, "" + fileSize);

				// Total bytes downloaded currently
				int downloaded = 0;

				byte[] buffer = new byte[1024];

				// Temporary size of buffer
				int bufferSize = 0;

				// Read the input buffer and write output file
				while ((bufferSize = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bufferSize);

					// Increment the amount that is downloaded
					downloaded += bufferSize;
					Log.d(TAG, "" + downloaded + "/" + fileSize + " bytes downloaded.");
				}

				fos.flush();
				fos.close();
				is.close();
				Log.d(TAG, "Vocab file download closed.");
				displayMessage("Update Complete");
			}

			catch (MalformedURLException e) {
				e.printStackTrace();
				displayMessage("Malformed URL Exception");
			} catch (ProtocolException e) {
				e.printStackTrace();
				displayMessage("Protocol Exception");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				displayMessage("File Not Found Exception");
			} catch (IOException e) {
				e.printStackTrace();
				displayMessage("IO Exception");
			}
		}
	}
}
