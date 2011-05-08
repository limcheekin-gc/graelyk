/* Copyright 2004-2005 the original author or authors.
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
//Original package org.codehaus.groovy.grails.plugins.web.taglib
package groovyx.gaelyk.graelyk.taglyk

//import org.springframework.web.servlet.support.RequestContextUtils as RCU

import java.text.DateFormat
import groovyx.gaelyk.graelyk.util.SynchronizerToken
import groovyx.gaelyk.graelyk.util.HttpMethod
import groovyx.gaelyk.graelyk.util.ObjectUtils
import groovyx.gaelyk.graelyk.domain.GraelykDomainClass

import com.google.appengine.api.datastore.*

import org.springframework.beans.SimpleTypeConverter
/*
import org.codehaus.groovy.grails.web.util.StreamCharBuffer;
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.springframework.http.HttpMethod;
import org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerToken
*/

/**
*  A  tag lib that provides tags for working with form controls
*
* @author Graeme Rocher
* @since 17-Jan-2006
*/

class FormTaglyk {
    //def out // to facilitate testing

	//def grailsApplication

    /**
      * Creates a new text field
      */
	static g_textField(script, attrs){script.out << script.textField(attrs)}
    static textField(script, attrs)
	{
		def out = new StringBuffer()
        attrs.type = "text"
        attrs.tagName = "textField"
		script.fieldImpl(out, attrs)
		return out.toString()
    }

    /**
     * Creates a new password field
     */
	static g_passwordField(script, attrs){script.out << script.passwordField(attrs)}
    static passwordField(script, attrs)
	{
		def out = new StringBuffer()
        attrs.type = "password"
        attrs.tagName = "passwordField"
        script.fieldImpl(out, attrs)
		return out.toString()
    }

    /**
      * Creates a hidden field
      */
	static g_hiddenField(script, attrs){script.out << script.hiddenField(attrs)}
    static hiddenField(script, attrs)
	{
		def out = new StringBuffer()
        script.hiddenFieldImpl(out, attrs)        	
		return out.toString()
    }

    static hiddenFieldImpl(script, out, attrs)
	{
	    attrs.type = "hidden"
	    attrs.tagName = "hiddenField"
	    script.fieldImpl(out, attrs)
    }

    /**
      * Creates a submit button
      */
	static g_submitButton(script, attrs){script.out << script.submitButton(attrs)}
    static submitButton(script, attrs)
	{
		def out = new StringBuffer()
        attrs.type = attrs.type ?: "submit"
        attrs.tagName = "submitButton"
        if (script.request['flowExecutionKey']) {
            attrs.name = attrs.event ? "_eventId_${attrs.event}" : "_eventId_${attrs.name}"
        }
        if(attrs.name && (attrs.value == null)) {
            attrs.value = attrs.name
        }
        script.fieldImpl(out, attrs)
		return out.toString()
    }
	
    /**
      * A general tag for creating fields
      */
	static g_field(script, attrs){script.out << script.field(attrs)}
    static field(script, attrs)
	{
		def out = new StringBuffer()
        script.fieldImpl(out, attrs)
		return out.toString()
    }

    static fieldImpl(script, out, attrs)
	{
	    script.resolveAttributes(attrs)
	    attrs.id = attrs.id ? attrs.id : attrs.name
	    out << "<input type=\"${attrs.remove('type')}\" "
		
		try
		{
			//Try using the GraelykCategory asType method to convert Google data types like Link and Text to a String
			//attrs.value = attrs.value as String
			def transformOnDisplay = (attrs.remove("property"))?.transformOnDisplay
			attrs.value = script.castingRegistry.cast(attrs.value, String, transformOnDisplay)
		}
		catch(Exception e)
		{
			System.out.println("fieldImpl: " + e.toString())
		}
		
	    script.outputAttributes(out, attrs)
	    out << "/>"
    }
	

    /**
    * A helper tag for creating checkboxes
    **/
	static g_checkBox(script, attrs){script.out << script.checkBox(attrs)}
    static checkBox(script, attrs)
	{
		def out = new StringBuffer()
        attrs.id = attrs.id ? attrs.id : attrs.name
        def value = attrs.remove('value')
        def name = attrs.remove('name')
        def disabled = attrs.remove('disabled')
        if (disabled && Boolean.valueOf(disabled)) {
            attrs.disabled = 'disabled'
        }

        // Deal with the "checked" attribute. If it doesn't exist, we
        // default to a value of "true", otherwise we use Groovy Truth
        // to determine whether the HTML attribute should be displayed
        // or not.
        def checked = true
        if (attrs.containsKey('checked')) {
            checked = attrs.remove('checked')
        }

        if (checked instanceof String) checked = Boolean.valueOf(checked)

        if (value == null) value = false

        // the hidden field name should begin with an underscore unless it is
        // a dotted name, then the underscore should be inserted after the last
        // dot
        def lastDotInName = name.lastIndexOf('.')
        def hiddenFieldName = lastDotInName == -1 ? '_' + name : name[0..lastDotInName] + '_' + name[(lastDotInName+1)..-1]
        
        out << "<input type=\"hidden\" name=\"${hiddenFieldName}\" /><input type=\"checkbox\" name=\"${name}\" "
        if (value && checked) {
            out << 'checked="checked" '
        }
        
        def outputValue = !(value instanceof Boolean || value?.class == boolean.class)
        if (outputValue)
        {
            out << "value=\"${value}\" "
        }
        else
        {
        	out << "value=\"true\" "
        }
        // process remaining attributes but remove property attribute
        attrs.remove("property")
        script.outputAttributes(out, attrs)

        // close the tag, with no body
        out << ' />'

		return out.toString()
    }
	
	
    /**
      * A general tag for creating textareas
      */
	static g_textArea(script, attrs){script.out << script.textArea(attrs)}
	static g_textArea(script, attrs, body){script.out << script.textArea(attrs, body)}
	static textArea(script, attrs){script.textArea(attrs, null)}
    static textArea(script, attrs, body)
	{
		def out = new StringBuffer()
        script.resolveAttributes(attrs)
        attrs.id = attrs.id ? attrs.id : attrs.name
        // Pull out the value to use as content not attrib
        def value = attrs.remove('value')
        if(!value && body != null) 
		{
            value = body()
        }
		try
		{
			//Try using the GraelykCategory asType method to convert Google data types like Link and Text to a String
			value = script.castingRegistry.cast(value, String, (attrs.remove("property"))?.transformOnDisplay)
		}
		catch(Exception e)
		{
			System.out.println("textArea: " + e.toString())
		}


        def escapeHtml = true
        if (attrs.escapeHtml) escapeHtml = Boolean.valueOf(attrs.remove('escapeHtml'))

        out << "<textarea "
        script.outputAttributes(out, attrs)
        out << ">" << (escapeHtml ? value.encodeAs("HTML") : value) << "</textarea>"
		
		return out.toString()
    }

	
	
	
    /**
     * Check required attributes, set the id to name if no id supplied, extract bean values etc.
     */
    static void resolveAttributes(script, attrs)
    {
        if (!attrs.name && !attrs.field) {
            throw new TaglykException("Tag [${attrs.tagName}] is missing required attribute [name] or [field]")
        }
        attrs.remove('tagName')

        attrs.id = (!attrs.id ? attrs.name : attrs.id)

        def val = attrs.remove('bean')
        if (val) {
            if (attrs.name.indexOf('.'))
                attrs.name.split('\\.').each {val = val?."$it"}
            else {
                val = val[attrs.name]
            }
            attrs.value = val
        }
        attrs.value = (attrs.value != null ? attrs.value : "")
    }
	
    /**
     * Dump out attributes in HTML compliant fashion
     */
    static void outputAttributes(script, out, attrs)
    {
        attrs.remove('tagName') // Just in case one is left
        attrs.each {k, v ->
            out << "$k=\"${v.encodeAs("HTML")}\" "
        }
    }
	
	
    /**
     * Same as <g:form>, except sets the relevant enctype for a file upload form
     */
	static g_uploadForm(script, attrs, body){script.out << script.uploadForm(attrs, body)}
    static uploadForm(script, attrs, body)
	{
		def out = new StringBuffer()
        attrs.enctype = "multipart/form-data"
        out << script.form(attrs, body)
		return out.toString()
    }

    
    /**
     *  General linking to controllers, actions etc. Examples:
     *
     *  <g:form action="myaction">...</gr:form>
     *  <g:form controller="myctrl" action="myaction">...</gr:form>
     */
	static g_form(script, attrs, body){script.out << script.form(attrs, body)}
    static form(script, attrs, body)
	{
		def writer = new StringBuffer()
        def useToken = attrs.remove('useToken')
		
		def id = attrs.remove("id") //keep this away from createLink which will try to turn it into a url parameter (?id=...) instead of an html property (id="...")

        writer << "<form action=\"${script.createLink(attrs)}\" "
        // default to post
        def method = attrs.remove('method')?.toUpperCase() ?: 'POST'
        def httpMethod = Enum.valueOf(HttpMethod.class, method)
        boolean notGet = httpMethod != HttpMethod.GET
        
        if (notGet) {
            writer << 'method="post" '
        }
        else {
        	writer << 'method="get" '
        }
		
        // process remaining attributes
        attrs.id = id ? id : attrs.name //recreate the attrs.id property, possibly from the id variable extracted above
        if(attrs.id == null) attrs.remove('id')


        script.outputAttributes(writer, attrs)


        writer << ">"
        if (script.request['flowExecutionKey']) {
            writer.println()
            script.hiddenFieldImpl(writer, [name: "execution", value: script.request['flowExecutionKey']])
        }
        if(notGet && httpMethod != HttpMethod.POST) {
        	script.hiddenFieldImpl(writer, [name: "_method", value: httpMethod.toString()])
        }
        if(useToken) {            
            def token = SynchronizerToken.store(script.session)
            writer.println()
            hiddenFieldImpl(writer, [name: SynchronizerToken.KEY, value: token.currentToken])
            writer.println()
            hiddenFieldImpl(writer, [name: SynchronizerToken.URI, value: script.request.forwardURI])
        }
        
        // output the body
        writer << script.captureOut(body)

        // close tag
        writer << "</form>"
		
		return writer.toString()
    }
	
	
    /**
     * Creates a submit button that submits to an action in the controller specified by the form action
     * The name of the action attribute is translated into the action name, for example "Edit" becomes
     * "_action_edit" or "List People" becomes "_action_listPeople"
     * If the action attribute is not specified, the value attribute will be used as part of the action name
     *
     *  <g:actionSubmit value="Edit" />
     *  <g:actionSubmit action="Edit" value="Some label for editing" />
     *
     */
	static g_actionSubmit(script, attrs){script.out << script.actionSubmit(attrs)}
    static actionSubmit(script, attrs)
	{
		def out = new StringBuffer()
        attrs.tagName = "actionSubmit"
        if (!attrs.value) {
            throw new TaglykException("Tag [$attrs.tagName] is missing required attribute [value]")
        }

        // add action and value
        def value = attrs.remove('value')
        def action = attrs.action ? attrs.remove('action') : value

        out << "<input type=\"submit\" name=\"_action_${action}\" value=\"${value}\" "

        // process remaining attributes
        script.outputAttributes(out, attrs)

        // close tag
        out << '/>'

		return out.toString()
    }
	
    /**
     * Creates a an image submit button that submits to an action in the controller specified by the form action
     * The name of the action attribute is translated into the action name, for example "Edit" becomes
     * "_action_edit" or "List People" becomes "_action_listPeople"
     * If the action attribute is not specified, the value attribute will be used as part of the action name
     *
     *  <g:actionSubmitImage src="/images/submitButton.gif" action="Edit" />
     *
     */
	static g_actionSubmitImage(script, attrs){script.out << script.actionSubmitImage(attrs)}
    static actionSubmitImage(script, attrs)
	{
		def out = new StringBuffer()
        attrs.tagName = "actionSubmitImage"

        if (!attrs.value) {
            throw new TaglykException("Tag [$attrs.tagName] is missing required attribute [value]")
        }

        // add action and value
        def value = attrs.remove('value')
        def action = attrs.action ? attrs.remove('action') : value

        out << "<input type=\"image\" name=\"_action_${action}\" value=\"${value}\" "

        // add image src
        def src = attrs.remove('src')
        if (src) {
            out << "src=\"${src}\" "
        }

        // process remaining attributes
        script.outputAttributes(out, attrs)

        // close tag
        out << '/>'

		return out.toString()
    }	


	
    /**
    * A simple date picker that renders a date as selects
    * eg. <g:datePicker name="myDate" value="${new Date()}" />
    */
	static g_datePicker(script, attrs){script.out << script.datePicker(attrs)}
    static datePicker(script, attrs)
	{
    	def out = new StringWriter()
        def xdefault = attrs['default']
        if (xdefault == null) {
            xdefault = new Date()
        } else if (xdefault.toString() != 'none') {
            if (xdefault instanceof String || xdefault instanceof GString) {
                xdefault = DateFormat.getInstance().parse(xdefault)
            }else if(!(xdefault instanceof Date)){
                throw new TaglykException("Tag [datePicker] requires the default date to be a parseable String or a Date")
            }
        } else {
            xdefault = null
        }

        def value = attrs['value']
		if (value.toString() == 'none') {
            value = null
        } else if (!value) {
            value = xdefault
        }
		if(value instanceof String || value instanceof GString)
		{
			value = DateFormat.getInstance().parse(value)
		}
		
        def name = attrs['name']
        def id = attrs['id'] ? attrs['id'] : name

        def noSelection = attrs['noSelection']
        if (noSelection != null)
        {
            noSelection = noSelection.entrySet().iterator().next()
        }

        def years = attrs['years']

        final PRECISION_RANKINGS = ["year": 0, "month": 10, "day": 20, "hour": 30, "minute": 40]
        def precision = (attrs['precision'] ? PRECISION_RANKINGS[attrs['precision']] :
			(script.meta("graelyk.taglyk.datePicker.default.precision") ?
				PRECISION_RANKINGS["${script.meta("graelyk.taglyk.datePicker.default.precision")}"] :
				PRECISION_RANKINGS["minute"]))

        def day
        def month
        def year
        def hour
        def minute
        def dfs = new java.text.DateFormatSymbols(script.resolveLocale(attrs.get('locale'), script.userDateLocale))

        def c = null
        if (value instanceof Calendar) {
            c = value
        }
        else if (value != null) {
            c = new GregorianCalendar();
            c.setTime(value)
        }

        if (c != null) {
            day = c.get(GregorianCalendar.DAY_OF_MONTH)
            month = c.get(GregorianCalendar.MONTH)
            year = c.get(GregorianCalendar.YEAR)
            hour = c.get(GregorianCalendar.HOUR_OF_DAY)
            minute = c.get(GregorianCalendar.MINUTE)
        }

        if (years == null) {
            def tempyear
            if (year == null) {
                // If no year, we need to get current year to setup a default range... ugly
                def tempc = new GregorianCalendar()
                tempc.setTime(new Date())
                tempyear = tempc.get(GregorianCalendar.YEAR)
            } else {
                tempyear = year
            }
            years = (tempyear - 100)..(tempyear + 100)
        }

        out.println "<input type=\"hidden\" name=\"${name}\" value=\"date.STRUCT\" />"

        // create day select
        if (precision >= PRECISION_RANKINGS["day"]) {
            out.println "<select name=\"${name}_day\" id=\"${id}_day\">"

            if (noSelection) {
                script.renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in 1..31) {
                out.println "<option value=\"${i}\"${i == day ? ' selected="selected"' : ''}>${i}</option>"
            }
            out.println '</select>'
        }

        // create month select
        if (precision >= PRECISION_RANKINGS["month"]) {
            out.println "<select name=\"${name}_month\" id=\"${id}_month\">"

            if (noSelection) {
                script.renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            dfs.months.eachWithIndex {m, i ->
                if (m) {
                    def monthIndex = i + 1
                    out.println "<option value=\"${monthIndex}\"${i == month ? ' selected="selected"' : ''}>$m</option>"
                }
            }
            out.println '</select>'
        }

        // create year select
        if (precision >= PRECISION_RANKINGS["year"]) {
            out.println "<select name=\"${name}_year\" id=\"${id}_year\">"

            if (noSelection) {
                script.renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in years) {
                out.println "<option value=\"${i}\"${i == year ? ' selected="selected"' : ''}>${i}</option>"
            }
            out.println '</select>'
        }

        // do hour select
        if (precision >= PRECISION_RANKINGS["hour"]) {
            out.println "<select name=\"${name}_hour\" id=\"${id}_hour\">"

            if (noSelection) {
                script.renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in 0..23) {
                def h = '' + i
                if (i < 10) h = '0' + h
                out.println "<option value=\"${h}\"${i == hour ? ' selected="selected"' : ''}>$h</option>"                
            }
            out.println '</select> :'

            // If we're rendering the hour, but not the minutes, then display the minutes as 00 in read-only format
            if (precision < PRECISION_RANKINGS["minute"]) {
                out.println '00'
            }
        }

        // do minute select
        if (precision >= PRECISION_RANKINGS["minute"]) {
            out.println "<select name=\"${name}_minute\" id=\"${id}_minute\">"

            if (noSelection) {
                script.renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in 0..59) {
                def m = '' + i
                if (i < 10) m = '0' + m
                out.println "<option value=\"${m}\"${i == minute ? ' selected="selected"' : ''}>$m</option>"
            }
            out.println '</select>'
        }
		
		return out.toString()
    }
	
    static renderNoSelectionOption(script, noSelectionKey, noSelectionValue, value)
	{
		def out = new StringBuffer()
    	script.renderNoSelectionOptionImpl(out, noSelectionKey, noSelectionValue, value)
		return out.toString()
    }

    def renderNoSelectionOptionImpl(out, noSelectionKey, noSelectionValue, value)
	{
        // If a label for the '--Please choose--' first item is supplied, write it out
        out << "<option value=\"${(noSelectionKey == null ? '' : noSelectionKey)}\"${noSelectionKey == value ? ' selected="selected"' : ''}>${noSelectionValue.encodeAs("HTML")}</option>"
    }

	
	
    /**
     *  A helper tag for creating TimeZone selects
     * eg. <g:timeZoneSelect name="myTimeZone" value="${tz}" />
     */
	static g_timeZoneSelect(script, attrs){script.out << script.timeZoneSelect(attrs)}
    static timeZoneSelect(script, attrs)
	{
		def out = new StringBuffer()
		
		def timezones = ["GMT-12:00","GMT-11:00","GMT-10:00","GMT-09:30","GMT-09:00","GMT-08:00","GMT-07:00","GMT-06:00","GMT-05:00","GMT-04:30","GMT-04:00","GMT-03:30","GMT-03:00","GMT-02:00","GMT-01:00","GMT+00:00","GMT+01:00","GMT+02:00","GMT+03:00","GMT+03:30","GMT+04:00","GMT+04:30","GMT+05:00","GMT+05:30","GMT+05:45","GMT+06:00","GMT+06:30","GMT+07:00","GMT+08:00","GMT+08:45","GMT+09:00","GMT+09:30","GMT+10:00","GMT+10:30","GMT+11:00","GMT+11:30","GMT+12:00","GMT+12:45","GMT+13:00","GMT+14:00"]

		attrs['value'] = (attrs['value'] ? attrs['value'].ID : TimeZone.getDefault().ID)

		if(attrs.containsKey("timezones"))
		{
			timezones = attrs.remove("timezones")
		}
		if(timezones == "all")
		{
	        attrs['from'] = TimeZone.getAvailableIDs();
	        def date = new Date()
	
	        // set the option value as a closure that formats the TimeZone for display
	        attrs['optionValue'] = {
	            TimeZone tz = TimeZone.getTimeZone(it);
	            def shortName = tz.getDisplayName(tz.inDaylightTime(date), TimeZone.SHORT);
	            def longName = tz.getDisplayName(tz.inDaylightTime(date), TimeZone.LONG);
	
	            def offset = tz.rawOffset;
	            def hour = (offset / (60 * 60 * 1000)) as int;
	            if(hour > 0 && hour < 10){hour = "+0" + hour}
	            else if(hour >= 10){hour = "+" + hour}
	            else if(hour < 0 && hour > -10){hour = "-0" + (hour*-1)}
	            def min = (Math.abs(offset / (60 * 1000)) % 60) as int;
	            if(min < 10){min = "0" + min}

	            return "GMT${hour}:${min} ${shortName}, ${longName} ${tz.ID}"
	        }
		}
		else
		{
			attrs['from'] = timezones
			attrs['optionValue'] = {
				return script.message("timezone." + it)
			}
		}

        // use generic select
        out << script.select(attrs)
		return out.toString()
    }

    /**
     *  A helper tag for creating locale selects
     *
     * eg. <g:localeSelect name="myLocale" value="${locale}" />
     */
	static g_localeSelect(script, attrs){script.out << script.localeSelect(attrs)}
    static localeSelect(script, attrs)
	{
		def out = new StringBuffer()
        attrs['from'] = script.localeListForUserLocale()
        attrs['value'] = (attrs['value'] ? attrs['value'] : script.userLocale)
        // set the key as a closure that formats the locale
        //attrs['optionKey'] = {"${it.language}_${it.country}"}
        // set the option value as a closure that formats the locale for display
        //attrs['optionValue'] = {script.localizeLanguageName(it) + (it.getCountry() ? " (" + script.localizeCountryName(it) + ")" : "")}

        attrs['optionKey'] = {it[0]}
		attrs['optionValue'] = {it[1]}
        
        // use generic select
        out << script.select(attrs)
		return out.toString()
    }
    //This retrieves and formats the list of locales supported by this JVM, and stores them in StaticResources.resources
    //If the list for this user's locale list already exists, it is retrieved from there instead of beign regenerated
    static List localeListForUserLocale(script)
    {
    	def userLocaleKey = "localeList:" + script.userLocaleAsString()
    	def list = []
    	if(script.resources.containsKey(userLocaleKey))
    	{
    		list = script.resources[userLocaleKey]
    	}
    	else
    	{
    		//Get the list of locales supported by this JVM
    		list = Locale.getAvailableLocales().sort{script.localizeLanguageName(it) + " (" + script.localizeCountryName(it) + ")"}
    		
    		//Format them and store the key and value in a list inside the list
            // set the key as a closure that formats the locale
            def optionKey = {"${it.language}_${it.country}"}
            // set the option value as a closure that formats the locale for display
            def optionValue = {script.localizeLanguageName(it) + (it.getCountry() ? " (" + script.localizeCountryName(it) + ")" : "")}

            list = list.collect{[optionKey.call(it), optionValue.call(it)]}
    		
    		script.resources[userLocaleKey] = list
    	}
    	return list
    }


    /**
     * A helper tag for creating currency selects
     *
     * eg. <g:currencySelect name="myCurrency" value="${currency}" />
     */
    //used to have a "body" parameter as the third parameter 
	static g_currencySelect(script, attrs){script.out << script.currencySelect(attrs)}
    static currencySelect(script, attrs)
	{
		def out = new StringBuffer()
        if (!attrs['from']) {
            attrs['from'] = ['EUR', 'XCD', 'USD', 'XOF', 'NOK', 'AUD', 'XAF', 'NZD', 'MAD', 'DKK', 'GBP', 'CHF', 'XPF', 'ILS', 'ROL', 'TRL']
        }
        try {
            def currency = (attrs['value'] ? attrs['value'] : Currency.getInstance(script.userCurrencyLocale))
            attrs.value = currency.currencyCode
        }
        catch (IllegalArgumentException iae) {
            attrs.value = null
        }
        // invoke generic select
        out << script.select(attrs)
		return out.toString()
    }
	
    /**
     * A helper tag for creating HTML selects
     *
     * Examples:
     * <g:select name="user.age" from="${18..65}" value="${age}" />
     * <g:select name="user.company.id" from="${Company.list()}" value="${user?.company.id}" optionKey="id" />
     */
	static g_select(script, attrs){script.out << script.select(attrs)}
    static select(script, attrs)
	{
        def locale = script.userLocale
        def writer = new StringWriter()
        attrs.id = attrs.id ? attrs.id : attrs.name
        def from = attrs.remove('from')
        def keys = attrs.remove('keys')
        def optionKey = attrs.remove('optionKey')
        def optionValue = attrs.remove('optionValue')
        def value = attrs.remove('value')
        def name = attrs.remove('name')
        def widget = attrs.remove('widget') //checkbox, radio, or the default: select
        if(widget == null || !(widget == "select" || widget == "checkbox" || widget == "radio"))
        {
        	widget = "select"
        }
        if(attrs.multiple == true || (attrs.property && (attrs.property.isCollection || attrs.property.isArray)))
		{
            attrs.multiple = 'multiple'
        }
		else if(attrs.multiple == false)
		{
			attrs.remove("multiple")
		}
        def transformOnDisplay = (attrs.remove("property"))?.transformOnDisplay
		
        def valueMessagePrefix = attrs.remove('valueMessagePrefix')
        def noSelection = attrs.remove('noSelection')
        if (noSelection != null) {
            noSelection = noSelection.entrySet().iterator().next()
        }
        def disabled = attrs.remove('disabled')
        if (disabled && Boolean.valueOf(disabled)) {
            attrs.disabled = 'disabled'
        }
        
    	def checkboxRadio = false
        if(widget == "select")
        {
        	checkboxRadio = false
        	
        	writer << "<select name=\"${name?.encodeAs("HTML")}\" "
        	
            // process remaining attributes
            script.outputAttributes(writer, attrs)

            writer << '>'
            writer.println()

            if (noSelection) {
                script.renderNoSelectionOptionImpl(writer, noSelection.key, noSelection.value, value)
                writer.println()
            }
        }
        else
        {
        	checkboxRadio = true
        	attrs.remove("multiple")
        }

        // create options from list
        if (from) 
		{
            //from.eachWithIndex {el, i ->
        	def i = -1
        	for(el in from)
        	{
        		i++
                def keyValue = null
                
                if(widget == "select")
                {
                    writer << '<option '
                    checkboxRadio = false
                }
                else if(widget == "checkbox")
                {
                	writer << "<input type=\"checkbox\" name=\"${name?.encodeAs("HTML")}\" "
                    // process remaining attributes
                    script.outputAttributes(writer, attrs)
                }
                else if(widget == "radio")
                {
                	writer << "<input type=\"radio\" name=\"${name?.encodeAs("HTML")}\" "
                    // process remaining attributes
                    script.outputAttributes(writer, attrs)
                }

                if (keys) {
                    keyValue = keys[i]
                    script.writeValueAndCheckIfSelected(checkboxRadio, keyValue, value, writer, transformOnDisplay)
                }
                else if (optionKey) {
                    def keyValueObject = null
                    if (optionKey instanceof Closure) {
                        keyValue = optionKey(el)
                    }
                    //else if (el != null && optionKey == 'id' && grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, el.getClass().name)) {
					else if (el != null && optionKey == 'id' && el instanceof GraelykDomainClass)
					{
						//The id/name of a Key is used as the value of the select list
                        //keyValue = el.ident().toString()
						keyValue = el.id.toString()
                        keyValueObject = el
                    }
                    else {
                        keyValue = el[optionKey]
                        keyValueObject = el
                    }
                    script.writeValueAndCheckIfSelected(checkboxRadio, keyValue, value, writer, transformOnDisplay, keyValueObject)
                }
                else {
                    keyValue = el
                    script.writeValueAndCheckIfSelected(checkboxRadio, keyValue, value, writer, transformOnDisplay)
                }
                writer << '>'
                if (optionValue) {
                    if (optionValue instanceof Closure) {
                        writer << optionValue(el).toString().encodeAs("HTML")
                    }
                    else {
                        writer << el[optionValue].toString().encodeAs("HTML")
                    }
                }
                else if (valueMessagePrefix) {
                    def message = script.message("${valueMessagePrefix}.${keyValue}", null, el, locale)
                    if (message != null) {
                        writer << message.encodeAs("HTML")
                    }
                    else if (keyValue) {
                        writer << keyValue.encodeAs("HTML")
                    }
                    else {
                        def s = el.toString()
                        if (s) writer << s.encodeAs("HTML")
                    }
                }
                else {
                    def s = el.toString()
                    if (s) writer << s.encodeAs("HTML")
                }
                
                if(widget == "selected")
                {
                    writer << '</option>'
                }
                else
                {
                	writer << "<br />"
                }
                writer.println()
            }
        }
        // close tag
        writer << '</select>'
		return writer.toString()
    }
	
    static writeValueAndCheckIfSelected(script, checkboxRadio, keyValue, value, writer, transformOnDisplay)
	{
        script.writeValueAndCheckIfSelected(checkboxRadio, keyValue, value, writer, transformOnDisplay, null)
    }

	
    static writeValueAndCheckIfSelected(script, checkboxRadio, keyValue, value, writer, transformOnDisplay, el)
	{
        boolean selected = false
        if(keyValue instanceof GString)
        {
        	keyValue = keyValue.toString()
        }
        def keyClass = keyValue?.getClass()
		if(keyValue == value)
		{
			selected = true
		}
        else if (keyClass.isInstance(value)) 
		{
            selected = (keyValue == value)
        }
        else if(ObjectUtils.isListOrArray(value))
		{
			//convert all the elements in the array/collection before they are tested against the current select list option
			if(keyClass)
			{
				value = value.collect{
					return script.castingRegistry.cast(it, keyClass, transformOnDisplay)
				}
			}
            // first try keyValue
            selected = value.contains(keyValue)
            if (! selected && el != null) {
                selected = value.contains(el)
            }
            if(!selected)
            {
            	for(v in value)
            	{
            		if(keyValue == v)
            		{
            			selected = true
            			break
            		}
            	}
            }
        }
        else if (keyClass && value) 
		{
            try {
                value = script.castingRegistry.cast(value, keyClass, transformOnDisplay)
                selected = (keyValue == value)
            } catch (Exception e) {
                // ignore
            }
        }
        writer << "value=\"${(keyValue as String).encodeAs('HTML')}\" "
        if (selected) {
        	if(checkboxRadio)
        	{
        		writer << " checked=\"checked\" "
        	}
        	else
        	{
        		writer << " selected=\"selected\" "
        	}
        }
    }

	
	
	/*
	//Old Grails version that was expecting full domain objects
	//Saved here in case the Objectify/Key method doesn't work right
    static writeValueAndCheckIfSelected(script, keyValue, value, writer, el)
	{
		def typeConverter = new SimpleTypeConverter()
        boolean selected = false
        def keyClass = keyValue?.getClass()
        if (keyClass.isInstance(value)) {
            selected = (keyValue == value)
        }
        else if (value instanceof Collection) {
            // first try keyValue
            selected = value.contains(keyValue)
            if (! selected && el != null) {
                selected = value.contains(el)
            }
        }
        else if (keyClass && value) {
            try {
                value = typeConverter.convertIfNecessary(value, keyClass)
                selected = (keyValue == value)
            } catch (Exception) {
                // ignore
            }
        }
        writer << "value=\"${keyValue}\" "
        if (selected) {
            writer << 'selected="selected" '
        }
    }
	*/
	
    /**
     * A helper tag for creating radio buttons
     */
	static g_radio(script, attrs){script.out << script.radio(attrs)}
    static radio(script, attrs)
	{
		def out = new StringBuffer()
        def value = attrs.remove('value')
        attrs.id = attrs.id ? attrs.id : attrs.name
        def name = attrs.remove('name')
        def disabled = attrs.remove('disabled')
        if (disabled && Boolean.valueOf(disabled)) {
            attrs.disabled = 'disabled'
        }
        def checked = (attrs.remove('checked') ? true : false)
        out << "<input type=\"radio\" name=\"${name}\"${ checked ? ' checked="checked" ' : ' '}value=\"${value?.toString()?.encodeAs("HTML")}\" "
        // process remaining attributes
        script.outputAttributes(out, attrs)

        // close the tag, with no body
        out << ' />'
		return out.toString()
    }

    /**
    * A helper tag for creating radio button groups
    */
	static g_radioGroup(script, attrs, body){script.out << script.radioGroup(attrs, body)}
	static radioGroup(script, attrs, body)
	{
		def out = new StringWriter()
        def value = attrs.remove('value')
        def values = attrs.remove('values')
        def labels = attrs.remove('labels')
        def name = attrs.remove('name')
        //values.eachWithIndex {val, idx ->
        def idx = -1
        for(val in values)
        {
        	idx++
            def it = new Expando();
            it.radio = "<input type=\"radio\" name=\"${name}\" "
            if (value?.toString().equals(val.toString())) {
                it.radio += 'checked="checked" '
            }
            it.radio += "value=\"${val.toString().encodeAs("HTML")}\" />"

            it.label = labels == null ? 'Radio ' + val : labels[idx]

            //out << body(it)
			//Todo: does this really work?
			script.captureOut(body, it)
            out.println()
			return out.toString()
        }
    }
}