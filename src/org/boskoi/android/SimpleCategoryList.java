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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SimpleCategoryList extends ListActivity {//implements ListView.OnScrollListener{
	 	private RemoveWindow mRemoveWindow = new RemoveWindow();
	    Handler mHandler = new Handler();
	    private WindowManager mWindowManager;
	    private TextView mDialogText;
	    private boolean mShowing;
	    private boolean mReady;
	    private Vector<String> vectorCategories = new Vector<String>();
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

        BoskoiService.trackPageView(SimpleCategoryList.this,"/SimpleCategoryList");
		setContentView(R.layout.category_listview);
		BoskoiService.loadSettings(SimpleCategoryList.this);

       mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
       
		categories = new String[0];
   	hmCategories = new HashMap<String, CategoriesData>();
   	hmParentCategories = new HashMap<Integer, CategoriesData>();
   	
   	String newCatAll[] = {"All"}; //add all cat
   	adapter.addSection("All",
			new ArrayAdapter<String>(
					this,R.layout.category_list_item,
					newCatAll));
	
//			List<String> list=Arrays.asList(newCat);
//			Collections.shuffle(list);
	
	categories = Util.joinStringArray(categories, newCatAll);
   	
   	for(CategoriesData cat : BoskoiService.getParentCategories()){
   		
   			
  			CategoriesData[] childcat = BoskoiService.getCategoriesFromParentString(cat.getCategoryId());
   			String[] childStrings = new String[childcat.length];
   			String[] englishStrings = new String[childcat.length];
   			
   			
   			int i = 0;
   			for(CategoriesData childCat : BoskoiService.getCategoriesFromParent(cat.getCategoryId())){
   					childStrings[i] = childCat.getCategoryTitle() + " ("+childCat.getCategoryTitleLA()+")";
   				englishStrings[i] = childCat.getCategoryId()+"";
   				i++;
   			}
   		
   		//only add filled categories
   		if(childcat.length != 0){

	    		adapter.addSection(cat.getCategoryTitle() + " ("+cat.getCategoryTitleLA() + ")",
	    				new ArrayAdapter<String>(
	    						this,R.layout.category_list_item,
	    						childStrings));
	    		
	//			List<String> list=Arrays.asList(newCat);
	//			Collections.shuffle(list);
	    		//for actual filtering string without the latin/dutch
	    		String sectionCat[] = {cat.getCategoryId()+""};
	    		categories = Util.joinStringArray(categories, sectionCat);
				categories = Util.joinStringArray(categories, englishStrings);
	
	    		//here we also need to set the array with real CatData
	    		for(CategoriesData childCat : BoskoiService.getCategoriesFromParent(cat.getCategoryId())){
	        		hmCategories.put(childCat.getCategoryTitle(), childCat);
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
		listView.setBackgroundColor(Color.WHITE);
		listView.setCacheColorHint(Color.parseColor("#00000000"));
	}
	
   @Override
   protected void onResume() {
       super.onResume();
       BoskoiService.trackPageView(SimpleCategoryList.this,"/SimpleCategoryList");
       mReady = true;
   }

   @Override
   protected void onPause() {
       super.onPause();
       removeWindow();
       mReady = false;
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
   
//   public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//       if (mReady) {
//       	int lastItem = firstVisibleItem + visibleItemCount - 1;
//       	
//       	//this is to show the last possible categoryparent
//       	if(lastItem == totalItemCount-1){
//       		firstVisibleItem = lastItem;//quick fix
//       	}
//       	
//       	//fetch and show the parent
//       	CategoriesData cat = hmCategories.get(view.getItemAtPosition(firstVisibleItem));
//       	if(cat != null){
//               if (!mShowing ) {
//
//                   mShowing = true;
//                   mDialogText.setVisibility(View.VISIBLE);
//                }
//               mDialogText.setText(hmParentCategories.get(cat.getCategoryParentId()).getCategoryTitle());
//       	}
//           mHandler.removeCallbacks(mRemoveWindow);
//           mHandler.postDelayed(mRemoveWindow, 1500);
//       }
//   }
//   
//
//   public void onScrollStateChanged(AbsListView view, int scrollState) {
//   }
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
//   	String selection = l.getItemAtPosition(position).toString();     
//   	vectorCategories.add(selection);
   		ListIncidents.selectedCategory = categories[position-1];
        finish();
		
   }
}

