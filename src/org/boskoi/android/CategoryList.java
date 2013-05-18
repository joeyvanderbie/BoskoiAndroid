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

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.boskoi.android.data.CategoriesData;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class CategoryList extends ListActivity {//implements ListView.OnScrollListener{
	 private RemoveWindow mRemoveWindow = new RemoveWindow();
	    Handler mHandler = new Handler();
	    private WindowManager mWindowManager;
	    private TextView mDialogText;
	    private boolean mShowing;
	    private boolean mReady;
	    private Vector<String> vectorCategories = new Vector<String>();
	    private Vector<String> vectorCategoriesData = new Vector<String>();
	    private HashMap<String, CategoriesData> hmCategories;
	    private HashMap<Integer, CategoriesData> hmParentCategories;
	    private String categories[];

	    
	    private final class RemoveWindow implements Runnable {
	        public void run() {
	            removeWindow();
	        }
	    }

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        BoskoiService.trackPageView(CategoryList.this,"/CategoryList");   
		
		setContentView(R.layout.category_listview);
		
		Button ok = (Button) findViewById(R.id.ok_btn);
		ok.setVisibility(View.VISIBLE);
		
        ok.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
            	
                finish();
;            }
        });
        
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        
		categories = new String[0];
    	hmCategories = new HashMap<String, CategoriesData>();
    	hmParentCategories = new HashMap<Integer, CategoriesData>();
    	
	    Locale locale = this.getBaseContext().getResources().getConfiguration().locale;

    	
	for(CategoriesData cat : BoskoiService.getParentCategories()){
   			
  			CategoriesData[] childcat = BoskoiService.getCategoriesFromParentString(cat.getCategoryId());
   			String[] childStrings = new String[childcat.length];
   			String[] englishStrings = new String[childcat.length];
   			

   			
   			int i = 0;
   			for(CategoriesData childCat : BoskoiService.getCategoriesFromParent(cat.getCategoryId(), locale)){
   				childStrings[i] = childCat.getCategoryTitle() + " ("+childCat.getCategoryTitleLA()+")";
   				englishStrings[i] = childCat.getCategoryTitle();
   				i++;
   			}
   		
    		//only add filled categories
   			if(childcat.length != 0){

	    		adapter.addSection(cat.getCategoryTitle() + " ("+cat.getCategoryTitleLA() + ")",
	    				new ArrayAdapter<String>(
	    						this,android.R.layout.simple_list_item_multiple_choice,
	    						childStrings));
	    		
	//			List<String> list=Arrays.asList(newCat);
	//			Collections.shuffle(list);
	    		//for actual filtering string without the latin/dutch
	    		String sectionCat[] = {cat.getCategoryTitle()};
	    		categories = Util.joinStringArray(categories, sectionCat);
				categories = Util.joinStringArray(categories, englishStrings);
				
	    		//here we also need to set the hashmap with real CatData
	    		for(CategoriesData childCat : BoskoiService.getCategoriesFromParent(cat.getCategoryId())){
	    			hmCategories.put(childCat.getCategoryTitle() + " ("+childCat.getCategoryTitleLA()+")", childCat);
	    		}
	    		hmParentCategories.put(cat.getCategoryId(), cat);
    		}
    	}
    	
        
        LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mDialogText = (TextView) inflate.inflate(R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        
        mHandler.post(new Runnable() {

            public void run() {
                mReady = true;
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mDialogText, lp);
            }});
			
		setListAdapter(adapter);
		
		ListView listView = this.getListView();
		//listView.setOnScrollListener(this);
		listView.setFastScrollEnabled(true);
	    listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 
		listView.setBackgroundColor(Color.WHITE);
		listView.setCacheColorHint(Color.parseColor("#00000000"));
		

        setCategoriesChecked();
	}
	
    @Override
    protected void onResume() {
        super.onResume();

        BoskoiService.trackPageView(CategoryList.this,"/CategoryList");
        mReady = true;
        setCategoriesChecked();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeWindow();
        mReady = false;
        
        AddIncident.checkedCategories = getListView().getCheckedItemPositions();
        AddIncident.vectorCategories = vectorCategories;
        AddIncident.vectorCategoriesData = vectorCategoriesData;
    }

   @Override
   protected void onDestroy() {
       super.onDestroy();
       mWindowManager.removeView(mDialogText);
       mReady = false;
   }
	    
    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }
	
	SectionedAdapter adapter=new SectionedAdapter() {
		protected View getHeaderView(String caption, int index,View convertView,ViewGroup parent) {
			TextView result=(TextView)convertView;
			
			if (convertView==null) {
				result=(TextView)getLayoutInflater().inflate(R.layout.category_list_header, null);
			}
			Typeface font = Typeface.createFromAsset(getAssets(), BoskoiService.fontPath);//"font/telegrafico.ttf");  
			result.setTypeface(font);  
			
			result.setText(caption);
			
			return(result);
		}
	};
	
    private void setCategoriesChecked(){
    	SparseBooleanArray checked = AddIncident.checkedCategories;
    	AddIncident.checkedCategories = null;
    	if(checked == null){
    		return;
    	}
    	
    	for(int i = 0; i < categories.length; i++){
    		if(checked.get(i)){
    			getListView().setItemChecked(i, true);
    		}
    	}
    	vectorCategories = AddIncident.vectorCategories;
    	vectorCategoriesData = AddIncident.vectorCategoriesData;
    	
    }
    
//    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        if (mReady) {
//        	int lastItem = firstVisibleItem + visibleItemCount - 1;
//        	
//        	//this is to show the last possible categoryparent
//        	if(lastItem == totalItemCount-1){
//        		firstVisibleItem = lastItem;//quick fix
//        	}
//        	
//        	//fetch and show the parent
//        	CategoriesData cat = hmCategories.get(view.getItemAtPosition(firstVisibleItem));
//        	if(cat != null){
//                if (!mShowing ) {
//
//                    mShowing = true;
//                    mDialogText.setVisibility(View.VISIBLE);
//                 }
//                mDialogText.setText(hmParentCategories.get(cat.getCategoryParentId()).getCategoryTitle());
//        	}
//            mHandler.removeCallbacks(mRemoveWindow);
//            mHandler.postDelayed(mRemoveWindow, 1500);
//        }
//    }
//    
//
//    public void onScrollStateChanged(AbsListView view, int scrollState) {
//    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
     String selection = l.getItemAtPosition(position).toString();     
     boolean isChecked = this.getListView().isItemChecked(position); 
     CategoriesData cat = hmCategories.get(selection);

	     if( isChecked ) {
	 		vectorCategories.add(cat.getCategoryId()+"");
	 		vectorCategoriesData.add(cat.getCategoryTitle());

 		} else {
	 		//fixed a crash here.
	 		vectorCategories.remove(cat.getCategoryId()+"");
	 		vectorCategoriesData.remove(cat.getCategoryTitle());
	 		} 	
    }
}

