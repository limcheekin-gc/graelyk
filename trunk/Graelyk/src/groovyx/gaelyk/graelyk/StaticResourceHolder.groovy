package groovyx.gaelyk.graelyk

import groovyx.gaelyk.graelyk.validation.GraelykDomainClassValidator
import groovyx.gaelyk.graelyk.util.MessageMap
import groovyx.gaelyk.graelyk.util.MultiLinePropertyReader
import groovyx.gaelyk.graelyk.util.StringUtils;
import com.google.apphosting.api.ApiProxy

class StaticResourceHolder
{
	static Map appProperties //Stores properties set in the graelyk.properties file.
	static MessageMap messageBundle //Stores localization/il8n message bundles.
	static Map validators = [:] //Stores validators for domain classes - once a domain class' validator is loaded, future instances of the same domain class will load their validator from this Map to save time.
	static Map resources = [:] //A place to store any other static resources your application might need. e.g. see FormTaglyk.localeListForUserLocale()
	static boolean localMode = ApiProxy.currentEnvironment.class.name.contains("LocalHttpRequestEnvironment")
	static Map getAppProperties()
	{
		if(!appProperties)
		{
			String filename = "WEB-INF/graelyk.properties"
			String encoding="UTF-8"
			appProperties = new MultiLinePropertyReader(filename, encoding)
			
			//Process certain properties to transform them for easier use
			def localeSeparator = ","
			if(appProperties.appLocaleSeparator)
			{
				localeSeparator = appProperties.appLocaleSeparator
			}
			if(appProperties.appLocales)
			{
				appProperties.appLocales = appProperties.appLocales.split(localeSeparator).toList().collect{StringUtils.parseLocaleString(it.trim())} ?: [Locale.getDefault()]
			}
			if(appProperties.appDefaultLocales)
			{
				appProperties.appDefaultLocales = appProperties.appDefaultLocales.split(localeSeparator).toList().collect{StringUtils.parseLocaleString(it.trim())} ?: [Locale.getDefault()]
			}
			if(appProperties.appDefaultNumberLocale)
			{
				appProperties.appDefaultNumberLocale = StringUtils.parseLocaleString(appProperties.appDefaultNumberLocale.trim()) ?: Locale.getDefault()
			}
			if(appProperties.appDefaultDateLocale)
			{
				appProperties.appDefaultDateLocale = StringUtils.parseLocaleString(appProperties.appDefaultDateLocale.trim()) ?: Locale.getDefault()
			}
			if(appProperties.appDefaultCurrencyLocale)
			{
				appProperties.appDefaultCurrencyLocale = StringUtils.parseLocaleString(appProperties.appDefaultCurrencyLocale.trim()) ?: Locale.getDefault()
			}
			
			//Set the correct server url.
			if(StaticResourceHolder.localMode)
			{
				if(appProperties["serverURL.localMode"])
				{
					appProperties.serverURL = appProperties["serverURL.localMode"]
				}
			}
			else
			{
				if(appProperties["serverURL.liveMode"])
				{
					appProperties.serverURL = appProperties["serverURL.liveMode"]
				}
			}
		}
		
		return appProperties
	}
	
	static Map getMessageBundle(String domainClassName)
	{
		if(!messageBundle)
		{
			messageBundle = new MessageMap(getAppProperties()["appLocales"])
		}
		
		getMessageBundle(domainClassName, [])
		/*
		if(!messageBundle.containsKey("domainClassName))
		{
			def localeList = getAppProperties()["appLocales"]
			messageBundle[domainClassName] = new MessageMap("graelyk-i18n/${domainClassName}", "messages", ".properties", localeList)
			messageBundle[domainClassName].addFallbackMessages(new MessageMap("graelyk-i18n/", "messages", ".properties", localeList))
		}
		return messageBundle[domainClassName]
		*/
	}
	
	static Map getMessageBundle(String domainClassName, List localeList)
	{
		//Add the default locales to the localeList that was passed in. The localeList passed in could be the user's preferred locales. The site's default locales are added to this list.
		getAppProperties()["appLocales"].each{loc->
			if(!localeList.contains(loc))
			{
				localeList << loc
			}
		}
		
		//Load the messages for the base message file for each locale. (MessageMap takes care of making sure that the same file is not loaded over and over again.)
		messageBundle.loadFiles("graelyk-i18n/", "messages", ".properties", localeList)

		//Load the messages for the domainClassName message file for each locale. (MessageMap takes care of making sure that the same file is not loaded over and over again.)
		messageBundle.loadFiles("graelyk-i18n/${domainClassName}", "messages", ".properties", localeList)
		
		return messageBundle
	}
	
	
	static GraelykDomainClassValidator getValidator(String domainClassName)
	{
		return validators[domainClassName]
	}
	
	static void setValidator(String domainClassName, GraelykDomainClassValidator v)
	{
		validators[domainClassName] = v
	}
	
	static getAt(String resourceKey)
	{
		return resources[resourceKey]
	}
	
	static setAt(String resourceKey, value)
	{
		resources[resourceKey] = value
	}
	
	static getResource(String resourceKey)
	{
		return resources[resourceKey]
	}
	
	static void setValidator(String resourceKey, value)
	{
		resources[resourceKey] = value
	}
}