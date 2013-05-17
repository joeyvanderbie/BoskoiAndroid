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

package org.boskoi.android.net;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.boskoi.android.BoskoiService;
import org.boskoi.android.data.BoskoiDatabase;

import android.util.Log;


public class Incidents {
	
	
	public static boolean getAllIncidentsFromWeb(String sinceDate) throws IOException {
        BoskoiService.tracker.trackPageView("/Incidents/IncidentsFromWeb");
		
		HttpResponse response;
		String incidents = "";
		
		StringBuilder uriBuilder = new StringBuilder( BoskoiService.domain);
		uriBuilder.append("/api?task=incidents");
		uriBuilder.append("&by=sincedate");
		uriBuilder.append("&date="+sinceDate.replace(" ", "%20")); //format date url correctly by replacing whitespace
		uriBuilder.append("&limit=5000");
		uriBuilder.append("&resp=xml");
		Log.i("URL: ", uriBuilder.toString());
		
		response = BoskoiHttpClient.GetURL( uriBuilder.toString());
		
		if( response == null ) {
			return false;
		}
		
		final int statusCode = response.getStatusLine().getStatusCode();
		
		if( statusCode == 200 ) {
			
			incidents = BoskoiHttpClient.GetText(response);
			BoskoiService.incidentsResponse = incidents;
			return true;
		} else {
			return false;
		}
		
	}	
	public static boolean getDeletedIncidentsFromWeb(String sinceDate) throws IOException {
        BoskoiService.tracker.trackPageView("/Incidents/DeletedIncidentsFromWeb");
		
		HttpResponse response;
		String incidents = "";
		
		StringBuilder uriBuilder = new StringBuilder( BoskoiService.domain);
		uriBuilder.append("/api?task=incidents");
		uriBuilder.append("&by=deletedsince");
		uriBuilder.append("&date="+sinceDate);
		uriBuilder.append("&limit=1000000");
		uriBuilder.append("&resp=xml");
		//Log.i("URL: ", uriBuilder.toString());
		
		response = BoskoiHttpClient.GetURL( uriBuilder.toString());
		
		if( response == null ) {
			return false;
		}
		
		final int statusCode = response.getStatusLine().getStatusCode();
		
		if( statusCode == 200 ) {
			
			incidents = BoskoiHttpClient.GetText(response);
			BoskoiService.incidentsResponse = incidents;
			return true;
		} else {
			return false;
		}
		
	}
}
