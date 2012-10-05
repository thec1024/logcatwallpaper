/**
 * 
 */
package com.css.logcatwallpaper;

import java.io.IOException;

import android.util.Log;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.Shell;

/**
 * @author Chaitanya.Shende
 *
 */
public class LogReaderThreadAPI16  extends AbstractReaderThread {
	private Shell rootShell;
	private Command logcatCommand;

	public LogReaderThreadAPI16(LogcatWallpaper container) {
		super(container);
	}
	
	public synchronized void start() {
		try {
			logDebugMessage("Starting reader thread...");
			rootShell = Shell.startRootShell();
			logcatCommand = new Command(0, getLogcatCommand()) {
				@Override
				public void output(int arg0, String msg) {
					if(msg != null) {
						addToDisplayBuffer(msg);
					}
					try {
						Thread.sleep(getPollingDelay());
					} catch (InterruptedException e) {}
				}	
			};
			rootShell.add(logcatCommand);
			logDebugMessage("Logcat process started... Listening for logs...");
		} catch (IOException e) {
			addToDisplayBuffer(e.getMessage());
			Log.e(getApplication().getString(R.string.app_name), "I/O error while opening a stream from logcat process. Message: " + e.getMessage());
		} catch(Exception e) {
			addToDisplayBuffer(e.getMessage());
			Log.e(getApplication().getString(R.string.app_name), "General error while opening a stream from logcat process. Message: " + e.getMessage());
		}
	}

	public void destroy() {
		try {
			logDebugMessage("Closing reader and destroying thread...");
			rootShell.close();
		} catch (IOException e) {
			Log.e(getApplication().getString(R.string.app_name), "Error while closing reader for logcat process");
		}
		clearDisplayBuffer();
		logDebugMessage("Thread destroyed...");
	}
}
