
package groovyx.gaelyk.graelyk.util

import groovyx.gaelyk.GaelykCategory
import groovyx.gaelyk.graelyk.GraelykCategory

import groovyx.gaelyk.graelyk.StaticResourceHolder

import groovyx.gaelyk.graelyk.cast.KeyEditor
import groovyx.gaelyk.graelyk.cast.StructuredDateEditor

import java.util.Locale
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.text.NumberFormat
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;

import com.googlecode.objectify.Key

import com.google.appengine.api.datastore.Blob
import com.google.appengine.api.datastore.Category
import com.google.appengine.api.datastore.Email
import com.google.appengine.api.datastore.GeoPt
import com.google.appengine.api.datastore.IMHandle
//import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.Link
import com.google.appengine.api.datastore.PhoneNumber
import com.google.appengine.api.datastore.PostalAddress
import com.google.appengine.api.datastore.Rating
import com.google.appengine.api.datastore.ShortBlob
import com.google.appengine.api.datastore.Text
import com.google.appengine.api.users.User

class CastingUtils
{
	public static castParam(Class propertyType, Class genericType, Object value)
	{
		return castParam(propertyType, genericType, value, Locale.getDefault())
	}
	
	public static castParam(Class propertyType, Class genericType, Object value, Locale locale)
	{
		use(GaelykCategory, GraelykCategory)
		{
			//Only allow casting from a String
			if(value != null && value.class != String && propertyType == String){return value}

			//Don't try to cast if the source and destination class are the same
			if(value != null && propertyType.isAssignableFrom(value.class)){return value}
	
			NumberFormat floatFormat = NumberFormat.getInstance(locale);
	        NumberFormat integerFormat = NumberFormat.getIntegerInstance(locale);
	        DateFormat dateFormat = new SimpleDateFormat(StaticResourceHolder.appProperties["defaultDateFormat"], locale);
	
			try
			{
				if(String.class.isAssignableFrom(propertyType)){return value}
				
				else if(com.google.appengine.api.datastore.Category.isAssignableFrom(propertyType))
				{
					return value as com.google.appengine.api.datastore.Category
				}
				//else if(com.google.appengine.api.datastore.Category.isAssignableFrom(propertyType)){return new com.google.appengine.api.datastore.Category(value)}
				else if(com.google.appengine.api.datastore.Email.isAssignableFrom(propertyType)){return value as Email}
				//else if(com.google.appengine.api.datastore.GeoPt.isAssignableFrom(propertyType)){return value.split(",").toList().collect{Float.parseFloat(it.trim())} as GeoPt}
				else if(com.google.appengine.api.datastore.GeoPt.isAssignableFrom(propertyType)){return value as GeoPt} //This relies on a modification to GaelykCategory
				else if(com.google.appengine.api.datastore.Link.isAssignableFrom(propertyType)){return value as Link}
				else if(com.google.appengine.api.datastore.PhoneNumber.isAssignableFrom(propertyType)){return value as PhoneNumber}
				else if(com.google.appengine.api.datastore.PostalAddress.isAssignableFrom(propertyType)){return value as PostalAddress}
				else if(com.google.appengine.api.datastore.Rating.isAssignableFrom(propertyType))
				{
					return value as Rating
				}
				else if(com.google.appengine.api.datastore.Text.isAssignableFrom(propertyType)){return value as Text}
	
				
				else if(Date.class.isAssignableFrom(propertyType)){def editor = new CustomDateEditor(dateFormat,true); editor.setAsText(value); return editor.getValue() }
				else if(BigDecimal.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(BigDecimal.class, floatFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(BigInteger.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(BigInteger.class, floatFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(Double.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Double.class, floatFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(double.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Double.class, floatFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(Float.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Float.class, floatFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(float.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Float.class, floatFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(Long.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Long.class, integerFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(long.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Long.class, integerFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(Integer.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Integer.class, integerFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(int.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Integer.class, integerFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(Short.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Short.class, integerFormat, true); editor.setAsText(value); return editor.getValue()}
				else if(short.class.isAssignableFrom(propertyType)){def editor = new CustomNumberEditor(Short.class, integerFormat, true); editor.setAsText(value); return editor.getValue()}
				//else if(Date.class.isAssignableFrom(propertyType)){def editor = new StructuredDateEditor(dateFormat,true); editor.setAsText(value); return editor.getValue()}
				else if(Calendar.class.isAssignableFrom(propertyType)){def editor = new StructuredDateEditor(dateFormat,true); editor.setAsText(value); return editor.getValue()}
				else if(Key.class.isAssignableFrom(propertyType))
				{
					def editor = new KeyEditor();
					editor.setAsText(genericType, value);
					return editor.getValue()
				}
				else {return value}
			}
			catch(Exception e)
			{
				//Todo: add an error to the errors property
				return null
			}
		}
	}
}
