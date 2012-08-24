/**
 * 
 */
package com.css.logcatwallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
/**
 * @author Chaitanya.Shende
 *
 */
public class LogReaderThread extends Thread implements IReaderThread {
	private Process readerProcess = null;
	private BufferedReader reader = null;
	private final String separator = System.getProperty("line.separator");
	private List<String> displayBuffer = new ArrayList<String>();
	private LogcatWallpaper container;
	private boolean run = false;
	
	public LogReaderThread(LogcatWallpaper container) {
		this.container = container;
	}
		
	@Override
	public synchronized void start() {
		try {
			container.logDebugMessage("Starting reader thread...");
			readerProcess = Runtime.getRuntime().exec("logcat");
			reader = new BufferedReader(new InputStreamReader(readerProcess.getInputStream()));
			if(reader == null)
				throw new IOException("Can not open reader for logcat");
			container.logDebugMessage("Logcat process started... Listening for logs...");
			run = true;
		} catch (IOException e) {
			displayBuffer.add(e.getMessage());
			Log.e(LogcatWallpaper.APP_TAG_NAME, "I/O error while opening a stream from logcat process. Message: " + e.getMessage());
		} catch(Exception e) {
			displayBuffer.add(e.getMessage());
			Log.e(LogcatWallpaper.APP_TAG_NAME, "General error while opening a stream from logcat process. Message: " + e.getMessage());
		}
		super.start();
	}

	@Override
	public void run() {
		container.logDebugMessage("Started LogReaderThread.run()...");
		if(readerProcess != null && reader != null) {
			try {
				while(run) {
					String msg = reader.readLine();
					if(msg != null) {
//						container.logDebugMessage("Read in: " + (msg == null ? "null" : msg));
						displayBuffer.add(msg);
						displayBuffer.add(separator);
					}
					try {
						sleep(THREAD_SLEEP_DURATION);
					} catch (InterruptedException e) {}
				}
			} catch (IOException e) {
				displayBuffer.add(e.getMessage());
				Log.e(LogcatWallpaper.APP_TAG_NAME, "Error while reading from logcat process. Message: " + e.getMessage());
			} catch(Exception e) {
				displayBuffer.add(e.getMessage());
				Log.e(LogcatWallpaper.APP_TAG_NAME, "General error while reading from logcat process. Message: " + e.getMessage());
			}
		}
	}
	
	@Override
	public void destroy() {
		run = false;
		try {
			container.logDebugMessage("Closing reader and destroying thread...");
			reader.close();
		} catch (IOException e) {
			Log.e(LogcatWallpaper.APP_TAG_NAME, "Error while closing reader for logcat process");
		}
		displayBuffer.clear();
		readerProcess.destroy();
		container.logDebugMessage("Thread destroyed...");
	}

	public List<String> getDisplayBuffer() {
		List<String> finalString = new ArrayList<String>(displayBuffer.size());
		finalString.addAll(displayBuffer);
		displayBuffer.clear();
		return finalString;
	}
	
	public Integer getNumLines() {
		return displayBuffer.size();
	}
}
