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

import groovy.xml.MarkupBuilder

import java.beans.PropertyEditor
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import org.apache.commons.lang.StringEscapeUtils
import groovyx.gaelyk.graelyk.codecs.HTMLCodec
import groovyx.gaelyk.graelyk.util.ObjectUtils
import org.springframework.validation.Errors
import com.googlecode.objectify.Key
import com.google.appengine.api.datastore.*

/*
import org.springframework.beans.PropertyEditorRegistry
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.validation.Errors
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils as RCU
*/

/**
 * Tags to handle validation and errors.
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
class ValidationTaglyk
{
	static void g_fieldValue(script, attrs){script.out << script.fieldValue(attrs)}

    static returnObjectForTags = ['message', 'fieldError', 'formatValue']

    /**
     * Renders an error message for the given bean and field
     *
     * eg. <g:fieldError bean="${book}" field="title" />
     */
    static fieldError(script, attrs)//, body ->
	{
        def bean = attrs.bean
        def field = attrs.field

        if(bean && field)
		{
            if(bean.metaClass.hasProperty(bean,'errors'))
			{
               return script.message(error:bean.errors?.getFieldError(field), encodeAs:"HTML")
            }
        }
        return ''
    }

   /**
    * Obtains the value of a field either from the original errors
    *
    * eg. <g:fieldValue bean="${book}" field="title" />
    */
   static String fieldValue(script, attrs) //body
   {
        def bean = attrs.bean
        def field = attrs.field?.toString()
        if(bean && field)
		{
            if(bean.metaClass.hasProperty(bean,'errors'))
			{
                Errors errors = bean.errors
                def rejectedValue = errors?.getFieldError(field)?.rejectedValue
                if(rejectedValue == null )
				{
                    rejectedValue = bean
                    field.split("\\.").each{ String fieldPart ->
                        rejectedValue = rejectedValue?."$fieldPart"
                    }
                }
                if(rejectedValue != null)
				{
                    return script.formatValue(rejectedValue, attrs)
                }
            }
            else
            {
                def rejectedValue = bean
                field.split("\\.").each{ String fieldPart ->
                    rejectedValue = rejectedValue?."$fieldPart"
                }
                if(rejectedValue != null)
                {
                    return script.formatValue(rejectedValue, attrs)
                }
            }
        }
    }

    static extractErrors(script, attrs)
	{
        def model = attrs['model']
        def checkList = []
        if (attrs?.containsKey('bean'))
		{
            if(attrs.bean)
                checkList << attrs.bean
        }
		else if (attrs.containsKey('model')) 
		{
            if(model)
                checkList = model.findAll {it.value?.errors instanceof Errors}.collect {it.value}
        } 
		else 
		{
            script.request.attributeNames.each 
			{
                def ra = request[it]
                if(ra) 
				{
                    def mc = GroovySystem.metaClassRegistry.getMetaClass(ra.getClass())
                    if (ra instanceof Errors && !checkList.contains(ra))
					{
                        checkList << ra
					}
                    else if (mc.hasProperty(ra, 'errors') && ra.errors instanceof Errors && !checkList.contains(ra.errors))
					{
                        checkList << ra.errors
                    }
                }
            }
        }

        def resultErrorsList = []

        for (i in checkList) 
		{
            def errors = null
            if (i instanceof Errors) 
			{
                errors = i
            } 
			else 
			{
                def mc = GroovySystem.metaClassRegistry.getMetaClass(i.getClass())
                if (mc.hasProperty(i, 'errors')) 
				{
                    errors = i.errors
                }
            }
            if (errors?.hasErrors()) 
			{
                // if the 'field' attribute is not provided then we should output a body,
                // otherwise we should check for field-specific errors
                if (!attrs['field'] || errors.hasFieldErrors(attrs['field'])) 
				{
                    resultErrorsList << errors
                }
            }
        }

        return resultErrorsList
    }

    /**
     * Checks if the request has errors either for a field or global errors
     */
	static void g_hasErrors(script, attrs, body){script.out << script.hasErrors(attrs, body)}
    static hasErrors(script, attrs, body)
	{
        def errorsList = script.extractErrors(attrs)
        if(errorsList)
		{
			return script.captureOut(body)
        }
        else
        {
        	return ""
        }
    }

    /**
     * Loops through each error for either field or global errors
     */
    static eachError(script, attrs, body)
	{
        script.eachErrorInternal(attrs,body,true)
    }

    static eachErrorInternal(script, attrs, body, outputResult=false)
	{
        def errorsList = extractErrors(attrs)
        def var = attrs.var
        def field = attrs['field']

        def errorList = []
        errorsList.each { errors ->
            if(field) 
			{
                if(errors.hasFieldErrors(field)) 
				{
                    errorList += errors.getFieldErrors(field)
                }
            } 
			else 
			{
                errorList += errors.allErrors
            }
        }

        errorList.each { error ->
            def result
            if(var) 
			{
                result=body([(var):error])
            } 
			else 
			{
                result=body(error)
            }
            if(outputResult) 
			{
                return result
            }
        }

        return null
    }

    /**
     * Loops through each error and renders it using one of the supported mechanisms (defaults to "list" if unsupported)
     */
	static void g_renderErrors(script, attrs){script.out << script.renderErrors(attrs)}
    static renderErrors(script, attrs) //, body ->
	{
		def out = new StringWriter()

        def renderAs = attrs.remove('as')
        if(!renderAs) renderAs = 'list'

        if(renderAs == 'list')
		{
            def codec = attrs.codec ?: 'HTML'
            if (codec=='none') codec = ''

            out << "<ul>"
            script.eachErrorInternal(attrs, {
                out << "<li>${script.message(error:it, encodeAs:codec)}</li>"
              }
            )
            out << "</ul>"
        }
        else if(renderAs.equalsIgnoreCase("xml")) 
        {
            def mkp = new MarkupBuilder(out)
            mkp.errors() {
                eachErrorInternal(attrs, {
                    error(object:it.objectName,
                          field:it.field,
                          message:script.message(error:it)?.toString(),
                          'rejected-value':StringEscapeUtils.escapeXml(it.rejectedValue))
                })
            }
        }
		
		return out.toString()
    }


    // Maps out how Grails contraints map to Apache commons validators
    static CONSTRAINT_TYPE_MAP = [ email : 'email',
                                             creditCard : 'creditCard',
                                             match : 'mask',
                                             blank: 'required',
                                             nullable: 'required',
                                             maxSize: 'maxLength',
                                             minSize: 'minLength',
                                             range: 'intRange',
                                             size: 'intRange',
                                             length: 'maxLength,minLength' ]
    /**
     * Validates a form using Apache commons validator javascript against constraints defined in a Grails
     * domain class
     *
     * TODO: This tag is a work in progress
     */
	 /*
    static validate(script, attrs, body)
	{
        def form = attrs["form"]
        def againstClass = attrs["against"]
        if(!form)
		{
            throw new TaglykException("Tag [validate] is missing required attribute [form]")
		}

        if(!againstClass) {
            againstClass = form.substring(0,1).toUpperCase() + form.substring(1)
        }

        def app = grailsAttributes.getGrailsApplication()
        def dc = app.getDomainClass(againstClass)

        if(!dc)
            throw new TaglykException("Tag [validate] could not find a domain class to validate against for name [${againstClass}]")

        def constrainedProperties = dc.constrainedProperties.collect { k,v -> return v }
        def appliedConstraints = []

        constrainedProperties.each {
           appliedConstraints += it.collect{ it.appliedConstraints }
        }

        appliedConstraints = appliedConstraints.flatten()
        def fieldValidations = [:]
        appliedConstraints.each {
            def validateType = CONSTRAINT_TYPE_MAP[it.name]
            if(validateType) {
                if(fieldValidations[validateType]) {
                    fieldValidations[validateType] << it
                }
                else {
                     fieldValidations[validateType] =  [it]
                }
            }
        }

        out << '<script type="text/javascript">\n'
        fieldValidations.each { k,v ->
           def validateType = k

           if(validateType) {

                def validateTypes = [validateType]

                if(validateType.contains(",")) {
                    validateTypes = validateType.split(",")
                }


                validateTypes.each { vt ->
                    // import required script
                    def scriptName = "org/apache/commons/validator/javascript/validate" + vt.substring(0,1).toUpperCase() + vt.substring(1) + ".js"
                    def inStream = getClass().classLoader.getResourceAsStream(scriptName)
                    if(inStream) {
                        out << inStream.text
                    }

                    out << "function ${form}_${vt}() {"
                    v.each { constraint ->
                           out << "this.${constraint.propertyName} = new Array("
                           out << "document.forms['${form}'].elements['${constraint.propertyName}']," // the field
                           out << '"Test message"' // TODO: Resolve the actual message
                           switch(vt) {
                                case 'mask': out << ",function() { return '${constraint.regex}'; }";break;
                                case 'intRange': out << ",function() { if(arguments[0]=='min') return ${constraint.range.from}; else return ${constraint.range.to} }";break;
                                case 'floatRange': out << ",function() { if(arguments[0]=='min') return ${constraint.range.from}; else return ${constraint.range.to} }";break;
                                case 'maxLength': out << ",function() { return ${constraint.maxSize};  }";break;
                                case 'minLength': out << ",function() { return ${constraint.minSize};  }";break;
                           }
                           out << ');\n'
                    }
                    out << "}\n"
                }
            }
        }
        out << 'function validateForm(form) {\n'
        fieldValidations.each { k,v ->
           def validateType = k.substring(0,1).toUpperCase() + k.substring(1)
           out << "if(!validate${validateType}(form)) return false;\n"
        }
        out << 'return true;\n';
        out << '}\n'
      //  out << "document.forms['${attrs['form']}'].onsubmit = function(e) {return validateForm(this)}\n"
        out << '</script>'
    }
	*/
}
