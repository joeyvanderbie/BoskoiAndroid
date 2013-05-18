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

import org.boskoi.android.BoskoiApplication;
import org.boskoi.android.BoskoiService;
import org.boskoi.android.ImageManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HandleXml {
	
	public static List<IncidentsData> processIncidentsXml( String xml, Context context ) {

		BoskoiService.loadSettings(context);

		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			doc = builder.parse(new InputSource(new StringReader( xml )));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<IncidentsData> listIncidentsData = new ArrayList<IncidentsData>();
		
		NodeList node = doc.getElementsByTagName("incident");
		String categories = "";
		String media = "";
		
		for( int i = 0; i < node.getLength(); i++ ) {
			
			Node firstNode = node.item(i);
			IncidentsData incidentData = new IncidentsData();
			listIncidentsData.add( incidentData );
			
			if( firstNode.getNodeType() == Node.ELEMENT_NODE ) {
				Element element = (Element) firstNode;
				
				NodeList idElementList = element.getElementsByTagName("id");
				Element idElement = (Element) idElementList.item(0);
				
				NodeList id = idElement.getChildNodes();
		
				incidentData.setIncidentId(Integer.parseInt(((Node) id.item(0)).getNodeValue()));
				
				NodeList dateElementList = element.getElementsByTagName("date");
				Element dateElement = (Element) dateElementList.item(0);
				
				NodeList date = dateElement.getChildNodes();
				incidentData.setIncidentDate(((Node) date.item(0)).getNodeValue());
				
				try{

				NodeList titleElementList = element.getElementsByTagName("title");
				Element titleElement = (Element) titleElementList.item(0);
				
				NodeList title = titleElement.getChildNodes();
				incidentData.setIncidentTitle(((Node) title.item(0)).getNodeValue());
				
				NodeList descElementList = element.getElementsByTagName("description");
				Element descElement = (Element) descElementList.item(0);
				
				NodeList desc = descElement.getChildNodes();
				String descString = "";
				for(int j = 0; j < desc.getLength(); j++){
					descString += ((Node) desc.item(j)).getNodeValue();
				}
				incidentData.setIncidentDesc(descString);			

				
				NodeList modeElementList = element.getElementsByTagName("mode");
				Element modeElement = (Element) modeElementList.item(0);
				
				NodeList mode = modeElement.getChildNodes();
				incidentData.setIncidentMode(Integer.parseInt( ((Node) mode.item(0)).getNodeValue() ));
				
				NodeList verifiedElementList = element.getElementsByTagName("verified");
				Element verifiedElement = (Element) verifiedElementList.item(0);
				
				NodeList verified = verifiedElement.getChildNodes();
				incidentData.setIncidentVerified(Integer.parseInt(((Node) verified.item(0)).getNodeValue()));
				
				//location
				NodeList locationElementList = element.getElementsByTagName
				("location");
				
				Node locationNode = locationElementList.item(0);
				
				Element locationElement = (Element) locationNode;
				NodeList locationNameList = locationElement.getElementsByTagName("name");
				
				Element locationInnerNameElement = (Element) locationNameList.item(0);
				NodeList locationInnerName = locationInnerNameElement.getChildNodes();
				incidentData.setIncidentLocation(((Node) locationInnerName.item(0)).getNodeValue());
				
				NodeList locationLatitudeList = locationElement.getElementsByTagName("latitude");
				
				Element locationInnerLatitudeElement = (Element) locationLatitudeList.item(0);
				NodeList locationInnerLatitude = locationInnerLatitudeElement.getChildNodes();
				incidentData.setIncidentLocLatitude(((Node)locationInnerLatitude.item(0)).getNodeValue());
								
				NodeList locationLongitudeList = locationElement.getElementsByTagName("longitude");
				
				Element locationInnerLongitudeElement = (Element) locationLongitudeList.item(0);
				NodeList locationInnerLongitude = locationInnerLongitudeElement.getChildNodes();
				incidentData.setIncidentLocLongitude(((Node)locationInnerLongitude.item(0)).getNodeValue());
				
				//categories
				NodeList categoryList = element.getElementsByTagName("category");
				for( int w=0; w < categoryList.getLength(); w++ ) { 
					
					Node categoryNode = categoryList.item(w);
					if (categoryNode.getNodeType() == Node.ELEMENT_NODE) {
						Element categoryElement = (Element) categoryNode;
						NodeList categoryNameList = categoryElement.getElementsByTagName("id");
						Element categoryInnerTitleElement = (Element) categoryNameList.item(0);
						NodeList categoryInnerTitle = categoryInnerTitleElement.getChildNodes();
						categories +=  (w == categoryList.getLength() - 1) ?  ((Node)categoryInnerTitle.item(0)).getNodeValue() : ((Node)categoryInnerTitle.item(0)).getNodeValue()+",";
					}
				}
				
				incidentData.setIncidentCategories(categories);
				categories = "";
				
				//categories
				NodeList mediaList = element.getElementsByTagName
				("media");
				for( int j=0; j < mediaList.getLength(); j++ ) { 
					
					Node mediaNode = mediaList.item(j);
					if (mediaNode.getNodeType() == Node.ELEMENT_NODE) {
						Element mediaElement = (Element) mediaNode;
						NodeList mediaThumbList = mediaElement.getElementsByTagName("thumb");
						
						if( mediaThumbList.getLength() != 0) {
						
							Element mediaInnerThumbElement = (Element) mediaThumbList.item(0);
							NodeList mediaThumb = mediaInnerThumbElement.getChildNodes();
							BoskoiService.mNewIncidentsImages.add( ((Node)mediaThumb.item(0)).getNodeValue() );
							
							media += (j == mediaList.getLength() -1)? ( (Node)mediaThumb.item(0)).getNodeValue(): ( (Node)mediaThumb.item(0)).getNodeValue()+",";
						}
					}
				}
				incidentData.setIncidentMedia(media);
				}catch(Exception ex){
					//Handle silently if elements are missing (otherwise it crashes when only requesting deleted items)
					
				}
				
				media = "";
					
			}
			
		}
		

			try {
				node = doc.getElementsByTagName("timestamp");
				Node firstNode = node.item(0);

				Element element = (Element) firstNode;
				NodeList stampElementList = element.getElementsByTagName("updatestamp");

				Element stampElement = (Element) stampElementList.item(0);
				NodeList stamp = stampElement.getChildNodes();
				
				BoskoiService.lastUpdate = ((Node) stamp.item(0)).getNodeValue();
				BoskoiService.saveSettings(context);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		//save images
		ImageManager.saveImage();
		
		return listIncidentsData;
		
		
	}
	
	public static List<CategoriesData> processCategoriesXml( String xml ) {
		
		List<CategoriesData> categoriesData = new ArrayList<CategoriesData>();
		String categories = "";
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			doc = builder.parse(new InputSource(new StringReader( xml )));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NodeList node = doc.getElementsByTagName("category");
		for( int i = 0; i < node.getLength(); i++ ) {
			Node firstNode = node.item(i);
			CategoriesData category = new CategoriesData(); 
			
			
			if( firstNode.getNodeType() == Node.ELEMENT_NODE ) {
				Element element = (Element) firstNode;
				
				NodeList idElementList = element.getElementsByTagName("id");
				Element idElement = (Element) idElementList.item(0);
				
				NodeList id = idElement.getChildNodes();
				category.setCategoryId(Integer.parseInt(((Node) id.item(0)).getNodeValue()));
				
				
				NodeList parentidElementList = element.getElementsByTagName("parentid");
				Element parentidElement = (Element) parentidElementList.item(0);
				NodeList parentid = parentidElement.getChildNodes();
				category.setCategoryParentId(Integer.parseInt(((Node) parentid.item(0)).getNodeValue()));
				
				//category.setCategoryParentId(0);
		
				
				NodeList titleElementList = element.getElementsByTagName("title");
				Element titleElement = (Element) titleElementList.item(0);
				
				NodeList title = titleElement.getChildNodes();
				category.setCategoryTitle(((Node) title.item(0)).getNodeValue());
				categories += ((Node) title.item(0)).getNodeValue()+", ";
				
		
				try{
//				NodeList titleNLElementList = element.getElementsByTagName("titlenl");
//				Element titleNLElement = (Element) titleNLElementList.item(0);
				
//				NodeList titleNL = titleNLElement.getChildNodes();
//				category.setCategoryTitleNL(((Node) titleNL.item(0)).getNodeValue());
//				

				
				NodeList titleLAElementList = element.getElementsByTagName("titlela");
				Element titleLAElement = (Element) titleLAElementList.item(0);
				
				NodeList titleLA = titleLAElement.getChildNodes();
				category.setCategoryTitleLA(((Node) titleLA.item(0)).getNodeValue());
				}catch(Exception ex){
					// do nothing if elements is empty
				}
				
				NodeList localeList = element.getElementsByTagName("locale");
				Element localeElement = (Element) localeList.item(0);
				
				/* TODO temporary fix untill api is fixed*/
				if(localeElement != null){
					NodeList locale = localeElement.getChildNodes();
					category.setCategoryLocale(((Node) locale.item(0)).getNodeValue());
				}else{
					category.setCategoryLocale(Locale.US.toString());
				}
				
				
				NodeList descElementList = element.getElementsByTagName("description");
				Element descElement = (Element) descElementList.item(0);
				
				NodeList desc = descElement.getChildNodes();
				category.setCategoryDescription( ((Node) desc.item(0)).getNodeValue());
				
				NodeList dateElementList = element.getElementsByTagName("color");
				Element dateElement = (Element) dateElementList.item(0);
				
				NodeList date = dateElement.getChildNodes();
				category.setCategoryColor( ((Node) date.item(0)).getNodeValue());
				
			}
			categoriesData.add( category );

		}
		
		return categoriesData;
	}
	
	public static List<CategoriesLangData> processCategoriesLangXml( String xml ) {
		
		List<CategoriesLangData> categoriesLangData = new ArrayList<CategoriesLangData>();
		String categories = "";
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			doc = builder.parse(new InputSource(new StringReader( xml )));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NodeList node = doc.getElementsByTagName("category_lang");
		for( int i = 0; i < node.getLength(); i++ ) {
			Node firstNode = node.item(i);
			CategoriesLangData category = new CategoriesLangData(); 
			
			
			if( firstNode.getNodeType() == Node.ELEMENT_NODE ) {
				Element element = (Element) firstNode;
				
				NodeList idElementList = element.getElementsByTagName("id");
				Element idElement = (Element) idElementList.item(0);
				
				NodeList id = idElement.getChildNodes();
				category.setCategoryId(Integer.parseInt(((Node) id.item(0)).getNodeValue()));

				NodeList langidElementList = element.getElementsByTagName("lang_id");
				Element langidElement = (Element) langidElementList.item(0);
				
				NodeList langid = langidElement.getChildNodes();
				category.setCategoryLangId(Integer.parseInt(((Node) langid.item(0)).getNodeValue()));
				
				NodeList titleElementList = element.getElementsByTagName("title");
				Element titleElement = (Element) titleElementList.item(0);
				
				NodeList title = titleElement.getChildNodes();
				category.setCategoryTitle(((Node) title.item(0)).getNodeValue());
				categories += ((Node) title.item(0)).getNodeValue()+", ";
				
				NodeList localeList = element.getElementsByTagName("locale");
				Element localeElement = (Element) localeList.item(0);
				
					NodeList locale = localeElement.getChildNodes();
					category.setCategoryLocale(((Node) locale.item(0)).getNodeValue());

				
				
				NodeList descElementList = element.getElementsByTagName("description");
				Element descElement = (Element) descElementList.item(0);
				
				NodeList desc = descElement.getChildNodes();
				category.setCategoryDescription( ((Node) desc.item(0)).getNodeValue());
				
			}
			categoriesLangData.add( category );

		}
		
		return categoriesLangData;
	}
	
	protected static ImageManager getImageManager() {
	    return BoskoiApplication.mImageManager;
	}
	
}
