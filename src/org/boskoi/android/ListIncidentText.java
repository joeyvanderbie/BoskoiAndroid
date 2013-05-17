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

import android.graphics.drawable.Drawable;
import android.net.Uri;

public class ListIncidentText {
	private String title;
	private String date;
	private String status;
	private int id;
	private Drawable thumbnail;
	private Drawable arrow;
	private Uri thumbnailUri;
	private String description;
	private String location;
	private String media;
	private String categories;
	private boolean isSelectable;
	
	public ListIncidentText(Drawable thumbnail, 
			String title, String date, 
			String status, 
			String description, 
			String location,
			String media, 
			String categories,
			int id,
			Drawable arrow) {
		
		this.thumbnail = thumbnail;
		this.title = title;
		this.date = date;
		this.status = status;
		this.description = description;
		this.location = location;
		this.media = media;
		this.id = id;
		this.arrow = arrow;
	}
	
	public ListIncidentText(Uri uri, 
			String title, 
			String date, 
			String status,
			String description, 
			String location,
			String media, 
			String categories, 
			int id, 
			Drawable arrow) {
		
		this.thumbnailUri = uri;
		this.title = title;
		this.date = date;
		this.status = status;
		this.description = description;
		this.location = location;
		this.media = media;
		this.id = id;
		this.arrow = arrow;
	}
	
	public boolean isSelectable() {
		return isSelectable;
	}
	
	public void setSelectable( boolean selectable ){
		isSelectable = selectable;
	}
	
	public void setThumbnail( Drawable thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	public Drawable getThumbnail() {
		return this.thumbnail;
	}
	
	public void setThumbnailUri( Uri uri ) {
		this.thumbnailUri = uri;
	}
	
	public Uri getThumbnailUri() {
		return this.thumbnailUri;
	}
	
	public void setTitle( String title ) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setDate( String date ) {
		
		this.date = date;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public void setStatus( String status ) {
		this.status = status;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public void setDesc( String description ) {
		this.description = description;
	}
	
	public String getDesc() {
		return this.description;
	}
	
	public void setLocation( String location ) {
		this.location = location;
	}
	
	public String getLocation() {
		return this.location;
	}
	
	public void setMedia( String media ) {
		this.media = media;
	}
	
	public String getMedia() {
		return this.media; 
	}
	
	public void setCategories( String categories ) {
		this.categories = categories;
	}
	
	public String getCategories() {
		return this.categories;
	}
	
	public void setId( int id ) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setArrow( Drawable arrow) {
		this.arrow = arrow;
	}
	
	public Drawable getArrow() {
		return this.arrow;
	}
}
