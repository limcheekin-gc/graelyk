

/* Copyright 2004-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.graelyk.taglyk

import java.math.RoundingMode;
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat
//import org.springframework.context.NoSuchMessageException
//import org.springframework.web.servlet.support.RequestContextUtils as RCU
import groovyx.gaelyk.graelyk.util.ObjectUtils;
import groovyx.gaelyk.graelyk.util.StringUtils
import groovyx.gaelyk.graelyk.util.FileHolder
import groovyx.gaelyk.graelyk.codecs.*
import groovyx.gaelyk.obgaektify.ObgaektifyCategory
import com.google.appengine.api.blobstore.BlobKey
import com.google.appengine.api.datastore.Blob
import com.google.appengine.api.datastore.Rating
import com.googlecode.objectify.Key

 /**
 * The base application tag library for Grails many of which take inspiration from Rails helpers (thanks guys! :)
 * This tag library tends to get extended by others as tags within here can be re-used in said libraries
 *
 * @author Jason Rudolph
 * @author Lari Hotari
 * @since 0.6
 *
 * Created:17-Jan-2006
 */
class FormatTaglyk
{
    static void g_formatValue(script, value, attrs){script.out << script.formatValue(value, attrs)}
	static void g_formatBoolean(script, attrs){script.out << script.formatBoolean(attrs)}
	static void g_formatDate(script, attrs){script.out << script.formatDate(attrs)}
	static void g_formatNumber(script, attrs){script.out << script.formatNumber(attrs)}
	static void g_formatFile(script, attrs){script.out << script.formatFile(attrs)}
	static void g_formatList(script, list, listStart, listJoin, listEnd, wrapEvenIfSingle=false){script.out << script.formatList(list, listStart, listJoin, listEnd, wrapEvenIfSingle)}
	static void g_encodeAs(script, String codec, body){script.out << script.encodeAs(codec, body)}
	static void g_encodeAs(script, Map attrs, body){script.out << script.encodeAs(attrs, body)}

	
	
    /**
     * Formats a given value for output to an HTML page by converting
     * it to a string and encoding it. If the value is a number, it is
     * formatted according to the current user's locale during the
     * conversion to a string.
     */
    static formatValue(script, value, attrs)
    {
		//Todo: Figure out what this does and add similar functionality back in.
		//See the Grails package org.codehaus.groovy.grails.web.binding
		/*
        PropertyEditorRegistry registry = RequestContextHolder.currentRequestAttributes().getPropertyEditorRegistry()
        PropertyEditor editor = registry.getCustomEditor(value.getClass())
        if (editor != null) {
            editor.setValue(value)
            return HTMLCodec.shouldEncode() && !(value instanceof Number) ? editor.asText?.encodeAs("HTML") : editor.asText
        }
		*/
    	
    	//Put single values in a List so we only need code to handle lists/arrays
		if(!ObjectUtils.isListOrArray(value))
		{
			value = [value]
		}

		def escapeHTML = true
		if(attrs.containsKey("escapeHTML")){escapeHTML = attrs.remove("escapeHTML")}
		
		def collection = value.collect{innerValue->
			if(innerValue instanceof Number)
			{
				def pattern = "0"
				if (innerValue instanceof Double || innerValue instanceof Float || innerValue instanceof BigDecimal) {
					pattern = "0.00#####"
				}
				def locale = script.userNumberLocale
				def dcfs = locale ? new DecimalFormatSymbols(locale) : new DecimalFormatSymbols()
				def decimalFormat = new DecimalFormat(pattern, dcfs)
				innerValue = decimalFormat.format(innerValue)
			}
			else if(innerValue instanceof Key)
			{
				innerValue = innerValue.get().toString()
			}
			else if(innerValue instanceof Rating)
			{
				innerValue = innerValue as int
			}
			else
			{
				try
				{
					//Try using the GraelykCategory asType method to convert Google data types like Link and Text to a String
					innerValue = innerValue as String
				}
				catch(Exception e){}
			}
			return innerValue
		}

		//escapeHTML is true by default. To avoid encoding as HTML, set escapeHTML:false
		if(escapeHTML)
		{
			collection = collection.collect{it.toString().encodeAs("HTML")}
		}
		collection = script.formatList(collection, attrs.listStart, attrs.listJoin, attrs.listEnd)

		return collection
    }

	
    /**
      * Outputs the given boolean as the specified text label. If the
      * <code>true</code> and <code>false</code> option are not given,
      * then the boolean is output using the default label.
      *
      * Attributes:
      *
      * boolean - the boolean to output
      * true (optional) - text label for boolean true value
      * false (optional) - text label for boolean false value
      *
      * Examples:
      *
      * <g:formatBoolean boolean="${myBoolean}" />
      * <g:formatBoolean boolean="${myBoolean}" true="True!" false="False!" />
      */
	static String formatBoolean(script, attrs)
	{
		if (!attrs.containsKey("boolean"))
		{
			throw new TaglykException("Tag [formatBoolean] is missing required attribute [boolean]")
		}
		
		def locale = script.resolveLocaleList(attrs.get('locale'), script.userLocale)
		def trueMessage = script.message('boolean.true', [], { script.message('default.boolean.true', [], 'True',  locale) }, locale)
		def falseMessage = script.message('boolean.false', [], { script.message('default.boolean.false', [], 'False',  locale) }, locale)

		def booleans = attrs.get("boolean")
		if(!ObjectUtils.isListOrArray(booleans))
		{
			booleans = [booleans]
		}		
		
		def collection = booleans.collect{b->
			if (b == null)
			{
				return null
			}
			else if(!(b instanceof Boolean))
			{
				b = Boolean.valueOf(b)
			}
			
			if(b)
			{
				return attrs["true"] ?: trueMessage
			}
			else
			{
				return attrs["false"] ?: falseMessage
			}
		}
		
		return script.formatList(collection, attrs.listStart, attrs.listJoin, attrs.listEnd)
	}

    /**
     * Outputs the given <code>Date</code> object in the specified format.  If
     * the <code>date</code> is not given, then the current date/time is used.
     * If the <code>format</code> option is not given, then the date is output
     * using the default format.
     *
     * e.g., <g:formatDate date="${myDate}" format="yyyy-MM-dd HH:mm" />
     *
     * @see java.text.SimpleDateFormat
     */
    static String formatDate(script, attrs)
	{    
    	def date
    	if (attrs.containsKey('date'))
		{
        	date = attrs.get('date')
        	if(date == null) return null
    	}
    	else
		{
            date = new Date()
    	}
    	
    	//If date is a single value, turn it into a list
    	if(!(ObjectUtils.isListOrArray(date)))
    	{
    		date = [date]
    	}

    	def locale = script.resolveLocale(attrs.get('locale'), script.userDateLocale)
    	def timeStyle = null
    	def dateStyle = null
    	if(attrs.get('style') != null)
		{
    		def style=attrs.get('style').toString().toUpperCase()
    		timeStyle = style
    		dateStyle = style
    	}
    	if(attrs.get('dateStyle') != null)
		{
    		dateStyle=attrs.get('dateStyle').toString().toUpperCase()
    	}
    	if(attrs.get('timeStyle') != null)
		{
    		timeStyle=attrs.get('timeStyle').toString().toUpperCase()
    	}
    	def type = attrs.get('type')?.toString()?.toUpperCase()
        def formatName = attrs.get('formatName')
        def format = attrs.get('format')
        def timeZone = attrs.get('timeZone')
        if(timeZone!=null)
		{
        	if(!(timeZone instanceof TimeZone))
			{
        		timeZone = TimeZone.getTimeZone(timeZone as String)
        	} 
        }
		else
		{
        	timeZone = TimeZone.getDefault()
        }
        
        def dateFormat
        if(!type)
		{
	        if(!format && formatName)
			{
	            format = script.message(formatName,[],null,locale)
	            if(!format)
				{
					throw new TaglykException("Attribute [formatName] of Tag [formatDate] specifies a format key [$formatName] that does not exist within a message bundle!")
				}
	        }
	        else if (!format)
			{
	            format = script.message('date.format', [], { script.message('default.date.format', [], 'yyyy-MM-dd HH:mm:ss z', locale) }, locale)
	        }
	        dateFormat = FastDateFormat.getInstance(format, timeZone, locale)
        }
		else
		{
        	if(type=='DATE')
			{
    	        dateFormat = FastDateFormat.getDateInstance(parseStyle(dateStyle), timeZone, locale)
        	}
			else if (type=='TIME')
			{
        		dateFormat = FastDateFormat.getTimeInstance(parseStyle(timeStyle), timeZone, locale)
        	}
			else
			{
				// 'both' or 'datetime'
        		dateFormat = FastDateFormat.getDateTimeInstance(parseStyle(dateStyle), parseStyle(timeStyle), timeZone, locale)
        	}
        }
        
        def collection = date.collect{dateFormat.format(it)}
        return script.formatList(collection, attrs.listStart, attrs.listJoin, attrs.listEnd)
    }

     static parseStyle(styleStr) 
	 {
      	def style=FastDateFormat.SHORT
    	if(styleStr=='FULL') {
    		style=FastDateFormat.FULL
    	} else if (styleStr=='LONG') {
    		style=FastDateFormat.LONG
    	} else if (styleStr=='MEDIUM') {
    		style=FastDateFormat.MEDIUM
    	}
      	return style
     }
     
    /**
     * Outputs the given number in the specified format.  If the
     * <code>format</code> option is not given, then the number is output
     * using the default format.
     *
     * e.g., <g:formatNumber number="${myNumber}" format="###,##0" />
     *
     * @see java.text.DecimalFormat
     */
    static String formatNumber(script, attrs)
	{
		if (!attrs.containsKey('number'))
		{
			throw new TaglykException("Tag [formatNumber] is missing required attribute [number]")
		}
		
    	def number = attrs.get('number')
    	if (number == null) return null

        def formatName = attrs.get('formatName')
        def format = attrs.get('format')
        def type = attrs.get('type')
        def locale = script.resolveLocale(attrs.get('locale'), script.userNumberLocale)
        
        if(type==null)
		{
	        if(!format && formatName)
			{
	            format = script.message(formatName,[],null,locale)
	            if(!format)
				{
					throw new TaglykException("Attribute [formatName] of Tag [formatNumber] specifies a format key [$formatName] that does not exist within a message bundle!")
				}
	        }
	        else if (!format)
			{
	            format = script.message( "number.format", [], { script.message( "default.number.format", [], "0", locale) }, locale)
	        }
        }

        DecimalFormatSymbols dcfs = locale ? new DecimalFormatSymbols( locale ) : new DecimalFormatSymbols()

        DecimalFormat decimalFormat
        if(!type)
		{
        	decimalFormat = new java.text.DecimalFormat( format, dcfs )
        }
		else
		{
        	if(type=='currency')
			{
        		decimalFormat = NumberFormat.getCurrencyInstance(locale)
            }
			else if (type=='number')
			{
        		decimalFormat = NumberFormat.getNumberInstance(locale)
        	}
			else if (type=='percent')
			{
        		decimalFormat = NumberFormat.getPercentInstance(locale)
        	}
			else
			{
        		throw new TaglykException("Attribute [type] of Tag [formatNumber] specifies an unknown type. Known types are currency, number and percent.")
        	}
        }

        // ensure formatting accuracy
        decimalFormat.setParseBigDecimal(true)

        if(attrs.get('currencyCode') != null)
		{
        	Currency currency=Currency.getInstance(attrs.get('currencyCode') as String)
        	decimalFormat.setCurrency(currency)
        }
        if(attrs.get('currencySymbol') != null)
		{
        	dcfs = decimalFormat.getDecimalFormatSymbols()
        	dcfs.setCurrencySymbol(attrs.get('currencySymbol') as String)
        	decimalFormat.setDecimalFormatSymbols(dcfs)
        }
        if(attrs.get('groupingUsed') != null)
		{
			if(attrs.get('groupingUsed') instanceof Boolean)
			{
				decimalFormat.setGroupingUsed(attrs.get('groupingUsed'))
			}
			else
			{
				// accept true, y, 1, yes
				decimalFormat.setGroupingUsed(attrs.get('groupingUsed').toString().toBoolean() || attrs.get('groupingUsed').toString()=='yes')
			}
        }
        if(attrs.get('maxIntegerDigits') != null)
		{
        	decimalFormat.setMaximumIntegerDigits(attrs.get('maxIntegerDigits') as Integer)
        }
        if(attrs.get('minIntegerDigits') != null)
		{
        	decimalFormat.setMinimumIntegerDigits(attrs.get('minIntegerDigits') as Integer)
        }
        if(attrs.get('maxFractionDigits') != null)
		{
        	decimalFormat.setMaximumFractionDigits(attrs.get('maxFractionDigits') as Integer)
        }
        if(attrs.get('minFractionDigits') != null)
		{
        	decimalFormat.setMinimumFractionDigits(attrs.get('minFractionDigits') as Integer)
        }
        if(attrs.get('roundingMode') != null)
		{
        	def roundingMode=attrs.get('roundingMode')
        	if(!(roundingMode instanceof RoundingMode))
			{
        		roundingMode = RoundingMode.valueOf(roundingMode)
        	}
        	decimalFormat.setRoundingMode(roundingMode)
        }

        if(!(number instanceof Number))
		{
        	number = decimalFormat.parse(number as String)
        }
        
        def formatted
        try
		{
        	formatted=decimalFormat.format(number)
        }
		catch(ArithmeticException e)
		{
        	// if roundingMode is UNNECESSARY and ArithemeticException raises, just return original number formatted with default number formatting
        	formatted=NumberFormat.getNumberInstance(locale).format(number)
        }
        return formatted
    }
    

    //Format a file (Blob) or array/list of files (Blob[] / @Contains(Blob) List<Blob>)
    //for display. Defaults to displaying a link that allows the user to download the file.
    //Images can also be displayed inline.
    static String formatFile(script, attrs)
    {
    	//Attributes:
    	//bean: (required)
    	//field: (required) name of the Blob or Blob[] field
    	//text: text to display for the link to the file
    	//showImage: (default true) if the Blob has metadata showing it is an image, show the image
    	//isImage: (default false) state that the file *is* an image, even if no metadata is available
    	//showLink: (default true) if false, the link to download the file will not be shown (only the text/image)
		def bean = attrs.bean
		def field = attrs.field
		def text = attrs.text
		def showImage = true
		if(attrs.containsKey("showImage")){showImage = attrs.showImage}
		def isImage = attrs.isImage ?: false
		def showLink = true
		if(attrs.containsKey("showLink")){showLink = attrs.showLink}
		
		def property = bean.getPropertyMap()[field]
		def key = ObgaektifyCategory.getKeyString(bean)
			
		def values = bean[field]
		if(!ObjectUtils.isListOrArray(property.type))
		{
			values = [values]
		}
		
		def collection = []
		values.eachWithIndex{val, index->
			def name = ""
			def size = ""
			def type = ""
			if(val instanceof FileHolder)
			{
			    //If the value is a FileHolder, get the file metdata:
				name = val.getFilename()
				type = val.getContentType()
				size = val.getSize() % 1024
			}
			else if(val instanceof Blob)
			{
				//We can get the size from a Blob
				size = val.getBytes().size() % 1024
			}
			else if(val instanceof BlobKey)
			{
				name = val.getFilename()
				type = val.getContentType()
				size = val.getSize() % 1024
				//Downloading from the blobstore uses the BlobKey's key string, not the key string of the domain class object that holds the BlobKey
				key = val.getKeyString()
			}
		
			def queryIndex = (values.size() > 1) ? "&index=$index" : ""
			def linkText = text
			if(!isImage){isImage = (type =~ /image/)}
			if(!linkText)
			{
				linkText = name
				linkText += " (" + size + "KB)"
			}
			
			if(!linkText)
			{
				linkText = script.message("default.file.download.label")
			}
			
			def downloadURL = ""
			if(val instanceof BlobKey)
			{
				downloadURL = "${appProperties['graelyk.download.blobstore.url']}?key=${key}&field=${field}${queryIndex}"
			}
			else
			{
				downloadURL = "${appProperties['graelyk.download.datastore.url']}?key=${key}&field=${field}${queryIndex}"
			}
			
			if(showImage && isImage)
			{
				linkText = """<img src="${downloadURL}" />"""
			}
			
			if(showLink)
			{
				collection << """<a href="${downloadURL}">${linkText}</a>"""
			}
			else
			{
				collection << linkText
			}
		}
		
		return script.formatList(collection, attrs.listStart, attrs.listJoin, attrs.listEnd)
	}
    
    //join an already-formatted list of values
    static formatList(script, list, listStart, listJoin, listEnd, wrapEvenIfSingle=false)
    {
    	if(!list)
    	{
    		return ""
    	}
    	
    	def single = list.size() <= 1
    	
    	if(listJoin == null){listJoin = ", "}
    	if(listStart == null){listStart = ""}
    	if(listEnd == null){listEnd = ""}
    	
    	def out = new StringBuffer()
    	if(!single || wrapEvenIfSingle){out << listStart}
    	out << list.join(listJoin)
    	if(!single || wrapEvenIfSingle){out << listEnd}
    	return out.toString()
    }

	
	static encodeAs(Object object, String codec)
	{
		def codecClass = Class.forName("groovyx.gaelyk.graelyk.codecs.${codec}Codec")
		return codecClass.encode(object.toString())
	}
	
	static encodeAs(script, String codec, body)
	{
		return (script.captureOut(body)).encodeAs(codec)
	}

    static encodeAs(script, Map attrs, body)
	{
        if(!attrs.codec)
		{
            throw new TaglykException("Taglyk [encodeAs] requires a codec name in the [codec] attribute")
		}
		return (script.captureOut(body)).encodeAs(attrs.codec)
    }
}
