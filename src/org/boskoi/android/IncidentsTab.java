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

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;


public class IncidentsTab extends BoskoiTabActivity {
	
	private TabHost tabHost;
	private Bundle bundle;
	private Bundle extras;
	private Bundle tabContext;
	public static IncidentsTab context;
 
	 
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.tabhost);
        context = this;
        bundle = new Bundle();
       
        if( BoskoiService.domain.length() == 0 ) {
        	BoskoiService.loadSettings(this);
        }
		extras = this.getIntent().getExtras();  
       tabHost = getTabHost();
        Intent map = new Intent(this, IncidentMap.class);
        Intent addEdible = new Intent(this, AddIncident.class);
        Intent loadList = new Intent(this, ListIncidents.class);
        if(extras!=null){
        	if(extras.getBundle("report") != null){
        		map.putExtra("report", extras.getBundle("report"));
        	}
        	if(extras.getBundle("locations")!= null){
        		addEdible.putExtra("locations", extras.getBundle("locations"));
        	}
        	
        }
        Intent blogTab = new Intent(this, BoskoiBlogTabGroup.class);
        
       
        tabHost.addTab(tabHost.newTabSpec("map")
                .setIndicator(getText(R.string.boskoi_map),getResources().getDrawable(R.drawable.ic_tab_map))
                .setContent(map));
        
        tabHost.addTab(tabHost.newTabSpec("list_reports")
        		.setIndicator(getText(R.string.boskoi_list),getResources().getDrawable(R.drawable.ic_tab_list))
                .setContent(loadList));
        
        tabHost.addTab(tabHost.newTabSpec("add_report")
        		.setIndicator(getText(R.string.boskoi_report),getResources().getDrawable(R.drawable.ic_tab_report))
                .setContent(addEdible));
        
       /* tabHost.addTab(tabHost.newTabSpec("News")
        		.setIndicator(getText(R.string.menu_blog),getResources().getDrawable(R.drawable.ic_tab_blog))
                .setContent(blogTab));
        */
        tabHost.addTab(tabHost.newTabSpec("About")
        		.setIndicator(getText(R.string.menu_about),getResources().getDrawable(R.drawable.ic_tab_about))
                .setContent(new Intent(this, About.class)));
        


        tabHost.setCurrentTab(0);        
        if( extras != null ) {
        	bundle = extras.getBundle("tab");
        	tabHost.setCurrentTab(bundle.getInt("tab_index"));
        }
        setTabColor(tabHost);
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener(){

			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				setTabColor(tabHost);
			}
        });
        
    }
	
	public static void setTabColor(TabHost tabhost) {
	    for(int i=0;i<tabhost.getTabWidget().getChildCount();i++)
	    {
	       tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#7ba42b")); //unselected
	    }
	    tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#DDDEDC")); // selected
	}
	
}
