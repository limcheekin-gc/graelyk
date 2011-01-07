/* Copyright 2010 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
//Originally package org.codehaus.groovy.grails.web.binding;
package groovyx.gaelyk.graelyk.cast

import org.apache.commons.lang.StringUtils;
import java.beans.PropertyEditorSupport;
import com.google.appengine.api.datastore.GeoPt
import java.util.*;
import java.text.NumberFormat

/**
* Structured editor for editing GeoPt that takes 2 fields to edit the Latitude and Longitude 
* and constructs a GeoPt instance
*
* @author Jeremy Brown
*/
public class StructuredGeoPtEditor extends PropertyEditorSupport implements StructuredPropertyEditor
{
	NumberFormat floatFormat
	
	public StructuredGeoPtEditor()
	{
		this(NumberFormat.getInstance(Locale.getDefault()))
	}
	
	public StructuredGeoPtEditor(NumberFormat format)
	{
		floatFormat = format
	}
	
   public List getRequiredFields() {
       List requiredFields = new ArrayList();
       requiredFields.add("latitude")
       requiredFields.add("longitude")
       return requiredFields;
   }

   public List getOptionalFields() {
       List optionalFields = new ArrayList();
       return optionalFields;
   }

   public Object assemble(Class type, Map fieldValues) throws IllegalArgumentException
   {
       if (!fieldValues.containsKey("latitude")) {
           throw new IllegalArgumentException("Can't populate a GeoPt without a latitude");
       }
       if (!fieldValues.containsKey("longitude")) {
           throw new IllegalArgumentException("Can't populate a GeoPt without a longitude");
       }

       String latitudeString = (String) fieldValues.get("latitude")
       String longitudeString = (String) fieldValues.get("longitude")
       float latitude
       float longitude

       try
       {
           if(StringUtils.isBlank(latitudeString)) {
               throw new IllegalArgumentException("Can't populate a GeoPt without a latitude");
           }
           else {
        	   latitude = Float.parseFloat(latitudeString);
           }
           
           if(StringUtils.isBlank(longitudeString)) {
               throw new IllegalArgumentException("Can't populate a GeoPt without a longitude");
           }
           else {
        	   longitude = Float.parseFloat(longitudeString);
           }


           GeoPt geo = new GeoPt(latitude, longitude);
           return geo;
       }
       catch(NumberFormatException nfe)
       {
           throw new IllegalArgumentException("Unable to parse structured GeoPt from request for GeoPt [\"+propertyName+\"]\"");
       }
   }

   /*
   private int getIntegerValue(Map values, String name, int defaultValue) throws NumberFormatException {
       if (values.get(name) != null) {
           return Integer.parseInt((String) values.get(name));
       }
       return defaultValue;
   }
   */
}
