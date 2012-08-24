/**
 * 
 */
package com.css.logcatwallpaper;

/**
 * @author Chaitanya.Shende
 *
 */
public abstract class ReaderThreadFactory {
	public static final IReaderThread getReaderThread(Boolean isREADLOGSEnabled, LogcatWallpaper container) {
		if(isREADLOGSEnabled) 
			return new LogReaderThread(container);
		else
			return new LogReaderThreadAPI16(container);
	}
}
