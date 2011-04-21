
import java.lang.reflect.*
import java.beans.PropertyDescriptor

public class GenericsUtils
{
	static List getGenericType(Type type)
	{
		/*
		if(type instanceof ParameterizedType)
		{
			ParameterizedType aType = (ParameterizedType)type;
			List types = getActualTypeArguments(aType);
			return types
		}
		else
		{
		*/
			return getActualTypeArguments(type)
		/*
		}
		*/
	}

	static List getGenericType(Class clazz, String fieldName)
	{
		try
		{
			Field field = clazz.getDeclaredField(fieldName);

			if(field)
			{
				Type genericFieldType = field.getGenericType();
				/*	
				if(genericFieldType instanceof ParameterizedType)
				{
					ParameterizedType aType = (ParameterizedType)genericFieldType;
					List fieldArgTypes = getActualTypeArguments(aType);
					return fieldArgTypes
				}
				else
				{
				*/
					return getActualTypeArguments(genericFieldType)
				/*
				}
				*/
			}
		}
		catch(NoSuchFieldException e)
		{
			if(clazz != Object)
			{
				return getGenericType(clazz.getSuperclass(), fieldName)
			}
		}
		return []
	}
	
	static boolean hasGenericTypeAssignableTo(Class clazz, String fieldName, int index, Class assignableTo)
	{
		//First Generic Argument is index 0
		//Second Generic Argument is index 1
		def types = getGenericType(clazz, fieldName)
		if(types.size() <= index+1){return false}
		return assignableTo.isAssignableFrom(types[index+1][0])
	}
	
	static boolean hasGenericTypeAssignableTo(Class clazz, String fieldName, Class[] genericTypes)
	{
		def types = getGenericType(clazz, fieldName)
		if(types.size() == 1 && (genericTypes == null || genericTypes.size() == 0))
		{
			return true
		}
		else if(genericTypes == null || genericTypes.size() == 0)
		{
			return false
		}
		else if(types.size() == genericTypes.size()+1)
		{
			for(int i=0; i<genericTypes.size(); i++)
			{
				if(!genericTypes[i+1][0].isAssignableFrom(types[i][0]))
				{
					return false
				}
			}
			return true
		}
		return false
	}
	
	
	static List getActualTypeArguments(Type type)
	{
		def typeString = "[" + type.toString() + "]"
		typeString = typeString.replaceAll(/</, ", [")
		typeString = typeString.replaceAll(/>/, "]")
		typeString = typeString.replaceAll(/([ ,\[])interface /, "\$1")
		typeString = typeString.replaceAll(/([ ,\[])class /, "\$1")
		typeString = typeString.replaceAll(/\[L([^ ,;]+);/, "\$1[]")
		println(typeString)
		def shell = new GroovyShell()
		return shell.evaluate(typeString)
	}
	
	
	/*
		List<Key<String>>
	
		[List,
			[Key,
				[String]
			]
		]
		
		Map<String, Key<String>>
		
		[Map, [String, Key[String]]]
	
	*/
}