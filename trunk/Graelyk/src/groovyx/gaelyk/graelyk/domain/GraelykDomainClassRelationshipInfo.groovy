package groovyx.gaelyk.graelyk.domain

import groovyx.gaelyk.graelyk.annotation.GDC
import groovyx.gaelyk.graelyk.annotation.Contains
import groovyx.gaelyk.graelyk.util.AnnotationUtils
import groovyx.gaelyk.graelyk.util.GraelykNameUtils
import com.googlecode.objectify.Key

class GraelykDomainClassRelationshipInfo
{
	private Class relationshipInfoDomainClass
	private String relationshipInfoName
	private Class relationshipInfoType
	//private Class relationshipInfoControllerName
	private boolean isCollection = false
	private boolean isArray = false
	private boolean hasKey = false
	private Class collectionOf = null
	private Class keyOf = null
	private boolean xToOne = false //this property only links to one of the related domain class
	private boolean xToMany = false //this property links to multiple of the related domain class
	
	public GraelykDomainClassRelationshipInfo(Class domainClass, String propertyName, Class type)
	{
		this.relationshipInfoDomainClass = domainClass
		this.relationshipInfoName = propertyName
		this.relationshipInfoType = type
		
		establishArrayCollection()
		establishHasKey()
		establishRelationshipValence()
	}
	
	public void establishArrayCollection()
	{
		if(Collection.class.isAssignableFrom(this.relationshipInfoType) || Map.class.isAssignableFrom(this.relationshipInfoType))
		{
			isCollection = true
		}
		else if(this.relationshipInfoType.isArray())
		{
			isArray = true
		}
		
		if(isArray)
		{
			collectionOf = this.relationshipInfoType.getComponentType()
		}
		else if(isCollection)
		{
			collectionOf = AnnotationUtils.getAnnotationValue(this.relationshipInfoDomainClass, this.relationshipInfoName, Contains)
		}
	}
	
	
	public void establishHasKey()
	{
		if(Key.class.isAssignableFrom(this.relationshipInfoType))
		{
			hasKey = true
			keyOf = AnnotationUtils.getAnnotationValue(this.relationshipInfoDomainClass, this.relationshipInfoName, GDC)
		}
		else if((isArray || isCollection) && collectionOf != null && Key.class.isAssignableFrom(collectionOf))
		{
			hasKey = true
			keyOf = AnnotationUtils.getAnnotationValue(this.relationshipInfoDomainClass, this.relationshipInfoName, GDC)
		}
		
		/*
		if(hasKey)
		{
			relationshipInfoControllerName = GraelykNameUtils.getPropertyName(keyOf)
		}
		*/
	}
	
	
	public void establishRelationshipValence()
	{
		if(this.isArray || this.isCollection)
		{
			xToMany = true
		}
		else
		{
			xToOne = true
		}
	}
}
