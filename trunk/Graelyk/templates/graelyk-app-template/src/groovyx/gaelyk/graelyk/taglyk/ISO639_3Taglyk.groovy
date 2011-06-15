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
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx.gaelyk.graelyk.taglyk

import java.util.Locale
import groovyx.gaelyk.graelyk.cast.LocaleEditor
import groovyx.gaelyk.graelyk.util.ObjectUtils;
import org.ethnologue.iso639_3.*
import org.sil.iso639_3.*

class ISO639_3Taglyk
{
    static String localizeLanguageName(script, Locale locale)
    {
    	def defaultName = ELanguagesByCountry.getPrimaryLanguage(locale.getCountry(), locale.getLanguage())?.name
    	if(!defaultName)
    	{
    		defaultName = LanguageByCode.get(locale.getLanguage())?.name
    	}
    	def localName = script.message(code:"lang." + locale.getLanguage(), default:defaultName)
		if(localName =~ /\t/){localName = localName.replaceAll(/\t.*$/, "")}
		return localName
    }
	
    static String localizeLanguageNameWithForeignName(script, Locale locale)
    {
    	def defaultName = ELanguagesByCountry.getPrimaryLanguage(locale.getCountry(), locale.getLanguage())?.name
    	if(!defaultName)
    	{
    		defaultName = LanguageByCode.get(locale.getLanguage())?.name
    	}
    	def localName = script.message(code:"lang." + locale.getLanguage(), default:defaultName)
		return localName
    }
	
    static String localizeCountryName(script, Locale locale)
    {
    	def defaultName = locale.getDisplayCountry()
    	return script.message(code:"country." + locale.getCountry(), default:defaultName)
    }
	
	
	//escapeHTML: default true. Whether the HTML should be escaped. This is important since some language names seem to have "<" in them.
	static g_formatISO639_3Locale(script, attrs=[:]){script.out << script.formatISO639_3Locale(attrs)}
	static formatISO639_3Locale(script, attrs=[:])
	{
		//Attributes:
		//locale: (required) Locale (or String) value or List/Array of Locale (or String)
		//appendCountryCode: see note for iso639_3LocaleSelect() method
		//appendLanguageName: see note for iso639_3LocaleSelect() method
		//showCountry: see note for iso639_3LocaleSelect() method
		//showCode: see note for iso639_3LocaleSelect() method
		//listStart
		//listJoin
		//listEnd

		if (!attrs.containsKey("locale"))
		{
			throw new TaglykException("Tag [formatLocale] is missing required attribute [locale]")
		}
		def locales = attrs.remove("locale")
		if(!ObjectUtils.isListOrArray(locales))
		{
			locales = [locales]
		}
		locales = locales.collect{locale->
			if(locale instanceof String)
			{
				def editor = new LocaleEditor();
				editor.setAsText(locale.toString());
				locale = editor.getValue()
			}
			return locale
		}

		def appendCountryCode = true
		if(attrs.containsKey("appendCountryCode")){appendCountryCode = attrs.remove("appendCountryCode")}
		def appendLanguageName = true
		if(attrs.containsKey("appendLanguageName")){appendLanguageName = attrs.remove("appendLanguageName")}
		def showCountry = true
		if(attrs.containsKey("showCountry")){showCountry = attrs.remove("showCountry")}
		def showCode = true
		if(attrs.containsKey("showCode")){showCode = attrs.remove("showCode")}
		def escapeHTML = true
		if(attrs.containsKey("escapeHTML")){escapeHTML = attrs.remove("escapeHTML")}

		// set the variant (i.e. language name) as a closure that gets the language name
		def localeVariant = {((it.variant && it.variant != "null") ? it.variant : (it.country ? ELanguagesByCountry.getPrimaryLanguage(it.country, it.language)?.getName() : LanguageByCode.get(it.language)?.getName()))}
		// set the key as a closure that formats the locale
		def localeKey = {it.language + "_" + (appendCountryCode ? it.country : "") + "_" + (appendLanguageName ? localeVariant.call(it) : "")}
		// set the option value as a closure that formats the locale for display
		def localeValue = {it == null ? "" : localeVariant.call(it) + (showCountry ? " [" + it.displayCountry + "]" : "") + (showCode ? " (" + localeKey.call(it) + ")" : "")}
		
		locales = locales.collect{locale-> localeValue.call(locale)}
		if(escapeHTML)
		{
			locales = locales.collect{it.encodeAs("HTML")}
		}
		
		if(attrs.listStart == null && attrs.listJoin == null && attrs.listEnd == null)
		{
			return script.formatList(locales, "<ol><li>", "</li><li>", "</li></ol>")
		}
		else
		{
			return script.formatList(locales, attrs.listJoin, attrs.listStart, attrs.listEnd)
		}
	}
	
	static g_iso639_3HeaderIncludes(script, attrs=[:]){script.out << script.iso639_3HeaderIncludes(attrs)}
	static iso639_3HeaderIncludes(script, attrs=[:])
	{
		def out = new StringBuffer()
		def format = "Div"
		if(attrs.containsKey("format")){format = attrs.remove("format")}
		out << """<link rel="stylesheet" type="text/css" href="/css/LanguagePicker${format}.css" />
<script type="text/javascript" src="/js/jquery-1.4.2.min.js" />
<script type="text/javascript" src="/js/LanguagePicker/LanguagePicker.js" />
"""
	}
	
    /**
     *  A helper tag for creating locale selects with ISO 639-3 language codes
     *
     * eg. <g:iso639_3LocaleSelect name="myLocale" value="${locale}" />
     */
	static g_iso639_3LocaleSimpleSelect(script, attrs=[:]){script.out << script.iso639_3LocaleSimpleSelect(attrs)}
    static iso639_3LocaleSimpleSelect(script, attrs=[:])
    {
		def appendCountryCode = true
		if(attrs.containsKey("appendCountryCode")){appendCountryCode = attrs.remove("appendCountryCode")}
		def appendLanguageName = true
		if(attrs.containsKey("appendLanguageName")){appendLanguageName = attrs.remove("appendLanguageName")}
		def showCountry = true
		if(attrs.containsKey("showCountry")){showCountry = attrs.remove("showCountry")}
		def showCode = true
		if(attrs.containsKey("showCode")){showCode = attrs.remove("showCode")}
		
		// set the variant (i.e. language name) as a closure that gets the language name
		def localeVariant = {def variantName = ((it.variant && it.variant != "null") ? it.variant : (it.country ? ELanguagesByCountry.getPrimaryLanguage(it.country, it.language)?.getName() : LanguageByCode.get(it.language)?.getName())); return script.message(code:"lang." + it.language, default:variantName);}
		// set the key as a closure that formats the locale
		def localeKey = {def key = it.language + "_" + (appendCountryCode ? it.country : "") + "_" + (appendLanguageName ? localeVariant.call(it) : ""); return key.replaceAll(/__$/, "")}
		// set the option value as a closure that formats the locale for display
		def localeValue = {localeVariant.call(it) + (showCountry ? " [" + it.displayCountry + "]" : "") + (showCode ? " (" + localeKey.call(it) + ")" : "")}
		
		attrs["optionKey"] = localeKey
		attrs["optionValue"] = localeValue
		
		script.select(attrs)
    }
	
    /**
     *  A helper tag for creating locale selects with ISO 639-3 language codes
     *
     * eg. <g:iso639_3LocaleSelect name="myLocale" value="${locale}" />
     */
	static g_iso639_3LocaleSelect(script, attrs=[:]){script.out << script.iso639_3LocaleSelect(attrs)}
    static iso639_3LocaleSelect(script, attrs=[:])
	{
		def out = new StringBuffer()
		
		script.languagePickerAttributes = attrs
		
		//Attributes:
		//multiple: true or false (default true) Should multiple locale selections be possible?
		//regionFilter: a list of regions to restrict the list to (from these options: ["Africa", "Americas", "Asia", "Europe", "Pacific"]). Default is all regions.
		//countryFilter: a list of country codes to restrict the list to e.g. ["US", "MX", "CA"]. Default is all countries.
		//languageStatus: a list that filters what languages should be displayed by status, e.g. ["L", "N", "X", "S"]. L=Living, N=Nearly Extinct, X=Extinct, S=Second Language Only. Default is ["L", "N", "S"]
		//nameType: a list that filters what language/dialect names should be displayed, e.g. ["L", "LA", "LP", "D", "DA", "DP"]. L=Language Primary Name, LA=Language Alternate Name, LP=Language Pejorative Name, D=Dialect Primary Name, DA=Dialect Alternate Name, DP=Dialect Pejorative Name. The default is ['L', 'D']
		//appendCountryCode: true or false (default true) - Should the country code should be appended to the locale that will be sent with the form submission?
		//appendLanguageName: true or false (default true) - Should the language/dialect name be appended to the locale (as the "variant" part of the locale)?
		//showCountry: true or false (default true) - Should the country code should be shown in the Selected Languages select list?
		//showCode: true or false (default true) - Should the three letter ISO639-3 language code be visible in the select list?
		//languageInfo: true or false (default true) - Should a button be shown that opens a new window with information about the selected language from www.ethnologue.org?
		//languageTree: true or false (default true) - Should a button be shown that opens a new window with information about the selected language's language family from www.ethnologue.org?
		//format: "div", "table", or "vertical", "simple" (default "table") - Should the horizontal "div" or "table" version of the form be used, or the compressed "vertical" version?
		//size: the size for the select lists
		//value: the currently selected Locale(s)
		//useUserLocale: (default true) - Should the user's current locale(s) be used to populate the list?
		//locale: list of locales to included in the select for the "simple" format

		
		script.wrapVariables(["languagePickerAttributes"])
		script.include("/WEB-INF/includes/LanguagePicker.gtpl")
		out << script.request.getAttribute("includeReturn")
		
		return out.toString()
    }
}
