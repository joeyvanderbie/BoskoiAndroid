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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.boskoi.android.R;
import org.boskoi.android.data.IncidentsData;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * An abstract extention to the ItemizedOverlay for displaying information 
 * on a ballon upon a tap of each marker overlay.
 * 
 * @credits - http://github.com/jgilfelt/android-mapviewballoons/
 * 
 * @author Jeff Gilfelt
 *
 */
public abstract class BoskoiItemizedOverlay<Item> extends ItemizedOverlay<OverlayItem> {
	
	private MapView mapView;
	private BalloonOverlayView balloonView;
	private IncidentMap iMap;
	private List<IncidentsData> mNewIncidents;
	private Bundle extras;
	private int viewOffset;
	private View clickRegion;
	final MapController mc;
	/**
	 * 
	 * @param marker -  An icon to be drawn on the map for each item in the overlay.
	 * @param mapView - The map view upon which the overlay item will be drawn.
	 */
	public BoskoiItemizedOverlay(Drawable marker, 
			MapView mapView, 
			IncidentMap iMap,
			List<IncidentsData> mNewIncidents, 
			Bundle extras  ) {
		
		super(marker);
		
		this.mapView = mapView;
		this.viewOffset = 32;
		this.iMap = iMap;
		this.mNewIncidents = mNewIncidents;
		this.extras = extras;
		this.mc = mapView.getController();
	}
		
	
	/**
	 * Set the horizontal distance between the marker and the bottom of the information
	 * balloon. The default is 0 which works well for center bounded markers. If your
	 * marker is center-bottom bounded, call this before adding overlay items to ensure
	 * the balloon hovers exactly above the marker. 
	 * 
	 * @param pixels - The padding between the center point and the bottom of the
	 * information balloon.
	 */
	public void setBalloonBottomOffset(int pixels) {
		viewOffset = pixels;
	}
	
	/**
	 * Override this method to handle a "tap" on a balloon. By default, does nothing 
	 * and returns false.
	 * 
	 * @param index - The index of the item whose balloon is tapped.
	 * @return true if you handled the tap, otherwise false.
	 */
	protected boolean onBalloonTap(int index) {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
	 */
	protected final boolean onTap(int index) {
		boolean isRecycled;
		final int thisIndex;
		GeoPoint point;
		thisIndex = index;
		point = createItem(index).getPoint();
		
		if( balloonView != null ) {
			balloonView.setVisibility(View.INVISIBLE);
		}
			
			balloonView = new BalloonOverlayView(this.iMap,mapView.getContext(),viewOffset,
					this.mNewIncidents, thisIndex, extras);
			clickRegion = (View) balloonView.findViewById(R.id.balloon_inner_layout);
			isRecycled = false;
			
//		} else {
//			isRecycled = true;
//		}
		
		balloonView.setVisibility(View.GONE);
		
		balloonView.setData(createItem(index));
		
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point, 
				MapView.LayoutParams.BOTTOM_CENTER );
		
		params.mode = MapView.LayoutParams.MODE_MAP;
		
		setBalloonTouchListener(thisIndex);
		balloonView.setVisibility(View.VISIBLE);
		
		if( isRecycled ) {
			balloonView.setLayoutParams(params);
		} else {
			mapView.addView(balloonView,params);
		}
		
		mc.animateTo(point);
		return true;
	}
	
	/**
	 * Sets the onTouchListener for the balloon being displayed, calling the
	 * overridden onBalloonTap if implemented.
	 * 
	 * @param thisIndex - The index of the item whose balloon is tapped.
	 */
	private void setBalloonTouchListener(final int thisIndex) {
		
		try {
			@SuppressWarnings("unused")
			Method m = this.getClass().getDeclaredMethod("onBalloonTap", int.class);
			
			clickRegion.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					
					View l =  ((View) v.getParent()).findViewById(R.id.balloon_main_layout);
					Drawable d = l.getBackground();
					
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						int[] states = {android.R.attr.state_pressed};
						if (d.setState(states)) {
							d.invalidateSelf();
						}
						return true;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						int newStates[] = {};
						if (d.setState(newStates)) {
							d.invalidateSelf();
						}
						// call overridden method
						onBalloonTap(thisIndex);
						return true;
					} else {
						return false;
					}
					
				}
			});
			
		} catch (SecurityException e) {
			return;
		} catch (NoSuchMethodException e) {
			// method not overridden - do nothing
			return;
		}
	}
}
