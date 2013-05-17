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

import org.boskoi.android.data.BlogData;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;


public class BoskoiNewsView extends Activity {
	private String subject, text;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
        setContentView( R.layout.view_news);
        
        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("id");
        BlogData blog = BoskoiService.getBlogData(id);
        BoskoiService.trackPageView(BoskoiNewsView.this,"/Blog/"+blog.getTitle());//extras.getString("title"));
       
        String content = blog.getDescription();//extras.getString("postDescription");
        
        content = content.replace("src=\"", "src=\"http://i.tinysrc.mobi/");
        content = content.replaceAll("width=\"\\d*", "width=\"100%");
        content = content.replaceAll("height=\"\\d*", "height=\"");
        content = content.replaceAll("%", "%25");
        
        subject = getString(R.string.blogShareText)+ " '"+ blog.getTitle()+"'";//extras.getString("title")+"'";
        text =  getString(R.string.blogShareTextInteresting)+" " + subject + " " + blog.getLink();//extras.getString("url");
        
        TextView title = (TextView) findViewById(R.id.blogposttitle);
        title.setText(blog.getTitle());//extras.getString("title"));
        Typeface font = Typeface.createFromAsset(getAssets(), BoskoiService.fontPath);
        title.setTypeface(font);
        
        
        WebView mWebView;
        mWebView = (WebView) findViewById(R.id.webviewnews);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadData(content, "text/html", "utf-8");
   
	}


	
	@Override
	public void onResume(){
		super.onResume();
		BoskoiService.trackPageView(BoskoiNewsView.this,"/Blog");
	}
	
	public void sharePost(View view) {
		BoskoiService.trackPageView(BoskoiNewsView.this, "Blog/Share/"+subject);
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				subject);
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				text);

		startActivity(Intent.createChooser(shareIntent,subject));
		}


}
