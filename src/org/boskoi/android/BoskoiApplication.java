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

import java.util.HashSet;
import java.util.Locale;

import org.boskoi.android.data.BoskoiDatabase;
import org.boskoi.android.net.BoskoiHttpClient;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class BoskoiApplication extends Application {
	
	public static final String TAG = "BoskoiApplication";
	  
	  public static ImageManager mImageManager;
	  public static BoskoiDatabase mDb; 
	  public static BoskoiHttpClient mApi;
      private Locale locale = null;

	  @Override
	  public void onCreate() {
	    super.onCreate();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = settings.getString("Language", "");
        String langCountry = settings.getString("LanguageCountry", "");
        if (! "".equals(lang) && (! config.locale.getLanguage().equals(lang) || !config.locale.getCountry().equals(langCountry)))
        {
            locale = new Locale(lang, langCountry);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
	    
	    
	    mImageManager = new ImageManager();
	    mDb = new BoskoiDatabase(this);
	    mDb.open();
	    mApi = new BoskoiHttpClient();
	    
	    
	    
	    
	  }

	  @Override
	  public void onTerminate() {
	    cleanupImages();
	    mDb.close();
	    
	    super.onTerminate();
	  }
	  
	  private void cleanupImages() {
		  HashSet<String> keepers = new HashSet<String>();
	    
		  Cursor cursor = mDb.fetchAllIncidents();
	    
		  if (cursor.moveToFirst()) {
			  int imageIndex = cursor.getColumnIndexOrThrow(
					  BoskoiDatabase.INCIDENT_MEDIA);
			  do {
				  keepers.add(cursor.getString(imageIndex));
			  } while (cursor.moveToNext());
		  }
	    
		  cursor.close();
	    
		  cursor = mDb.fetchAllCategories();
	    
		  if (cursor.moveToFirst()) {
			  int imageIndex = cursor.getColumnIndexOrThrow(
					  BoskoiDatabase.INCIDENT_MEDIA);
			  do {
				  keepers.add(cursor.getString(imageIndex));
			  } while (cursor.moveToNext());
		  }
	    
		  cursor.close();
	    
		  //mImageManager.cleanup(keepers);
	  }

	      @Override
	      public void onConfigurationChanged(Configuration newConfig)
	      {
	          super.onConfigurationChanged(newConfig);
	          if (locale != null)
	          {
	              SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	              String lang = settings.getString("Language", "");
	              String langCountry = settings.getString("LanguageCountry", "");
	              newConfig.locale = new Locale(lang, langCountry);
	              Locale.setDefault(locale);
	              getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
	          }
	      }
}
