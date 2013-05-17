package org.boskoi.android;

import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BoskoiOverlayItem extends OverlayItem{
	private Drawable marker ;
	private int color;

	public BoskoiOverlayItem(GeoPoint point, String title, String snippet, Drawable marker, int color) {
		super(point, title, snippet);
		this.marker = marker;
		this.color = color;
		
	}

	@Override
	public Drawable getMarker(int stateBitset){
		ColorFilter filter = new PorterDuffColorFilter(color,PorterDuff.Mode.MULTIPLY);
		marker.setColorFilter(filter);
		
		return marker;
	}
}
