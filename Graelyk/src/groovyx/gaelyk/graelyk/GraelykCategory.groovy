/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.graelyk

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import com.google.appengine.api.datastore.*
import groovyx.gaelyk.GaelykCategory;
import groovyx.gaelyk.graelyk.cast.CastingRegistry
import groovyx.gaelyk.graelyk.cast.LocaleEditor
import com.google.appengine.api.blobstore.BlobInfo
import groovy.servlet.ServletCategory
import com.google.appengine.api.users.User
import groovyx.gaelyk.graelyk.codecs.MD5Codec
import groovyx.gaelyk.graelyk.codecs.Base64Codec
import javax.servlet.http.Cookie

/**
 * Provides methods that give easier access to the com.googlecode.objectify packages
 *
 * @author Jeremy Brown
 */
class GraelykCategory
{
	static void processController(Object script)
	{
		processController(script, true, null)
	}
	
	static void processController(Object script, boolean forwardToView)
	{
		processController(script, forwardToView, null)
	}
	
	static void processController(Object script, Closure defaultAction)
	{
		processController(script, true, defaultAction)
	}

	static void processController(Object script, boolean forwardToView, Closure defaultAction)
	{
		script.identity{
			//Set default values for various properties in case they were not set in the controller
			processedController = true
			defaultValue("domainClassName", "")
			defaultValue("view", "")
			defaultValue("controllerName", "")
			defaultValue("controllerURL", "")
			defaultValue("controllerDefaultAction", "")
			defaultValue("allowedActions", [])
			defaultValue("allowedMethods", [:])
			defaultValue("userActions", [])
			defaultValue("userMethods", [:])
			defaultValue("adminActions", [])
			defaultValue("adminMethods", [:])
			defaultValue("flash", [:])
			defaultValue("userLocale", null)
			defaultValue("userNumberLocale", null)
			defaultValue("userDateLocale", null)
			defaultValue("userCurrencyLocale", null)
			
			//Set the admin variable based on whether the current users is an Administrator
			admin = (user && users.isUserAdmin())
			
			//Even if there is an "action" param, look for a param like "_action_edit", "_action_delete" and extract the action name ("edit", "delete")
			actionName = (params.find{it.key.startsWith("_action_")})?.key?.minus("_action_")
			//If there was no special action, use the default action param
			if(!actionName)
			{
				actionName = params.action
			}
			
			//Check the proposed action against the list of valid actions from the Controller
			if(!isActionAllowed(actionName))
			{
				actionName = controllerDefaultAction
			}
			
			//Give the conventional paths to the views and internationalization message files
			viewPath = "/graelyk-views/${domainClassName}"
			i18nPath = "graelyk-i18n/${domainClassName}"

			//Read properties from the file war/graelyk.properties and save them in a Map variable request.appProperties.
			//The "appProperties" file is created by the createConfig target of the CreateApp.gant Graelyk script,
			//and creates the file "/war/graelyk.properties" in the Graelyk application directory.
			appProperties = StaticResourceHolder.getAppProperties()
			
			//If a locale has not been specified by the calling script
			//(e.g. by finding the current user, accessing their profile, 
			//and reading their locale preferences, then setting: 
			//userLocale = userProfile.locales)
			//Fall back to the locales specified in the query variable specified in graelyk.properties (graelyk.userLocale.query), and this will set a cookie if (graelyk.userLocale.cookie) exists
			if(!userLocale)
			{
				def queryName = appProperties["graelyk.userLocale.query"]
				if(queryName)
				{
					userLocale = params[queryName]
					if(userLocale)
					{
						//Set the cookie if a name for the cookie is in the graelyk.properties file
						def cookieName = appProperties["graelyk.userLocale.cookie"]
						if(cookieName)
						{
							Cookie cookie = new Cookie(cookieName, userLocale)
							cookie.setMaxAge(60*60*24*365)
							cookie.setPath("/")
							response.addCookie(cookie)
						}
						//Set the variable for this script
						userLocale = userLocale.split(appProperties["graelyk.userLocale.cookieQuerySeparator"]).toList().collect{it.toLocale()}
					}
				}
			}
			//Fall back to the locales specified in a cookie whose name is specified in graelyk.properties (graelyk.userLocale.cookie)
			if(!userLocale)
			{
				userLocale = request.cookies.find{it.getName() == appProperties["graelyk.userLocale.cookie"]}
				if(userLocale)
				{
					userLocale = userLocale.getValue().split(appProperties["graelyk.userLocale.cookieQuerySeparator"]).toList().collect{it.toLocale()}
				}
			}
			//Fall back to the locales specified by the browser in the HTTP header
			if(!userLocale)
			{
				userLocale = request.getLocales().collect{it}
			}
			//Fall back to the appDefaultLocales specified in the graelyk.properties file
			if(!userLocale)
			{
				userLocale = appProperties.appDefaultLocales
			}
			//Fall back to the appLocales specified in the graelyk.properties file
			if(!userLocale)
			{
				userLocale = appProperties.appLocales
			}
			//Fall back to ""
			if(!userLocale)
			{
				userLocale = [ Locale.getDefault() ]
			}
			
			//Specify locales for NumberFormat, Currency, and DateFormat, separate from the userLocale that is used
			//for message localization. This allows the user to choose a message locale that is in a minority
			//language (e.g. one of the language codes in ISO-639-3) but choose a separate locale for number
			//formatting (since Java does not have locales specified for most of Earth's almost 7000 languages).

			//Specify a locale specifically for NumberFormat
			//In your controller script, you could specify something like:
			//userNumberLocale = new Locale("en_US")
			//Fall back to the appDefaultNumberLocale specified in graelyk.properties
			//Fall back to the first locale in userLocale
			if(!userNumberLocale)
			{
				userNumberLocale = appProperties.appDefaultNumberLocale
			}
			if(!userNumberLocale)
			{
				userNumberLocale = userLocale[0]
			}

			//Specify a locale specifically for DateFormat (separate from locale for messages)
			//In your controller script, you could specify something like:
			//userNumberLocale = new Locale("fr_FR")
			//Fall back to the appDefaultDateLocale specified in graelyk.properties
			//Fall back to the first locale in userLocale
			if(!userDateLocale)
			{
				userDateLocale = appProperties.appDefaultDateLocale
			}
			if(!userDateLocale)
			{
				userDateLocale = userLocale[0]
			}
			
			//Specify a locale specifically for Currency (separate from locale for messages)
			//In your controller script, you could specify something like:
			//userCurrencyLocale = new Locale("fr_FR")
			//Fall back to the appDefaultDateLocale specified in graelyk.properties
			//Fall back to the first locale in userLocale
			if(!userCurrencyLocale)
			{
				userCurrencyLocale = appProperties.appDefaultCurrencyLocale
			}
			if(!userCurrencyLocale)
			{
				userCurrencyLocale = userLocale[0]
			}
			
			//Create the default registry of closures defining how to cast from certain classes to other classes
			castingRegistry = CastingRegistry.createDefaultRegistry(userLocale, userNumberLocale, userCurrencyLocale, userDateLocale)
			
			//Read properties from the message/internationalization properties file for this domain/controller/view
			//The locales specified by the appLocales variable in graelyk.properties are loaded as well as the default no-locale file (messages.properties)
			messageBundle = StaticResourceHolder.getMessageBundle(domainClassName)
			
			//A HashMap that can statically hold objects
			resources = StaticResourceHolder.resources
			

			//Allow the developer to create an init closure in the Controller class that will be called at this point.
			try
			{
				script.init.call()
			}
			catch(Exception e)
			{
				//It is OK for an init closure not to exist.
			}


			//Run the controller action. Any key/values the controller returns in a Map will be
			//removed from their map and individually added to the request object using wrapVariables(...)
			//so they can later be retrieved by the view script
			if(defaultAction)
			{
				controllerResults = defaultAction.call()
				wrapVariables(controllerResults)
			}
			else if(actionName)
			{
				controllerResults = script."$actionName".call()
				wrapVariables(controllerResults)
			}
			
			//Save a bunch of variables in the request object so they can be passed on to the view.
			//The view can then use the unwrapVariables() method to copy them from request into the local scope of the view script
			def varsToWrap = ["domainClassName", "controllerName", "controllerURL", "actionName",
				"viewPath", "view", 
				"controllerDefaultAction", "allowedActions", "adminActions", "userActions", "allowedMethods", "adminMethods", "userActions",
				"appProperties", "messageBundle", "resources",
				"admin", "flash",
				"userLocale", "userNumberLocale", "userCurrencyLocale", "userDateLocale", "castingRegistry",
				"processedController"]
			wrapVariables(varsToWrap)

			if(forwardToView)
			{
				forward "${viewPath}/${view}.gtpl"
			}
		}
	}
	
	static isActionAllowed(script, String action)
	{
		script.defaultValue("isActionAllowedClosure", null)
		if(script.isActionAllowedClosure != null)
		{
			return script.isActionAllowedClosure.call(script, action)
		}
		else
		{
			if(script.allowedActions.contains(action)){return true}
			if(script.user && script.userActions.contains(action)){return true}
			if(script.admin && script.adminActions.contains(action)){return true}
		}
		return false
	}
	
	static defaultValue(script, String propertyName, value)
	{
		try
		{
			def test = script[propertyName]
		}
		catch(Exception e)
		{
			script[propertyName] = value
		}
	}
	
	static void wrapVariables(script, Map varMap)
	{
		use(ServletCategory)
		{
			if(!script.request.variableWrapper)
			{
				script.request.variableWrapper = [:]
			}
			varMap.each{k,v->
				script.request.variableWrapper."$k" = v
			}
		}
	}
	
	static void wrapVariables(script, List varList)
	{
		use(ServletCategory)
		{
			if(!script.request.variableWrapper)
			{
				script.request.variableWrapper = [:]
			}
			varList.each{var->
				script.request.variableWrapper."$var" = script."$var"
			}
		}
	}
	
	static void unwrapVariables(script)
	{
		use(ServletCategory)
		{
			if(script.request.variableWrapper)
			{
				script.request.variableWrapper.each{k,v->
					script."$k" = v
				}
			}
		}
	}
	
	
	/*
		This method can be used to allow taglyks to have a body closure that contains HTML.
		Normally, bodyClosure() or bodyClosure.call() would have the HTML sent to the response.out
		This method captures the output to a StringWriter instead and allows the HTML to be returned to the caller as a String
		e.g. 
		def myOutput = script.callOut(bodyClosure)
	*/
	static captureOut(script, body, Object... closureArgs)
	{
		if(body == null)
		{
			return ""
		}
		if(body instanceof Closure)
		{
			Closure bodyClosure = body
			//Temporarily circumvent the normal "script.out" variable...
			def originalOut = script.out

			//...and capture what would have gone there in a StringWriter instead
			script.out = new StringWriter()

			//Run the body closure, with all of its out.print("....") statements being captured in the StringWriter
			def closureResult
			if(closureArgs)
			{
				closureResult = bodyClosure(*closureArgs)
			}
			else
			{
				closureResult = bodyClosure()
			}

			//Save a copy of the StringWriter
			def newOut = script.out
			
			//Set script.out back to its original writer
			script.out = originalOut
			
			//Return the data that was captured in the StringWriter; or the result returned from the closure
			return (newOut.toString() ?: closureResult)
		}
		else
		{
			return body.toString()
		}
		return ""
	}
	
	
	/*
	This method gets a salted and hashed UserId from the user object
	*/
	static String getHashedUserId(User user)
	{
		if(user == null){return null}
		//Get the "salt" value from 
		def salt = StaticResourceHolder.getAppProperties().get("graelyk.security.userid.salt")
		def uid = salt + user.getUserId()
		def md5Hash = (new MD5Codec()).encode(uid)
		def base64Encoded = (new Base64Codec()).encode(md5Hash)
		return base64Encoded
	}
	
	
	/*
		These methods help pull values from a Map and cast the value to a particular type
	*/
	static Integer "int"(Map map, String key)
	{
		def value = map[key]
		if(value != null)
		{
			try{return Integer.parseInt(value)}
			catch(Exception e){return null}
		}
		return null
	}
	
	static Double "double"(Map map, String key)
	{
		def value = map[key]
		if(value != null)
		{
			try{return Double.parseDouble(value)}
			catch(Exception e){return null}
		}
		return null
	}
	
	static Float "float"(Map map, String key)
	{
		def value = map[key]
		if(value != null)
		{
			try{return Float.parseFloat(value)}
			catch(Exception e){return null}
		}
		return null
	}
	
	
	
	/*
		These methods help switch a Google data type to a String (or an Integer/Long/etc. for Rating)
	*/
	static Object asType(com.google.appengine.api.datastore.Category self, Class clazz)
	{
		if(clazz == String)
		{
			return self.getCategory()
		}
		else DefaultGroovyMethods.asType(self, clazz)
	}
	
	static Object asType(Email self, Class clazz)
	{
		if(clazz == String)
		{
			return self.getEmail()
		}
		else DefaultGroovyMethods.asType(self, clazz)
	}

	static Object asType(Link self, Class clazz)
	{
		if(clazz == String)
		{
			return self.getValue()
		}
		else DefaultGroovyMethods.asType(self, clazz)
	}
	
	static Object asType(PhoneNumber self, Class clazz)
	{
		if(clazz == String)
		{
			return self.getNumber()
		}
		else DefaultGroovyMethods.asType(self, clazz)
	}
	
	static Object asType(PostalAddress self, Class clazz)
	{
		if(clazz == String)
		{
			return self.getAddress()
		}
		else DefaultGroovyMethods.asType(self, clazz)
	}
	
	static Object asType(Rating self, Class clazz)
	{
		if(clazz == String)
		{
			return self.getRating().toString()
		}
		else if(clazz == Integer || clazz == int.class)
		{
			return self.getRating() as Integer
		}
		else if(clazz == Byte || clazz == byte.clas)
		{
			return self.getRating() as Byte
		}
		else if(clazz == Long || clazz == long.class)
		{
			return self.getRating() as Long
		}
		else if(clazz == Number)
		{
			return self.getRating() as Number
		}
		else DefaultGroovyMethods.asType(self, clazz)
	}
	
	static Object asType(Text self, Class clazz)
	{
		if(clazz == String)
		{
			return self.getValue()
		}
		else DefaultGroovyMethods.asType(self, clazz)
	}
	
	static GeoPt toGeoPt(String self)
	{
		if(self == null || self.trim() == ""){return null}
		def list = self.split(/,/)
		list = list.collect{Float.parseFloat(it.trim())}
		return GaelykCategory.asType(list, GeoPt)
	}
	
	static Locale toLocale(String self)
	{
		if(self == null){return null}
		def editor = new LocaleEditor();
		editor.setAsText(self.toString());
		return editor.getValue()
	}
	
	static List toLocale(List self)
	{
		if(self == null){return null}
		def list = []
		self.each{locale->
			list << GraelykCategory.toLocale(locale)
		}
		return list
	}
	
	static List toLocale(Object[] self)
	{
		if(self == null){return null}
		def list = []
		self.each{locale->
			list << GraelykCategory.toLocale(locale)
		}
		return list
	}
}