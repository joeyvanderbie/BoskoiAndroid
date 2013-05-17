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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.boskoi.android.R;
import org.boskoi.android.data.AddIncidentData;
import org.boskoi.android.data.BoskoiDatabase;
import org.boskoi.android.data.CategoriesData;
import org.boskoi.android.net.BoskoiHttpClient;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup; 
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class AddIncident extends MapActivity {
	private static final int HOME = Menu.FIRST+1;
	private static final int LIST_INCIDENT = Menu.FIRST+2;
	private static final int SETTINGS = Menu.FIRST+5;
	private static final int ABOUT = Menu.FIRST+6;
	private static final int GOTOHOME = 0;
	private static final int LIST_INCIDENTS = 2;
	private static final int REQUEST_CODE_SETTINGS = 2;
	private static final int REQUEST_CODE_ABOUT = 3;
	private static final int REQUEST_CODE_IMAGE = 4;
	private static final int REQUEST_CODE_CAMERA = 5;
	private static final int REQUEST_CODE_CATEGORY = 6;
	private static final int VIEW_MAP = 1;
	
	private Geocoder gc;
	private List<Address> foundAddresses;
	
	// date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private int counter = 0;
    private static double longitude = 0;
    private static double latitude = 0;
    private String errorMessage = "";
    private String dateToSubmit = "";
	private boolean error = false;
	//private EditText incidentTitle;
	private EditText incidentLocation;
	private EditText incidentDesc;
	//private TextView incidentDate;
	//private TextView selectedPhoto;
	private TextView selectedCategories;
	private Button btnSend;
//	private Button btnCancel;
	private Button btnAddCategory;
	private Button pickTime;
	private Button pickDate;
	private Button btnPicture;
	private Button addLocation;
	private ImageView imagePreview;
	private HashMap<Integer,Integer> timeDigits;
	private Bundle bundle;
	private Bundle extras;
	private static final int DIALOG_ERROR_NETWORK = 0;
	private static final int DIALOG_ERROR_SAVING = 1;
    private static final int DIALOG_LOADING_CATEGORIES= 2;
    private static final int DIALOG_LOADING_LOCATIONS = 3;
	private static final int DIALOG_CHOOSE_IMAGE_METHOD = 4;
	private static final int DIALOG_MULTIPLE_CATEGORY = 6;
	private static final int TIME_DIALOG_ID = 7;
    private static final int DATE_DIALOG_ID = 8;
	private String filename = "";
	private final static Handler mHandler = new Handler();
	public static Vector<String> vectorCategoriesData = new Vector<String>();
	public static Vector<String> vectorCategories = new Vector<String>();
	private Vector<String> categoriesId = new Vector<String>();
	private HashMap<String, String> categoriesTitle = new HashMap<String, String>();
	private HashMap<String,String> params = new HashMap<String, String>();
	public static SparseBooleanArray checkedCategories;
	
	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private Typeface font;
	
	private MapController mapController;
	private GeoPoint defaultLocation;
	private MapView mapView = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BoskoiService.trackPageView(AddIncident.this,"/AddIncident");
        setContentView(R.layout.add_incident);
        foundAddresses = new ArrayList<Address>();
		gc = new Geocoder(this);

        font = Typeface.createFromAsset(getAssets(), BoskoiService.fontPath);
        
		//load settings
        BoskoiService.loadSettings(AddIncident.this);
        
        bundle = null; 
		extras = getIntent().getExtras();
		if( extras != null ) {
			bundle = extras.getBundle("locations");
		}
		
		if( bundle != null && !bundle.isEmpty() ) {
			//incidentLocation.setText( bundle.getString("location"));
			AddIncident.latitude = bundle.getDouble("latitude");
			AddIncident.longitude = bundle.getDouble("longitude");
		}
        
        initComponents();
    }
	
	
	/**
	 * Initialize UI components
	 */
	private void initComponents(){
		btnPicture = (Button) findViewById(R.id.btnPicture);
		btnAddCategory = (Button) findViewById(R.id.add_category);
		//selectedPhoto = (TextView) findViewById(R.id.lbl_photo);
		selectedCategories = (TextView) findViewById(R.id.lbl_category);
		
		incidentLocation = (EditText) findViewById(R.id.incident_location);
		incidentDesc = (EditText) findViewById(R.id.incident_desc);
		mapView = (MapView) findViewById(R.id.location_map_inline);
		imagePreview = (ImageView) findViewById(R.id.imagepreview);
		mapController = mapView.getController();
		
		placeMarker((int)(AddIncident.latitude*1.0E6), (int)(AddIncident.longitude*1.0E6));
		mapController.setCenter(this.getPoint(AddIncident.latitude, AddIncident.longitude));
		

//		incidentDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//			//@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if( !hasFocus ) {
//					if(TextUtils.isEmpty(incidentDesc.getText())) {
//						//incidentDesc.setError(getString(R.string.empty_report_description));
//					}
//				}
//			}
//			
//		});
		
		btnSend = (Button) findViewById(R.id.incident_add_btn);
		pickDate = (Button) findViewById(R.id.pick_date);
		pickTime = (Button) findViewById(R.id.pick_time);
		addLocation = (Button) findViewById(R.id.location);
		
		//open location map window
		addLocation.setOnClickListener( new View.OnClickListener(){
			public void onClick( View v ) {
				
				Intent intent = new Intent( AddIncident.this,LocationMap.class);
				startActivityForResult(intent,VIEW_MAP);
				setResult( RESULT_OK, intent );
				
			}
		});
		
		
		imagePreview.setOnClickListener( new View.OnClickListener(){
			public void onClick( View v ) {
				String[] imagePath = new String[1];
				imagePath[0] = BoskoiService.savePath+BoskoiService.fileName;
		    	Intent intent = new Intent(AddIncident.this,LargeImageView.class);
				intent.putExtra("ImageName", imagePath);
				startActivity(intent);
				setResult( RESULT_OK, intent );
				
			}
		});
		

		  //show/hide location button/map when selecting a new location
		if(AddIncident.longitude != 0){
			//addLocation.setVisibility(View.GONE);
			addLocation.setText(R.string.change_location);
			mapView.setVisibility(View.VISIBLE);
		}else{
			mapView.setVisibility(View.GONE);
			//addLocation.setVisibility(View.VISIBLE);
			addLocation.setText(R.string.add_location);
		}
		
		TextView cathead = (TextView) findViewById(R.id.lbl_category_dec);
		cathead.setTypeface(font);  
		
		TextView deschead = (TextView) findViewById(R.id.lbl_incidents_dec);
		deschead.setTypeface(font);  
		
		TextView lochead = (TextView) findViewById(R.id.incident_loc);
		lochead.setTypeface(font);
		
		TextView photohead = (TextView) findViewById(R.id.lbl_incidents_photo);
		photohead.setTypeface(font);
		
		TextView datehead = (TextView) findViewById(R.id.lbl_incidents_date);
		datehead.setTypeface(font);
		
		TextView optionalhead = (TextView) findViewById(R.id.optional);
		optionalhead.setTypeface(font);
		
		TextView firsthead = (TextView) findViewById(R.id.lbl_firstname);
		firsthead.setTypeface(font);
		
		TextView lasthead = (TextView) findViewById(R.id.lbl_lastname);
		lasthead.setTypeface(font);
		
		TextView mailhead = (TextView) findViewById(R.id.lbl_email);
		mailhead.setTypeface(font);
		
		btnSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				//Dipo Fix
				error = false;
//				if( TextUtils.isEmpty(incidentTitle.getText())) {
//					errorMessage = getString(R.string.empty_report_title);
//					error = true;
//				}
				
				if( TextUtils.isEmpty(incidentDesc.getText())) {
					errorMessage += getString(R.string.empty_report_description)+"\n";
					error = true;
				}
				
				if( TextUtils.isEmpty(incidentLocation.getText()) || incidentLocation.getText().length() < 3) {
					errorMessage += getString(R.string.empty_report_location)+"\n";
					error = true;
				}
				
				//Dipo Fix
				if(vectorCategories.size() == 0) {
					errorMessage += getString(R.string.empty_report_categories)+"\n";
					error = true;
				}

				if(!TextUtils.isEmpty(firstName.getText()) && firstName.getText().length() < 3){
					errorMessage += getString(R.string.not_enough_first_name)+"\n";
					error = true;
				}
				
				if(!TextUtils.isEmpty(lastName.getText()) && lastName.getText().length() < 3){
					errorMessage += getString(R.string.not_enough_last_name)+"\n";
					error = true;
				}
				
				if(!Util.validateEmail(email.getText().toString()) ){
					errorMessage += getString(R.string.invalid_email_address)+"\n";
					error = true;
				}
				
				if( !error ) {
					if( Util.isConnected(AddIncident.this) ){ 
						if( !postToOnline() ) {
							mHandler.post(mSentIncidentFail);
						}else { 
							mHandler.post(mSentIncidentSuccess);
							clearFields();
							
							//after a successful upload, delete the file
							File f = new File(BoskoiService.savePath + BoskoiService.fileName);
							if(f.exists()){
								f.delete();
							}
						}
					}else {
					final Thread tr = new Thread() {
						@Override
						public void run() {
							try {
								mHandler.post(mSentIncidentOffline);
								
							} finally {
							}
						}
					};
					tr.start();
					}
				
				}else{
					final Toast t = Toast.makeText(AddIncident.this,
							errorMessage.substring(0, errorMessage.length()-1),
							Toast.LENGTH_LONG);
					t.show();
					errorMessage = "";
				}
			 
				
				}
			});
		
		btnPicture.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_CHOOSE_IMAGE_METHOD);
			}
		});
		
		
		btnAddCategory.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent().setClass(AddIncident.this, CategoryList.class);
				// Make it a subactivity so we know when it returns
				startActivityForResult(intent, REQUEST_CODE_CATEGORY);

				counter++;
			}
		});
		
		pickDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
		
        pickTime.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
        
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        c.get(Calendar.AM_PM);
        
        firstName = (EditText) findViewById(R.id.firstname);
        lastName = (EditText) findViewById(R.id.lastname);
        email = (EditText) findViewById(R.id.email);
        
        updateDisplay();
        
	}
	
private void placeMarker( int markerLatitude, int markerLongitude ) {
		
		Drawable marker = getResources().getDrawable( R.drawable.marker);
		 
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				 marker.getIntrinsicHeight());
		//mapView.getController().setZoom(14);

		mapView.setBuiltInZoomControls(true);
		mapView.getOverlays().clear();
		mapView.getOverlays().add(new MapMarker(marker,
				    markerLatitude, markerLongitude));
	}
	
	public GeoPoint getPoint(double lat, double lon) {
	    return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}
	
	private void centerLocation(GeoPoint centerGeoPoint) {
		
		mapController.animateTo(centerGeoPoint);
		
		//initilaize latitude and longitude for them to be passed to the AddIncident Activity.
		this.latitude = centerGeoPoint.getLatitudeE6() / 1.0E6;
		this.longitude = centerGeoPoint.getLongitudeE6() / 1.0E6;
		
		placeMarker(centerGeoPoint.getLatitudeE6(), centerGeoPoint.getLongitudeE6());
	
	}
	
	/**
	 * get the real location name from
	 * the latitude and longitude.
	 */
	private String getLocationFromLatLon( double lat, double lon ) {
		
		try {
			
    		foundAddresses = gc.getFromLocation( lat, lon, 5 );
    		
    		Address address = foundAddresses.get(0);
    		
    		return address.getLocality();
		
    	} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	//fetch categories
	public String[] showCategories() {
		  Cursor cursor = BoskoiApplication.mDb.fetchAllCategories();
		  
		  String categories[] = new String[cursor.getCount()];
	
		  int i = 0;
		  if (cursor.moveToFirst()) {
			  
			  int titleIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_TITLE);
			  
			  int idIndex = cursor.getColumnIndexOrThrow(BoskoiDatabase.CATEGORY_ID);
			  
			  do {
				  categories[i] = cursor.getString(titleIndex);
				  categoriesTitle.put(String.valueOf(cursor.getInt(idIndex)), 
						  cursor.getString(titleIndex));
				  categoriesId.add(String.valueOf(cursor.getInt(idIndex)));
				  i++;
			  }while( cursor.moveToNext() );
		  }
		 
		  cursor.close();
		  return categories;
		  
	}
	
	
	//reset records in the field
	private void clearFields() {
		vectorCategoriesData = new Vector<String>();
		vectorCategories = new Vector<String>();
		checkedCategories = new SparseBooleanArray();
		
		btnPicture = (Button) findViewById(R.id.btnPicture);
		btnAddCategory = (Button) findViewById(R.id.add_category);
		//incidentTitle.setText("");
		incidentLocation.setText("");
		incidentDesc.setText("");
		vectorCategories.clear();
		//selectedPhoto.setText("");
		selectedCategories.setText(" ");
		counter = 0;
		updateDisplay();
		
		//clear persistent data
		 SharedPreferences.Editor editor = getPreferences(0).edit();
	     editor.putString("title", "");
	     editor.putString("desc", "");
	     editor.putString("date", "");
	     editor.commit();
		
	}
	
	public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri,
                        proj, // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// The preferences returned if the request code is what we had given
		// earlier in startSubActivity
		switch(requestCode){
			case REQUEST_CODE_CAMERA:
				//selectedPhoto.setText(BoskoiService.fileName);
				if(BoskoiService.fileName != ""){
					Util.showToast(AddIncident.this, R.string.toast_photos);
					
					//imagePreview.setMaxWidth(50);
					//imagePreview.setMaxHeight(50);
					imagePreview.setImageURI(Uri.parse(BoskoiService.savePath + BoskoiService.fileName));				
				}
				break;
	
			case REQUEST_CODE_IMAGE:
				if(resultCode != RESULT_OK){
					return;
				}
				Uri uri = data.getData();
				Bitmap b = null;
				try {
		            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
	                bitmapOptions.inSampleSize = 2;  
	                b = BitmapFactory.decodeFile(getRealPathFromURI(uri), bitmapOptions);
	                
					//b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
				} catch (Exception e) {
					break;
				}
				ByteArrayOutputStream byteArrayos = new ByteArrayOutputStream();
				try {
					b.compress(CompressFormat.JPEG, 75, byteArrayos);				
					byteArrayos.flush();
				} catch (OutOfMemoryError e){
					break;
				} catch (IOException e) {
					break;
				}
				filename = "android_pic_upload" + randomString() + ".jpg";
				ImageManager.writeImage(byteArrayos.toByteArray(), filename);
				BoskoiService.fileName = filename;
				//selectedPhoto.setText(BoskoiService.fileName);
				
				if(BoskoiService.fileName != ""){
					Util.showToast(AddIncident.this, R.string.toast_photos);
					//imagePreview.setMaxWidth(50);
					//imagePreview.setMaxHeight(50);
					imagePreview.setImageURI(Uri.parse(BoskoiService.savePath + BoskoiService.fileName));
				}
				break;
				
			case VIEW_MAP:
				if(resultCode != RESULT_OK){
					return;
				}
				
				bundle = null;
				extras = data.getExtras();
				if( extras != null ) bundle = extras.getBundle("locations");
				
				if( bundle != null && !bundle.isEmpty() ) {
					//incidentLocation.setText( bundle.getString("location"));
				
					AddIncident.latitude = bundle.getDouble("latitude");
					AddIncident.longitude = bundle.getDouble("longitude");

				}
				break;
			case REQUEST_CODE_CATEGORY:
				//nothing right now
				break;
		}
	}
	
	private static Random random = new Random();

	protected static String randomString() {
		return Long.toString(random.nextLong(), 10);
	}
	
	//
	final Runnable mSentIncidentOffline = new Runnable() {
		public void run() {
			if( addToDb() == -1 ) {
				mHandler.post(mSentIncidentFail);
			}else { 
				mHandler.post(mSentIncidentOfflineSuccess);
				//clearFields();
			}
		}
	};
	
	final Runnable mSentIncidentFail = new Runnable() {
		public void run() {
			Util.showToast(AddIncident.this, R.string.failed_to_add_report_online);
		}
	};
	
	final Runnable mSentIncidentOfflineFail = new Runnable() {
		public void run() {
			Util.showToast(AddIncident.this, R.string.failed_to_add_report_offline);
		}
	};
	
	final Runnable mSentIncidentOfflineSuccess = new Runnable() {
		public void run() {
			Util.showToast(AddIncident.this, R.string.report_successfully_added_offline);
	
		}
	};
	
	//
	final Runnable mSendIncidentOnline = new Runnable() {
		public void run() {
			if( !postToOnline() ) {
				mHandler.post(mSentIncidentFail);
			}else { 
				mHandler.post(mSentIncidentSuccess);
				
			}
		}
	};
	
	//
	final Runnable mSentIncidentSuccess = new Runnable() {
		public void run() {
			Util.showToast(AddIncident.this, R.string.report_successfully_added_online);
			//success and dismiss
			finish();
		}
	};

	/**
	 * Create various dialog
	 */
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ERROR_NETWORK: {
                AlertDialog dialog = (new AlertDialog.Builder(this)).create();
                dialog.setTitle("Network error!");
                dialog.setMessage("Network error, please ensure you are connected to the internet");
                dialog.setButton2("Ok", new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();						
					}
        		});
                dialog.setCancelable(false);
                return dialog;
            }
            case DIALOG_ERROR_SAVING:{
           	 	AlertDialog dialog = (new AlertDialog.Builder(this)).create();
                dialog.setTitle("File System error!");
                dialog.setMessage("File System error, please ensure your save path is correct!");
                dialog.setButton2("Ok", new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();						
					}
        		});
                dialog.setCancelable(false);
                return dialog;
           }
            case DIALOG_LOADING_CATEGORIES: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Loading Categories");
                dialog.setMessage("Please wait while categories are loaded...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                return dialog;
            }
            
            case DIALOG_LOADING_LOCATIONS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Loading Categories");
                dialog.setMessage("Please wait while categories are loaded...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                return dialog;
            }
            case DIALOG_CHOOSE_IMAGE_METHOD:{
            	BoskoiService.trackPageView(AddIncident.this,"/AddIncident/ChooseImage");
            	AlertDialog dialog = (new AlertDialog.Builder(this)).create();
                dialog.setTitle(R.string.choose_method);
                dialog.setMessage(getText(R.string.choose_pictures_method));
                dialog.setButton(getText(R.string.btn_gallery), new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						BoskoiService.trackPageView(AddIncident.this,"/AddIncident/ChooseImage/Gallery");
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_PICK);
						intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(intent, REQUEST_CODE_IMAGE);
						dialog.dismiss();
					}
                });
                dialog.setButton2(getText(R.string.btn_cancel), new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
                });
                
                /**
                 * Disabling camera functionality for now. will be re implemented in the next release.
                 */ dialog.setButton3(getText(R.string.btn_camera), new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();				
						Intent launchPreferencesIntent = new Intent().setClass(AddIncident.this, CameraView.class);
						// Make it a subactivity so we know when it returns
						startActivityForResult(launchPreferencesIntent, REQUEST_CODE_CAMERA);
					}
        		});
                
                dialog.setCancelable(false);
                return dialog;
            	
            }
            
            case DIALOG_MULTIPLE_CATEGORY: {		
            	BoskoiService.trackPageView(AddIncident.this,"/AddIncident/ChooseCategories");
            	return new AlertDialog.Builder(this)
                .setTitle(R.string.add_categories)
                .setMultiChoiceItems(showCategories(),
                        null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton,
                                    boolean isChecked) {
                            	
                            	if( isChecked ) {
                            		 
                            		vectorCategories.add(categoriesId.get( whichButton ));
                            		if( !vectorCategories.isEmpty()){
                            			for(int i=0; i< vectorCategories.size(); i++){
                            				if(i==0){
	                            				selectedCategories.setText(Util.limitString(
	                                					categoriesTitle.get(vectorCategories.get(i)), 15));
	                            			}else{
	                            				selectedCategories.setText(selectedCategories.getText()+","+Util.limitString(
	                            					categoriesTitle.get(vectorCategories.get(i)), 15));
	                            			}
                            			}
                            		}
                            		error = false;
                            	} else {
                            		//fixed a crash here.
                            		vectorCategories.remove(categoriesId.get( whichButton ));
                            		
                            		if( vectorCategories.isEmpty()){
                            			selectedCategories.setText("");
                            		} else {
                            			for(int i=0; i< vectorCategories.size(); i++){
	                            			if(i==0){
	                            				selectedCategories.setText(Util.limitString(
	                                					categoriesTitle.get(vectorCategories.get(i)), 15));
	                            			}else{
	                            				selectedCategories.setText(selectedCategories.getText()+","+Util.limitString(
	                            					categoriesTitle.get(vectorCategories.get(i)), 15));
	                            			}
                            			}
                            		}
                            	}
                            	
                                /* User clicked on a check box do some stuff */
                            }
                        })
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                        /* User clicked Yes so do some stuff */
                    }
                })
               .create();
            }
            
            case TIME_DIALOG_ID:
                BoskoiService.trackPageView(AddIncident.this,"/AddIncident/TimePicker");
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, false);
                
            case DATE_DIALOG_ID:
                BoskoiService.trackPageView(AddIncident.this,"/AddIncident/DatePicker");
                return new DatePickerDialog(this,
                            mDateSetListener,
                            mYear, mMonth, mDay);
        }
        return null;
    }
	

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(mHour, mMinute);
                break;
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }    

    private void updateDisplay() {
    	String amPm;
    	timeDigits = new HashMap<Integer,Integer>();
        
        timeDigits.put(00, 12);
        timeDigits.put(13, 1);
        timeDigits.put(14, 2);
        timeDigits.put(15, 3);
        timeDigits.put(16, 4);
        timeDigits.put(17, 5);
        timeDigits.put(18, 6);
        timeDigits.put(19, 7);
        timeDigits.put(20, 8);
        timeDigits.put(21, 9);
        timeDigits.put(22, 10);
        timeDigits.put(23, 11);
        timeDigits.put(24, 12);
        timeDigits.put(12, 12);
        timeDigits.put(1, 1);
        timeDigits.put(2, 2);
        timeDigits.put(3, 3);
        timeDigits.put(4, 4);
        timeDigits.put(5, 5);
        timeDigits.put(6, 6);
        timeDigits.put(7, 7);
        timeDigits.put(8, 8);
        timeDigits.put(9, 9);
        timeDigits.put(10, 10);
        timeDigits.put(11, 11);
        timeDigits.put(12, 12);
    	if( mHour >=12 )
    		amPm = "PM";
    	else
    		amPm = "AM";
    	
    	String strDate = new StringBuilder()
        
    	// Month is 0 based so add 1
    	.append(mYear).append("-")
    	.append(pad(mMonth + 1)).append("-")
        .append(pad(mDay)).toString();
    	
    	String dateTime = Util.formatDate("yyyy-MM-dd",strDate,"MMMM dd, yyyy");
    	
    	//incidentDate.setText( dateTime + " at "+pad(timeDigits.get(mHour))+":"+pad(mMinute) +" "+amPm);
    	pickDate.setText( dateTime);
    	pickTime.setText(" at "+pad(timeDigits.get(mHour))+":"+pad(mMinute) +" "+amPm);
    	
    	dateToSubmit =  new StringBuilder()
        // Month is 0 based so add 1
        .append(pad(mMonth + 1)).append("/")
        .append(pad(mDay)).append("/")
        .append(mYear).append(" ")
        .append(pad(timeDigits.get(mHour))).append(":")
        .append(pad(mMinute)).append(" ")
		.append(amPm).toString();
    	
        firstName.setText(BoskoiService.firstname);
        lastName.setText(BoskoiService.lastname);
        email.setText(BoskoiService.email);
        
        updateSelectedCategoryTextView();
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;
                    
                    updateDisplay();
                }
            };

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    
    /**
     * Insert incident data into db when app is offline.
     * @author henryaddo
     *
     */
    public long addToDb() {
    	String dates[] = dateToSubmit.split(" ");
    	String time[] = dates[1].split(":");
    	
    	List<AddIncidentData> addIncidentsData = new ArrayList<AddIncidentData>();
    	AddIncidentData addIncidentData = new AddIncidentData();
    	addIncidentsData.add(addIncidentData);
    	
    	//addIncidentData.setIncidentTitle(incidentTitle.getText().toString());
    	addIncidentData.setIncidentDesc(incidentDesc.getText().toString());
    	addIncidentData.setIncidentDate(dates[0]);
    	addIncidentData.setIncidentHour(Integer.parseInt(time[0]));
    	addIncidentData.setIncidentMinute(Integer.parseInt(time[1]));
    	addIncidentData.setIncidentAmPm(dates[2]);
    	addIncidentData.setIncidentCategories(Util.implode(vectorCategories));
    	addIncidentData.setIncidentLocName(incidentLocation.getText().toString());
    	addIncidentData.setIncidentLocLatitude(String.valueOf(latitude));
    	addIncidentData.setIncidentLocLongitude(String.valueOf(longitude));
    	addIncidentData.setIncidentPhoto(BoskoiService.fileName);
    	addIncidentData.setPersonFirst(firstName.getText().toString());
    	addIncidentData.setPersonLast(lastName.getText().toString());
    	addIncidentData.setPersonEmail(email.getText().toString());
    	
    	//add it to database.
    	return BoskoiApplication.mDb.addIncidents(addIncidentsData);
    }
    
    /**
     * Post directly to online.
     * @author henryaddo
     *
     */
    public boolean postToOnline() {
    	
    	//String dates[] = incidentDate.getText().toString().split(" ");
    	String dates[] = dateToSubmit.split(" ");
    	String time[] = dates[1].split(":");
    	String categories = Util.implode(vectorCategories);
    	
    	StringBuilder urlBuilder = new StringBuilder(BoskoiService.domain);
    	urlBuilder.append("/api");
    	params.put("task","report");
    	
		params.put("incident_title",selectedCategories.getText().toString());
    	Log.i("Categories selected", selectedCategories.getText().toString());
    	
    	params.put("incident_description", incidentDesc.getText().toString()); 
		params.put("incident_date", dates[0]); 
		params.put("incident_hour", time[0]); 
		params.put("incident_minute", time[1]);
		params.put("incident_ampm", dates[2].toLowerCase());
		params.put("incident_category", categories);
		params.put("latitude", String.valueOf(latitude));
		params.put("longitude", String.valueOf(longitude)); 
		params.put("location_name", incidentLocation.getText().toString());
		//params.put("location_name", "Not Specified");
		params.put("person_first", firstName.getText().toString());
		params.put("person_last", lastName.getText().toString());
		params.put("person_email", email.getText().toString());
		params.put("filename", BoskoiService.fileName);
		
		BoskoiService.firstname = firstName.getText().toString();
		BoskoiService.lastname = lastName.getText().toString();
		BoskoiService.email = email.getText().toString();
        BoskoiService.saveSettings(this);
		try {
			return BoskoiHttpClient.PostFileUpload(urlBuilder.toString(), params);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
    }
	
	 /**
     * Upon being resumed we can retrieve the current state.  This allows us
     * to update the state if it was changed at any time while paused.
     */
    @Override
    protected void onResume() {
        super.onResume();
        BoskoiService.trackPageView(AddIncident.this,"/AddIncident");
        SharedPreferences prefs = getPreferences(0); 
        
        //show/hide location button/map when selecting a new location
		if(AddIncident.longitude != 0){
			//addLocation.setVisibility(View.GONE);
			addLocation.setText(R.string.change_location);
			mapView.setVisibility(View.VISIBLE);
		}else{
			mapView.setVisibility(View.GONE);
			//addLocation.setVisibility(View.VISIBLE);
			addLocation.setText(R.string.add_location);
		}
		//show and center a marker on the minimap
		placeMarker((int)(AddIncident.latitude*1.0E6), (int)(AddIncident.longitude*1.0E6));
		mapController.setCenter(this.getPoint(AddIncident.latitude, AddIncident.longitude));

        
       if(prefs.getString("desc", null)!=null){
            incidentDesc.setText(prefs.getString("desc", null), TextView.BufferType.EDITABLE);
    	}
       this.updateDisplay();
        
    }

    /**
     * Any time we are paused we need to save away the current state, so it
     * will be restored correctly when we are resumed.
     */
    @Override
    protected void onPause() {
    	super.onPause();

        SharedPreferences.Editor editor = getPreferences(0).edit();
         editor.putString("desc", incidentDesc.getText().toString());
        if(!firstName.getText().toString().equals(BoskoiService.firstname)){
    		BoskoiService.firstname = firstName.getText().toString();

        }
        if(!lastName.getText().toString().equals(BoskoiService.lastname)){
     		BoskoiService.lastname = lastName.getText().toString();

        }
        if(!email.getText().toString().equals(BoskoiService.email)){
    		BoskoiService.email = email.getText().toString();

        }
        BoskoiService.saveSettings(this);
         
         editor.commit();
    }
    
    /**
     * Geocode user entered location name.
     * 
     * @param String - the location to be geocoded
     * 
     * @return int - 0 on success, 1 network failure, 2 couldn't geocode wrong location name
     */
    public int geocodeLocationName( String locationName ) {
    	if(Util.isConnected(AddIncident.this)) {
    		try {
				foundAddresses = gc.getFromLocationName(locationName,5);
				Address address = foundAddresses.get(0);
				AddIncident.latitude = address.getLatitude();
				AddIncident.longitude = address.getLongitude();
				
			} catch (IOException e) {
				return 2;
			}
    	} 
    	return 1;
    }
    
    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. 
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     *
     */
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set.  children[i] contains the children (String[]) for groups[i].
        private String[] groups = { "Plants", "Threes" };
        private String[][] children = {
                { "Potato", "Tomato" },
                { "Apple", "Mango" }
        };
        
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);

            TextView textView = new TextView(AddIncident.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            
        	TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());

            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

		public String[] getGroups() {
			return groups;
		}

		public void setGroups(String[] groups) {
			this.groups = groups;
		}

		public String[][] getChildren() {
			return children;
		}

		public void setChildren(String[][] children) {
			this.children = children;
		}
		

    }
    
    private void updateSelectedCategoryTextView(){
    	selectedCategories.setText(" ");
        String categoriesName = "";
        if( !vectorCategoriesData.isEmpty()){
			for(int i=0; i< vectorCategoriesData.size(); i++){
				if(i==0){
    				categoriesName = vectorCategoriesData.get(i);
    			}else{
    				categoriesName +=","+vectorCategoriesData.get(i);
    			}
			}
		}
        selectedCategories.setText(categoriesName);
    }


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;

	}
	
private class MapMarker extends ItemizedOverlay<OverlayItem> {
		
		private List<OverlayItem> locations =new ArrayList<OverlayItem>();
		private Drawable marker;
		private OverlayItem myOverlayItem;
		private boolean MoveMap = false;
		private long timer;
		
		public MapMarker( Drawable defaultMarker, int LatitudeE6, int LongitudeE6 ) {
			super(defaultMarker);
			this.timer = 0;
			this.marker = defaultMarker;
			
			// create locations of interest
			GeoPoint myPlace = new GeoPoint(LatitudeE6 ,LongitudeE6);
			
			myOverlayItem = new OverlayItem(myPlace, " ", " ");
			
			locations.add(myOverlayItem);
			   
			populate();
			   
		}
		@Override
		protected OverlayItem createItem(int i) {
			return locations.get(i);
		}

		@Override
		public int size() {
			return locations.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView,
				boolean shadow) {
			super.draw(canvas, mapView, shadow);   
			boundCenterBottom(marker);
		}

		@Override
		public boolean onTouchEvent(MotionEvent motionEvent, MapView mapview) {
			
			int Action = motionEvent.getAction();
			
			if (Action == MotionEvent.ACTION_UP){
				if(!MoveMap) {
					Projection proj = mapView.getProjection();
					GeoPoint loc = proj.fromPixels((int)motionEvent.getX(), (int)motionEvent.getY());
					
					//remove the last marker
					mapView.getOverlays().remove(0);
					centerLocation(loc);
				}
		    
		   }
		   else if (Action == MotionEvent.ACTION_DOWN) {
			   MoveMap = false;
		   }
		   else if (Action == MotionEvent.ACTION_MOVE){
			    MoveMap = true;

		   }

		   return super.onTouchEvent(motionEvent, mapview);
		}
	}
	
	
	
}



