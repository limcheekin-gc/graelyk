/* Copyright 2004-2005 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//Originally package org.codehaus.groovy.grails.validation;
package groovyx.gaelyk.graelyk.validation

import groovyx.gaelyk.graelyk.util.GraelykClassUtils;

import org.springframework.validation.Errors;

/**
 * A constraint that validates the property is contained within the supplied list.
 *
 * @author Graeme Rocher
 * @since 0.4
 */
public class InListConstraint extends AbstractConstraint {

    List<?> list;

    /**
     * @return Returns the list.
     */
    public List<?> getList() {
        return list;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public boolean supports(Class type) {
        return type != null;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
     */
    @Override
    public void setParameter(Object constraintParameter) {
        if (!(constraintParameter instanceof List<?>)) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                    ConstrainedProperty.IN_LIST_CONSTRAINT + "] of property [" +
                    constraintPropertyName + "] of class [" + constraintOwningClass +
                    "] must implement the interface [java.util.List]");
        }

        list = (List<?>)constraintParameter;
        
    	//Try to cast the items in the list to the same type as the property type
    	try
    	{
   			list = list.collect{it.asType(GraelykClassUtils.getPropertyValueType(this.constraintOwningClass, this.constraintPropertyName))}
   			constraintParameter = list
    	}
    	catch(Exception e){System.out.println("exception $e")}
    	
        
        super.setParameter(constraintParameter);
    }

    public String getName() {
        return ConstrainedProperty.IN_LIST_CONSTRAINT;
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors)
	{
		//Array of values (multiple select list) that each need to match the inList options
		if(propertyValue instanceof Object[] || propertyValue instanceof Collection)
		{
			propertyValue.each{value->
				// Check that the list contains the given value. If not, add an error.
				if (!list.contains(value)) {
					Object[] args = [constraintPropertyName, constraintOwningClass, value, list] as Object[]
					rejectValue(target, errors, ConstrainedProperty.DEFAULT_NOT_INLIST_MESSAGE_CODE,
							ConstrainedProperty.NOT_PREFIX + ConstrainedProperty.IN_LIST_CONSTRAINT, args);
				}
			}
		}
		//A single value that needs to match the inList options
		else
		{
			// Check that the list contains the given value. If not, add an error.
			if (!list.contains(propertyValue)) {
				Object[] args = [constraintPropertyName, constraintOwningClass, propertyValue, list] as Object[]
				rejectValue(target, errors, ConstrainedProperty.DEFAULT_NOT_INLIST_MESSAGE_CODE,
						ConstrainedProperty.NOT_PREFIX + ConstrainedProperty.IN_LIST_CONSTRAINT, args);
			}
		}
    }
}
