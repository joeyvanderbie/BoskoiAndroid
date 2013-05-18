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

public class CategoriesLangData {
	
	private int categoryId = 0;
	private int categoryLangId = 0;
	private String categoryTitle = "";
	private String categoryDescription = "";
	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getCategoryLangId() {
		return categoryLangId;
	}

	public void setCategoryLangId(int categoryLangId) {
		this.categoryLangId = categoryLangId;
	}

	public String getCategoryTitle() {
		return categoryTitle;
	}

	public void setCategoryTitle(String categoryTitle) {
		this.categoryTitle = categoryTitle;
	}

	public String getCategoryDescription() {
		return categoryDescription;
	}

	public void setCategoryDescription(String categoryDescription) {
		this.categoryDescription = categoryDescription;
	}

	private String categoryLocale = "";
	
	
	public String getCategoryLocale() {
		return categoryLocale;
	}

	public void setCategoryLocale(String categoryLocale) {
		this.categoryLocale = categoryLocale;
	}

}
