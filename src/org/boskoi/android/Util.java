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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.boskoi.android.data.BoskoiDatabase;
import org.boskoi.android.data.CategoriesData;
import org.boskoi.android.data.HandleXml;
import org.boskoi.android.data.IncidentsData;
import org.boskoi.android.net.BoskoiHttpClient;
import org.boskoi.android.net.Categories;
import org.boskoi.android.net.Geocoder;
import org.boskoi.android.net.Incidents;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Util{

	private static NetworkInfo networkInfo;
	private static List<IncidentsData> mNewIncidents;
	private static List<CategoriesData> mNewCategories;
	private static JSONObject jsonObject;
	private static List mOldIncidents = new ArrayList<IncidentsData>();
 
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@" +
			"[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	public static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * joins two strings together
	 * @param first
	 * @param second
	 * @return
	 */
	public static String joinString(String first, String second ) {
		return first.concat(second);
	}
	
	public static String[] joinStringArray(String[] arr1, String[] arr2){
		String arr3[] = new String[arr1.length+arr2.length];
		System.arraycopy(arr1, 0, arr3, 0, arr1.length);
		System.arraycopy(arr2, 0, arr3, arr1.length, arr2.length);
		
		return arr3;
	}
	
	/**
	 * Converts a string integer 
	 * @param value
	 * @return
	 */
	public static int toInt( String value){
		return Integer.parseInt(value);
	}
	
	/**
	 * Capitalize any string given to it.
	 * @param text
	 * @return capitalized string
	 */
	public static String capitalizeString( String text ) {
		return text.substring(0,1).toUpperCase() + text.substring(1);
	}
	
	/**
	 * Create csv
	 * @param Vector<String> text
	 * 
	 * @return csv
	 */
	public static String implode( Vector<String> text ) {
		String implode = "";
		int i = 0;
		for( String value : text ) {
			implode += i == text.size() -1 ? value : value+",";
			i++;
		}
		
		return implode;
	}
	
	/**
	 * Is there internet connection
	 */
	public static boolean isConnected(Context context )  {
		  
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		networkInfo = connectivity.getActiveNetworkInfo();
		//NetworkInfo info
		
		if(networkInfo == null || !networkInfo.isConnected()){  
	        return false;  
	    } 
	    return true; 
	     
	}
	
	/**
	 * Limit a string to defined length
	 * 
	 * @param int limit - the total length 
	 * @param string limited - the limited string
	 */
	public static String limitString( String value, int length ) {
		StringBuilder buf = new StringBuilder(value);
		if( buf.length() > length ) {
			buf.setLength(length);
			buf.append(" ...");
		}
		return buf.toString();
	}
	
	
	/**
	 * Format date into more readable format.
	 * 
	 * @param  date - the date to be formatted.
	 * @return String
	 */
	public static String formatDate( String fromFormat, String date, String toFormat) {
	String formatted = "";
		
		DateFormat formatter = new SimpleDateFormat(fromFormat, Locale.ENGLISH);
		try {
			Date dateStr = formatter.parse(date);
			formatted = formatter.format(dateStr);
			Date formatDate = formatter.parse(formatted);
			formatter = new SimpleDateFormat(toFormat);
			formatted = formatter.format(formatDate);
		
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return formatted;
	}
	
	
	/**
	 * Extract Boskoi payload JSON data
	 * 
	 * @apram json_data - the json data to be formatted.
	 * @return String 
	 */
	public static boolean extractPayloadJSON( String json_data ) {
	
		try {
			jsonObject = new JSONObject(json_data);
			return jsonObject.getJSONObject("payload").getBoolean("success");
		
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		return false;
	}
	

	public static void findDeletedIncidents(Context context) {
		
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new java.util.Date();
        date.setYear(date.getYear()-1);
        StringBuilder datetime = new StringBuilder( dateFormat.format( date ) );
		String sinceDate = datetime.toString();
		//request all deleted incidents from the last year
		
		try {
			if(Incidents.getDeletedIncidentsFromWeb(sinceDate)){
				List<IncidentsData> deletedIncidents =  HandleXml.processIncidentsXml( BoskoiService.incidentsResponse, context);	
				BoskoiApplication.mDb.deleteIncidents(deletedIncidents);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
	// get incidents from the db
	public static List<IncidentsData> showIncidents(String by) {

		Cursor cursor;
		String title;
		String description;
		String location;
		String categories;
		String media;

		if (by.equals("All"))
			cursor = BoskoiApplication.mDb.fetchAllIncidents();
		else
			cursor = BoskoiApplication.mDb.fetchIncidentsByCategories(by);

		if (cursor.moveToFirst()) {
			int idIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_ID);
			int titleIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_TITLE);
			int dateIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_DATE);
			int verifiedIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_VERIFIED);
			int locationIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_LOC_NAME);

			int descIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_DESC);

			int categoryIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_CATEGORIES);

			int mediaIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_MEDIA);

			int latitudeIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_LOC_LATITUDE);

			int longitudeIndex = cursor
					.getColumnIndexOrThrow(BoskoiDatabase.INCIDENT_LOC_LONGITUDE);

			do {

				IncidentsData incidentData = new IncidentsData();
				mOldIncidents.add(incidentData);

				int id = Util.toInt(cursor.getString(idIndex));
				incidentData.setIncidentId(id);

				title = Util.capitalizeString(cursor.getString(titleIndex));
				incidentData.setIncidentTitle(title);

				description = cursor.getString(descIndex);
				incidentData.setIncidentDesc(description);

				categories = cursor.getString(categoryIndex);
				incidentData.setIncidentCategories(categories);

				location = cursor.getString(locationIndex);
				incidentData.setIncidentLocLongitude(location);

				Util.joinString("Date: ", cursor.getString(dateIndex));
				incidentData.setIncidentDate(cursor.getString(dateIndex));

				media = cursor.getString(mediaIndex);
				incidentData.setIncidentMedia(media);

				incidentData.setIncidentVerified(Util.toInt(cursor
						.getString(verifiedIndex)));

				incidentData.setIncidentLocLatitude(cursor
						.getString(latitudeIndex));
				incidentData.setIncidentLocLongitude(cursor
						.getString(longitudeIndex));

			} while (cursor.moveToNext());
		}

		cursor.close();
		return mOldIncidents;

	}
	
	/**
	 * process reports
	 * 0 - successful
	 * 1 - failed fetching categories
	 * 2 - failed fetching reports
	 * 3 - non ushahidi instance
	 * 4 - No internet connection
	 * 
	 * @return int - status
	 */
	public static int processReports( Context context , boolean clear) {   

		BoskoiService.loadSettings(context);
		try {
			if( Util.isConnected(context)) {
				 
				if(Categories.getAllCategoriesFromWeb()) {
						
					mNewCategories = HandleXml.processCategoriesXml(BoskoiService.categoriesResponse); 
				} else {
					return 1;
				}
				  String timeLastUpdated = BoskoiService.lastUpdate;
				  if(BoskoiApplication.mDb.fetchAllIncidentsCount() == 0){
					  BoskoiService.lastUpdate = "1970-01-01 00:00:00";
					  timeLastUpdated = "1970-01-01 00:00:00";
				  }
				if( Incidents.getAllIncidentsFromWeb(timeLastUpdated) ){
					mNewIncidents =  HandleXml.processIncidentsXml( BoskoiService.incidentsResponse , context);					
					
				} else {
					return 1;
				}
				
				if(mNewCategories != null && mNewIncidents != null ) {

					 BoskoiService.numberOfNewReports = mNewIncidents.size();
					 BoskoiApplication.mDb.addCategories(mNewCategories, false);
					 BoskoiApplication.mDb.addIncidents(mNewIncidents, false);
					 
					 findDeletedIncidents(context);
					 
					 return 0;
				 
				 } else {
					 return 1;
				 }
				  

				} else {
					return 4;
				}
		} catch (IOException e) {
			//means there was a problem getting it
		}
		return 0;
	}
	
	/**
	 * Show toast
	 * 
	 * @param Context - the application's context
	 * @param Int - string resource id
	 * 
	 * @return void
	 */
	public static void showToast(Context context, int i ) {
		int duration = Toast.LENGTH_LONG;
		Toast.makeText(context, i, duration).show();
	}
	
	/**
	 * Validates an email address
	 * Credits: http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
	 * 
	 * @param String - email address to be validated
	 * 
	 * @return boolean
	 */
	public static boolean validateEmail( String emailAddress) {
		Pattern pattern;
		Matcher matcher;
		boolean returnvalue = false;
		if( !emailAddress.equals("") ) {
			pattern = Pattern.compile(EMAIL_PATTERN);
			matcher = pattern.matcher(emailAddress);
			returnvalue = matcher.find();
		}else{
			returnvalue = true;
		}
		return returnvalue;
	}
	
	/**
	 * Validate an Boskoi instance
	 * 
	 * @param String - URL to be validated.
	 * 
	 * @return boolean
	 */
	public static boolean validateUshahidiInstance( String ushahidiUrl ) {
		//make an http get request to a dummy api call
		//TODO improve on how to do this
		boolean status = false;
		if( ushahidiUrl == null ) {
			return false;
		}
		
		HttpResponse response;
		String json_string = "";
		//String message = "";
		StringBuilder uriBuilder = new StringBuilder( ushahidiUrl );
		uriBuilder.append("/api");

		try {
			response = BoskoiHttpClient.GetURL( uriBuilder.toString() );

				final int statusCode = response.getStatusLine().getStatusCode();
				
				if( statusCode == 200 ) {
					json_string = BoskoiHttpClient.GetText(response);
					
					//extract data from json object
					try {
						jsonObject = new JSONObject(json_string);
						
						jsonObject.getJSONObject("error").getString("message");
						status=true;
					} catch (JSONException e) {
						status = false;
					}
					
				} else {
					status = false;
				}
			
		} catch (IOException e) {
			status = false;
		}
		
		return status;
	}	
}
