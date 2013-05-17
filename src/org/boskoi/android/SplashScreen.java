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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.boskoi.android.R;
import org.boskoi.android.data.IncidentsData;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

public class SplashScreen extends Activity implements Eula.OnEulaAgreedTo {


    private boolean mAlreadyAgreedToEula = false;

    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
        BoskoiService.loadSettings(SplashScreen.this);
        BoskoiService.trackPageView(SplashScreen.this, "/SplashScreen");        
        
        mAlreadyAgreedToEula = Eula.show(this);
        
        startSplashThread();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
           // active = false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    public void onEulaAgreedTo() {
    	startActivity(new Intent(SplashScreen.this,IncidentsTab.class));
    }

    
    private void startSplashThread(){
        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                	//do cleanup activity during splashscreen
            		//do version check
            		BoskoiService.loadSettings(getBaseContext());
            		String currentVersion = "";
            		
                    PackageManager manager = getPackageManager(); 
                    PackageInfo info;
            		try {
            			info = manager.getPackageInfo(getPackageName(), 0);
            			currentVersion = info.versionName;
            		} catch (NameNotFoundException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            		//if we updated then we rebuild  table structure
            		if(!currentVersion.equals(BoskoiService.lastVersion)){
            			Log.i("UPDATING", "Detected version change, clear all cache, settings, and database");

            			BoskoiApplication.mDb.clearData();
            			BoskoiService.clearSettings(getBaseContext());
            			BoskoiService.loadSettings(getBaseContext());
            			BoskoiService.lastVersion = currentVersion;
            			BoskoiService.lastUpdate = "1970-01-01 00:00:00";
            			BoskoiService.saveSettings(getBaseContext());
            			
                 			
            		}
                    //Get a list of incidents
                    List mNewIncidents = new ArrayList<IncidentsData>();
            		mNewIncidents = Util.showIncidents("All");
            		
            		
            		// delete old large images
                    for (Iterator i = mNewIncidents.iterator(); i.hasNext(); ){
            			IncidentsData inci = (IncidentsData)i.next();
            			String[] media = inci.getIncidentMedia().split(",");
            			
            			File f = new File(BoskoiService.savePath+media[0].replace("_t",""));
            			
            			if(f.exists() && !f.isDirectory()){
            				f.delete();
            			}
            		}
                    //delete deleted items, now done in util when updates are retrieved
                   // Util.findDeletedIncidents(SplashScreen.this);
                    

                    
                } catch(Exception e) {
                    // do nothing
                } finally {
                	if( mAlreadyAgreedToEula){
                		startActivity(new Intent(SplashScreen.this,IncidentsTab.class));//Boskoi.class));
                		finish();
                	}
                }
            }
            
        };
        splashTread.start();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
        BoskoiService.trackPageView(SplashScreen.this,"/SplashScreen");        
    }
    
    
}
