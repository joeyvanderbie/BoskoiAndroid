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

import java.util.List;
import java.util.Vector;

import org.boskoi.android.R;
import org.boskoi.android.data.CategoriesData;
import org.boskoi.android.data.IncidentsData;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;


public class BalloonOverlayView extends FrameLayout {
	private LinearLayout layout;
	private TextView title;
	private Button readmore;
	private Button zoomTo;
	private Button navigate;
	private TextView snippet;
    private TextView wikilink;
	private Bundle incidentsBundle = new Bundle();
	private static final int VIEW_INCIDENT = 1;
	
	private Button viewMap;
	private Button back;
    private TextView body;
    private TextView date;
    private TextView location;
    private TextView category;
    private TextView status;
    private String media;
    private String thumbnails [];
    private int id;
    private String reportLatitude;
    private String reportLongitude; 
    private String reportTitle;
    private String reportDescription;
    private static final int VIEW_MAP = 1;
	
	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @credits - http://github.com/jgilfelt/android-mapviewballoons/
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 * 
	 * @author Jeff Gilfelt
	 */
	public BalloonOverlayView( final IncidentMap iMap,
			final Context context, 
			final int balloonBottomOffset,
			final List<IncidentsData> mNewIncidents, 
			final int index,
			final Bundle extras) {

		super(context);
		BoskoiService.trackPageView(context,"/BalloonOverlay");
		BoskoiService.loadSettings(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_map_overlay, layout);
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);
		
		readmore = (Button) v.findViewById(R.id.btn_details);
		zoomTo = (Button) v.findViewById(R.id.btn_zoom);
		navigate = (Button) v.findViewById(R.id.btn_navigate);
	
		readmore.setOnClickListener(new OnClickListener() { 
			public void onClick(View view) {
				

					incidentsBundle.putString("title",mNewIncidents.get(index).getIncidentTitle());
					incidentsBundle.putString("desc", mNewIncidents.get(index).getIncidentDesc());
					incidentsBundle.putString("category", mNewIncidents.get(index).getIncidentCategories());
					incidentsBundle.putString("location", mNewIncidents.get(index).getIncidentLocation());
					incidentsBundle.putString("date", mNewIncidents.get(index).getIncidentDate());
					incidentsBundle.putString("media", mNewIncidents.get(index).getIncidentMedia());
					incidentsBundle.putString("status", ""+mNewIncidents.get(index).getIncidentVerified());
					incidentsBundle.putInt("id", mNewIncidents.get(index).getIncidentId());
					incidentsBundle.putString("longitude",mNewIncidents.get(index).getIncidentLocLongitude());
					incidentsBundle.putString("latitude",mNewIncidents.get(index).getIncidentLocLatitude());
					
					showDialogScreen(mNewIncidents.get(index),iMap);
			}
		} );
		
		zoomTo.setOnClickListener(new OnClickListener() { 
			public void onClick(View view) {
				BoskoiService.trackPageView(context,"/BalloonOverlay/Zoom");
				//zoom to the selected marker
				iMap.mapView.getController().setCenter(getPoint(Double.parseDouble(mNewIncidents.get(index).getIncidentLocLatitude()), Double.parseDouble(mNewIncidents.get(index).getIncidentLocLongitude())));
				iMap.mapView.getController().setZoom((iMap.mapView.getMaxZoomLevel() -1));
			}
		} );
		navigate.setOnClickListener(new OnClickListener() { 
			public void onClick(View view) {		
				BoskoiService.trackPageView(context,"/BalloonOverlay/Navigate");
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				Uri.parse("http://maps.google.com/maps?daddr="+mNewIncidents.get(index).getIncidentLocLatitude()+","+mNewIncidents.get(index).getIncidentLocLongitude()));
				iMap.startActivity(intent);
			}
		} );	
		ImageView close = (ImageView) v.findViewById(R.id.close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}

	public void showDialogScreen(IncidentsData inci, final IncidentMap iMap){
		BoskoiService.trackPageView(BalloonOverlayView.this.getContext(),"/BalloonOverlay/IncidentDetails");
		final Dialog dialog = new Dialog(this.getContext());

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
        title.setTextColor(Color.parseColor(getContext().getText(R.string.title_string).toString()));//Color.rgb(144, 80, 62));
        
        //get full category names from db based on the id's found in the incident
        String locale = this.getContext().getResources().getConfiguration().locale.getDisplayLanguage();
        
        CategoriesData[] categories = BoskoiService.getCategoriesDetails(inci.getIncidentCategories());
        String categ = "";
        for (CategoriesData cate : categories){
        	if(locale.equals("Nederlands")){
        		categ = categ + cate.getCategoryTitleNL() + " (" + cate.getCategoryTitleLA()+"), ";
        	}else{
        		categ = categ + cate.getCategoryTitle() + " (" + cate.getCategoryTitleLA()+"), ";      		
        	}
        }
        
        title.setText(categ);
            

//        //display wikipedia link. If latin is not available we put english string in
        String wiki = "";
        for (CategoriesData cate : categories){
        	if(!cate.getCategoryTitle().equals("Not available")){
	        	if(!cate.getCategoryTitleLA().equals("")){
	        		if(locale.equals("Nederlands") || BoskoiService.language.equals("Nederlands")){
	        			wiki = wiki + "<a href=http://nl.wikipedia.org/w/index.php?title=Special%3ASearch&search="+cate.getCategoryTitleLA().replace(" ", "%20")+">" + cate.getCategoryTitle() +" on Wikipedia</a><br>" ;      		
	        		}else{
	        			wiki = wiki + "<a href=http://en.wikipedia.org/w/index.php?title=Special%3ASearch&search="+cate.getCategoryTitleLA().replace(" ", "%20")+">" + cate.getCategoryTitle() +" on Wikipedia</a><br>" ;      		      			
	        		}
	        	}else{
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
			status.setTextColor(Color.parseColor(getContext().getText(R.string.verified_string).toString()));//Color.rgb(41, 142, 40));
		} else {
			status.setTextColor(Color.parseColor(getContext().getText(R.string.notverified_string).toString()));//Color.rgb(237, 0, 0));
		}
        status.setText(iStatus);
    	
    	media = inci.getIncidentMedia();
    		
    	ImageAdapter imageAdapter = new ImageAdapter(this.getContext());
    	
    	if( !media.equals("")) {
    		
    		thumbnails = media.split(",");    	
    
        	for( int i = 0; i < thumbnails.length; i++ ) {
        		Drawable image = ImageManager.getImages( thumbnails[i] );
        		imageAdapter.mImageIds.add( image );
        		
        	}
    	}
        
        Gallery g = (Gallery) dialog.findViewById(R.id.gallery);
        
        g.setAdapter( imageAdapter );
        
        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
            	Intent intent = new Intent(getContext(),LargeImageView.class);
				intent.putExtra("ImageName", thumbnails);
	
				iMap.startActivityForResult(intent,1);
				iMap.setResult(iMap.RESULT_OK );
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
        		BoskoiService.trackPageView(BalloonOverlayView.this.getContext(),"/BalloonOverlay/IncidentDetails/ShowOnMap");
        		
				Bundle tab = new Bundle();
				tab.putInt("tab_index", 0);
				
				Intent intent = new Intent(getContext(),IncidentsTab.class);
				intent.putExtra("report", incidentsBundle);
				intent.putExtra("tab", tab);
				iMap.startActivityForResult(intent,VIEW_INCIDENT);
				iMap.setResult(iMap.RESULT_OK );
				
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
				BoskoiService.trackPageView(BalloonOverlayView.this.getContext(),"/BalloonOverlay/IncidentDetails/Navigate");
				
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				Uri.parse("http://maps.google.com/maps?daddr="+reportLatitude+","+reportLongitude));
				iMap.startActivity(intent);
			}
		} );

		dialog.show();

	}
	
	public GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	public void setData(OverlayItem item) {
		
		layout.setVisibility(VISIBLE);
		if (item.getTitle() != null) {
			title.setVisibility(VISIBLE);
			title.setText(item.getTitle());
		} else {
			title.setVisibility(GONE);
		}
		if (item.getSnippet() != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(item.getSnippet());
		} else {
			snippet.setVisibility(GONE);
		}
		
	}
	
	 public class ImageAdapter extends BaseAdapter {
	    	
	    	public Vector<Drawable> mImageIds;
	    	private Context mContext;
	    	private int mGalleryItemBackground;
	    	
	    	public ImageAdapter( Context context ){
	    		mContext = context;
	    		mImageIds = new Vector<Drawable>();
	    		//Log.i("image","adapter");

	    		// styling does not work in this context
//	    		TypedArray a = obtainStyledAttributes(R.styleable.PhotoGallery);
//	            mGalleryItemBackground = a.getResourceId(
//	                    R.styleable.PhotoGallery_android_galleryItemBackground, 0);
//	            a.recycle();
	    		
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

