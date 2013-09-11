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
		//Check whether the vocab file exists at given directory.
		if (!vocabFile.exists()) {
			Log.d(TAG, "Vocabulary file does not exist.");
			vocabFileExists = false;
		} else {
			Log.d(TAG, "Vocab file exists");
			vocabFileExists = true;
		}

	}


	public void downloadVocab(File filesDir) {
		File vocabFile = new File(filesDir + "/vocabUTF8.txt");

		// If the vocab file is found, check its filesize, open an http connection to get the filesize of the remote file.
		// Compare the filesize of the existing file with the filesize of the remote file.  If the sizes differ, download the remote file.
		if (vocabFileExists) {
			try {
				Log.d(TAG, "Checking For Updates");
				// Determine size of the local file.
				int vocabFileSize = (int) vocabFile.length();
				Log.d(TAG, "Existing vocab file size " + vocabFileSize + " bytes");

				// Open an http connection and determine size of the remote file.
				URL url = new URL(vocabURL);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				String header = connection.getHeaderField("Date");
				Log.d(TAG, "HEADER: " + header);
				int remoteFileSize = connection.getContentLength();
				Log.d(TAG, "Remote vocab file size " + remoteFileSize + " bytes");

				// Compare filesizes.  Set flag so that we 'pretend' the vocab file does not exist, so that a new one is downloaded.
				if (vocabFileSize != remoteFileSize) {
					Log.d(TAG, "Update Found");
					vocabFileExists = false;
					Log.d(TAG, "Existing vocab and remote file have a size mismatch.");
				}

				// Delete later. Debug message purposes only.
				else {
					Log.d(TAG, "No Updates Found");
					Log.d(TAG, "Existing vocab and remote file have the same size. No need to update.");
				}
			}

			catch (MalformedURLException e) {
				e.printStackTrace();
				Log.d(TAG, "Malformed URL Exception");
			} catch (ProtocolException e) {
				e.printStackTrace();
				Log.d(TAG, "Protocol Exception");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.d(TAG, "File Not Found Exception");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAG, "IO Exception");
			}
		}

		// Either no vocab file existed in the first place, or there is a size mismatch so a new one is downloaded.
		if (!vocabFileExists) {
			try {
				Log.d(TAG, "Downloading Update");
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
				Log.d(TAG, "Update Complete");
			}

			catch (MalformedURLException e) {
				e.printStackTrace();
				Log.d(TAG, "Malformed URL Exception");
			} catch (ProtocolException e) {
				e.printStackTrace();
				Log.d(TAG, "Protocol Exception");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.d(TAG, "File Not Found Exception");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAG, "IO Exception");
			}
		}
	}
}
