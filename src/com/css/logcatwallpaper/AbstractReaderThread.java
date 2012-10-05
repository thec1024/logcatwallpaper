/**
 * 
 */
package com.css.logcatwallpaper;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

/**
 * @author Chaitanya.Shende
 *
 */
public abstract class AbstractReaderThread extends Thread  {
	
	public final Integer THREAD_SLEEP_DURATION = 300;
	
	
	
	private List<String> displayBuffer = new ArrayList<String>();
	private LogcatWallpaper container;
	
	private Integer pollingDelay = THREAD_SLEEP_DURATION;
	private String logcatCommand = "logcat";
	
	public abstract void destroy();
	
	public AbstractReaderThread(LogcatWallpaper container) {
		this.container = container;
	}
	
	public void setPollingDelay(Integer pollingDelay) {
		this.pollingDelay = pollingDelay != null && pollingDelay > 0 ? pollingDelay : THREAD_SLEEP_DURATION;		
	}

	public void setFilterSpecs(String filterSpecs) {
		if(filterSpecs != null && !(filterSpecs.length() == 0) && !filterSpecs.startsWith("-")) 
			logcatCommand = "logcat " + filterSpecs;
		else
			logcatCommand = "logcat";
	}
	
	protected String[] getLogcatCommand() {
//		return logcatCommand.split(" ");
		return new String[] {logcatCommand};
	}

	protected void logDebugMessage(String msg) {
		container.logDebugMessage(msg);
	}
	
	protected void addToDisplayBuffer(String msg) {
		if(msg != null && displayBuffer != null) displayBuffer.add(msg);
	}
	
	protected Integer getPollingDelay() {
		return pollingDelay;
	}
	
	protected Integer getDisplayBufferSize() {
		return displayBuffer == null ? 0 : displayBuffer.size();
	}
	
	protected void clearDisplayBuffer() {
		if(displayBuffer != null) displayBuffer.clear();
	}
	
	protected LogcatWallpaper getApplication() {
		return container;
	}
	
	public Integer getNumLines() {
		return getDisplayBufferSize();
	}
	
	public synchronized void start() {
		super.start();
	}
	
	public List<String> getDisplayBuffer() {
		List<String> newList = new ArrayList<String>(displayBuffer.size());
		newList.addAll(displayBuffer);
		displayBuffer.clear();
		return newList;
	}
	
	public void reset(Integer pollDelay, String filter) {
		clearDisplayBuffer();
		setPollingDelay(pollDelay);
		setFilterSpecs(filter);
	}
}
