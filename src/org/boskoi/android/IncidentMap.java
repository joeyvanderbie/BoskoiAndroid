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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.boskoi.android.data.IncidentsData;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class IncidentMap extends MapActivity {
	public MapView mapView = null;
	private static final int HOME = Menu.FIRST + 1;
	private static final int LIST_INCIDENT = Menu.FIRST + 2;
	private static final int INCIDENT_CACHE = Menu.FIRST + 8;
	private static final int INCIDENT_ADD = Menu.FIRST + 3;
	private static final int INCIDENT_REFRESH = Menu.FIRST + 4;
	private static final int SETTINGS = Menu.FIRST + 5;
	private static final int SATELITE = Menu.FIRST + 7;
	private static final int FIND = Menu.FIRST + 6;

	private static final int GOTOHOME = 0;
	private static final int ADD_INCIDENTS = 1;
	private static final int LIST_INCIDENTS = 2;
	private static final int REQUEST_CODE_SETTINGS = 1;
	private static final int DIALOG_MESSAGE = 0;
	private static double latitude ;
	private static double longitude;
	public static Geocoder gc;
	private List<IncidentsData> mNewIncidents;
	private ReportsTask reportsTask;

	private Handler mHandler;
	private Bundle extras;
	private int id;
	private String reportLatitude;
	private String reportLongitude;
	private String reportTitle;
	private String reportDescription;
	private boolean isPotentialLongPress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.incidents_map);

		BoskoiService.trackPageView(IncidentMap.this, "/IncidentMap");

		BoskoiService.loadSettings(IncidentMap.this);
		mapView = (MapView) findViewById(R.id.map);
		mapView.getController().setZoom(18);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);

		mNewIncidents = new ArrayList<IncidentsData>();

		mNewIncidents = Util.showIncidents("All");
		mHandler = new Handler();

		Bundle incidents = getIntent().getExtras();

		if (incidents != null) {
			extras = incidents.getBundle("report");
			id = extras.getInt("id");
			reportTitle = extras.getString("title");
			reportDescription = extras.getString("desc");
			reportLatitude = extras.getString("latitude");
			reportLongitude = extras.getString("longitude");
		}

		if (mNewIncidents.size() > 0) {
			DateFormat formatter = new SimpleDateFormat(Util.dateFormat);
			try {
				Date dateStr = formatter.parse(BoskoiService.lastUpdate);
				if (System.currentTimeMillis() - dateStr.getTime() > BoskoiService.updateInterval) {
					// time to update
					reportsTask = new ReportsTask();
					reportsTask.appContext = this;
					reportsTask.execute();
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (id > 0) {
				IncidentMap.latitude = Double.parseDouble(reportLatitude);
				IncidentMap.longitude = Double.parseDouble(reportLongitude);
			} else {
				updateLocation();
				IncidentMap.latitude = Double.parseDouble(mNewIncidents.get(0)
						.getIncidentLocLatitude());
				IncidentMap.longitude = Double.parseDouble(mNewIncidents.get(0)
						.getIncidentLocLongitude());

			}

			mapView.getController().setCenter(
					getPoint(IncidentMap.latitude, IncidentMap.longitude));

			mHandler.post(mMarkersOnMap);

		} else {
			Util.showToast(IncidentMap.this, R.string.no_reports_found);
			reportsTask = new ReportsTask();
			reportsTask.appContext = this;
			reportsTask.execute();

		}

		List<Overlay> overlays = mapView.getOverlays();
		overlays.clear();
		overlays.add(new MapGestureDetectorOverlay());

		Util.showToast(IncidentMap.this, R.string.add_tip);

	}

	/**
	 * add marker to the map
	 */
	private void populateMap() {
		Drawable marker = getResources().getDrawable(R.drawable.marker_gray);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());
		mapView.getOverlays().add(new SitesOverlay(marker, mapView));
	}

	// put this stuff in a seperate thread
	final Runnable mMarkersOnMap = new Runnable() {
		public void run() {
			populateMap();
		}
	};

	@Override
	protected boolean isRouteDisplayed() {
		return (false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_I) {
			// Zoom not closer than possible
			mapView.getController().zoomIn();
			// this.myMapController.zoomInFixing(Math.min(21,
			// this.myMapView.getZoomLevel() + 1));
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_O) {
			// Zoom not farer than possible
			mapView.getController().zoomOut();
			// this.myMapController.zoomInFixing(Math.max(1,
			// this.myMapView.getZoomLevel() - 1),0);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_T) {
			// Switch to satellite view
			mapView.setSatellite(true);

			return true;
		} else if (keyCode == KeyEvent.KEYCODE_M) {
			// Switch to satellite view
			mapView.setSatellite(false);

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Restart the receiving, when we are back on line.
	 */
	@Override
	public void onResume() {
		super.onResume();

		BoskoiService.trackPageView(IncidentMap.this, "/IncidentMap");
		if (mNewIncidents.size() == 0) {
			mHandler.post(mMarkersOnMap);
		}
	}

	public void onDestroy() {
		super.onDestroy();

		try {
			reportsTask.mNotificationManager.cancel(ReportsTask.BOSKOI_ID);
		} catch (Exception e) {
			// no mNotificationManageractive
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
	}

	// menu stuff
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		populateMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);

		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// applyMenuChoice(item);

		return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
	}

	public boolean onContextItemSelected(MenuItem item) {

		return (applyMenuChoice(item) || super.onContextItemSelected(item));
	}

	protected Dialog onCreateDialog(int id, String message, String title) {
		switch (id) {
		case DIALOG_MESSAGE: {
			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
			dialog.setTitle(title);
			dialog.setMessage(message);
			dialog.setButton2("Ok", new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dialog.setCancelable(false);
			return dialog;
		}
		}
		return null;
	}

	private void populateMenu(Menu menu) {
		MenuItem i;

		i = menu.add(Menu.NONE, SATELITE, Menu.NONE,
				R.string.incident_menu_satelite);
		i.setIcon(R.drawable.boskoi_map);
//
//		i = menu.add(Menu.NONE, INCIDENT_ADD, Menu.NONE,
//				R.string.incident_menu_add);
//		i.setIcon(R.drawable.boskoi_add);

		i = menu.add(Menu.NONE, INCIDENT_REFRESH, Menu.NONE,
				R.string.incident_menu_refresh);
		i.setIcon(R.drawable.boskoi_refresh);

		i = menu.add(Menu.NONE, FIND, Menu.NONE, "Find me");
		i.setIcon(R.drawable.boskoi_find);

	}

	// thread class
	private class ReportsTask extends AsyncTask<Void, Void, Integer> {

		protected Integer status;
		protected Context appContext;
		protected boolean clear = false;

		private static final int BOSKOI_ID = 1;
		public NotificationManager mNotificationManager;
		private int icon = R.drawable.icon;
		private String ns = Context.NOTIFICATION_SERVICE;

		@Override
		protected void onPreExecute() {
			Util.showToast(IncidentMap.this, R.string.retrieving_reports);
			setProgressBarIndeterminateVisibility(true);

			mNotificationManager = (NotificationManager) getSystemService(ns);
			CharSequence tickerText = getText(R.string.retrieving_reports);// "Boskoi";
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= notification.FLAG_AUTO_CANCEL;
			Context context = getApplicationContext();
			CharSequence contentTitle = getText(R.string.notification_title);
			CharSequence contentText = getText(R.string.retrieving_reports);
			Intent notificationIntent = new Intent(IncidentMap.this,
					IncidentsTab.class);
			PendingIntent contentIntent = PendingIntent.getActivity(
					IncidentMap.this, 0, notificationIntent, 0);

			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			mNotificationManager.notify(BOSKOI_ID, notification);

		}

		@Override
		protected Integer doInBackground(Void... params) {
			status = Util.processReports(appContext, clear);
			return status;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == 4) {

				Util.showToast(appContext, R.string.internet_connection);
			} else if (result == 0) {
				Toast.makeText(
						IncidentMap.this,
						"Found "
								+ Integer
										.toString(BoskoiService.numberOfNewReports)
								+ " new reports", Toast.LENGTH_LONG).show();
				mNewIncidents = Util.showIncidents("All");
				populateMap();
				setProgressBarIndeterminateVisibility(false);

				mNotificationManager = (NotificationManager) getSystemService(ns);
				String contentText = "Found "
						+ Integer.toString(BoskoiService.numberOfNewReports)
						+ " new reports";
				long when = System.currentTimeMillis();
				Notification notification = new Notification(icon, contentText,
						when);
				notification.flags |= notification.FLAG_AUTO_CANCEL;
				Context context = getApplicationContext();
				CharSequence contentTitle = getText(R.string.notification_title);
				Intent notificationIntent = new Intent(IncidentMap.this,
						IncidentsTab.class);
				PendingIntent contentIntent = PendingIntent.getActivity(
						IncidentMap.this, 0, notificationIntent, 0);

				notification.setLatestEventInfo(context, contentTitle,
						contentText, contentIntent);
				mNotificationManager.notify(BOSKOI_ID, notification);
				updateLocation();

			}
		}

	}

	private boolean applyMenuChoice(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {

		case INCIDENT_REFRESH:
			reportsTask = new ReportsTask();
			reportsTask.appContext = this;
			reportsTask.execute();
			return (true);

		case FIND:
			updateLocation();
			return true;

		case SATELITE:
			BoskoiService.trackPageView(IncidentMap.this,
					"/IncidentMap/Satelite/" + !mapView.isSatellite());
			// Switch to satellite view
			mapView.setSatellite(!mapView.isSatellite());
		}
		return false;
	}

	public GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	private class SitesOverlay extends BoskoiItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

		public SitesOverlay(Drawable marker, MapView mapView) {
			super(boundCenterBottom(marker), mapView, IncidentMap.this,
					mNewIncidents, extras);
			mapView.getContext();
			int selected = mNewIncidents.size();
			int size = mNewIncidents.size();
			for (int i = 0; i < size; i++) {
				IncidentsData incidentData = mNewIncidents.get(i);

				IncidentMap.latitude = Double.parseDouble(incidentData
						.getIncidentLocLatitude());
				IncidentMap.longitude = Double.parseDouble(incidentData
						.getIncidentLocLongitude());

//				items.add(new OverlayItem(getPoint(IncidentMap.latitude,
//						IncidentMap.longitude),
//						incidentData.getIncidentTitle(), Util.limitString(
//								incidentData.getIncidentDesc(), 30)))
				
				items.add(new BoskoiOverlayItem(getPoint(IncidentMap.latitude,
						IncidentMap.longitude),
						incidentData.getIncidentTitle(), Util.limitString(
								incidentData.getIncidentDesc(), 30),marker, BoskoiService.getColorForIncident(incidentData)));
				if (incidentData.getIncidentId() == id) {
					selected = i;
				}
			}

			populate();
			if (selected < mNewIncidents.size()) {
				onTap(selected);
			}
		}

		@Override
		protected OverlayItem createItem(int i) {
			return items.get(i);
		}

		@Override
		protected boolean onBalloonTap(int i) {
			return true;
		}

		@Override
		public int size() {
			return (items.size());
		}
	}

	// update the device current location
	private void updateLocation() {
		Util.showToast(IncidentMap.this, R.string.find_location);
		LocationManager manager;
		MyLocationListener listener;

		listener = new MyLocationListener();
		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		long updateTimeMsec = 1000L;

		// DIPO Fix
		List<String> providers = manager.getProviders(true);
		boolean gps_provider = false, network_provider = false;

		for (String name : providers) {
			if (name.equals(LocationManager.GPS_PROVIDER))
				gps_provider = true;
			if (name.equals(LocationManager.NETWORK_PROVIDER))
				network_provider = true;
		}

		// Register for GPS location if enabled or if neither is enabled
		if (gps_provider || (!gps_provider && !network_provider)) {
			manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					updateTimeMsec, 500.0f, listener);
		} else if (network_provider) {
			manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					updateTimeMsec, 500.0f, listener);
		} else {
			Util.showToast(IncidentMap.this, R.string.location_not_found);
		}
	}

	// get the current location of the user
	public class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			double latitude = 0;
			double longitude = 0;

			if (location != null) {
				// Dipo Fix
				// Stop asking for updates when location has been retrieved
				((LocationManager) getSystemService(Context.LOCATION_SERVICE))
						.removeUpdates(this);

				latitude = location.getLatitude();
				longitude = location.getLongitude();

				mapView.getController()
						.animateTo(getPoint(latitude, longitude));
				mapView.getController().setZoom(16);
				Util.showToast(IncidentMap.this, R.string.found_location);

				// re set controls.. appears to be buggy
				mapView.setBuiltInZoomControls(true);

			} else {
				Util.showToast(IncidentMap.this, R.string.location_not_found);
			}
		}

		public void onProviderDisabled(String provider) {

		}

		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

	public class MapGestureDetectorOverlay extends Overlay implements
			OnGestureListener {
		private GestureDetector gestureDetector;
		private OnGestureListener onGestureListener;

		public MapGestureDetectorOverlay() {
			gestureDetector = new GestureDetector(this);
		}

		public MapGestureDetectorOverlay(OnGestureListener onGestureListener) {
			this();
			setOnGestureListener(onGestureListener);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (gestureDetector.onTouchEvent(event)) {
				return true;
			}
			return false;
		}

		public boolean onDown(MotionEvent e) {
			if (onGestureListener != null) {
				return onGestureListener.onDown(e);
			}
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (onGestureListener != null) {
				return onGestureListener.onFling(e1, e2, velocityX, velocityY);
			}
			return false;
		}

		public void onLongPress(MotionEvent e) {
			Log.i("longPress", e.getX() + " " + e.getY() + " ");
			addReportDialog(e);
			if (onGestureListener != null) {
				onGestureListener.onLongPress(e);
			}
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (onGestureListener != null) {
				onGestureListener.onScroll(e1, e2, distanceX, distanceY);
			}
			return false;
		}

		public void onShowPress(MotionEvent e) {
			if (onGestureListener != null) {
				onGestureListener.onShowPress(e);
			}
		}

		public boolean onSingleTapUp(MotionEvent e) {
			if (onGestureListener != null) {
				onGestureListener.onSingleTapUp(e);
			}
			return false;
		}

		public boolean isLongpressEnabled() {
			return gestureDetector.isLongpressEnabled();
		}

		public void setIsLongpressEnabled(boolean isLongpressEnabled) {
			gestureDetector.setIsLongpressEnabled(isLongpressEnabled);
		}

		public OnGestureListener getOnGestureListener() {
			return onGestureListener;
		}

		public void setOnGestureListener(OnGestureListener onGestureListener) {
			this.onGestureListener = onGestureListener;
		}
	}

	public void addReportDialog(final MotionEvent motionEvent) {
		Vibrator vibr = (Vibrator) this.getBaseContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		vibr.vibrate(100);
		vibr.vibrate(100);

		AlertDialog dialog;
		dialog = (new AlertDialog.Builder(IncidentMap.this)).create();
		dialog.setTitle("Add report");
		dialog.setMessage("Do you want to add a report on this location?");
		dialog.setButton("Ok", new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				GeoPoint geoPoint = mapView.getProjection().fromPixels(
						(int) motionEvent.getX(), (int) motionEvent.getY());
				int latitude = geoPoint.getLatitudeE6();
				int longitude = geoPoint.getLongitudeE6();

				Intent intent = new Intent(IncidentMap.this, IncidentsTab.class);

				Bundle bundle = new Bundle();
				bundle.putDouble("latitude", (double) (latitude / 1E6));
				bundle.putDouble("longitude", (double) (longitude / 1E6));
				bundle.putString("location", "");
				intent.putExtra("locations", bundle);

				Bundle tab = new Bundle();
				tab.putInt("tab_index", 2);
				intent.putExtra("tab", tab);

				IncidentMap.this.startActivityForResult(intent, 2);
				IncidentMap.this.setResult(IncidentMap.this.RESULT_OK);
			}
		});
		dialog.setButton2("Cancel", new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.setCancelable(true);

		dialog.show();
	}
}