package groovyx.gaelyk.graelyk.taglyk

import groovyx.gaelyk.graelyk.util.StringUtils
import groovyx.gaelyk.graelyk.StaticResourceHolder
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException

class MessageTaglyk
{
	static appDefaultLocales = StaticResourceHolder.getAppProperties().appDefaultLocales
	
	//Methods that start with g_ print straight to script.out
	//Methods that don't start with g_ return a String or some other object

	/*
	static void g_resolveLocale(script, firstLocale, secondLocale){script.out << script.resolveLocale(firstLocale, secondLocale).toString()}
	static void g_resolveLocaleList(script, firstLocale, secondLocale){script.out << script.resolveLocaleList(firstLocale, secondLocale).toString()}
	*/
	static void g_message(script, Map attrs){script.out << script.message(attrs)}
	static void g_message(script, String code, List args=[]){script.out << script.message(code, args)}
	static void g_message(script, String code, List args, defaultMessage){script.out << script.message(code, args, defaultMessage)}
	static void g_message(script, String code, List args, defaultMessage, locale){script.out << script.message(code, args, defaultMessage, locale)}
	
	static List joinLocaleLists(list1, list2)
	{
		def newList = []
		if(!(list1 instanceof List))
		{
			list1 = [list1]
		}
		if(!(list2 instanceof List))
		{
			list2 = [list2]
		}
		for(item in list1)
		{
			if(!newList.contains(item))
			{
				newList << item
			}
		}
		for(item in list2)
		{
			if(!newList.contains(item))
			{
				newList << item
			}
		}
		return newList
	}
	
	/*
	static Locale resolveLocale(script, localeAttr=null)
	{
		 def locale = script.userLocale
		 if(!locale)
		 {
			locale = localeAttr
		 }
         if(locale != null && !(locale instanceof Locale))
		 {
         	locale=StringUtils.parseLocaleString(locale as String)
         }
         if(locale==null)
		 {
      		locale=Locale.getDefault()
         }
         return locale
    }
    */
	static Locale resolveLocale(script, firstLocale, secondLocale)
	{
		return script.resolveLocaleList(firstLocale, secondLocale)[0]
	}
	
	static List resolveLocaleList(script, firstLocale, secondLocale)
	{
		for(testLocale in [firstLocale, secondLocale])
		{
			if(testLocale)
			{
				if(testLocale instanceof Locale)
				{
					return [testLocale]
				}
				else if(testLocale instanceof List)
				{
					def allLocales = true
					for(locale in testLocale)
					{
						if(!(locale instanceof Locale))
						{
							allLocales = false
						}
					}
					if(allLocales)
					{
						return testLocale
					}
				}
			}
		}
		return secondLocale
    }
	
	
	/*
		Return the message from the MessageMap in script.messageBundle
		with the key of "code" and the replacement values "args"
		using the user's current userLocale
	*/
	static String message(script, Map attrs)
	{
        def text
        def error = attrs['error'] ?: attrs['message']
        if(error)
		{
            try
            {
                text = script.message(error, script.userLocale)
            } 
			catch (NoSuchMessageException e)
			{
                if(error instanceof MessageSourceResolvable)
				{
                    text = error?.code
                }
				else
				{
                    text = error?.toString()
                }
            }
		}
		else if(attrs['code'])
		{
            def code = attrs['code']
            def args = attrs['args']
            def defaultMessage = ( attrs['default'] != null ? attrs['default'] : code )

            def message = script.message(code, args, defaultMessage)
			
            if(message)
			{
                text = message
            }
            else
			{
                text = defaultMessage
            }
        }
        if(text)
		{
            return (attrs.encodeAs ? text.encodeAs(attrs.encodeAs) : text)
        }
        return ''
	}
	
	static String message(script, String code, List args=[])
	{
		return script.message(code, args, code, script.userLocale)
	}
	
	static String message(script, String code, List args, defaultMessage)
	{
		return script.message(code, args, defaultMessage, script.userLocale)
	}
	
	static String message(script, String code, List args, defaultMessage, locale)
	{
		def jointLocale = joinLocaleLists(locale, appDefaultLocales)
		def message = script.messageBundle.getMessage(code, jointLocale, args)
		
		if(!message)
		{
			if(defaultMessage != null)
			{
				if(defaultMessage instanceof Closure)
				{
					message = defaultMessage()
				}
				else
				{
					message = defaultMessage as String
				}
			}
		}

		return message
	}
	
	static message(script, MessageSourceResolvable resolvable, Locale locale)
	{
		def jointLocale = joinLocaleLists([locale], appDefaultLocales)
		return script.messageBundle.getMessage(resolvable, jointLocale)
	}
	
	static message(script, MessageSourceResolvable resolvable, List locales)
	{
		def jointLocale = joinLocaleLists(locales, appDefaultLocales)
		return script.messageBundle.getMessage(resolvable, jointLocale)
	}
}