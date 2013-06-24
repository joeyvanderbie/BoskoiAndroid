package org.boskoi.android;

import android.app.TabActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;


public class BoskoiTabActivity extends TabActivity{
	
	private Configuration config; // variable declaration in globally
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = new Configuration(getResources().getConfiguration());
        config.locale = BoskoiService.language ;
        getResources().updateConfiguration(config,getResources().getDisplayMetrics());

	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
		 super.onConfigurationChanged(newConfig);
		 Log.d("locale", "configurationsChanged");
		 config = new Configuration(getResources().getConfiguration());
	        config.locale = BoskoiService.language ;
	        getResources().updateConfiguration(config,getResources().getDisplayMetrics());
    }

}
