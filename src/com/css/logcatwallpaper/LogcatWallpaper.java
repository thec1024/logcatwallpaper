/**
 * 
 */
package com.css.logcatwallpaper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
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
public class LogcatWallpaper extends WallpaperService {
	public static String APP_TAG_NAME = "";
	
	private Boolean isREADLOGSEnabled = true;
	
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
		return new LogcatWallpaperEngine();
	}
	
	private class LogcatWallpaperEngine extends Engine {
		 private final Handler handler = new Handler();
		 private final Paint paint = new Paint();
		 private IReaderThread readerThread = ReaderThreadFactory.getReaderThread(isREADLOGSEnabled, LogcatWallpaper.this);
		 private LinkedList<String> screenBuffer = new LinkedList<String>();
		 private Integer maxScreenLines;
		 private Integer textSize;
		 
		 private final int V_COLOR = Color.BLUE;
		 private final int D_COLOR = Color.GREEN;
		 private final int I_COLOR = Color.WHITE;
		 private final int W_COLOR = Color.YELLOW;
		 private final int E_COLOR = Color.RED;
		 private final int F_COLOR = Color.RED; 
		 
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
			 readerThread.start();
//			 textSize = (int)FloatMath.ceil((paint.getTextSize()));
			 textSize = 14;
			 paint.setTextSize(textSize);
			 
			 DisplayMetrics metrics = new DisplayMetrics();
			 Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			 display.getMetrics(metrics);
			 maxScreenLines = metrics.heightPixels / textSize;
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
					 handler.postDelayed(theDrawer, 600);
			 }
		 }
		 
		 private void drawPattern(Canvas canvas) {
			 logDebugMessage("Started drawing...");
			 canvas.drawColor(Color.BLACK);
			 int numLinesFromThread = readerThread.getNumLines();			 
			 List<String> textToDraw = readerThread.getDisplayBuffer();
			 logDebugMessage("Number of lines to draw: " + textToDraw.size());
			 logDebugMessage("Number of lines in buffer: " + screenBuffer.size());
			 if(numLinesFromThread > maxScreenLines) {
				 screenBuffer.clear();
			 }
			 while(!screenBuffer.isEmpty() && (numLinesFromThread + screenBuffer.size()) >= maxScreenLines) {
				 screenBuffer.removeFirst();
			 }
			 
			 screenBuffer.addAll(textToDraw);
			 logDebugMessage("Number of lines in buffer to draw: " + screenBuffer.size());
			 int y = 0;
			 Iterator<String> iterator = screenBuffer.iterator();
			 while(iterator.hasNext()) {
				 String msg = iterator.next();
				 if(msg == null || msg.length() == 0) continue;
				 String level = msg.substring(0, 1).toUpperCase();
				 logDebugMessage("Drawing msg: " + msg + " at level: " + level);
				 int origColor = paint.getColor();
				 int newColor = levelMap.containsKey(level) ? levelMap.get(level) : Color.WHITE;
				 paint.setColor(newColor);
				 canvas.drawText(msg, canvas.getClipBounds().left, canvas.getClipBounds().top + y, paint);
				 paint.setColor(origColor);
				 y += textSize;
			 }
			 logDebugMessage("Finished drawing...");
		 }
	}
	
	public void logDebugMessage(String msg) {
		if(DEBUG_ENABLED)
			Log.v(APP_TAG_NAME, msg);
	}
}
