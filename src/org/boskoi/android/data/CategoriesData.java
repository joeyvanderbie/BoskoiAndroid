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

package org.boskoi.android.data;

public class CategoriesData {
	
	private int categoryId = 0;
	private int categoryParentId = 0;
	private String categoryTitle = "";
	private String categoryDescription = "";
	private String categoryColor = "";
	//private String categoryTitleNL = "";
	private String categoryTitleLA = "";
	private String categoryLocale = "";
	
	
	public String getCategoryLocale() {
		return categoryLocale;
	}

	public void setCategoryLocale(String categoryLocale) {
		this.categoryLocale = categoryLocale;
	}

//	public String getCategoryTitleNL() {
//		return categoryTitleNL;
//	}
//
//	public void setCategoryTitleNL(String categoryTitleNL) {
//		if(categoryTitleNL != null){
//			this.categoryTitleNL = categoryTitleNL;
//		}
//	}
//
	public String getCategoryTitleLA() {
		return categoryTitleLA;
	}

	public void setCategoryTitleLA(String categoryTitleLA) {
		if(categoryTitleLA != null){
			this.categoryTitleLA = categoryTitleLA;
		}
	}

	
	public String getCategoryTitle() {
		return categoryTitle;
	}

	public void setCategoryTitle(String title ) {
		this.categoryTitle = title;
	}

	
	public String getCategoryDescription() {
		return categoryDescription;
	}
	
	public void setCategoryDescription( String description ) {
		this.categoryDescription = description;
	}
	
	public int getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId( int id ) {
		this.categoryId = id;
	}
	
	public String getCategoryColor() {
		return categoryColor;
	}
	
	public void setCategoryColor( String color ) {
		this.categoryColor = color;
	}
	
	public int getCategoryParentId() {
		return categoryParentId;
	}

	public void setCategoryParentId(int categoryParentId) {
		this.categoryParentId = categoryParentId;
	}
}
