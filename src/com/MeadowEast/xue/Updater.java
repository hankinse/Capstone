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
import android.util.Log;

public class Updater {

	public static boolean vocabFileExists;
	public static String vocabURL = "http://www.meadoweast.com/capstone/vocabUTF8.txt";
	static final String TAG = "XUE Updater";

	public void checkVocabFileExists(File filesDir) {
		File vocabFile = new File(filesDir + "/vocabUTF8.txt");

		Log.d(TAG, "Checking if vocabulary file exists.");
		// Check whether the vocab file exists at given directory.
		if (!vocabFile.exists()) {
			Log.d(TAG, "Vocabulary file does not exist.");
			vocabFileExists = false;
		} else {
			Log.d(TAG, "Vocab file exists");
			vocabFileExists = true;
		}

	}

	// / Returns the 'date' field of the file header from the website or "ERROR"
	public String getCurrentVersion() {
		String header = "ERROR";
		try {
			// Open an http connection and get the "date" field of the header.
			URL url = new URL(vocabURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			header = connection.getHeaderField("Last-Modified");
			Log.d(TAG, "Header date found: " + header);
		}

		catch (MalformedURLException e) {
			Log.d(TAG, "Malformed URL Exception");
		} catch (ProtocolException e) {
			Log.d(TAG, "Protocol Exception");
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File Not Found Exception");
		} catch (IOException e) {
			Log.d(TAG, "IO Exception");
		}

		return header;
	}

	// We only download the vocab file if we already know the local version is
	// not
	// the current version.
	public void downloadVocab(File filesDir) {
		File vocabFile = new File(filesDir + "/vocabUTF8.txt");

		try {
			Log.d(TAG, "Downloading Update");
			// URL of remote vocabulary file
			URL url = new URL(vocabURL);

			// Open http connection to get the vocab file.
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
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
				Log.d(TAG, "" + downloaded + "/" + fileSize
						+ " bytes downloaded.");
			}

			fos.flush();
			fos.close();
			is.close();
			Log.d(TAG, "Vocab file download closed.");
			Log.d(TAG, "Update Complete");
		}

		catch (MalformedURLException e) {
			Log.d(TAG, "Malformed URL Exception");
		} catch (ProtocolException e) {
			Log.d(TAG, "Protocol Exception");
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File Not Found Exception");
		} catch (IOException e) {
			Log.d(TAG, "IO Exception");
		}
	}
}
