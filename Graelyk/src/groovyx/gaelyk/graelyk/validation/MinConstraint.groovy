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
 * A Constraint that implements a minimum value constraint.
 *
 * @author Graeme Rocher
 * @since 0.4
 */
public class MinConstraint extends AbstractConstraint {

    private Comparable minValue;

    /**
     * @return Returns the minValue.
     */
    public Comparable getMinValue() {
        return minValue;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public boolean supports(Class type) {
        return type != null && (
                Comparable.class.isAssignableFrom(type) ||
                GraelykClassUtils.isAssignableOrConvertibleFrom(Number.class, type));
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
     */
    @Override
    public void setParameter(Object constraintParameter) {
        if (constraintParameter == null) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                    ConstrainedProperty.MIN_CONSTRAINT + "] of property [" +
                    constraintPropertyName + "] of class [" + constraintOwningClass + "] cannot be null");
        }

        if (!(constraintParameter instanceof Comparable<?>) && (!constraintParameter.getClass().isPrimitive())) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                    ConstrainedProperty.MIN_CONSTRAINT + "] of property [" +
                    constraintPropertyName + "] of class [" + constraintOwningClass +
                    "] must implement the interface [java.lang.Comparable]");
        }

        Class<?> propertyClass = GraelykClassUtils.getPropertyType( constraintOwningClass, constraintPropertyName );
        if (!GraelykClassUtils.isAssignableOrConvertibleFrom( constraintParameter.getClass(),propertyClass )) {
            throw new IllegalArgumentException("Parameter for constraint ["+ConstrainedProperty.MIN_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be the same type as property: [" + propertyClass.getName() + "]");
        }

        minValue = (Comparable<?>)constraintParameter;
        super.setParameter(constraintParameter);
    }

    public String getName() {
        return ConstrainedProperty.MIN_CONSTRAINT;
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors)        {
        if (minValue.compareTo(propertyValue) > 0) {
            Object[] args = [constraintPropertyName, constraintOwningClass, propertyValue, minValue] as Object[]
            rejectValue(target,errors,ConstrainedProperty.DEFAULT_INVALID_MIN_MESSAGE_CODE, ConstrainedProperty.MIN_CONSTRAINT + ConstrainedProperty.NOTMET_SUFFIX,args);
        }
    }
}
