<%
import java.util.Locale
import groovyx.gaelyk.graelyk.cast.LocaleEditor
import groovyx.gaelyk.graelyk.util.ObjectUtils;
import org.ethnologue.iso639_3.*
import org.sil.iso639_3.*
%>
<% try{unwrapVariables()}catch(Exception e){e.toString()}
request.includeReturn = captureOut(){
		def script = this
		def attrs = languagePickerAttributes
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
		//format: "div", "table", or "vertical" (default "table") - Should the horizontal "div" or "table" version of the form be used, or the compressed "vertical" version?
		//size: the size for the select lists
		//value: the currently selected Locale(s)
		//useUserLocale: (default true) - Should the user's current locale(s) be used to populate the list?

		def multiple = "multiple='multiple'"
		if(attrs.containsKey("multiple") && !attrs["multiple"]){multiple = ""}

		def regionFilter = attrs["regionFilter"] ? ("[" + attrs.remove("regionFilter").split(/,/).collect{"'${it.trim()}'"}.join(",") + "]") : ["Africa", "Americas", "Asia", "Europe", "Pacific"];
		def singleCountry = false;
		def autoLoad = false;
		if(attrs["countryFilter"])
		{
			if(attrs["countryFilter"].split(/,/).size() == 1)
			{
				singleCountry = true;
				autoLoad = attrs["countryFilter"];
			}
		}
		if(attrs["loadCountry"])
		{
			autoLoad = attrs.remove("loadCountry")
		}
		
		def countryFilter = attrs["countryFilter"] ? ("[" + attrs.remove("countryFilter").split(/,/).collect{"'${it.trim()}'"}.join(",") + "]") : ""
		def languageStatus = attrs["languageStatus"] ? ("[" + attrs.remove("languageStatus").split(/,/).collect{"'${it.trim()}'"}.join(",") + "]") : ("['L','N','S']")
		def nameType = attrs["nameType"] ? ("[" + attrs.remove("nameType").collect{"$it"}.join(",") + "]") : ("['L','D']")
		def format = "table"
		if(attrs.containsKey("format")){format = attrs.remove("format").toLowerCase()}

		def size
		if(!multiple && format == "vertical" && !attrs["size"])
		{
			size = ""
		}
		else
		{
			size = attrs["size"] ? "size='" + attrs.remove("size") + "'" : "size='10'"
		}
        
		def appendCountryCode = true
		if(attrs.containsKey("appendCountryCode")){appendCountryCode = attrs.remove("appendCountryCode")}
		def appendLanguageName = true
		if(attrs.containsKey("appendLanguageName")){appendLanguageName = attrs.remove("appendLanguageName")}
		def showCountry = true
		if(attrs.containsKey("showCountry")){showCountry = attrs.remove("showCountry")}
		def showCode = true
		if(attrs.containsKey("showCode")){showCode = attrs.remove("showCode")}
		def languageInfo = true
		if(attrs.containsKey("languageInfo")){languageInfo = attrs.remove("languageInfo")}
		def languageTree = true
		if(attrs.containsKey("languageTree")){languageTree = attrs.remove("languageTree")}
        
		def useUserLocale =  true
		if(attrs.containsKey("useUserLocale")){useUserLocale = attrs.remove("useUserLocale")}
		
		def name = attrs.remove('name')
		def values = (attrs['value'] ? attrs['value'] : (useUserLocale ? script.userLocale : []))
		//If values isn't a List or array, turn it into a list
		if(!ObjectUtils.isListOrArray(values))
		{
			values = [values]
		}
		
		// set the key as a closure that formats the locale
		//def optionKey = {it.language + "_" + (appendCountryCode ? it.country : "") + "_" + (appendLanguageName ? it.variant : "")}
		// set the option value as a closure that formats the locale for display
		//def optionValue = {((it.variant && it.variant != "null") ? it.variant : ELanguagesByCountry.getPrimaryLanguage(it.country, it.language)) + (showCountry ? " [" + it.displayCountry + "]" : "") + (showCode ? " (" + optionKey.call(it) + ")" : "")}

		// set the variant (i.e. language name) as a closure that gets the language name
		def localeVariant = {((it.variant && it.variant != "null") ? it.variant : (it.country ? ELanguagesByCountry.getPrimaryLanguage(it.country, it.language)?.getName() : LanguageByCode.get(it.language)?.getName()))}
		// set the key as a closure that formats the locale
		def localeKey = {def key = it.language + "_" + (appendCountryCode ? it.country : "") + "_" + (appendLanguageName ? localeVariant.call(it) : ""); return key.replaceAll(/__$/, "")}
		// set the option value as a closure that formats the locale for display
		def localeValue = {localeVariant.call(it) + (showCountry ? " [" + it.displayCountry + "]" : "") + (showCode ? " (" + localeKey.call(it) + ")" : "")}

		def regionLabel = script.message("languagePicker.selectRegion")
		def regionSelect = """<select id="${name}_region" class="languagePickerRegion" onchange="window.languagePicker.autoLoadCountries('#${name}_region', '#${name}_country' ${countryFilter ? ", {countryFilter:${countryFilter}}" : ""});">"""
		def regionSelectOptions = """${regionFilter.contains("Africa")?"<option value='Africa'>${script.message("languagePicker.Africa")}</option>":''}
${regionFilter.contains("Americas")?"<option value='Americas'>${script.message("languagePicker.Americas")}</option>":''}
${regionFilter.contains("Asia")?"<option value='Asia'>${script.message("languagePicker.Asia")}</option>":''}
${regionFilter.contains("Europe")?"<option value='Europe'>${script.message("languagePicker.Europe")}</option>":''}
${regionFilter.contains("Pacific")?"<option value='Pacific'>${script.message("languagePicker.Pacific")}</option>":''}
"""

		def countryLabel = script.message("languagePicker.selectCountry")
		def countrySelect = """<select id="${name}_country" class="languagePickerCountry" ${size} onchange="languagePicker.autoLoadLanguages('#${name}_country', '#${name}_language', {type:${nameType}, status:${languageStatus}, showCode:${showCode}, appendCountryCode:${appendCountryCode}});"></select>"""
		
		def languageSelectLabel = multiple ? script.message("languagePicker.selectLanguages") : script.message("languagePicker.selectLanguage")
		def languageSelect = """<select id="${name}_language" class="languagePickerLanguage" ${multiple} ${size}></select>"""
			
		def autoLoadLanguages = ""
		if(autoLoad)
		{
			autoLoadLanguages = """<script type="text/javascript">
				jQuery(function()
					{
						languagePicker.autoLoadLanguages('${autoLoad}', '#${name}_language', {type:${nameType}, status:${languageStatus}, showCode:${showCode}, appendCountryCode:${appendCountryCode}});
					}
				);</script>"""
		}
			
		def selectedLanguagesLabel = script.message("languagePicker.selectedLanguages")
			
		def addButton
		def removeButton
		def upButton
		def downButton
		def infoButton
		def treeButton
		if(format == "vertical")
		{
			addButton = script.message("languagePicker.addVertical")
			removeButton = script.message("languagePicker.removeVertical")
		}
		else
		{
			addButton = script.message("languagePicker.add")
			removeButton = script.message("languagePicker.remove")
		}
		addButton = """<input class="languagePickerAddButton" type="button" value="${addButton}" onclick="languagePicker.copySelectedTo('#${name}_language', '#${name}_selectedLanguages', '#${name}_hiddenDiv', '${name}', {countrySelect:'#${name}_country', appendCountryCode:${appendCountryCode}, appendLanguageName:${appendLanguageName}, showCountry:${showCountry}, showCode:${showCode} ${multiple ? '' : ', single:true'}});">"""
		removeButton = """<input class="languagePickerRemoveButton" type="button" value="${removeButton}" onclick="languagePicker.removeSelectedFrom('#${name}_selectedLanguages', '#${name}_hiddenDiv', '${name}');">"""
		upButton = """<input class="languagePickerRemoveButton" type="button" value="${script.message("languagePicker.moveUp")}" onclick="languagePicker.moveSelectedUp('#${name}_selectedLanguages', '#${name}_hiddenDiv', '${name}');">"""
		downButton = """<input class="languagePickerRemoveButton" type="button" value="${script.message("languagePicker.moveDown")}" onclick="languagePicker.moveSelectedDown('#${name}_selectedLanguages', '#${name}_hiddenDiv', '${name}');">"""
		
		if(languageInfo)
		{
			infoButton = """<input class="languagePickerRemoveButton" type="button" value="${script.message("languagePicker.languageInfo")}" onclick="languagePicker.openEthnologue('#${name}_language');">"""
		}
		if(languageTree)
		{
			treeButton = """<input class="languagePickerRemoveButton" type="button" value="${script.message("languagePicker.languageTree")}" onclick="languagePicker.openEthnologueFamily('#${name}_language');">"""
		}

		
		def selectedLanguages = """<select id="${name}_selectedLanguages" class="languagePickerSelectedLanguage" ${multiple} ${size}>"""
		def hiddenDiv = """<div id="${name}_hiddenDiv" class="languagePickerHiddenInputs">"""
		for(value in values)
		{
			//Convert String to Locale
			if(value instanceof String)
			{
				def editor = new LocaleEditor();
				editor.setAsText(value.toString());
				value = editor.getValue()
			}
			def key = localeKey.call(value).encodeAs("HTML")
			selectedLanguages += """<option value="${key}">${localeValue.call(value).encodeAs("HTML")}</option>\r\n"""
			hiddenDiv += """<input type="hidden" name="${name}" value="${key}" />"""
			if(!multiple){break;}
		}
		selectedLanguages += "</select>"
		hiddenDiv += "</div>"

		if(format == "table")
		{
%>


<table class="languagePickerArea">
<tr>
<% if(!singleCountry) { %>
<td class="languagePickerRegionHeader">
${regionSelect}
<option value=''>${regionLabel}</option>
${regionSelectOptions}
</select>
</td>
<% } %>
<td class="languagePickerLanguageHeader">${multiple ? script.message("languagePicker.selectLanguages") : script.message("languagePicker.selectLanguage")}</td>
<td class="languagePickerButtonHeader"></td>
<td class="languagePickerSelectedLanguageHeader">${selectedLanguagesLabel}</td>
</tr>
<tr>
<% if(!singleCountry) { %>
<td class="languagePickerCountryColumn">${countrySelect}</td>
<% } %>
${autoLoadLanguages}
<td class="languagePickerLanguageColumn">${languageSelect}</td>
<td class="languagePickerButtonColumn">${addButton}<br />${removeButton}<br />${upButton}<br />${downButton}<br />${infoButton}<br />${treeButton}</td>
<td class="languagePickerSelectedLanguageColumn">${selectedLanguages}${hiddenDiv}</td>
</tr>
</table>


<% } else if(format == "square") { %>


<table class="languagePickerArea">
			
<% if(!singleCountry) { %>
<tr>
<td class="languagePickerRegionHeader">${regionLabel}</td>
<td class="languagePickerButtonHeader"></td>
<td class="languagePickerRegionHeader">${countryLabel}</td>
</tr>
<tr>
<td class="languagePickerRegionHeader">
${regionSelect}
<option value=''>${regionLabel}</option>
${regionSelectOptions}
</select>
</td>
<td class="languagePickerButtonColumn"></td>
<td class="languagePickerCountryColumn">${countrySelect}</td>
</tr>
<% } %>
${autoLoadLanguages}
<tr>
<td class="languagePickerLanguageHeader">${multiple ? script.message("languagePicker.selectLanguages") : script.message("languagePicker.selectLanguage")}</td>
<td class="languagePickerButtonHeader"></td>
<td class="languagePickerSelectedLanguageHeader">${selectedLanguagesLabel}</td>
</tr>
<tr>
<td class="languagePickerLanguageColumn">${languageSelect}</td>
<td class="languagePickerButtonColumn">${addButton}<br />${removeButton}<br />${upButton}<br />${downButton}<br />${infoButton}<br />${treeButton}</td>
<td class="languagePickerSelectedLanguageColumn">${selectedLanguages}${hiddenDiv}</td>
</tr>
</table>


<% } else if(format == "vertical") { %>

<% if(!singleCountry) { %>
${regionSelect}
<option value=''>${regionLabel}</option>
${regionSelectOptions}
</select>
<br />
${countrySelect}<br />
<% } %>
${autoLoadLanguages}
${languageSelectLabel}<br />
${languageSelect}${addButton}<br />
${selectedLanguagesLabel}<br />
${selectedLanguages}<br />
${removeButton}${upButton}${downButton}${infoButton}${treeButton}
${hiddenDiv}


<% } else if(format == "div") { %>


<div class="languagePickerArea">
<% if(!singleCountry) { %>
<div class="languagePickerCountryColumn">
${regionSelect}
<option value=''>${regionLabel}</option>
${regionSelectOptions}
</select>
<br />
${countrySelect}
</div>

<div class="languagePickerLanguageColumn">
${languageSelectLabel}<br />
${languageSelect}
</div>
<% } %>
${autoLoadLanguages}
<div class="languagePickerButtonColumn">
&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;
${addButton}<br />${removeButton}<br />${upButton}<br />${downButton}<br />${infoButton}<br />${treeButton}
</div>

<div class="languagePickerSelectedLanguageColumn">
${selectedLanguagesLabel}<br />
${selectedLanguages}
${hiddenDiv}
</div>
</div>


<% } 
} %>