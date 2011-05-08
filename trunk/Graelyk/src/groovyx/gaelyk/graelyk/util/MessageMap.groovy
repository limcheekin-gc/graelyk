package groovyx.gaelyk.graelyk.util

import java.io.File
import java.text.MessageFormat
import java.util.Locale
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.sil.iso639_3.LanguageByCode

public class MessageMap extends HashMap implements MessageSource
{
	List preferredLocales = [ Locale.getDefault() ]
	Map loadedFiles = [:]
	
	public MessageMap(Locale locale)
	{
		this([locale])
	}
	
	public MessageMap(List locales)
	{
		preferredLocale = locales
	}
	
	public void loadFiles(String filePath, String filePrefix, String fileSuffix, List locales)
	{
		//clear()
		//locales should look something like this: ["en_US", "fr_CA_Alberta", "fr_FR", "es_MX", "ln_CG", "ja"]
		//locales are made of three parts separated by _ underscore, the first is required, the others are optional
		//Part 1: language code
		//Part 2: country code
		//Part 3: Variant string
		if(locales)
		{
			//separate locale list into an array of locales, loop over each locale
			//locales.each{loc->
			for(loc in locales)
			{
				//split this locale (loc) into parts divided by underscore, loop over parts, joining them back together
				def locParts = loc.toString().split("_").toList()
				def currentLocale = ""
				for(locPart in locParts)
				{
					if(locPart)
					{
						currentLocale += "_" + locPart
					}
					//at this point the currentLocale string should look like one of these examples
					//_en
					//_en_US
					//_en_US_Minnesota
					
					//try to find a file that matches this filename:
					File messageFile = new File(filePath + "/" + filePrefix + replaceInvalidFileCharacters(currentLocale) + fileSuffix)
					//make sure the file actually exists, and that we haven't loaded it already
					try
					{
						//localeCode = the currentLocale (without the leading _)
						def localeCode = currentLocale[1..-1]
						if(messageFile.exists() && !(getLoadedFiles().containsKey(localeCode + "|" + messageFile.toString() + "|loaded")))
						{
							MultiLinePropertyReader reader = new MultiLinePropertyReader(messageFile, "UTF-8")
							//load the HashMap that is <reader> and load it into the HashMap that is <this> MessageMap object
							loadMap(reader, StringUtils.parseLocaleString(localeCode))
							//add current locale to map, so we don't repeat loading the same file
							getLoadedFiles()[localeCode + "|" + messageFile.toString() + "|loaded"] = true
						}
					}
					catch(Exception e)
					{
						System.out.println("Error loading message file ${messageFile.toString()}: " + e.toString())
					}
				}
			}
		}
		
		//Get the default file (without any locale embedded)
		try
		{
			File messageFile = new File(filePath + "/" + filePrefix + fileSuffix)
			if(messageFile.exists())
			{
				MultiLinePropertyReader reader = new MultiLinePropertyReader(messageFile, "UTF-8")
				//load the HashMap that is <reader> and load it into the HashMap that is <this> EasyTranslation object
				loadMap(reader, new Locale(""))
			}
		}
		catch(Exception e)
		{
			System.out.println("Error loading default message file ${messageFile.toString()}: " + e.toString())
		}
	}
	
	public static String replaceInvalidFileCharacters(String filename)
	{
		//Replace invalid filename characters with "-"
		return filename.replaceAll(/["<>:\\\/\?\*\|]/, "-")
	}
	
	private void loadMap(HashMap map, Locale locale)
	{
		//This method is used to take HashMaps of data loaded with 
		//MultiLinePropertyReader in the loadFiles() method above
		//and load them into one HashMap (this object) with a slight
		//change to the key-name. The locale of the file is prepended
		//to the key-name.  So a key of "title" loaded from a file with 
		//a locale of "fr_CA" would be put into *this* object's HashMap
		//with a key-name of "fr_CA|title"
		def localePrefix = ""
		if(locale)
		{
			localePrefix = locale.toString().trim() + "|"
		}
		
		//Copy all entries from map into *this*
		//Replace ' (single quote) with '' (two single quotes) in all localization text, because MessageFormat treats single quote as a special character
		for(Object key in map.keySet())
		{
			this.put(localePrefix + key.toString(), map.get(key).replaceAll(/'/, "''"))
		}
	}
	
	//Given a MessageMap, this method will add all key-value pairs in the fallback MessageMap
	//to the current MessageMap, except where the key already exists in this MessageMap.
	public void addMessages(MessageMap msgMapToAdd)
	{
		//msgMapToAdd.each{key, value->
		for(kvEntry in msgMapToAdd)
		{
			def key = kvEntry.key
			def value = kvEntry.value
			if(!this.containsKey(key))
			{
				this[key] = value
			}
		}
		
		//msgMapToAdd.getLoadedFiles().each{key, value->
		def lf = msgMapToAdd.getLoadedFiles()
		for(kvEntry in lf)
		{
			def key = kvEntry.key
			def value = kvEntry.value
			if(!this.getLoadedFiles().containsKey(key))
			{
				this.getLoadedFiles()[key] = value
			}
		}
	}
	
	public String getAt(String key)
	{
		return getMessage(key, preferredLocales)
	}
	
	public String getMessage(String key)
	{
		return getMessage(key, preferredLocales)
	}

	public String getMessage(String key, Locale locale)
	{
		return getMessage(key, [locale])
	}
	
	public String getMessage(String key, List localeOrArgList)
	{
		//If the localeOrArgList is a list of Locale, process it in this method.
		//Otherwise, process it in the getMessage(String key, Locale locale, List args) method.
		for(localeOrArg in localeOrArgList)
		{
			if(!localeOrArg instanceof Locale)
			{
				//This item in the list is not a Locale. 
				//Therefore we assume the list is a list of arguments for
				//insertion into the message, and we forward to the correct method.
				return getMessage(key, preferredLocale, args)
			}
		}
		def localeList = localeOrArgList
		String value = ""
		for(locale in localeList)
		{
			//tryLocales() retrieves locales in order from most specific to most general
			String[] locales = tryLocales(locale)
			for(String loc in locales)
			{
				if(loc != null)
				{
					def localePrefix = ""
					if(loc)
					{
						localePrefix = loc.toString().trim() + "|"
					}
					
					//get the value for the locale we're trying
					value = (String)get(localePrefix + key)
		
					//if the value is not null or blank, return it
					if(value)
					{
						break
					}
				}
			}
			if(value)
			{
				break
			}
		}
		//If no locale has returned a value, try the "no prefix" prefix
		if(!value){value = (String)get(new Locale("").toString() + "|" + key)}
		
		//If there is no value set, return ""
		if(!value){value = ""}
		return value
	}
	
	public String getMessage(String key, Object[] args)
	{
		return getMessage(key, preferredLocales, args)
	}
	
	public String getMessage(String key, Locale locale, List args)
	{
		return getMessage(key, [locale], args)
	}
	
	public String getMessage(String key, List localeList, List args)
	{
		def message = getMessage(key, localeList)
		return MessageFormat.format(message, args as Object[])
	}
	
	public String getMessage(String key, Locale locale, Object[] args)
	{
		return getMessage(key, [locale], args)
	}
	
	public String getMessage(String key, List locales, Object[] args)
	{
		def message = getMessage(key, locales)
		return MessageFormat.format(message, args as Object[])
	}
	
	//********************************************
	//Implementations of MessageSource methods
	String getMessage(String key, Object[] args, String defaultMessage, Locale locale)
	{
		def message = getMessage(key, [locale])
		if(!message){message = defaultMessage}
		return MessageFormat.format(message, args as Object[])
	}
	
	String getMessage(String key, Object[] args, Locale locale) throws NoSuchMessageException
	{
		def message = getMessage(key, [locale])
		if(!message){throw new NoSuchMessageException("No message defined for key [$key] and for locale [$locale] or default locale.")}
		return MessageFormat.format(message, args as Object[])
	}

	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException
	{
		if(resolvable)
		{
			def keyArray = resolvable.getCodes()
			def args = resolvable.getArguments()
			def defaultMessage = resolvable.getDefaultMessage()
			
			def message = getMessage(key, [locale])
			if(!message){message = defaultMessage}
			if(!message){throw new NoSuchMessageException("No message defined for key [$key] and for locale [$locale] or default locale.")}
			return MessageFormat.format(message, args as Object[])
		}
		return null
	}
	//End Implementations of MessageSource methods
	//********************************************
	String getMessage(MessageSourceResolvable resolvable, List locales) throws NoSuchMessageException
	{
		if(resolvable)
		{
			def keyArray = resolvable.getCodes()
			def args = resolvable.getArguments()
			def defaultMessage = resolvable.getDefaultMessage()
			
			def message = getMessage(key, locales)
			if(!message){message = defaultMessage}
			if(!message){throw new NoSuchMessageException("No message defined for key [$key] and for locale [$locale] or default locale.")}
			return MessageFormat.format(message, args as Object[])
		}
		return null
	}

	
	public static String[] tryLocales(Locale locale)
	{
		//This function takes a locale string and returns an array of
		//string locales, in the order they should be tried.
		//For example, given a locale of en_US_California, it should return the array:
		//String[]{"en_US_California", "en_US", "en", ""}
		if(!locale)
		{
			return [""] as String[]
		}
		
		String[] parts = locale.toString().split("_")

		def altCode = ""
		if(parts[0] =~ /^[a-z]{2,2}$/){altCode = LanguageByCode.get(parts[0]).getCode()}
		else if(parts[0] =~ /^[a-z]{3,3}$/){altCode = LanguageByCode.get(parts[0]).getPart1Code()}
		
		
		int length = (parts.length * 2) + 1
		if(length > 7){length = 7}
		String[] options = new String[length]
		if(parts.length == 3)
		{
			options[0] = parts[0] + "_" + parts[1] + "_" + parts[2]
			if(altCode){options[1] = altCode + "_" + parts[1] + "_" + parts[2]}
			options[2] = parts[0] + "_" + parts[1]
			if(altCode){options[3] = altCode + "_" + parts[1]}
			options[4] = parts[0]
			if(altCode){options[5] = altCode}
			options[6] = ""
		}
		else if(parts.length == 2)
		{
			options[0] = parts[0] + "_" + parts[1]
			if(altCode){options[1] = altCode + "_" + parts[1]}
			options[2] = parts[0]
			if(altCode){options[3] = altCode}
			options[4] = ""
		}
		else if(parts.length == 1)
		{
			options[0] = parts[0]
			if(altCode){options[1] = altCode}
			options[2] = ""
		}
		return options
	}
}