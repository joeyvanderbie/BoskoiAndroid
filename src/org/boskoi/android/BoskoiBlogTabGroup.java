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

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BoskoiBlogTabGroup extends ActivityGroup {
	public static BoskoiBlogTabGroup group = null;
	private ArrayList<View> history;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.history = new ArrayList<View>();
		group = this;

		View view = getLocalActivityManager().startActivity(
				"BoskoiBlog",
				new Intent(this, BoskoiBlog.class)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
				.getDecorView();

		replaceView(view);

	}

	public void replaceView(View v) {
		// Adds the old one to history
		history.add(v);
		// Changes this Groups View to the new View.
		setContentView(v);
	}

	public void back() {
		if (history.size() > 1) {
			history.remove(history.size() - 1);
			setContentView(history.get(history.size() - 1));
		} else {
			finish();
		}
	}

	public void onBackPressed() {
		BoskoiBlogTabGroup.group.back();
		return;
	}

	@Override
	public void onResume() {
		super.onResume();

	}

}
