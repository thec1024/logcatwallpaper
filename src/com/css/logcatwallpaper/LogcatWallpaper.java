/**
 * 
 */
package com.css.logcatwallpaper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

/**
 * @author Chaitanya.Shende
 *
 */
public class LogcatWallpaper extends WallpaperService implements SharedPreferences.OnSharedPreferenceChangeListener {
	public static String APP_TAG_NAME = "";
	
	private Boolean isREADLOGSEnabled = true;
	private LogcatWallpaperEngine engine;
	
	public static final Boolean DEBUG_ENABLED = false;
	
	public LogcatWallpaper() {
		super();
		isREADLOGSEnabled = !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN); 
	}
	
	/* (non-Javadoc)
	 * @see android.service.wallpaper.WallpaperService#onCreateEngine()
	 */
	@Override
	public Engine onCreateEngine() {
		APP_TAG_NAME = getApplication().getString(R.string.app_name);
		engine = new LogcatWallpaperEngine(); 
		return engine;
	}
	
	private class LogcatWallpaperEngine extends Engine {
		 private final Handler handler = new Handler();
		 private final Paint paint = new Paint();
		 private AbstractReaderThread readerThread = ReaderThreadFactory.getReaderThread(isREADLOGSEnabled, LogcatWallpaper.this);
		 private LinkedList<String> screenBuffer = new LinkedList<String>();
		 private Integer maxScreenLines;
		 private Integer textSize;
		 private Integer pollDelayValue = 200;
		 private Integer drawDelayValue = 200;
		 private Integer screenWidth;
		 private Boolean wrapLines = false;
		 private String filter = "";

		 private final int V_COLOR = Color.BLUE;
		 private final int D_COLOR = Color.GREEN;
		 private final int I_COLOR = Color.WHITE;
		 private final int W_COLOR = Color.YELLOW;
		 private final int E_COLOR = Color.RED;
		 private final int F_COLOR = Color.RED; 
		 
		 private static final String DEFAULT_COLOR = "#00FFFFFF";
		 
		 private final Map<String, Integer> levelMap = new HashMap<String, Integer>() {
			private static final long serialVersionUID = 1288203761570397850L;
			{
			 put("V", V_COLOR);
			 put("D", D_COLOR);
			 put("I", I_COLOR);
			 put("W", W_COLOR);
			 put("E", E_COLOR);
			 put("F", F_COLOR);
		 }};
		 
		 private final Runnable theDrawer = new Runnable() {
			 public void run() {
				 drawFrame();
			 }
		 };
		
		 
		 public LogcatWallpaperEngine() {
			 paint.setColor(0xffffffff);
			 paint.setDither(true);
			 paint.setTypeface(Typeface.SANS_SERIF);
			 paint.setAntiAlias(true);
			 paint.setStrokeWidth(1);
			 paint.setStrokeCap(Paint.Cap.SQUARE);
			 paint.setStyle(Paint.Style.STROKE);
			 
			 textSize = 14;
			 paint.setTextSize(textSize);
			 
			 calculateSizes();
			 
			 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LogcatWallpaper.this);
			 
			 pollDelayValue = Integer.parseInt(settings.getString(getString(R.string.key_poll_delay_param), "200"));
			 drawDelayValue = Integer.parseInt(settings.getString(getString(R.string.key_draw_delay_param), "200"));
			 wrapLines = settings.getBoolean(getString(R.string.key_wrap_lines_param), false);
			 setFilter(settings.getString(getString(R.string.key_filter_param), ""));
			 readerThread.setPollingDelay(pollDelayValue);
			 
			 levelMap.put("V", Color.parseColor(settings.getString(getString(R.string.key_verbose_color), DEFAULT_COLOR)));
			 logDebugMessage("V = " + levelMap.get("V"));
			 levelMap.put("D", Color.parseColor(settings.getString(getString(R.string.key_debug_color), DEFAULT_COLOR)));
			 logDebugMessage("D = " + levelMap.get("D"));
			 levelMap.put("I", Color.parseColor(settings.getString(getString(R.string.key_info_color), DEFAULT_COLOR)));
			 logDebugMessage("I = " + levelMap.get("I"));
			 levelMap.put("W", Color.parseColor(settings.getString(getString(R.string.key_warning_color), DEFAULT_COLOR)));
			 logDebugMessage("W = " + levelMap.get("W"));
			 levelMap.put("E", Color.parseColor(settings.getString(getString(R.string.key_error_color), DEFAULT_COLOR)));
			 logDebugMessage("E = " + levelMap.get("E"));
			 levelMap.put("F", Color.parseColor(settings.getString(getString(R.string.key_fatal_color), DEFAULT_COLOR)));
			 logDebugMessage("F = " + levelMap.get("F"));
			 
			 readerThread.start();
		 }
		 
		 private void calculateSizes() {
			 DisplayMetrics metrics = new DisplayMetrics();
			 Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			 display.getMetrics(metrics);
			 maxScreenLines = metrics.heightPixels / textSize;
			 screenWidth = metrics.widthPixels;
			 logDebugMessage("Setting max sceen lines to: " + maxScreenLines);
		 }
		 
		 @Override
		 public void onVisibilityChanged(boolean visible) {
			 if (visible)
				 drawFrame();
			 else
				 handler.removeCallbacks(theDrawer);
		 }
		 
		 
		 
		 @Override
		 public void onDestroy() {
			 super.onDestroy();
			 handler.removeCallbacks(theDrawer);
			 readerThread.destroy();
		 }
 
		 @Override
		 public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			 super.onSurfaceChanged(holder, format, width, height);
//			 clearFrame();
			 maxScreenLines = height / textSize;
			 screenWidth = width;
			 drawFrame();
		 }
		 
		 @Override
		 public void onSurfaceDestroyed(SurfaceHolder holder) {
			 super.onSurfaceDestroyed(holder);
			 handler.removeCallbacks(theDrawer);
		 }

		 @Override
		 public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
			 drawFrame();
		 }
		 
		 private void drawFrame() {
			 final SurfaceHolder holder = getSurfaceHolder();
			 Canvas c = null;
			 try {
				 c = holder.lockCanvas();
				 if (c != null) 
					 drawPattern(c);
			 }
			 finally {
				 if (c != null)
					 holder.unlockCanvasAndPost(c);
				 handler.removeCallbacks(theDrawer);
				 if (isVisible())
					 handler.postDelayed(theDrawer, drawDelayValue);
			 }
		 }
		 
		 private void clearFrame() {
			 final SurfaceHolder holder = getSurfaceHolder();
			 Canvas c = null;
			 try {
				 c = holder.lockCanvas();
				 if (c != null) 
					 c.drawColor(Color.BLACK);
			 }
			 finally {
				 if (c != null)
					 holder.unlockCanvasAndPost(c);
			 }
		 }
		 
		 private void drawPattern(Canvas canvas) {
			 canvas.drawColor(Color.BLACK);
			 int numLinesFromThread = readerThread.getNumLines();			 
			 List<String> textToDraw = readerThread.getDisplayBuffer();
			 if(numLinesFromThread > maxScreenLines) {
				 screenBuffer.clear();
			 }
			 while(!screenBuffer.isEmpty() && (numLinesFromThread + screenBuffer.size()) >= maxScreenLines) {
				 screenBuffer.removeFirst();
			 }
			 
			 screenBuffer.addAll(textToDraw);
			 int y = 0;
			 Iterator<String> iterator = screenBuffer.iterator();
			 int origColor = paint.getColor();
			 while(iterator.hasNext()) {
				 String msg = iterator.next();
				 if(filter != null && filter.length() != 0 && !msg.matches(filter)) continue;
				 if(msg == null || msg.length() == 0) continue;
				 String level = msg.substring(0, 1).toUpperCase();
				 int newColor = levelMap.containsKey(level) ? levelMap.get(level) : Color.WHITE;
				 paint.setColor(newColor);
				 
				 if(wrapLines) {
					 int finishedWidth = 0;
					 float widths[] = new float[msg.length()];
					 paint.getTextWidths(msg, widths);
					 StringBuffer subMsg = new StringBuffer();
					 int index = 0;
					 for(; index < msg.length(); index++) {
						 subMsg.append(msg.charAt(index));
						 finishedWidth += widths[index];
						 if(finishedWidth >= screenWidth ) {
							 canvas.drawText(subMsg.toString(), canvas.getClipBounds().left, canvas.getClipBounds().top + y, paint);
							 finishedWidth = 0;
							 y += textSize; 
							 subMsg.setLength(0);
						 }
					 }
					 if(finishedWidth < screenWidth) {
						 /* remainder */
						 canvas.drawText(subMsg.toString(), canvas.getClipBounds().left, canvas.getClipBounds().top + y, paint);
						 y += textSize;
					 }
				 } else {
					 canvas.drawText(msg, canvas.getClipBounds().left, canvas.getClipBounds().top + y, paint);
					 y += textSize;
				 }
			 }
			 paint.setColor(origColor);
		 }
		
		public void updateLevelMap(Map<String, Integer> levelMap) {
			this.levelMap.putAll(levelMap);
		}
		
		public void setFilter(String filter) {
			this.filter = filter.replace(".", "\\.").replace("*", ".*").replace("?", ".");
		}
	}
	
	public void logDebugMessage(String msg) {
		if(DEBUG_ENABLED)
			Log.v(APP_TAG_NAME, msg);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Map<String, Integer> map = new HashMap<String, Integer>(1);
		if(key.equalsIgnoreCase(getString(R.string.key_poll_delay_param))) {
			 engine.pollDelayValue = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_poll_delay_param), "200"));
		} else if(key.equalsIgnoreCase(getString(R.string.key_draw_delay_param))) {
			engine.drawDelayValue = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_draw_delay_param), "200"));
		} else if(key.equalsIgnoreCase(getString(R.string.key_wrap_lines_param))) {
			engine.wrapLines = sharedPreferences.getBoolean(getString(R.string.key_wrap_lines_param), false);
		} else if(key.equalsIgnoreCase(getString(R.string.key_filter_param))) {
			engine.setFilter(sharedPreferences.getString(getString(R.string.key_filter_param), ""));
		}
		else {
			map.put(key, Color.parseColor(sharedPreferences.getString(key, "#"+Integer.toHexString(sharedPreferences.getInt(key, Color.WHITE)))));
			engine.updateLevelMap(map);
		}
		engine.readerThread.reset(engine.pollDelayValue, engine.filter);
//		engine.clearFrame();
	}
}
