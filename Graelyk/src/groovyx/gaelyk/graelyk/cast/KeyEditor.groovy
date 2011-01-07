package groovyx.gaelyk.graelyk.cast;

import java.beans.PropertyEditorSupport
import com.googlecode.objectify.Key
//import com.google.appengine.api.datastore.Key
import java.lang.reflect.*

import groovyx.gaelyk.obgaektify.ObgaektifyDAO
import groovyx.gaelyk.graelyk.domain.GraelykLongIdDomainClass
import groovyx.gaelyk.graelyk.domain.GraelykStringIdDomainClass

public class KeyEditor extends PropertyEditorSupport
{
	public void setAsText(Class clazz, String id) throws IllegalArgumentException
	{
		if(clazz == null)
		{
			throw new IllegalArgumentException("setAsText cannot be called without a valid Class parameter")
		}
		def key = null
		if(GraelykLongIdDomainClass.isAssignableFrom(clazz))
		{
			key = new Key(clazz, Long.parseLong(id))
		}
		else if(GraelykStringIdDomainClass.isAssignableFrom(clazz))
		{
			key = new Key(clazz, id)
		}
		
		//Set user-friendly key name if the domain class has a toStringForKey() method that returns a string the Key should store
		if(key != null && clazz.getMethods().find{it.name == "toStringForKey"})
		{
			//Run a query to get the referred-to object's data
			def data = (new ObgaektifyDAO()).find(key)
			key.setUserFriendlyName(data.toStringForKey())
		}
		
		setValue(key)
	}
	
	public String getAsText()
	{
		def id = getValue().getId()
		def name = getValue().getName()
		if(id != 0){return id.toString()}
		if(name != null){return name}
		return ""
	}
}
