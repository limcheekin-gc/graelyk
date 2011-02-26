package groovyx.gaelyk.graelyk.validation

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.io.Serializable
import groovyx.gaelyk.graelyk.StaticResourceHolder

class GraelykErrors extends BeanPropertyBindingResult implements Serializable
{
	def messageSource
	
	public GraelykErrors(Object target, String objectName)
	{
		this(target, objectName, true);
	}

	public GraelykErrors(Object target, String objectName, boolean autoGrowNestedPaths)
	{
		super(target, objectName, autoGrowNestedPaths);
	}
	
	public setMessageSource(msgSrc)
	{
		messageSource = this.messageSource = msgSrc
	}
	
	public void addError(String messageCode)
	{
		addError(messageCode, [] as Object[])
	}
	
    public void addError(String messageCode, Object[] args)
    {
        BindingResult result = (BindingResult) this;

        FieldError error = new FieldError(
                objectName,
                "",
                "",
                false,
                [] as String[],
                args,
                messageCode
        );
        ((BindingResult)this).addError(error);
    }
    
    protected String getDefaultMessage(String code)
    {
        String defaultMessage;

        try {
            if(messageSource != null) {
                defaultMessage = messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
            }
            else {
                defaultMessage = (String)ConstrainedProperty.DEFAULT_MESSAGES.get(code);
            }
        }
        catch(Exception e)
        {
            defaultMessage = (String)ConstrainedProperty.DEFAULT_MESSAGES.get(code);
        }
        return defaultMessage;
    }
}