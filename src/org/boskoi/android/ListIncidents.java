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
 
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.boskoi.android.R;
import org.boskoi.android.data.BoskoiDatabase;
import org.boskoi.android.data.CategoriesData;
import org.boskoi.android.data.IncidentsData;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
 
public class ListIncidents extends Activity
{
  
	/** Called when the activity is first created. */
	private ListView listIncidents = null;
	private ListIncidentAdapter ila = new ListIncidentAdapter( this );

	private static final int INCIDENT_REFRESH= Menu.FIRST+4;
	private static final int INCIDENT_LANG= Menu.FIRST+5;
	private static final int REQUEST_CODE_CATEGORY = 6;

	private Bundle incidentsBundle = new Bundle();
	private final Handler mHandler = new Handler();
	public static BoskoiDatabase mDb;
	public static String selectedCategory = "";
	
	private Button viewMap;
	private Button back;
    private TextView title;
    private TextView body;
    private TextView date;
    private TextView location;
    private TextView category;
    private TextView status;
    private TextView wikilink;
	private Button btnFilterCategory;

    private String media;
    private String thumbnails [];
    private int id;
    private String reportLatitude;
    private String reportLongitude; 
    private String reportTitle;
    private String reportDescription;
  
	private List<IncidentsData> mOldIncidents;

  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BoskoiService.trackPageView(ListIncidents.this,"/ListIncidents");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView( R.layout.list_incidents );
		BoskoiService.loadSettings(ListIncidents.this);
		
	
		btnFilterCategory = (Button) findViewById(R.id.filter_category);
		
		btnFilterCategory.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				//Clear any old data in the list (this is because it is not done here when selecting the all category again).
				ila.removeItems();
				mOldIncidents.clear();
				ila.notifyDataSetChanged();
				
				Intent intent = new Intent().setClass(ListIncidents.this, SimpleCategoryList.class);
				// Make it a subactivity so we know when it returns
				startActivityForResult(intent, REQUEST_CODE_CATEGORY);
			}
		});
		
       
		listIncidents = (ListView) findViewById( R.id.view_incidents );
        
		mOldIncidents = new ArrayList<IncidentsData>();
		listIncidents.setOnScrollListener(new EndlessScrollListener(this));
		listIncidents.setAdapter( ila );

		listIncidents.setOnItemClickListener( new OnItemClickListener(){  
      
			public void onItemClick(AdapterView<?> arg0, View view, int position,
        		  long id) {
				
				incidentsBundle.putInt("id", mOldIncidents.get(position).getIncidentId());
				incidentsBundle.putString("title",mOldIncidents.get(position).getIncidentTitle());
				incidentsBundle.putString("desc", mOldIncidents.get(position).getIncidentDesc());
				incidentsBundle.putString("longitude",mOldIncidents.get(position).getIncidentLocLongitude());
				incidentsBundle.putString("latitude",mOldIncidents.get(position).getIncidentLocLatitude());
				incidentsBundle.putString("category", mOldIncidents.get(position).getIncidentCategories());
				incidentsBundle.putString("location", mOldIncidents.get(position).getIncidentLocation());
				incidentsBundle.putString("date", mOldIncidents.get(position).getIncidentDate());
				incidentsBundle.putString("media", mOldIncidents.get(position).getIncidentMedia());
				incidentsBundle.putString("status", ""+mOldIncidents.get(position).getIncidentVerified());
          
				showDialogScreen(mOldIncidents.get(position));

			}
          
		});
		
		
		
		mHandler.post(mDisplayIncidents);
		
	}
	

  
	public void showDialogScreen(IncidentsData inci){
		BoskoiService.trackPageView(ListIncidents.this,"/IncidentDetails");
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.view_incidents);
		dialog.setTitle("Report Details");

		viewMap = (Button) dialog.findViewById(R.id.view_map);
        
        id = inci.getIncidentId();
        reportTitle = inci.getIncidentTitle();
        reportDescription = inci.getIncidentDesc();
        reportLatitude = inci.getIncidentLocLatitude();
        reportLongitude = inci.getIncidentLocLongitude();
        String iStatus = inci.getIncidentVerified()  == 0 ? "Unverified" : "Verified";
        title = (TextView) dialog.findViewById(R.id.title);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor(getText(R.string.title_string).toString()));//Color.rgb(144, 80, 62));

        //get full category names from db based on the id's found in the incident
        Locale locale = this.getBaseContext().getResources().getConfiguration().locale;
        
        CategoriesData[] categories = BoskoiService.getCategoriesDetails(inci.getIncidentCategories(), locale);
        String categ = "";
        for (CategoriesData cate : categories){
        	categ = categ + cate.getCategoryTitle() + " (" + cate.getCategoryTitleLA()+"), ";      		
        }
        title.setText(categ);
        
        
        //display wikipedia link. If latin is not available we put english string in
        String wiki = "";
        for (CategoriesData cate : categories){
        	if(!cate.getCategoryTitle().equals("Not available")){
	        	if(!cate.getCategoryTitleLA().equals("")){
	        		if(locale.equals("Nederlands") || BoskoiService.language.equals("Nederlands")){
	        			wiki = wiki + "<a href=http://nl.wikipedia.org/w/index.php?title=Special%3ASearch&search="+cate.getCategoryTitleLA().replace(" ", "%20")+">" + cate.getCategoryTitle() +" on Wikipedia</a><br>" ;      		
	        		}else{
	        			wiki = wiki + "<a href=http://en.wikipedia.org/w/index.php?title=Special%3ASearch&search="+cate.getCategoryTitleLA().replace(" ", "%20")+">" + cate.getCategoryTitle() +" on Wikipedia</a><br>" ;      		      			
	        		}	        	}else{
	        		wiki = wiki + "<a href=http://en.wikipedia.org/w/index.php?title=Special%3ASearch&search="+cate.getCategoryTitle().replace(" ", "%20")+">" + cate.getCategoryTitle() +" on Wikipedia</a><br>" ;   
	        	}
        	}
        }
    
        wikilink = (TextView) dialog.findViewById(R.id.wikilink);
        wikilink.setText(Html.fromHtml(wiki));
        wikilink.setMovementMethod(LinkMovementMethod.getInstance());
        
        date = (TextView) dialog.findViewById(R.id.date);
        date.setTextColor(Color.BLACK);
        date.setText( Util.joinString("Date: ",inci.getIncidentDate()));
        
        
        location = (TextView) dialog.findViewById(R.id.location);
        location.setTextColor(Color.BLACK);
        location.setText(Util.joinString("Location: ", inci.getIncidentLocation()));
       
        body = (TextView) dialog.findViewById(R.id.webview);
        body.setTextColor(Color.BLACK);
        body.setText(inci.getIncidentDesc());
        
        status = (TextView) dialog.findViewById( R.id.status);
        
		//change colored to red if text is not Verified
		if(iStatus.equals("Verified")) {
			status.setTextColor(Color.parseColor(getText(R.string.verified_string).toString()));//Color.rgb(41, 142, 40));
		} else {
			status.setTextColor(Color.parseColor(getText(R.string.notverified_string).toString()));//Color.rgb(237, 0, 0));
		}
        status.setText(iStatus);
    	
    	media = inci.getIncidentMedia();
    	
    	ImageAdapter imageAdapter = new ImageAdapter(this);
    	
    	if( !media.equals("")) {
    		
    		thumbnails = media.split(",");    	
    
        	for( int i = 0; i < thumbnails.length; i++ ) {
        		imageAdapter.mImageIds.add( ImageManager.getImages( thumbnails[i] ) );
        		
        	}
    	}
        
        Gallery g = (Gallery) dialog.findViewById(R.id.gallery);
        
        g.setAdapter( imageAdapter );
        
        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
            	Intent intent = new Intent(ListIncidents.this,LargeImageView.class);
				intent.putExtra("ImageName", thumbnails);
	
				startActivityForResult(intent,1);
				setResult(RESULT_OK );
            }
        });
        
		incidentsBundle.putInt("id", id);
		reportTitle = inci.getIncidentTitle();
        reportDescription = inci.getIncidentDesc();
        reportLatitude = inci.getIncidentLocLatitude();
        reportLongitude = inci.getIncidentLocLongitude();
        
		incidentsBundle.putString("title",reportTitle);
		incidentsBundle.putString("desc", reportDescription);
		incidentsBundle.putString("longitude",reportLongitude);
		incidentsBundle.putString("latitude",reportLatitude);
        
        viewMap.setOnClickListener( new View.OnClickListener() {  
            
        	public void onClick( View v ) {
        		BoskoiService.trackPageView(ListIncidents.this,"/ListIncidents/IncidentDetails/ShowOnMap");
				Bundle tab = new Bundle();
				tab.putInt("tab_index", 0);
				
				Intent intent = new Intent( ListIncidents.this,IncidentsTab.class);
				intent.putExtra("report", incidentsBundle);
				intent.putExtra("tab", tab);
				startActivityForResult(intent,1);
				setResult( RESULT_OK, intent );
				
				dialog.dismiss();
              
			}
		});
        
        back = (Button) dialog.findViewById(R.id.btn_back);
        back.setOnClickListener( new View.OnClickListener() {  
            
        	public void onClick( View v ) {
        		dialog.dismiss();
        	}
        });

        Button dialogNavigate = (Button) dialog.findViewById(R.id.btn_navigate);
        dialogNavigate.setOnClickListener(new OnClickListener() { 
			public void onClick(View view) {		
				BoskoiService.trackPageView(ListIncidents.this,"/ListIncidents/IncidentDetails/Navigate");
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				Uri.parse("http://maps.google.com/maps?daddr="+reportLatitude+","+reportLongitude));
				startActivity(intent);
			}
		} );
        
		dialog.show();

	}
	
	@Override
	protected void onResume(){
		super.onResume();
		BoskoiService.trackPageView(ListIncidents.this,"/ListIncidents");
		if(!selectedCategory.equals("")){
			showIncidents(selectedCategory, 0);
		}if(selectedCategory.equals("All")){
			showIncidents("All", 0);
			//WORKAROUND CODE:
			//In case users explicitly selects the All category in the filter the scroll listener is giving problems.
			// Here we reinitialize is, which solves the problem.
			listIncidents.setOnScrollListener(new EndlessScrollListener(this));
		}
	}
  
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
  
	final Runnable mDisplayIncidents = new Runnable() {
		public void run() {
			setProgressBarIndeterminateVisibility(true);
			showIncidents("All", 0);
			try{
				setProgressBarIndeterminateVisibility(false);
			} catch(Exception e){
				return;  //means that the dialog is not showing, ignore please!
			}
		}
	};
  


	//menu stuff
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
		populateMenu(menu);
	}
  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);
 
		return(super.onCreateOptionsMenu(menu));
	}
 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//applyMenuChoice(item);
 
		return(applyMenuChoice(item) ||
				super.onOptionsItemSelected(item));
	}
 
	public boolean onContextItemSelected(MenuItem item) {
 
		return(applyMenuChoice(item) ||
        super.onContextItemSelected(item));
	}
  
	private void populateMenu(Menu menu) {
		MenuItem i;

		i = menu.add( Menu.NONE, INCIDENT_REFRESH, Menu.NONE, R.string.incident_menu_refresh );
		i.setIcon(R.drawable.boskoi_refresh);
		i = menu.add( Menu.NONE, INCIDENT_LANG, Menu.NONE, R.string.incident_menu_lang );
		i.setIcon(R.drawable.boskoi_language);
	  
	}
  
	private boolean applyMenuChoice(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {

    		case INCIDENT_REFRESH:
    			ReportsTask reportsTask = new ReportsTask();
	            reportsTask.appContext = this;
	            reportsTask.execute();
    			return(true);
    		case INCIDENT_LANG:
    			if(BoskoiService.language.getCountry().equals(Locale.ENGLISH.getCountry()) && BoskoiService.language.getLanguage().equals(Locale.ENGLISH.getLanguage())){
    				//locale langue is english
    				//switch to dutch
    				BoskoiService.language = new Locale("nl", "NL");
    			}else{
    				//switch back to english
    				BoskoiService.language = new Locale("en", "US");
    			}
    			

				BoskoiService.saveSettings(this.getBaseContext());
                Toast.makeText(this, BoskoiService.language.getDisplayLanguage(), Toast.LENGTH_LONG).show();

                //force refresh
                Intent refreshIntent = new Intent(ListIncidents.this, IncidentsTab.class);
				Bundle tab = new Bundle();
				tab.putInt("tab_index", 1);
				refreshIntent.putExtra("tab", tab);

				ListIncidents.this.startActivityForResult(refreshIntent, 5);
				finish();
    			
    			return(true);	
        
		}
		return(false);
	}
	
	private class ReportsTask extends AsyncTask<Void, Void, Integer> {

		protected Integer status;
		protected Context appContext;
		protected boolean clear = false;
		
		private static final int BOSKOI_ID = 1;
		private NotificationManager mNotificationManager;
		private int icon = R.drawable.icon;
		private String ns = Context.NOTIFICATION_SERVICE;
		
		
		@Override
		protected void onPreExecute() {
			Util.showToast(ListIncidents.this,
					R.string.retrieving_reports);
			setProgressBarIndeterminateVisibility(true);
			
			mNotificationManager = (NotificationManager) getSystemService(ns);
			CharSequence tickerText = getText(R.string.retrieving_reports);
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= notification.FLAG_AUTO_CANCEL;
			Context context = getApplicationContext();
			CharSequence contentTitle = getText(R.string.notification_title);
			CharSequence contentText = getText(R.string.retrieving_reports);
			Intent notificationIntent = new Intent(ListIncidents.this, IncidentsTab.class);
			PendingIntent contentIntent = PendingIntent.getActivity(ListIncidents.this, 0, notificationIntent, 0);

			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
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
				Toast.makeText(ListIncidents.this,
						"Found " + Integer.toString(BoskoiService.numberOfNewReports) + " new reports", Toast.LENGTH_LONG)
						.show();
				setProgressBarIndeterminateVisibility(false);
				
				mNotificationManager = (NotificationManager) getSystemService(ns);
				String contentText = "Found " + Integer.toString(BoskoiService.numberOfNewReports) + " new reports";
				long when = System.currentTimeMillis();
				Notification notification = new Notification(icon, contentText, when);
				notification.flags |= notification.FLAG_AUTO_CANCEL;
				Context context = getApplicationContext();
				CharSequence contentTitle = getText(R.string.notification_title);
				Intent notificationIntent = new Intent(ListIncidents.this, IncidentsTab.class);
				PendingIntent contentIntent = PendingIntent.getActivity(ListIncidents.this, 0, notificationIntent, 0);

				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				mNotificationManager.notify(BOSKOI_ID, notification);
			}
		}
	} 
	
	// get incidents from the db
	public void showIncidents( String by, int page ) {
		//this showIncidents method is called on run (just first 10 records are fetched), and again during scrolling using the endlessscrolllistener class, 
		//when selecting a category as well but the endlessscrolllistener does not handle this at this point.
		
		BoskoiService.trackPageView(ListIncidents.this,"/ListIncidents/"+by);
		Cursor cursor = null;
		if( by.equals("All")) {
			//this is the "smart" call that requests only 10 records at the time
			cursor = BoskoiApplication.mDb.fetchIncidents(page);
			
		}else{
			//Clear any old data in the list
			ila.removeItems();
			mOldIncidents.clear();
			ila.notifyDataSetChanged();
			//in case a category is selected we just return all we can find
			cursor = BoskoiApplication.mDb.fetchIncidentsByCategories(by);
		


		}
		
			String title;
			String status;
			String date;
			String description;
			String location;
			String categories;
			String media;
	
			String thumbnails [];
			Drawable d = null;
			if (cursor.moveToFirst()) {
				int idIndex = cursor.getColumnIndexOrThrow( 
						BoskoiDatabase.INCIDENT_ID);
				int titleIndex = cursor.getColumnIndexOrThrow(
						BoskoiDatabase.INCIDENT_TITLE);
				int dateIndex = cursor.getColumnIndexOrThrow(
						BoskoiDatabase.INCIDENT_DATE);
				int verifiedIndex = cursor.getColumnIndexOrThrow(
				  	BoskoiDatabase.INCIDENT_VERIFIED);
				int locationIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_LOC_NAME);
		  
				int descIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_DESC);
		  
				int categoryIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_CATEGORIES);
		  
				int mediaIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_MEDIA);
				
				int latitudeIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_LOC_LATITUDE);
				
				int longitudeIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_LOC_LONGITUDE);
				
				
				do {
			  
					IncidentsData incidentData = new IncidentsData();
					mOldIncidents.add( incidentData );
			  
					int id = Util.toInt(cursor.getString(idIndex));
					incidentData.setIncidentId(id);
					incidentData.setIncidentLocLatitude(cursor.getString(latitudeIndex));
					incidentData.setIncidentLocLongitude(cursor.getString(longitudeIndex));
					title = Util.capitalizeString(cursor.getString(titleIndex));
					incidentData.setIncidentTitle(title);
			  
					description = cursor.getString(descIndex);
					incidentData.setIncidentDesc(description);
			  
					categories = cursor.getString(categoryIndex);
					incidentData.setIncidentCategories(categories);
			  
					location = cursor.getString(locationIndex);
					incidentData.setIncidentLocation(location);
					
					//TODO format the date to the appropriate format
					date = Util.formatDate("yyyy-MM-dd hh:mm:ss", cursor.getString(dateIndex), "MMMM dd, yyyy 'at' hh:mm:ss aaa" );
					
					incidentData.setIncidentDate(date);			  
			  
					media = cursor.getString(mediaIndex);
					incidentData.setIncidentMedia(media);
					thumbnails = media.split(",");
			  
					//TODO make the string readable from the string resource
					status = Util.toInt(cursor.getString(verifiedIndex) ) == 0 ? "Unverified" : "Verified";
					incidentData.setIncidentVerified(Util.toInt(cursor.getString(verifiedIndex) ));
			  
					//TODO do a proper check for thumbnails
					d = ImageManager.getImages( thumbnails[0]);
					
			        //get full category names from db based on the id's found in the incident (disregard original title)
			        Locale locale = this.getBaseContext().getResources().getConfiguration().locale;
			        
			        CategoriesData[] catdata = BoskoiService.getCategoriesDetails(incidentData.getIncidentCategories(), locale);
			        String categ = "";
			        int i =0;
			        for (CategoriesData cate : catdata){
			        	if(i > 0){
			        		categ = categ+ ", ";
			        	}
			        	categ = categ + cate.getCategoryTitle();      		
			        	i++;
			        }
			        title = categ;
			        
					ila.addItem( new ListIncidentText( d == null ? getResources().getDrawable( R.drawable.boskoi_report_icon):d, 
							title, date, 
							status,
							description,
							location,
							media,
							categories, 
							id,
							getResources().getDrawable( R.drawable.boskoi_arrow)) );
			  
				} while (cursor.moveToNext());
			}
			
			//remove old list in case no results are found. Inform the user with some toast
			if(cursor.getCount() == 0 && !by.equals("All")){
				ila.removeItems();
				Toast.makeText(this.getBaseContext(), R.string.report_not_found_for_category, Toast.LENGTH_SHORT).show();
			}
			
			cursor.close();

			ila.notifyDataSetChanged();

			//prevent the list from not loading on page 0
			if(page == 0){
				listIncidents.setSelection(0);
			}
			
			//set the filtered by string (convert the by id to string)
			Locale locale = this.getBaseContext().getResources().getConfiguration().locale;
			
			CategoriesData catDetails = new CategoriesData();
			
			if(!by.equals("All") ){
				CategoriesData[] cats = BoskoiService.getCategoriesDetails(by, locale);
				catDetails = cats[0];
			}else{
				catDetails.setCategoryTitle("All");
			}
	
			btnFilterCategory.setText(getString(R.string.incident_filter_category)+" "+catDetails.getCategoryTitle());
			
	}
  
  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	 
    public class ImageAdapter extends BaseAdapter {
    	
    	public Vector<Drawable> mImageIds;
    	private Context mContext;
    	private int mGalleryItemBackground;
    	
    	public ImageAdapter( Context context ){
    		mContext = context;
    		mImageIds = new Vector<Drawable>();
    	//	Log.i("image","adapter");

    		// styling does not work in this context
//    		TypedArray a = obtainStyledAttributes(R.styleable.PhotoGallery);
//            mGalleryItemBackground = a.getResourceId(
//                    R.styleable.PhotoGallery_android_galleryItemBackground, 0);
//            a.recycle();		
    	}
    	
    	public int getCount() {
    		return mImageIds.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);
			i.setImageDrawable( mImageIds.get( position ) );
			
			i.setScaleType(ImageView.ScaleType.FIT_START);
			i.setLayoutParams(new Gallery.LayoutParams(136, 88));
            
            // The preferred Gallery item background
            i.setBackgroundResource(mGalleryItemBackground);

			return i;
		}
		
    }
  
}