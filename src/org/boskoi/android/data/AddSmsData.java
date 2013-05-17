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

public class AddSmsData {
	private String addSmsUsername;
	private String addSmsPassword;
	private String addSmsMessageFrom;
	private String addSmsMessageDescription;
	private String addSmsMessageDate;
	
	public AddSmsData() {
		addSmsUsername = "";
		addSmsPassword = "";
		addSmsMessageFrom = "";
		addSmsMessageDescription = "";
		addSmsMessageDate = "";
	}
	
	public String getSmsUsername() {
		return this.addSmsUsername;
	}
	
	public void setSmsUsername( String smsUsername) {
		this.addSmsUsername = smsUsername;
	}
	
	public String getSmsPassword() {
		return addSmsPassword;
	}
	
	public void setSmsPassword( String smsPassword ) {
		this.addSmsPassword =  smsPassword;
	}
	
	public String getSmsMessageFrom() {
		return this.addSmsMessageFrom;
	}
	
	public void setSmsMessageFrom( String smsMessageFrom ) {
		this.addSmsMessageFrom = smsMessageFrom;
	}
	
	public String getSmsMessageDescription() {
		return this.addSmsMessageDescription;
	}
	
	public void setSmsMessageDescription( String smsMessageDescription ) {
		this.addSmsMessageDescription = smsMessageDescription;
	}
	
	public String getSmsMessageDate() {
		return this.addSmsMessageDate;
	}
	
	public void setSmsMessageDate( String smsMessageDate ) {
		this.addSmsMessageDate = smsMessageDate;
	}

}
