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

import groovy.lang.Range;

import groovyx.gaelyk.graelyk.util.GraelykClassUtils;
import org.springframework.validation.Errors;

/**
 * A Constraint that validates a range.
 *
 * @author Graeme Rocher
 * @since 0.4
 */
public class RangeConstraint extends AbstractConstraint {

    Range range;

    /**
     * @return Returns the range.
     */
    public Range getRange() {
        return range;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public boolean supports(Class type) {
        return type != null && (Comparable.class.isAssignableFrom(type) ||
                GraelykClassUtils.isAssignableOrConvertibleFrom(Number.class, type));
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
     */
    @Override
    public void setParameter(Object constraintParameter) {
        if (!(constraintParameter instanceof Range)) {
            throw new IllegalArgumentException("Parameter for constraint ["+ConstrainedProperty.RANGE_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a of type [groovy.lang.Range]");
        }

        range = (Range)constraintParameter;
        super.setParameter(constraintParameter);
    }

    public String getName() {
        return ConstrainedProperty.RANGE_CONSTRAINT;
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        if (!range.contains(propertyValue)) {
            Object[] args = [constraintPropertyName, constraintOwningClass,
                    propertyValue, range.getFrom(), range.getTo()] as Object[];

            Comparable from = range.getFrom();
            Comparable to = range.getTo();

            if (from instanceof Number && propertyValue instanceof Number) {
                // Upgrade the numbers to Long, so all integer types can be compared.
                from = new Long(((Number) from).longValue());
                to = new Long(((Number) to).longValue());
                propertyValue = new Long(((Number) propertyValue).longValue());
            }

            if (from.compareTo(propertyValue) > 0) {
                rejectValue(target,errors,ConstrainedProperty.DEFAULT_INVALID_RANGE_MESSAGE_CODE, ConstrainedProperty.RANGE_CONSTRAINT + ConstrainedProperty.TOOSMALL_SUFFIX,args );
            }
            else if (to.compareTo(propertyValue) < 0) {
                rejectValue(target,errors,ConstrainedProperty.DEFAULT_INVALID_RANGE_MESSAGE_CODE, ConstrainedProperty.RANGE_CONSTRAINT + ConstrainedProperty.TOOBIG_SUFFIX,args );
            }
        }
    }
}
