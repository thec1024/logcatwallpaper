/**
 * 
 */
package com.css.logcatwallpaper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.Shell;

/**
 * @author Chaitanya.Shende
 *
 */
public class LogReaderThreadAPI16  implements IReaderThread {
	private Shell rootShell;
	private Command logcatCommand;
	private final String separator = System.getProperty("line.separator");
	private List<String> displayBuffer = new ArrayList<String>();
	private LogcatWallpaper container;
	
	public LogReaderThreadAPI16(LogcatWallpaper container) {
		this.container = container;
	}
		
	public synchronized void start() {
		try {
			container.logDebugMessage("Starting reader thread...");
			rootShell = Shell.startRootShell();
			logcatCommand = new Command(0, "logcat") {
				@Override
				public void output(int arg0, String msg) {
					if(msg != null) {
						container.logDebugMessage("Read in: " + (msg == null ? "null" : msg));
						displayBuffer.add(msg);
						displayBuffer.add(separator);
					}
					try {
						Thread.sleep(THREAD_SLEEP_DURATION);
					} catch (InterruptedException e) {}
				}	
			};
			rootShell.add(logcatCommand);
			container.logDebugMessage("Logcat process started... Listening for logs...");
		} catch (IOException e) {
			displayBuffer.add(e.getMessage());
			Log.e(getApplication().getString(R.string.app_name), "I/O error while opening a stream from logcat process. Message: " + e.getMessage());
		} catch(Exception e) {
			displayBuffer.add(e.getMessage());
			Log.e(getApplication().getString(R.string.app_name), "General error while opening a stream from logcat process. Message: " + e.getMessage());
		}
	}

	public void destroy() {
		try {
			container.logDebugMessage("Closing reader and destroying thread...");
			rootShell.close();
		} catch (IOException e) {
			Log.e(getApplication().getString(R.string.app_name), "Error while closing reader for logcat process");
		}
		displayBuffer.clear();
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
	
	private LogcatWallpaper getApplication() {
		return container;
	}
}
