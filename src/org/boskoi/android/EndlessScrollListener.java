package org.boskoi.android;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class EndlessScrollListener implements OnScrollListener {
	 
    private int visibleThreshold = 2;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    ListIncidents l = null;
 
    public EndlessScrollListener(ListIncidents l) {
    	this.l = l;
    }
    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }
 
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {

        	if(l.selectedCategory.equals("") || l.selectedCategory.equals("All") ){
        		l.showIncidents("All", currentPage + 1);
        		loading = true;
        	}else{
        		//for now we do nothing if a category filter is selected by the user
        	    
        	}
          

        }
    }
 
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}