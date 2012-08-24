/**
 * 
 */
package com.css.logcatwallpaper;

import java.util.List;

/**
 * @author Chaitanya.Shende
 *
 */
public interface IReaderThread {
	
	public final long THREAD_SLEEP_DURATION = 300;
	
	public void destroy();
	public void start();
	public List<String> getDisplayBuffer();
	public Integer getNumLines();
}
