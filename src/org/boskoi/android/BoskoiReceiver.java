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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BoskoiReceiver extends BroadcastReceiver {

  public static final String ACTION_REFRESH_EARTHQUAKE_ALARM = "com.boskoi.android.app.ushahidi.ACTION_UPDATE_USHAHIDI_ALARM"; 
	
  @Override
  public void onReceive(Context context, Intent intent) {
    Intent startIntent = new Intent(context, BoskoiService.class);
    context.startService(startIntent);
	}
}
