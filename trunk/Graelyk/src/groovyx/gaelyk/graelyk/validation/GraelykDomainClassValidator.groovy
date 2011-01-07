/* Copyright 2004-2005 the original author or authors.
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

import groovy.lang.GroovyObject;
import org.codehaus.groovy.runtime.InvokerHelper;
/*
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
*/

import groovyx.gaelyk.graelyk.domain.GraelykDomainClass;
import groovyx.gaelyk.graelyk.domain.GraelykDomainClassProperty;
import groovyx.gaelyk.graelyk.util.MessageMap

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import java.util.*;

/**
 * A specialised Spring validator that validates a domain class instance using the constraints defined in the
 * static constraints closure.
 *
 *
 * @author Graeme Rocher
 * @since 0.1
 *
 * Created: 07-Nov-2005
 */
@SuppressWarnings("serial")
public class GraelykDomainClassValidator implements Validator, CascadingValidator 
{
    private static final List EMBEDDED_EXCLUDES = [GraelykDomainClassProperty.IDENTITY]

    protected Class targetClass;
    protected GraelykDomainClass domainClass;
    protected MessageSource messageSource;
    private static final String ERRORS_PROPERTY = "errors";
    //protected GrailsApplication grailsApplication;

    public boolean supports(Class clazz) {
        return this.targetClass.equals( clazz );
    }


    /**
     * @param domainClass The domainClass to set.
     */
    public void setDomainClass(GraelykDomainClass domainClass) {
        this.domainClass = domainClass;
        this.domainClass.setValidator(this);
        this.targetClass = this.domainClass.getClazz();
    }


    public GraelykDomainClass getDomainClass() {
        return domainClass;
    }

    /**
	 * @param messageSource The messageSource to set.
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

    /**
     * @see org.codehaus.groovy.grails.validation.CascadingValidator#validate(Object, org.springframework.validation.Errors, boolean)
     */
    public void validate(Object obj, Errors errors, boolean cascade) {
        if(!domainClass.getClazz().isInstance(obj))
            throw new IllegalArgumentException("Argument ["+obj+" " + obj?.class + "] is not an instance of ["+domainClass.getClazz()+"] which this validator is configured for");

        BeanWrapper bean = new BeanWrapperImpl(obj);

        Map constrainedProperties = domainClass.getConstrainedProperties();
        Set constrainedPropertyNames = new HashSet(constrainedProperties.keySet());

        GraelykDomainClassProperty[] persistentProperties = domainClass.getPersistentProperties();

        for (int i = 0; i < persistentProperties.length; i++) {
            GraelykDomainClassProperty persistentProperty = persistentProperties[i];
            String propertyName = persistentProperty.getName();
            if(constrainedProperties.containsKey(propertyName)) {
                validatePropertyWithConstraint(propertyName, obj, errors, bean, constrainedProperties);
            }

            if((persistentProperty.isAssociation() || persistentProperty.isEmbedded()) && cascade) {
                cascadeToAssociativeProperty(errors, bean, persistentProperty);
            }

            // Remove this property from the set of constrained property
            // names because we have already processed it.
            constrainedPropertyNames.remove(propertyName);
        }

        // Now process the remaining constrained properties, for example
        // any transients.
        for (Iterator iter = constrainedPropertyNames.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            validatePropertyWithConstraint(name, obj, errors, bean, constrainedProperties);
        }

         if(obj instanceof GroovyObject) {
            ((GroovyObject)obj).setProperty(ERRORS_PROPERTY, errors);
         }
         else {
            InvokerHelper.setProperty(obj,ERRORS_PROPERTY,errors);
         }

        postValidate(obj,errors);
    }

    /**
     * Subclasses can overrite to provide custom handling of the errors object post validation
     *
     * @param obj  The object to validate
     * @param errors The Errors object
     */
    protected void postValidate(Object obj, Errors errors) {
        // do nothing
    }

    /**
     * @see org.springframework.validation.Validator#validate(Object, org.springframework.validation.Errors)
     */
	public void validate(Object obj, Errors errors) {
        validate(obj,errors,false);
    }

    /**
     * Cascades validation onto an associative property maybe a one-to-many, one-to-one or many-to-one relationship
     *
     * @param errors The Errors instnace
     * @param bean The original bean
     * @param persistentProperty The associative property
     */
    protected void cascadeToAssociativeProperty(Errors errors, BeanWrapper bean, GraelykDomainClassProperty persistentProperty) {
        String propertyName = persistentProperty.getName();
        if(errors.hasFieldErrors(propertyName)) return;
        if(persistentProperty.isManyToOne() || persistentProperty.isOneToOne() || persistentProperty.isEmbedded() ) {
            Object associatedObject = bean.getPropertyValue(propertyName);
            cascadeValidationToOne(errors, bean,associatedObject, persistentProperty, propertyName);
        }
        else if(persistentProperty.isOneToMany()) {
            cascadeValidationToMany(errors, bean, persistentProperty, propertyName);
        }
    }

    /**
     * Cascades validation to a one-to-many type relationship. Normally a collection such as a List or Set each element
     * in the association will also be validated
     *
     * @param errors The Errors instance
     * @param bean The original BeanWrapper
     * @param persistentProperty An association whose isOneToMeny() method returns true
     * @param propertyName The name of the property
     */
    protected void cascadeValidationToMany(Errors errors, BeanWrapper bean, GraelykDomainClassProperty persistentProperty, String propertyName) {
        Object collection = bean.getPropertyValue(propertyName);
        if(collection instanceof Collection) {
            for (Iterator i = ((Collection) collection).iterator(); i.hasNext();) {
                Object associatedObject = i.next();
                cascadeValidationToOne(errors, bean,associatedObject, persistentProperty, propertyName);
            }
        }
        else if(collection instanceof Map) {
            Map map = (Map)collection;
            for (Iterator i = map.values().iterator(); i.hasNext();) {
                Object associatedObject = i.next();
                cascadeValidationToOne(errors, bean, associatedObject, persistentProperty, propertyName);
            }
        }
    }

    private void validatePropertyWithConstraint(String propertyName, Object obj, Errors errors, BeanWrapper bean, Map constrainedProperties) {
        int i = propertyName.lastIndexOf(".");
        String constrainedPropertyName;
        if (i > -1) {
            constrainedPropertyName = propertyName.substring(i + 1, propertyName.length());
        }
        else {
            constrainedPropertyName = propertyName;
        }
        FieldError fieldError = errors.getFieldError(constrainedPropertyName);
        if (fieldError == null) {
            ConstrainedProperty c = (ConstrainedProperty) constrainedProperties.get(constrainedPropertyName);
            c.setMessageSource(this.messageSource);
            c.validate(obj, bean.getPropertyValue(constrainedPropertyName), errors);
        }
    }



    /**
     * Cascades validation to a one-to-one or many-to-one property
     *
     * @param errors The Errors instance
     * @param bean The original BeanWrapper
     * @param associatedObject The associated object's current value
     * @param persistentProperty The GraelykDomainClassProperty instance
     * @param propertyName The name of the property
     */
    protected void cascadeValidationToOne(Errors errors, BeanWrapper bean, Object associatedObject, GraelykDomainClassProperty persistentProperty, String propertyName) {

        if(associatedObject != null) {

            GraelykDomainClass associatedDomainClass = getAssociatedDomainClass(associatedObject, persistentProperty);

            if(associatedDomainClass != null && isOwningInstance(bean, associatedDomainClass)) {
                GraelykDomainClassProperty otherSide = null;
                if(persistentProperty.isBidirectional()) {
                    otherSide = persistentProperty.getOtherSide();
                }

                Map associatedConstraintedProperties = associatedDomainClass.getConstrainedProperties();

                GraelykDomainClassProperty[] associatedPersistentProperties = associatedDomainClass.getPersistentProperties();
                String nestedPath = errors.getNestedPath();
                try {
                    errors.setNestedPath(nestedPath+propertyName);


                    for (int i = 0; i < associatedPersistentProperties.length; i++) {
                        GraelykDomainClassProperty associatedPersistentProperty = associatedPersistentProperties[i];
                        if(associatedPersistentProperty.equals(otherSide)) continue;
                        if(persistentProperty.isEmbedded() && EMBEDDED_EXCLUDES.contains(associatedPersistentProperty.getName())) continue;


                        String associatedPropertyName = associatedPersistentProperty.getName();
                        if(associatedConstraintedProperties.containsKey(associatedPropertyName)) {

                            validatePropertyWithConstraint(errors.getNestedPath() + associatedPropertyName, associatedObject, errors, new BeanWrapperImpl(associatedObject), associatedConstraintedProperties);
                        }

                        if(associatedPersistentProperty.isAssociation()) {
                            cascadeToAssociativeProperty(errors, new BeanWrapperImpl(associatedObject), associatedPersistentProperty);
                        }

                    }
                }
                finally {
                    errors.setNestedPath(nestedPath);
                }

            }

            }
    }

    private GraelykDomainClass getAssociatedDomainClass(Object associatedObject, GraelykDomainClassProperty persistentProperty) {
        if(persistentProperty.isEmbedded()) {
            return persistentProperty.getComponent();
        }
        else {
			/*
            if(grailsApplication!=null) {
                return getAssociatedDomainClassFromApplication(associatedObject);
            }
            else
			*/
                return persistentProperty.getReferencedDomainClass();
        }
    }

	/*
    protected GraelykDomainClass getAssociatedDomainClassFromApplication(Object associatedObject) {
        String associatedObjectType = associatedObject.getClass().getName();
        return (GraelykDomainClass) grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, associatedObjectType);
    }
	*/

    private boolean isOwningInstance(BeanWrapper bean, GraelykDomainClass associatedDomainClass)
	{
        Class currentClass = bean.getWrappedClass();
        while(currentClass != Object.class) {
            if(associatedDomainClass.isOwningClass(currentClass)) return true;
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

	/*
    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }
	*/
}
