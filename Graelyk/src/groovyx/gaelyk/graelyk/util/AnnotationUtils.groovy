package groovyx.gaelyk.graelyk.util

import java.lang.reflect.*
import java.lang.annotation.Annotation
import groovyx.gaelyk.graelyk.domain.GraelykDomainClass

class AnnotationUtils
{
	static hasAnnotation(Class clazz, String fieldName, Class annotationClass)
	{
		try
		{
			def field = clazz.getDeclaredField(fieldName)
			if(field)
			{
				def annotation = field.getAnnotation(annotationClass)
				if(annotation)
				{
					return true
				}
			}
		}
		catch(NoSuchFieldException e)
		{
			if(clazz != GraelykDomainClass)
			{
				return hasAnnotation(clazz.getSuperclass(), fieldName, annotationClass)
			}
		}
		return false
	}
	
	static Annotation getAnnotation(Class clazz, String fieldName, Class annotationClass)
	{
		try
		{
			def field = clazz.getDeclaredField(fieldName)
			if(field)
			{
				def annotation = field.getAnnotation(annotationClass)
				if(annotation)
				{
					return annotation
				}
			}
		}
		catch(NoSuchFieldException e)
		{
			if(clazz != GraelykDomainClass)
			{
				return getAnnotation(clazz.getSuperclass(), fieldName, annotationClass)
			}
		}
		return null
	}
	
	static Object getAnnotationValue(Class clazz, String fieldName, Class annotationClass)
	{
		try
		{
			def field = clazz.getDeclaredField(fieldName)
			if(field)
			{
				def annotation = field.getAnnotation(annotationClass)
				if(annotation)
				{
					return annotation.value()
				}
			}
		}
		catch(NoSuchFieldException e)
		{
			if(clazz != GraelykDomainClass)
			{
				return getAnnotationValue(clazz.getSuperclass(), fieldName, annotationClass)
			}
		}
		return null
	}

}