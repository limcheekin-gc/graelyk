package groovyx.gaelyk.graelyk.validation

import org.springframework.validation.BeanPropertyBindingResult
import java.io.Serializable

class GraelykErrors extends BeanPropertyBindingResult implements Serializable
{
	public GraelykErrors(Object target, String objectName)
	{
		this(target, objectName, true);
	}

	public GraelykErrors(Object target, String objectName, boolean autoGrowNestedPaths)
	{
		super(target, objectName, autoGrowNestedPaths);
	}
}