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
import android.os.Bundle;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.boskoi.android.data.BlogData;
import org.boskoi.android.data.HandleBlogXML;


public class BoskoiBlog extends ListActivity {
        private ArrayList<BlogData> itemlist = null;
        private RSSListAdaptor rssadaptor = null;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bloglistview);
        
        itemlist = new ArrayList<BlogData>();
        new RetrieveRSSFeeds().execute();
    }
    
    @Override
        protected void onListItemClick(ListView l, View v, int position, long id) {
                super.onListItemClick(l, v, position, id);
                
                Bundle tab = new Bundle();
				tab.putInt("tab_index", 4);
                
                BlogData data = itemlist.get(position);

                Intent intent = new Intent(IncidentsTab.context, BoskoiNewsView.class);
                //intent.putExtra("title", data.getTitle());
                //intent.putExtra("url", data.getLink());
                //intent.putExtra("postDescription", data.getDescription());
                intent.putExtra("id", data.getId());
                intent.putExtra("tab", tab);
//                
//                startActivity(intent);
                // Create the view using FirstGroup's LocalActivityManager  
                View view = BoskoiBlogTabGroup.group.getLocalActivityManager()  
                .startActivity("BoskoiNewsView", intent   
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))  
                .getDecorView();  
          
                // Again, replace the view  
                BoskoiBlogTabGroup.group.replaceView(view); 


        }

     private void retrieveRSSFeed(String urlToRssFeed,ArrayList<BlogData> list)
    {
        try
        {
           URL url = new URL(urlToRssFeed);
           SAXParserFactory factory = SAXParserFactory.newInstance();
           SAXParser parser = factory.newSAXParser();
           XMLReader xmlreader = parser.getXMLReader();
           HandleBlogXML theRssHandler = new HandleBlogXML(list);

           xmlreader.setContentHandler(theRssHandler);
           
           URLConnection urlConn = url.openConnection();
           urlConn.setConnectTimeout(7000);
           urlConn.setReadTimeout(7000);
           urlConn.setAllowUserInteraction(false);         
           //urlConn.setDoOutput(true);
           
           InputSource is = new InputSource(urlConn.getInputStream());
           

           xmlreader.parse(is);
        }catch(FileNotFoundException e){
        	e.printStackTrace();
        	list = null;
        }
        catch(SocketTimeoutException e){
        	e.printStackTrace();
        	list = null;
        }
        catch (Exception e)
        {
            //Toast.makeText(this, R.string.blog_error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        	list = null;
        }
    }
    
    private class RetrieveRSSFeeds extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog progress = null;
        
                @Override
                protected Void doInBackground(Void... params) {
                        //start new code
                        	//add items to database
                        List<BlogData> currentData = BoskoiService.getSimpleBlogData();
                        Date today = new Date();
                        today.setDate(today.getDate()>2?today.getDate()-1:1);
                        Date lastUpdate = new Date(BoskoiService.blogLastUpdate);
                        if(currentData.size() == 0){
                        	//this is temporary code, this code should be replaced by a boskoi api call using a sincedate
                        	 retrieveRSSFeed("http://boskoi.posterous.com/rss.xml",itemlist);
                        	BoskoiService.addBlogItems(itemlist);
                        	BoskoiService.blogLastUpdate = new Date().getTime();
                        }else if(lastUpdate.before(today)){
                        	//this is temporary code, this code should be replaced by a boskoi api call using a sincedate
                        	
                        	 retrieveRSSFeed("http://boskoi.posterous.com/rss.xml",itemlist);
	                        for(BlogData blogItem : itemlist){
	                        	 if(!currentData.get(0).getDate().equals(blogItem.getDate())){
	                        		 List<BlogData> temp = new ArrayList<BlogData>();
	                        		 temp.add(blogItem);
	                        		 BoskoiService.addBlogItems(temp);
	                        	 }else{
	                        		 break;
	                        	 }
	                        }
	                        long todaymilis = new Date().getTime();
	                    	BoskoiService.blogLastUpdate = todaymilis;
	                        itemlist = (ArrayList<BlogData>) BoskoiService.getSimpleBlogData();
                        }else{
                        	itemlist = (ArrayList<BlogData>) currentData;
                        }
                        
                        	//retrieve items from database
                        rssadaptor = new RSSListAdaptor(IncidentsTab.context, R.layout.blogitemview,itemlist);
                        
                        //end new code
                        //rssadaptor = new RSSListAdaptor(IncidentsTab.context, R.layout.blogitemview,itemlist);
                        
                        return null;
                }
        
                @Override
                protected void onCancelled() {
                        super.onCancelled();
                }
                
                @Override
                protected void onPreExecute() {
                        progress = ProgressDialog.show(
                        		IncidentsTab.context, null, getText(R.string.loadingBlog));
                        
                        super.onPreExecute();
                }
                
                @Override
                protected void onPostExecute(Void result) {
                        setListAdapter(rssadaptor);
                        
                        progress.dismiss();
                        
                        super.onPostExecute(result);
                }
                
                @Override
                protected void onProgressUpdate(Void... values) {
                        super.onProgressUpdate(values);
                }
    }
    
    private class RSSListAdaptor extends ArrayAdapter<BlogData>{
        private List<BlogData> objects = null;
        
                public RSSListAdaptor(Context context, int textviewid, List<BlogData> objects) {
                        super(context, textviewid, objects);
                        
                        this.objects = objects;
                }
                
                @Override
                public int getCount() {
                        return ((null != objects) ? objects.size() : 0);
                }
                
                @Override
                public long getItemId(int position) {
                        return position;
                }
                
                @Override
                public BlogData getItem(int position) {
                        return ((null != objects) ? objects.get(position) : null);
                }
                
                public View getView(int position, View convertView, ViewGroup parent) {
                        View view = convertView;
                        
                        if(null == view)
                        {
                                LayoutInflater vi = (LayoutInflater)IncidentsTab.context.getSystemService(getBaseContext().LAYOUT_INFLATER_SERVICE);
                                view = vi.inflate(R.layout.blogitemview, null);
                        }
                        
                        BlogData data = objects.get(position);
                        
                        if(null != data)
                        {
                                TextView title = (TextView)view.findViewById(R.id.txtTitle);
                                TextView date = (TextView)view.findViewById(R.id.txtDate);
                                //TextView description = (TextView)view.findViewById(R.id.txtDescription);
                                
                                try {
                                String[] dateTime = data.getDate().split(":");
                                title.setText(data.getTitle());
                                date.setText("on " + dateTime[0] + ":" + dateTime[1]);
                                //description.setText(Html.fromHtml(data.getDescription()));
                                }catch(ArrayIndexOutOfBoundsException e){
                                	//date is not fully formated.
                                	date.setText("");
                                }
                        }
                        
                        return view;
                }
    }
}