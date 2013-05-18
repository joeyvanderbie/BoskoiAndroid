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

import android.util.Log;


public class Categories {
	
	public static boolean getAllCategoriesFromWeb() throws IOException {
        BoskoiService.tracker.trackPageView("/Categories/CategoriesFromWeb");
		HttpResponse response;
		String categories = "";
		
		StringBuilder uriBuilder = new StringBuilder( BoskoiService.domain);
		uriBuilder.append("/api?task=categories");
		uriBuilder.append("&resp=xml");
		response = BoskoiHttpClient.GetURL( uriBuilder.toString() );
		
		if( response == null ) {
			return false;
		}
		
		final int statusCode = response.getStatusLine().getStatusCode();
		
		if( statusCode == 200 ) {
			categories = BoskoiHttpClient.GetText(response);
			BoskoiService.categoriesResponse = categories;
			return true;
			
		} else {
			return false;
		}
		
	}

	public static boolean getAllCategoriesLangFromWeb() throws IOException {
        BoskoiService.tracker.trackPageView("/Categories/CategoriesLangFromWeb");
		HttpResponse response;
		String categories = "";
		
		StringBuilder uriBuilder = new StringBuilder( BoskoiService.domain);
		uriBuilder.append("/api?task=categories_lang");
		uriBuilder.append("&resp=xml");
		response = BoskoiHttpClient.GetURL( uriBuilder.toString() );
		
		if( response == null ) {
			return false;
		}
		
		final int statusCode = response.getStatusLine().getStatusCode();
		
		if( statusCode == 200 ) {
			categories = BoskoiHttpClient.GetText(response);
			BoskoiService.categoriesResponse = categories;
			return true;
			
		} else {
			return false;
		}
		
	}

	
}
