package groovyx.gaelyk.graelyk.validation;

import org.springframework.validation.Errors;
import groovyx.gaelyk.obgaektify.ObgaektifyCategory;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;

class UniqueConstraint extends AbstractConstraint
{
	private boolean unique
	
    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
	boolean supports(Class type)
	{
		//Don't support null, Text, Blob, arrays or Collections (List, Map)
		if(type == null || type == Text || type == Blob || Collection.isAssignableFrom(type) || type.isArray())
		{
			return false
		}
		//Only support the index-able Google App Engine data types
		else if([String,ShortBlob,boolean.class,Boolean,short.class,Short,int.class,Integer,long.class,Long,float.class,Float,double.class,Double,Date,User,Key,Category,Email,GeoPt,IMHandle,Link,PhoneNumber,PostalAddress,Rating].contains(type))
		{
			return true
		}
		//Which means don't support Serializable
		return false
	}


    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
     */
    @Override
    public void setParameter(Object constraintParameter) {
        if (!(constraintParameter instanceof Boolean)) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                    ConstrainedProperty.UNIQUE_CONSTRAINT + "] of property [" +
                    constraintPropertyName + "] of class [" + constraintOwningClass +
                    "] must be a boolean value");
        }

        unique = ((Boolean)constraintParameter).booleanValue();
        super.setParameter(constraintParameter);
    }

    public String getName() {
        return ConstrainedProperty.UNIQUE_CONSTRAINT;
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        if(!unique)
        {
            return;
        }

		Object[] args = [constraintPropertyName, constraintOwningClass, propertyValue] as Object[]
        List keys = ObgaektifyCategory.searchKeys(constraintOwningClass, [filter:[(constraintPropertyName+"=="):propertyValue, "id!=":ObgaektifyCategory.resolveId(constraintOwningClass, target.id)]]);
        if(keys && keys.size() > 0)
        {
			rejectValue(target, errors, ConstrainedProperty.DEFAULT_NOT_UNIQUE_MESSAGE_CODE,
				ConstrainedProperty.NOT_PREFIX + ConstrainedProperty.UNIQUE_CONSTRAINT, args);
        }
    }
}
