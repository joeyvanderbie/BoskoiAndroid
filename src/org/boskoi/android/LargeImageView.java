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
import java.io.IOException;
import java.net.MalformedURLException;

import org.boskoi.android.R;
import org.boskoi.android.net.BoskoiHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class LargeImageView extends Activity {

	private ImageView img;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BoskoiService.trackPageView(LargeImageView.this, "/LargeImage");
		setContentView(R.layout.large_image_view);
		img = (ImageView) this.findViewById(R.id.ImageView);

		new SaveLargeImage().execute();

	}

	@Override
	public void onResume() {
		super.onResume();
		BoskoiService.trackPageView(LargeImageView.this, "/LargeImage");
	}

	private class SaveLargeImage extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progress = null;

		@Override
		protected Void doInBackground(Void... params) {
			//load images from web, while showing loading indicator
			Bundle extras = getIntent().getExtras();
			String[] thumbnails = extras.getStringArray("ImageName");
			File f = new File(thumbnails[0]);
			if(!f.exists()){
				ImageManager.saveImage(thumbnails[0].replace("_t", ""));
			}

			return null;
		}

		@Override
		protected void onCancelled() {
	
			super.onCancelled();
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(LargeImageView.this, null,
					"Loading image...");
			progress.setCancelable(true);


			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			
			//set image in interface
			Bundle extras = getIntent().getExtras();
			String[] thumbnails = extras.getStringArray("ImageName");
			File f = new File(thumbnails[0]);
			if(!f.exists()){
			img.setImageDrawable(ImageManager.getImages(thumbnails[0].replace(
					"_t", "")));
			}else{
				img.setImageURI(Uri.parse(thumbnails[0]));
			}
			img.setOnTouchListener(new View.OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					finish();
					return true;
				}
			});
			img.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			});
			progress.dismiss();

			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

	}
}
