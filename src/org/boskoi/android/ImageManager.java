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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.boskoi.android.net.BoskoiHttpClient;


import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class ImageManager {
	//Images
	public static Drawable getImages(String fileName) {
		
		Drawable d = null;
	
		FileInputStream fIn;
		if( !TextUtils.isEmpty( fileName) ) {
			try {
				fIn = new FileInputStream(BoskoiService.savePath + fileName );
				d = Drawable.createFromStream(fIn, "src");
			} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		return d;
	}
	
	
	public static void saveImage() {
		byte[] is;
		for( String image : BoskoiService.mNewIncidentsImages) {
			if(!TextUtils.isEmpty(image )) {
				File f = new File( BoskoiService.savePath + image );
				if(!f.exists()) {
					try {
						is = BoskoiHttpClient.fetchImage(BoskoiService.domain+"/media/uploads/"+image);
						if( is != null ) {
							writeImage( is, image );
						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}
			}
		}
		
	}
	
	public static void saveImage(String image) {
		byte[] is;

			if(!TextUtils.isEmpty(image )) {
				File f = new File( BoskoiService.savePath + image );
				
				if(!f.exists()) {
					try {
						is = BoskoiHttpClient.fetchImage(BoskoiService.domain+"/media/uploads/"+image);
						if( is != null ) {
							writeImage2( is, image );
						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}
			}
		
		
	}
	public static void writeImage2(byte[] data, String filename) {
		
		File f = new File(BoskoiService.savePath + filename);
		if(f.exists()){
			f.delete();
		}
		f.deleteOnExit();
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(BoskoiService.savePath + filename);
			fOut.write(data);
			fOut.flush();
			fOut.close();
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeImage(byte[] data, String filename) {
		
		File f = new File(BoskoiService.savePath + filename);
		if(f.exists()){
			f.delete();
		}
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(BoskoiService.savePath + filename);
			fOut.write(data);
			fOut.flush();
			fOut.close();
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
