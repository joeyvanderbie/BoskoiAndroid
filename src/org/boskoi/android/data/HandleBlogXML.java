package org.boskoi.android.data;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.boskoi.android.Util;
import org.boskoi.android.data.BlogData;

public class HandleBlogXML extends DefaultHandler {
        private final static String TAG_ITEM = "item";
        private final static String[] xmltags = { "title", "link", "pubDate", "description" };
        
        private BlogData currentitem = null;
        private ArrayList<BlogData> itemarray = null;
        private int currentindex = -1;
        private boolean isParsing = false;
        private StringBuilder builder = new StringBuilder();
        
        public HandleBlogXML(ArrayList<BlogData> itemarray) {
                super();
                
                this.itemarray = itemarray;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
                super.characters(ch, start, length);
                
                if(isParsing && -1 != currentindex && null != builder)
                {
                        builder.append(ch,start,length);
                }
        }
                
        @Override
        public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {
                super.startElement(uri, localName, qName, attributes);
                
                if(localName.equalsIgnoreCase(TAG_ITEM))
                {
                        currentitem = new BlogData();
                        currentindex = -1;
                        isParsing = true;
                        
                        itemarray.add(currentitem);
                }
                else
                {
                        currentindex = itemIndexFromString(localName);
                        
                        builder = null;
                        
                        if(-1 != currentindex)
                                builder = new StringBuilder();
                }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
                super.endElement(uri, localName, qName);
                
                if(localName.equalsIgnoreCase(TAG_ITEM))
                {
                        isParsing = false;
                }
                else if(currentindex != -1)
                {
                        if(isParsing)
                        {
                                switch(currentindex)
                                {
                                        case 0: 
                                        		currentitem.setTitle(builder.toString());                 
                                        break; 
                                        case 1: 
                                        		currentitem.setLink(builder.toString());                  
                                        break;
                                        case 2: 
                                        		currentitem.setDate( 
                                        							Util.formatDate("EEE, dd MMM yyyy HH:mm:ss Z", 
                                        												builder.toString(),
                                        												Util.dateFormat)
                    												);                  
                                        		//04-16 13:46:31.437: W/System.err(18954): java.text.ParseException: Unparseable date: "13 Oct 2011 08:02:00 -0700" (at offset 3)

                                        break;
                                        case 3: 
                                        		currentitem.setDescription(builder.toString());    
                                        break;
                                }
                        }
                }
        }

        private int itemIndexFromString(String tagname){
                int itemindex = -1;

                for(int index= 0; index<xmltags.length; ++index)
                {
                        if(tagname.equalsIgnoreCase(xmltags[index]))
                        {
                                itemindex = index;
                                
                                break;
                        }
                }
                
                return itemindex;
        }
}