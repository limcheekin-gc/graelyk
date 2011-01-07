package groovyx.gaelyk.graelyk.cast

import groovyx.gaelyk.GaelykCategory
import groovyx.gaelyk.graelyk.GraelykCategory
import groovyx.gaelyk.graelyk.StaticResourceHolder
import groovyx.gaelyk.graelyk.util.FileHolder
import groovyx.gaelyk.obgaektify.ObgaektifyCategory;

import com.googlecode.objectify.Key
//import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.*
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.codehaus.groovy.runtime.GStringImpl

class CastingRegistry extends HashMap<CastingSignature, Closure>
{
	List locales
	Locale numberLocale
	Locale currencyLocale
	Locale dateLocale
	NumberFormat floatFormat
	NumberFormat integerFormat
	DateFormat dateFormat
	
	CastingRegistry(Locale loc = [Locale.getDefault()])
	{
		super()
		setLocales([loc], loc, loc, loc)
	}
	
	CastingRegistry(Locale loc, Locale numLoc, Locale curLoc, Locale dateLoc)
	{
		super()
		setLocales([loc], numLoc, curLoc, dateLoc)
	}
	
	CastingRegistry(List loc, Locale numLoc, Locale curLoc, Locale dateLoc)
	{
		super()
		setLocales(loc, numLoc, curLoc, dateLoc)
	}
	
	public void setLocales(List loc, Locale numLoc, Locale curLoc, Locale dateLoc)
	{
		locales = loc
		numberLocale = numLoc
		currencyLocale = curLoc
		dateLocale = dateLoc
		floatFormat = NumberFormat.getInstance(numberLocale);
		integerFormat = NumberFormat.getIntegerInstance(numberLocale);
		try
		{
			dateFormat = new SimpleDateFormat(StaticResourceHolder.appProperties["defaultDateFormat"], dateLocale);
		}
		catch(Exception e)
		{
			//When this class is being run by the graelyk create-views script, the StaticResourceHolder is not accessible and will throw an error.
			dateFormat = new SimpleDateFormat("yyyy-mm-dd", dateLocale)
		}
	}
	
	public static CastingRegistry createDefaultRegistry(Locale locale)
	{
		return CastingRegistry.createDefaultRegistry([locale], locale, locale, locale)
	}
	
	public static CastingRegistry createDefaultRegistry(List locales, Locale numberLocale, Locale currencyLocale, Locale dateLocale)
	{
		def cr = new CastingRegistry(locales, numberLocale, currencyLocale, dateLocale)

		//cr.put(new CastingSignature(, )){from, to, value-> return value}
		//cr.put(new CastingSignature(, )){from, to, generic, value-> return value}
		
		//*************************
		//Java numeric classes
		//*************************
		
		//BigDecimal
		cr.put(new CastingSignature(String, BigDecimal.class)){from, to, value-> def editor = new CustomNumberEditor(BigDecimal.class, cr.floatFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(BigDecimal, String)){from, to, value-> return value.toString()}
		
		//BigInteger
		cr.put(new CastingSignature(String, BigInteger.class)){from, to, value-> def editor = new CustomNumberEditor(BigInteger.class, cr.floatFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(BigInteger, String)){from, to, value-> return value.toString()}
		
		//Double
		cr.put(new CastingSignature(String, Double.class)){from, to, value-> def editor = new CustomNumberEditor(Double.class, cr.floatFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(String, double.class)){from, to, value-> def editor = new CustomNumberEditor(Double.class, cr.floatFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(Double, String)){from, to, value-> return value.toString()}
		cr.put(new CastingSignature(double.class, String)){from, to, value-> return value.toString()}
		
		//Float
		cr.put(new CastingSignature(String, Float.class)){from, to, value-> def editor = new CustomNumberEditor(Float.class, cr.floatFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(String, float.class)){from, to, value-> def editor = new CustomNumberEditor(Float.class, cr.floatFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(Float, String)){from, to, value-> return value.toString()}
		cr.put(new CastingSignature(float.class, String)){from, to, value-> return value.toString()}
		
		//Long
		cr.put(new CastingSignature(String, Long.class)){from, to, value-> def editor = new CustomNumberEditor(Long.class, cr.integerFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(String, long.class)){from, to, value-> def editor = new CustomNumberEditor(Long.class, cr.integerFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(Long, String)){from, to, value-> return value.toString()}
		cr.put(new CastingSignature(long.class, String)){from, to, value-> return value.toString()}
		
		//Integer
		cr.put(new CastingSignature(String, Integer)){from, to, value-> def editor = new CustomNumberEditor(Integer, cr.integerFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(String, int.class)){from, to, value-> def editor = new CustomNumberEditor(Integer.class, cr.integerFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(Integer, String)){from, to, value-> return value.toString()}
		cr.put(new CastingSignature(int.class, String)){from, to, value-> return value.toString()}
		
		//Short
		cr.put(new CastingSignature(String, Short.class)){from, to, value-> def editor = new CustomNumberEditor(Short.class, cr.integerFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(String, short.class)){from, to, value-> def editor = new CustomNumberEditor(Short.class, cr.integerFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(Short, String)){from, to, value-> return value.toString()}
		cr.put(new CastingSignature(short.class, String)){from, to, value-> return value.toString()}
		
		//Boolean
		cr.put(new CastingSignature(String, Boolean.class)){from, to, value->  def editor = new BooleanEditor(); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(String, boolean.class)){from, to, value-> def editor = new BooleanEditor(); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(Boolean, String)){from, to, value-> return value.toString()}
		cr.put(new CastingSignature(boolean.class, String)){from, to, value-> return value.toString()}

		//*************************
		//Other Java classes
		//*************************
		
		//Calendar
		cr.put(new CastingSignature(String, Calendar)){from, to, value-> def editor = new StructuredDateEditor(cr.dateFormat,true); editor.setAsText(value); return editor.getValue()}
		
		//Date - why not use StructuredDateEditor instead?
		cr.put(new CastingSignature(String, Date)){from, to, value-> def editor = new CustomDateEditor(cr.dateFormat, true); editor.setAsText(value); return editor.getValue()}
		cr.put(new CastingSignature(Date, String)){from, to, value-> def editor = new CustomDateEditor(cr.dateFormat, true); editor.setValue(value); return editor.getAsText()}
		
		//Locale
		cr.put(new CastingSignature(String, Locale)){from, to, value-> def editor = new LocaleEditor(); editor.setAsText(value.toString()); return editor.getValue()}
		cr.put(new CastingSignature(Locale, String)){from, to, value-> return value.toString()}

		//Currency
		cr.put(new CastingSignature(String, Currency)){from, to, value-> def editor = new CurrencyEditor(); editor.setAsText(value.toString()); return editor.getValue()}
		cr.put(new CastingSignature(Currency, String)){from, to, value-> value.toString()}
		
		//TimeZone
		cr.put(new CastingSignature(String, TimeZone)){from, to, value-> def editor = new TimeZoneEditor(); editor.setAsText(value.toString()); return editor.getValue()}
		cr.put(new CastingSignature(TimeZone, String)){from, to, value-> return value.toString()}
		
		//Enum
		cr.put(new CastingSignature(String, Enum)){from, to, value-> return Enum.valueOf(to, value)}
		cr.put(new CastingSignature(Enum, String)){from, to, value-> return value.toString()}
		
		//*************************
		//Google App Engine classes
		//*************************
		
		//Key - 
		/* This translated to a "user friendly" string. Replaced in favor of keyToString and stringToKey
		cr.put(new CastingSignature(Long, Key, Object)){from, to, generic, value->
			def editor = new KeyEditor();
			editor.setAsText(generic, value.toString());
			return editor.getValue()
		}
		cr.put(new CastingSignature(String, Key, Object)){from, to, generic, value->
			def editor = new KeyEditor();
			editor.setAsText(generic, value.toString());
			return editor.getValue()
		}
		cr.put(new CastingSignature(Key, String)){from, to, value->
			def editor = new KeyEditor()
			editor.setValue(value)
			return editor.getAsText()
		}
		*/
		cr.put(new CastingSignature(String, Key, Object)){from, to, generic, value->
			return ObgaektifyCategory.stringToKey(value)
		}
		cr.put(new CastingSignature(Key, String)){from, to, value->
			return ObgaektifyCategory.keyToString(value)
		}
		
		//Blob
		cr.put(new CastingSignature(FileHolder, com.google.appengine.api.datastore.Blob)){from, to, value-> return value.getFile()}
		cr.put(new CastingSignature(com.google.appengine.api.datastore.Blob, FileHolder)){from, to, value-> return new FileHolder(value)}
		
		
		//Category
		cr.put(new CastingSignature(String, com.google.appengine.api.datastore.Category)){from, to, value-> return GaelykCategory.asType(value, com.google.appengine.api.datastore.Category)}
		cr.put(new CastingSignature(com.google.appengine.api.datastore.Category, String)){from, to, value-> return GraelykCategory.asType(value, String)}
		
		//Email
		cr.put(new CastingSignature(String, Email)){from, to, value-> return GaelykCategory.asType(value, Email)}
		cr.put(new CastingSignature(Email, String)){from, to, value-> return GraelykCategory.asType(value, String)}
		
		//GeoPt
		cr.put(new CastingSignature(List, GeoPt)){from, to, value-> System.out.println("CastingSignature(List, GeoPt)"); return GaelykCategory.asType(value, GeoPt)}
		cr.put(new CastingSignature(String, GeoPt)){from, to, value-> System.out.println("CastingSignature(String, GeoPt)"); return GraelykCategory.toGeoPt(value)}
		cr.put(new CastingSignature(GeoPt, String)){from, to, value-> return GraelykCategory.asType(value, String)}
		
		//IMHandle
		cr.put(new CastingSignature(String, IMHandle)){from, to, value-> return GaelykCategory.asType(value, IMHandle)}
		cr.put(new CastingSignature(IMHandle, String)){from, to, value-> return GraelykCategory.asType(value, String)}
		
		//Link
		cr.put(new CastingSignature(String, Link)){from, to, value-> return GaelykCategory.asType(value, Link)}
		cr.put(new CastingSignature(Link, String)){from, to, value-> return GraelykCategory.asType(value, String)}
		
		//PhoneNumber
		cr.put(new CastingSignature(String, PhoneNumber)){from, to, value-> return GaelykCategory.asType(value, PhoneNumber)}
		cr.put(new CastingSignature(PhoneNumber, String)){from, to, value-> return GraelykCategory.asType(value, String)}
		
		//PostalAddress
		cr.put(new CastingSignature(String, PostalAddress)){from, to, value-> return GaelykCategory.asType(value, PostalAddress)}
		cr.put(new CastingSignature(PostalAddress, String)){from, to, value-> return GraelykCategory.asType(value, String)}
		
		//Rating
		cr.put(new CastingSignature(String, Rating)){from, to, value-> return GaelykCategory.asType(value, Rating)}
		cr.put(new CastingSignature(Rating, String)){from, to, value-> return GraelykCategory.asType(value, String)}

		//Text
		cr.put(new CastingSignature(String, Text)){from, to, value-> return GaelykCategory.asType(value, Text)}
		cr.put(new CastingSignature(Text, String)){from, to, value-> return GraelykCategory.asType(value, String)}

		return cr
	}
	
	
	public Object cast(Object value, Class toClass)
	{
		return cast(value, toClass, null)
	}
	
	public Object cast(Object value, Class toClass, Class genericClass)
	{
		if(value == null){return null}
		Class fromClass = value.class
		
		//If the "cast" is from and to the same class, and there is no genericClass, simply return the original value
		if(fromClass == toClass && genericClass == null)
		{
			return value
		}
		
		//Look for a casting closure that exactly matches the CastingSignature
		def cs = new CastingSignature(fromClass, toClass, genericClass)
		def castClosure = this[cs]
		            
		//If there was no exact match for the CastingSignature, look for a CastingSignature
		//where the fromClass, toClass, and genericClass are each assignable from a CastingSignature
		//in this CastingRegistry
		if(castClosure == null)
		{
			def keySet = this.keySet()
			for(csign in keySet)
			{
				if(csign.from.isAssignableFrom(fromClass) && csign.to.isAssignableFrom(toClass)
						&& (csign.generic == genericClass || csign.generic.isAssignableFrom(genericClass)))
				{
					castClosure = this[csign]
					break
				}
			}
		}
		
		//If a casting closure has been found, use it
		if(castClosure != null)
		{	
			if(genericClass != null)
			{
				return castClosure.call(fromClass, toClass, genericClass, value)
			}
			else
			{
				return castClosure.call(fromClass, toClass, value)
			}
		}
		//If no casting closure was found, try to use default Java/Groovy casting
		else
		{
			try
			{
				//See if a default Java/Groovy cast is available
				if(genericClass != null)
				{
					value = value.asType(genericClass)
				}
				else
				{
					value = value.asType(toClass)
				}
			}
			catch(Exception e)
			{
				def sb = new StringBuffer("")
				this.each{sign, val->
					sb << sign.toString() + "\r\n"
				}
				throw new IllegalArgumentException("No method of casting from $fromClass to $toClass" + (genericClass?"<$genericClass>":"") + " has been defined." +
						"\r\n\r\n${sb.toString()}")		
			}
		}
	}
	
	public Object cast(Object value, Class toClass, Closure transformClosure)
	{
		return cast(value, toClass, null, transformClosure)
	}
	
	public Object cast(Object value, Class toClass, Class genericClass, Closure transformClosure)
	{
		if(transformClosure != null)
		{
			value = transformClosure.call(value)
		}
		//Now only cast if value has not been transformed to the desired toClass/genericClass
		if(genericClass == null && value.class.isAssignableFrom(toClass))
		{
			return value
		}
		else if(genericClass != null && value.class.isAssignableFrom(genericClass))
		{
			return value
		}
		//The value has not been transformed to the desired class so try to cast it
		else
		{
			value = cast(value, toClass, genericClass)
		}
		return value
	}
}
