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
import android.util.Log;

/**
 * This broadcast receiver is awoke after boot and registers the service that
 * checks for new ushahidi reports.
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "BootReceiver";
  
	public void onReceive(Context context, Intent intent) {
		//Log.i(TAG, "Boskoi BootReceiver is receiving.");
	  	if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {      
	  		//startService(new Intent(BootReceive.this, BoskoiService.class));
	  	}
	}
}
