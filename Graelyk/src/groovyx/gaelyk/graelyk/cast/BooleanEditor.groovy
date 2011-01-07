package groovyx.gaelyk.graelyk.cast

import java.beans.PropertyEditorSupport

class BooleanEditor extends PropertyEditorSupport
{

	public void setAsText(String text)
	{
		boolean objValue
		if(text == null){objValue = false}
		else
		{
			if(text.class == String)
			{
				if(text == "false" || text == "False" || text == "FALSE" || text == "0" || text == "null")
				{
					objValue = false
				}
				else
				{
					objValue = true
				}
			}
			else if(obj)
			{
				objValue = true
			}
			else
			{
				objValue = false
			}
		}
		
		setValue(objValue)
	}
	
	public String getAsText()
	{
		return getValue()
	}
}
