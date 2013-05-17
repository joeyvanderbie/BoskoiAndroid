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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.boskoi.android.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;


public class CameraView extends Activity implements SurfaceHolder.Callback,
		OnClickListener {
	static final int FOTO_MODE = 0;
	private static final String TAG = "CameraView";
	Camera mCamera;
	boolean mPreviewRunning = false;
	private Context mContext = this;
	private static Random random = new Random();
	

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		//Log.e(TAG, "onCreate");
		BoskoiService.trackPageView(CameraView.this,"/Camera");
				

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.surface_camera);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		Util.showToast(this, R.string.hint_camera);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {

			if (imageData != null) {

				Intent mIntent = new Intent();

				StoreByteImage(mContext, imageData,
						 50, "ImageName");
				mCamera.startPreview();
				
				//code for integration in application
				setResult(FOTO_MODE,mIntent);
				finish();
			}
		}
	};

	public static boolean StoreByteImage(Context mContext, byte[] imageData,
			int quality, String expName) {
		String nameFile = "pictureupload" + randomString()+".jpg";
		File sdImageMainDirectory = new File(BoskoiService.savePath + nameFile);//"/sdcard");
		FileOutputStream fileOutputStream = null;
		try {

			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 5;
			
			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0,
					imageData.length,options);
			
			fileOutputStream = new FileOutputStream(
					sdImageMainDirectory.toString());// +"/" + nameFile);
  
			BufferedOutputStream bos = new BufferedOutputStream(
					fileOutputStream);

			myImage.compress(CompressFormat.JPEG, quality, bos);

			bos.flush();
			bos.close();
			
			BoskoiService.fileName = nameFile;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	protected void onResume() {
		//Log.e(TAG, "onResume");
		BoskoiService.trackPageView(CameraView.this,"/Camera");
		super.onResume();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void onStop() {
		//Log.e(TAG, "onStop");
		super.onStop();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		//Log.e(TAG, "surfaceCreated");
		mCamera = Camera.open();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		//Log.e(TAG, "surfaceChanged");

		// XXX stopPreview() will crash if preview is not running
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
		Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		if(display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			p.setPreviewSize(p.getPreviewSize().height,p.getPreviewSize().width); 
		} else {
			 p.setPreviewSize(p.getPreviewSize().width, p.getPreviewSize().height); 
		}
		
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		//Log.e(TAG, "surfaceDestroyed");
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	public void onClick(View arg0) {
		mCamera.takePicture(null, mPictureCallback, mPictureCallback);
	}
	
	protected static String randomString() {
		return Long.toString(random.nextLong(), 36);
	}
}
