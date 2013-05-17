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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class About extends Activity {
	private TextView versionName;
	
	private static final int REQUEST_CODE_SETTINGS = 1;
	private static final int DIALOG_ERROR = 0;
		
	private String dialogErrorMsg = "An error occurred fetching the reports. " +
		"Make sure you have entered an Boskoi instance.";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BoskoiService.trackPageView(About.this,"/About");
        
        setContentView( R.layout.about);
   
        versionName = (TextView) findViewById(R.id.version_name);
        PackageManager manager = this.getPackageManager(); 
        PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			versionName.setText(versionName.getText()+" "+info.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        Linkify.addLinks((TextView) findViewById(R.id.intro), Linkify.ALL);
        Linkify.addLinks((TextView) findViewById(R.id.lgpl), Linkify.ALL);
        Linkify.addLinks((TextView) findViewById(R.id.privacy), Linkify.ALL);
        
        final Button eulaBtn = (Button) findViewById(R.id.eulaBtn);
        eulaBtn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
            	
                TextView eulaText = (TextView)findViewById(R.id.eula);
                if(eulaText.getVisibility() == View.GONE){
                	eulaText.setVisibility(View.VISIBLE);
                	eulaBtn.setText("Hide rules");
                }else{
                	eulaText.setVisibility(View.GONE);
                	eulaBtn.setText("Show rules");
                }
;            }
        });
        
        final Button lgplBtn = (Button) findViewById(R.id.lgplBtn);
        lgplBtn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
            	
                TextView lgplText = (TextView)findViewById(R.id.lgpl);
                if(lgplText.getVisibility() == View.GONE){
                	lgplText.setVisibility(View.VISIBLE);
                	lgplBtn.setText("Hide licence information");
                }else{
                	lgplText.setVisibility(View.GONE);
                	lgplBtn.setText("Show licence information");
                }
;            }
        });       
        
        final Button privacyBtn = (Button) findViewById(R.id.privacyBtn);
        privacyBtn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
            	
                TextView privacyText = (TextView)findViewById(R.id.privacy);
                if(privacyText.getVisibility() == View.GONE){
                	privacyText.setVisibility(View.VISIBLE);
                	privacyBtn.setText("Hide privacy information");
                }else{
                	privacyText.setVisibility(View.GONE);
                	privacyBtn.setText("Show privacy information");
                }
;            }
        });  
  
       
        
	}
	
	
	
	
	@Override
	 protected Dialog onCreateDialog(int id) {
		 switch (id) {
	     	case DIALOG_ERROR: {
	     		AlertDialog dialog = (new AlertDialog.Builder(this)).create();
	     		dialog.setTitle(R.string.alert_dialog_error_title);
	            dialog.setMessage(dialogErrorMsg);
	            dialog.setButton2("Ok", new Dialog.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int which) {
	            		
////	            		Intent launchPreferencesIntent = new Intent(About.this, 
////	            				Settings.class);
//	            		
//	    				// Make it a sub activity so we know when it returns
//	    				startActivityForResult(launchPreferencesIntent, REQUEST_CODE_SETTINGS);
//							dialog.dismiss();						
						}
        		});
	                dialog.setCancelable(false);
	                return dialog;
	     	}
		 }
		 return null;
	  }
	
	final Runnable mDisplayErrorPrompt = new Runnable() {
		public void run() {
			showDialog(DIALOG_ERROR);
		}
	};
	
	//thread class
	private class ReportsTask extends AsyncTask <Void, Void, Integer> {
		
		protected Integer status;
		private ProgressDialog dialog;
		protected Context appContext;
		@Override
		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(appContext, "Please wait...",
					"Fetching new reports", true);

		}
		
		@Override 
		protected Integer doInBackground(Void... params) {
			status = Util.processReports(appContext, false);
			return status;
		}
		
		@Override
		protected void onPostExecute(Integer result)
		{
			if( result == 4 ){
				Util.showToast(appContext, R.string.internet_connection);
			}
			this.dialog.cancel();
		}
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		BoskoiService.trackPageView(About.this,"/About");
	}

}
