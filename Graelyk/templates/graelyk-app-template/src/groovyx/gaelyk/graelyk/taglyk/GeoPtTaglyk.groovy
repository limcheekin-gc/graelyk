package groovyx.gaelyk.graelyk.taglyk

import com.google.appengine.api.datastore.GeoPt
import groovyx.gaelyk.graelyk.util.ObjectUtils;
import org.sil.iso639_3.LanguageByCode

class GeoPtTaglyk
{
    /**
     * A simple GeoPt picker that renders a GeoPt as two text input fields.
     * eg. <% g_geoPtPicker name="myGeoPt" value="${new GeoPt(0.0, 0.0)}" %>
     */
    static g_geoPtPicker(script, attrs){script.out << script.geoPtPicker(attrs)}
    static geoPtPicker(script, attrs)
    {
		def out = new StringWriter()
		def value = attrs['value']
		def lat = ""
		def lon = ""
		def name = attrs['name']
        def id = attrs['id'] ? attrs['id'] : name
        		
		if(value && value.toString() == 'none')
		{
			value = null
		}
		else
		{
			value = script.castingRegistry.cast(value, GeoPt, (attrs.remove("property"))?.transformOnDisplay)
		}

		if(value instanceof GeoPt)
		{
			lat = value.getLatitude()
			lon = value.getLongitude()
		}
		
		out.println("<input type=\"hidden\" name=\"${name}\" value=\"geoPt.STRUCT\" />")
		out.println(script.message(code: 'default.geopt.latitude') + " <input type=\"text\" name=\"${name}_latitude\" id=\"${id}_latitude\" value=\"${lat}\">")
		out.println(script.message(code: 'default.geopt.longitude') + " <input type=\"text\" name=\"${name}_longitude\" id=\"${id}_longitude\" value=\"${lon}\">")
		return out.toString()
    }
	
    /**
     * A GeoPt picker that renders a GeoPt as two text input fields and a Google Maps interface.
     * eg. <% g_geoPtMapPicker name="myGeoPt" value="${new GeoPt(0.0, 0.0)}" %>
     */
	static g_geoPtMapPicker(script, attrs=[:]){script.out << script.geoPtMapPicker(attrs)}
    static geoPtMapPicker(script, attrs=[:])
	{
    	def out = new StringBuffer()
    	script.geoPtPickerAttributes = attrs
		script.wrapVariables(["geoPtPickerAttributes"])
		script.include("/WEB-INF/includes/GeoPtPicker.gtpl")
		out << script.request.includeReturn
		return out.toString()
    }
    
    
    static g_localizedGoogleMapsURL(script, attrs){script.out << script.localizedGoogleMapsURL(attrs)}
    static String localizedGoogleMapsURL(script, attrs=[:])
    {
		//language: (Locale - defaults to userLocale)
		//country: (Locale - defaults to userLocale)
    	
		//Find the first Locale in the Locale list that has a 2-letter ISO639-1 language code compatible with the Google API
		def language = attrs.language ? attrs.remove("language") : script.userLocale
		def langCountry
		if(!ObjectUtils.isListOrArray(language))
		{
			language = [language]
		}
		for(lang in language)
		{
			langCountry = ""
			if(lang instanceof Locale)
			{
				langCountry = lang.getCountry()
				lang = lang.getLanguage()
			}
			else if(lang instanceof String)
			{
				if(lang.contains("_"))
				{
					def variant
					(lang, langCountry, variant) = lang.split("_").toList()
				}
			}
			lang = LanguageByCode.get(lang)?.getPart1Code()
			if(lang)
			{
				language = lang
				break
			}
		}
		
		//Find the first Locale in the Locale list that has a 2-letter country code compatible with the Google API
		def country = attrs.country ? attrs.remove("country") : langCountry
		if(!ObjectUtils.isListOrArray(country))
		{
			country = [country]
		}
		for(c in country)
		{
			if(c instanceof Locale)
			{
				c = c.getCountry()
			}
			else if(c instanceof String)
			{
				if(c.contains("_"))
				{
					def lang
					def variant
					(lang, c, variant) = c.split("_").toList()
				}
			}
			if(c)
			{
				country = c
				break
			}
		}
		
		return "http://maps.google.com/maps/api/js?sensor=false&language=${language}&region=${country}"
    }
    
    
    /**
     * Renders a GeoPt as Google Maps with a marker.
     * eg. <% g_formatGeoPtMap name="myGeoPt" value="${new GeoPt(0.0, 0.0)}" %>
     */
	static g_formatGeoPtMap(script, attrs=[:]){script.out << script.formatGeoPtMap(attrs)}
    static formatGeoPtMap(script, attrs=[:])
	{
    	def out = new StringBuffer()
    	attrs.enabled = false
    	attrs.showButtons = false
    	attrs.showSelect = false
    	script.geoPtPickerAttributes = attrs
		script.wrapVariables(["geoPtPickerAttributes"])
		script.include("/WEB-INF/includes/GeoPtPicker.gtpl")
		out << script.request.includeReturn
		return out.toString()
    }
}
