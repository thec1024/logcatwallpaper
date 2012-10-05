/**
 * 
 */
package com.css.logcatwallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;
/**
 * @author Chaitanya.Shende
 *
 */
public class LogReaderThread extends AbstractReaderThread {
	private Process readerProcess = null;
	private BufferedReader reader = null;
	private boolean run = false;
	
	public LogReaderThread(LogcatWallpaper container) {
		super(container);
	}
		
	@Override
	public synchronized void start() {
		try {
			logDebugMessage("Starting reader thread...");
			readerProcess = Runtime.getRuntime().exec(getLogcatCommand());
			reader = new BufferedReader(new InputStreamReader(readerProcess.getInputStream()));
			if(reader == null)
				throw new IOException("Can not open reader for logcat");
			logDebugMessage("Logcat process started... Listening for logs...");
			run = true;
		} catch (IOException e) {
			addToDisplayBuffer(e.getMessage());
			Log.e(LogcatWallpaper.APP_TAG_NAME, "I/O error while opening a stream from logcat process. Message: " + e.getMessage());
		} catch(Exception e) {
			addToDisplayBuffer(e.getMessage());
			Log.e(LogcatWallpaper.APP_TAG_NAME, "General error while opening a stream from logcat process. Message: " + e.getMessage());
		}
		super.start();
	}

	@Override
	public void run() {
		logDebugMessage("Started LogReaderThread.run()...");
		if(readerProcess != null && reader != null) {
			try {
				while(run) {
					String msg = reader.readLine();
					if(msg != null) {
						addToDisplayBuffer(msg);
					}
					try {
						sleep(getPollingDelay());
					} catch (InterruptedException e) {}
				}
			} catch (IOException e) {
				addToDisplayBuffer(e.getMessage());
				Log.e(LogcatWallpaper.APP_TAG_NAME, "Error while reading from logcat process. Message: " + e.getMessage());
			} catch(Exception e) {
				addToDisplayBuffer(e.getMessage());
				Log.e(LogcatWallpaper.APP_TAG_NAME, "General error while reading from logcat process. Message: " + e.getMessage());
			}
		}
	}
	
	@Override
	public void destroy() {
		run = false;
		try {
			logDebugMessage("Closing reader and destroying thread...");
			reader.close();
		} catch (IOException e) {
			Log.e(LogcatWallpaper.APP_TAG_NAME, "Error while closing reader for logcat process");
		}
		clearDisplayBuffer();
		readerProcess.destroy();
		logDebugMessage("Thread destroyed...");
	}
}
