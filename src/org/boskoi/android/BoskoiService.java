/** 
 ** Copyright (c) 2010 Boskoi
 ** All rights reserved
 ** Contact: developer@boskoi.org
 ** Developers: Joey van der Bie, Maarten van der Mark and Vincent Vijn
 ** Website: http://www.boskoi.org
 ** 
 ** GNU Lesser General Public License Usage
 ** This file may be used under the terms of the GNU Lesser
 ** General Public License version 3 as published by the Free Software
 ** Foundation and appearing in the file LICENSE.LGPL included in the
 ** packaging of this file. Please review the following information to
 ** ensure the GNU Lesser General Public License version 3 requirements
 ** will be met: http://www.gnu.org/licenses/lgpl.html.	
 **	
 **
 ** If you have questions regarding the use of this file, please contact
 ** Boskoi developers at developer@boskoi.org.
 ** 
 **/

package org.boskoi.android;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.http.impl.client.DefaultHttpClient;
import org.boskoi.android.data.BlogData;
import org.boskoi.android.data.BoskoiDatabase;
import org.boskoi.android.data.CategoriesData;
import org.boskoi.android.data.IncidentsData;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BoskoiService extends Service {
	public static final String PREFS_NAME = "BoskoiService";
	public static boolean httpRunning = false;
	public static final DefaultHttpClient httpclient = new DefaultHttpClient();
	public static Vector<String> mNewIncidentsImages = new Vector<String>();
	public static String incidentsResponse = "";
	public static String categoriesResponse = "";
	public static String categoriesLangResponse = "";
	public static String savePath = "";
	public static String domain = "http://futuretechnologies.nl/boskoi/";
	public static String firstname = "";
	public static String lastname = "";
	public static String email = "";
	public static int countries = 0;
	public static int AutoUpdateDelay = 0;
	public static String totalReports = "1000";
	public static String fileName = "";
	public static boolean AutoFetch = false;
	public static String total_reports = "";
	public static boolean smsUpdate = false;
	public static boolean vibrate = false;
	public static boolean ringtone = false;
	public static boolean flashLed = false;
	public static String username = "";
	public static String password = "";
	public static long blogLastUpdate = 0;
	public static String lastUpdate = "1970-01-01 00:00:00"; // default value
	public static long updateInterval = 24 * 60 * 60 * 1000; // currently 24
	// hours
	public static Locale language = Locale.getDefault();
	public static int numberOfNewReports = 0;
	private Handler mHandler = new Handler();
	public static String lastVersion = "";

	private static final String TAG = "Boskoi - New Updates";
	public static final String NEW_USHAHIDI_REPORT_FOUND = "New_Ushahidi_Report_Found";
	public static final int NOTIFICATION_ID = 1;

	private Notification newBoskoiReportNotification;
	private NotificationManager mNotificationManager;
	private static QueueThread queue;
	public static GoogleAnalyticsTracker tracker;

	public static String fontPath = "font/telegrafico.ttf";

	private BoskoiDatabase getDb() {
		return BoskoiApplication.mDb;
	}

	/**
	 * Local services Binder.
	 * 
	 * @author eyedol
	 * 
	 */
	public class LocalBinder extends Binder {
		BoskoiService getService() {
			return BoskoiService.this;
		}
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			BoskoiService.saveSettings(getApplicationContext());

			// Util.fetchReports(BoskoiService.this);

			showNotification(total_reports);
			mHandler.postAtTime(mUpdateTimeTask, SystemClock.uptimeMillis()
					+ (1000 * 60 * AutoUpdateDelay));

		}
	};

	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		queue = new QueueThread("ushahidi");
		mHandler = new Handler();

		if (AutoFetch) {
			// Log.i("Service ","Service is checked to start.");
			mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mHandler
					.postDelayed(mUpdateTimeTask, (1000 * 60 * AutoUpdateDelay));

		} else {
			// Log.i("Service ","Service is unchecked.");
		}

		final Thread tr = new Thread() {
			@Override
			public void run() {
				while (true) {
					queue.GetQueueItem().start();
				}
			}
		};
		tr.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mNotificationManager.cancel(NOTIFICATION_ID);

		// stop the tracker
		tracker.stop();

		// Tell the user we stopped.
		stopService(new Intent(BoskoiService.this, BoskoiService.class));

	}

	public static void AddThreadToQueue(Thread tr) {
		queue.AddQueueItem(tr);
	}

	private void showNotification(String tickerText) {
		// This is what should be launched if the user selects our notification.
		Intent baseIntent = new Intent(this, IncidentsTab.class);
		baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				baseIntent, 0);

		// choose the ticker text
		newBoskoiReportNotification = new Notification(R.drawable.favicon,
				tickerText, System.currentTimeMillis());
		newBoskoiReportNotification.contentIntent = contentIntent;
		newBoskoiReportNotification.flags = Notification.FLAG_AUTO_CANCEL;
		newBoskoiReportNotification.defaults = Notification.DEFAULT_ALL;
		newBoskoiReportNotification.setLatestEventInfo(this, TAG, tickerText,
				contentIntent);
		if (ringtone) {
			// set the ringer
			Uri ringURI = Uri.fromFile(new File(
					"/system/media/audio/ringtones/ringer.mp3"));
			newBoskoiReportNotification.sound = ringURI;
		}

		if (vibrate) {
			double vibrateLength = 100 * Math.exp(0.53 * 20);
			long[] vibrate = new long[] { 100, 100, (long) vibrateLength };
			newBoskoiReportNotification.vibrate = vibrate;

			if (flashLed) {
				int color = Color.BLUE;
				newBoskoiReportNotification.ledARGB = color;
			}

			newBoskoiReportNotification.ledOffMS = (int) vibrateLength;
			newBoskoiReportNotification.ledOnMS = (int) vibrateLength;
			newBoskoiReportNotification.flags = newBoskoiReportNotification.flags
					| Notification.FLAG_SHOW_LIGHTS;
		}

		mNotificationManager.notify(NOTIFICATION_ID,
				newBoskoiReportNotification);
	}

	/**
	 * Clear stored data
	 */
	public boolean clearCache() {

		return getDb().clearData();
	}

	public static void clearSettings(Context context) {
		final SharedPreferences settings = context.getSharedPreferences(
				PREFS_NAME, 0);
		Editor e = settings.edit();
		e.clear();
		e.commit();
	}

	public static void loadSettings(Context context) {
		final SharedPreferences settings = context.getSharedPreferences(
				PREFS_NAME, 0);
		savePath = "/data/data/org.boskoi.android/files/";// settings.getString("savePath","/data/data/com.boskoi.android.app/files/");
		// domain = settings.getString("Domain", "");
		firstname = settings.getString("Firstname", "");
		lastname = settings.getString("Lastname", "");
		lastUpdate = settings.getString("LastUpdate", "");
		email = settings.getString("Email", "");
		countries = settings.getInt("Countries", 0);
		AutoUpdateDelay = settings.getInt("AutoUpdateDelay", 5);
		// AutoFetch = settings.getBoolean("AutoFetch", false);
		AutoFetch = true;
		totalReports = settings.getString("TotalReports", "");
		smsUpdate = settings.getBoolean("SmsUpdate", false);
		username = settings.getString("Username", "");
		password = settings.getString("Password", "");
		language = new Locale(settings.getString("Language", "en"), settings.getString("LanguageCountry", "US"));
		lastVersion = settings.getString("LastVersion", "");
		blogLastUpdate = settings.getLong("BlogLastUpdate", 0);

		// make sure folder exists
		final File dir = new File(BoskoiService.savePath);
		dir.mkdirs();
		if (!dir.exists()) {
			// Log.i("SavePath ","does not exist");
			try {
				dir.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void saveSettings(Context context) {
		final SharedPreferences settings = context.getSharedPreferences(
				PREFS_NAME, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.putString("Domain", domain);
		editor.putString("Firstname", firstname);
		editor.putString("Lastname", lastname);
		editor.putString("LastUpdate", lastUpdate);
		editor.putString("Language", language.getLanguage());
		editor.putString("LanguageCountry", language.getCountry());
		editor.putString("LastVersion", lastVersion);

		if (Util.validateEmail(settings.getString("Email", ""))) {
			editor.putString("Email", email);
		}

		editor.putString("savePath", savePath);
		editor.putInt("AutoUpdateDelay", AutoUpdateDelay);
		editor.putBoolean("AutoFetch", AutoFetch);
		editor.putString("TotalReports", totalReports);
		editor.putBoolean("SmsUpdate", smsUpdate);
		editor.putString("Username", username);
		editor.putString("Password", password);
		editor.putLong("BlogLastUpdate", blogLastUpdate);
		editor.commit();
	}

	public static int getCategoriesCount() {
		return BoskoiApplication.mDb.fetchCategoriesCount();
	}

	public static String[] getParentCategoriesString(Context context) {
		String categories[] = new String[getParentCategories().length];
		Locale locale = context.getResources().getConfiguration().locale;

		int i = 0;
		for (CategoriesData cat : getParentCategories(locale)) {
				categories[i] = cat.getCategoryTitle() + " ("
						+ cat.getCategoryTitleLA() + ")";
			i++;
		}
		return categories;
	}

	public static CategoriesData[] getCategoriesFromParentString(int parentId) {

		CategoriesData[] categories = getCategoriesFromParent(parentId);

		return categories;
	}
	
	public static CategoriesData[] getCategoriesFromParentString(int parentId, Locale locale) {

		CategoriesData[] categories = getCategoriesFromParent(parentId, locale);

		return categories;
	}

	public static CategoriesData[] getParentCategories(){
		return getParentCategories(language);
	}
	
	public static CategoriesData[] getParentCategories(Locale locale) {
		Cursor cursor = BoskoiApplication.mDb.fetchParentCategories(locale);
		CategoriesData result[] = new CategoriesData[cursor.getCount()];

		int i = 0;
		if (cursor.moveToFirst()) {
			int titleIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_TITLE);
			int cat_locale = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_LOCALE);
			int titleLA = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_TITLE_LA);
			int idIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_ID);
			int parentId = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_PARENT_ID);
			int color = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_COLOR);
			int desc = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_DESC);

			do {
				CategoriesData cat = new CategoriesData();
				cat.setCategoryId(cursor.getInt(idIndex));
				cat.setCategoryTitle(cursor.getString(titleIndex));
				cat.setCategoryLocale(cursor.getString(cat_locale));
				cat.setCategoryTitleLA(cursor.getString(titleLA));
				cat.setCategoryParentId(cursor.getInt(parentId));
				cat.setCategoryColor(cursor.getString(color));
				cat.setCategoryDescription(cursor.getString(desc));

				result[i] = cat;

				i++;
			} while (cursor.moveToNext());
		}

		cursor.close();
		return result;
	}
	
	public static CategoriesData[] getCategoriesDetails(String categoryIds) {
		return getCategoriesDetails( categoryIds,  language) ;
	}

	public static CategoriesData[] getCategoriesDetails(String categoryIds, Locale locale) {

		String[] categories = categoryIds.split(",");
		CategoriesData result[] = new CategoriesData[categories.length];

		int i = 0;

		for (String categoryId : categories) {
			Cursor cursor = BoskoiApplication.mDb.fetchCategoriesById(Integer
					.parseInt(categoryId), locale);

			if (cursor.moveToFirst()) {
				int titleIndex = cursor
						.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_TITLE);
				int cat_locale = cursor
						.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_LOCALE);
				int titleLA = cursor
						.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_TITLE_LA);
				int idIndex = cursor
						.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_ID);
				int parentId = cursor
						.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_PARENT_ID);
				int color = cursor
						.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_COLOR);
				int desc = cursor
						.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_DESC);

				CategoriesData cat = new CategoriesData();
				cat.setCategoryId(cursor.getInt(idIndex));
				cat.setCategoryTitle(cursor.getString(titleIndex));
				cat.setCategoryLocale(cursor.getString(cat_locale));
				cat.setCategoryTitleLA(cursor.getString(titleLA));
				cat.setCategoryParentId(cursor.getInt(parentId));
				cat.setCategoryColor(cursor.getString(color));
				cat.setCategoryDescription(cursor.getString(desc));

				result[i] = cat;

				i++;
			}

			cursor.close();
		}
		return result;
	}

	public static CategoriesData[] getCategoriesFromParent(int categoryId) {
		return getCategoriesFromParent(categoryId, language);
	}
	
	public static CategoriesData[] getCategoriesFromParent(int categoryId, Locale locale) {
		Cursor cursor = BoskoiApplication.mDb
				.fetchCategoriesFromParent(categoryId, locale);
		CategoriesData result[] = new CategoriesData[cursor.getCount()];

		int i = 0;
		if (cursor.moveToFirst()) {
			int titleIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_TITLE);
			int cat_locale = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_LOCALE);
			int titleLA = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_TITLE_LA);
			int idIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_ID);
			int parentId = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_PARENT_ID);
			int color = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_COLOR);
			int desc = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_DESC);

			do {
				CategoriesData cat = new CategoriesData();
				cat.setCategoryId(cursor.getInt(idIndex));
				cat.setCategoryTitle(cursor.getString(titleIndex));
				cat.setCategoryLocale(cursor.getString(cat_locale));
				cat.setCategoryTitleLA(cursor.getString(titleLA));
				cat.setCategoryParentId(cursor.getInt(parentId));
				cat.setCategoryColor(cursor.getString(color));
				cat.setCategoryDescription(cursor.getString(desc));

				result[i] = cat;

				i++;
			} while (cursor.moveToNext());
		}

		cursor.close();
		return result;
	}

	public class QueueThread {
		protected Vector<Thread> queue;
		protected int itemcount;
		protected String queueName;

		public QueueThread(String name) {
			queue = new Vector<Thread>();
			queueName = name;
			itemcount = 0;
		}

		// Get an item from the vector. Wait if no items available
		public synchronized Thread GetQueueItem() {
			Thread item = null;
			// If no items available, drop into wait() call
			if (itemcount == 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					// Somebody woke me up!
				}
			}
			// Get first item from vector, remove it and decrement item count.
			item = (Thread) queue.firstElement();
			queue.removeElement(item);
			itemcount--;
			// Send it back
			return item;
		}

		// Place an item onto vector. Signal threads that an item is available.
		public synchronized void AddQueueItem(Thread o) {
			itemcount++;
			queue.addElement(o);
			notify();
		}

		// Handy place to put a separate notify call - used during shutdown.
		public synchronized void BumpQueue() {
			notify();
		}
	}

	public static void trackPageView(Context context, String page) {
		if (tracker == null) {
			startTracker(context);
		}

		try {
			tracker.trackPageView(page);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void startTracker(Context context) {
		// Start the tracker in manual dispatch mode...
		// tracker.start("UA-18696504-1", this);
		// ...alternatively, the tracker can be started with a dispatch interval
		// (in seconds).
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start("UA-18696504-1", 300, context);
	}

	private Typeface loadFont(String path) {
		return Typeface.createFromAsset(getAssets(), path);
	}
	
	public static BlogData getBlogData(int id){
		Cursor cursor;
		cursor = BoskoiApplication.mDb.fetchBlogById(id);
		BlogData blogData = null;
	
		if (cursor.moveToFirst()) {
			 int idIndex =
				 cursor.getColumnIndexOrThrow(BoskoiDatabase.BLOG_ID);
			int titleIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_TITLE);
			int dateIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_DATE);
			int descIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_DESCRIPTION);

			int linkIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_LINK);

			do {

				blogData = new BlogData();

				blogData.setId(Util.toInt(cursor.getString(idIndex)));
				blogData.setTitle(cursor.getString(titleIndex));
				blogData.setDescription(cursor.getString(descIndex));
				blogData.setLink(cursor.getString(linkIndex));
				blogData.setDate(cursor.getString(dateIndex));

			} while (cursor.moveToNext());

		}
		cursor.close();

		return blogData;
	}
	
	public static List<BlogData> getSimpleBlogData(){
		Cursor cursor;
		cursor = BoskoiApplication.mDb.fetchAllSimpleBlog();
		List blog = new ArrayList<BlogData>();
		if (cursor.moveToFirst()) {
			 int idIndex =
				 cursor.getColumnIndexOrThrow(BoskoiDatabase.BLOG_ID);
			int titleIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_TITLE);
			int dateIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_DATE);
			int linkIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_LINK);

			do {

				BlogData blogData = new BlogData();

				blogData.setId(Util.toInt(cursor.getString(idIndex)));
				blogData.setTitle(cursor.getString(titleIndex));
				blogData.setLink(cursor.getString(linkIndex));
				blogData.setDate(cursor.getString(dateIndex));
				blog.add(blogData);

			} while (cursor.moveToNext());

		}
		cursor.close();

		return blog;
	}

	public static List<BlogData> getBlogData() {
		Cursor cursor;
		cursor = BoskoiApplication.mDb.fetchAllBlog();
		List blog = new ArrayList<BlogData>();
		if (cursor.moveToFirst()) {
			 int idIndex =
				 cursor.getColumnIndexOrThrow(BoskoiDatabase.BLOG_ID);
			int titleIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_TITLE);
			int dateIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_DATE);
			int descIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_DESCRIPTION);

			int linkIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.BLOG_LINK);

			do {

				BlogData blogData = new BlogData();

				blogData.setId(Util.toInt(cursor.getString(idIndex)));
				blogData.setTitle(cursor.getString(titleIndex));
				blogData.setDescription(cursor.getString(descIndex));
				blogData.setLink(cursor.getString(linkIndex));
				blogData.setDate(cursor.getString(dateIndex));
				blog.add(blogData);

			} while (cursor.moveToNext());

		}
		cursor.close();

		return blog;
	}

	public static void addBlogItems(List<BlogData> blogData) {
		BoskoiApplication.mDb.addBlog(blogData, true);
	}
	
	public static int getColorForIncident(IncidentsData incident){
		try{
			int end = incident.getIncidentCategories().indexOf(',')>-1?incident.getIncidentCategories().indexOf(','):incident.getIncidentCategories().length();
			String category = incident.getIncidentCategories().substring(0, end);
			String color = "#"+getCategoriesDetails(category)[0].getCategoryColor();
			
			return Color.parseColor(color);
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
			return R.drawable.marker_color;
		}catch(NumberFormatException e){
			e.printStackTrace();
			return R.drawable.marker_color;
		}
	}
}
