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
//Originally package org.codehaus.groovy.grails.commons;
package groovyx.gaelyk.graelyk.domain

import groovyx.gaelyk.graelyk.exception.GraelykConfigurationException
import groovyx.gaelyk.graelyk.util.GraelykClassUtils
import groovyx.gaelyk.graelyk.validation.ConstrainedProperty
import groovyx.gaelyk.graelyk.validation.ConstrainedPropertyBuilder

import groovy.lang.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
//import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException;
//import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;
//import org.codehaus.groovy.grails.orm.hibernate.cfg.PropertyConfig;
//import org.codehaus.groovy.grails.validation.ConstrainedProperty;
//import org.codehaus.groovy.grails.validation.ConstrainedPropertyBuilder;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import javax.persistence.Entity;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * Utility methods used in configuring the Grails Hibernate integration.
 *
 * @author Graeme Rocher
 * @since 18-Feb-2006
 */
public class GraelykDomainConfigurationUtil {

    public static final String PROPERTY_NAME = "constraints";
    private static final String CONSTRAINTS_GROOVY = "Constraints.groovy";

    //private static Log //LOG = LogFactory.getLog(GraelykDomainConfigurationUtil.class);
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    public static Serializable getAssociationIdentifier(Object target, String propertyName, GraelykDomainClass referencedDomainClass) {
        String getterName = GraelykClassUtils.getGetterName(propertyName);

        try {
            Method m = target.getClass().getDeclaredMethod(getterName, EMPTY_CLASS_ARRAY);
            Object value = m.invoke(target);
            if(value != null && referencedDomainClass != null) {
                String identifierGetter = GraelykClassUtils.getGetterName(referencedDomainClass.getIdentifier().getName());
                m = value.getClass().getDeclaredMethod(identifierGetter, EMPTY_CLASS_ARRAY);
                return (Serializable)m.invoke(value);
            }
        } catch (NoSuchMethodException e) {
           // ignore
        } catch (IllegalAccessException e) {
           // ignore
        } catch (InvocationTargetException e) {
            // ignore
        }
        return null;
    }

    /**
     * Configures the relationships between domain classes after they have been all loaded.
     *
     * @param domainClasses The domain classes to configure relationships for
     * @param domainMap     The domain class map
     */
    public static void configureDomainClassRelationships(GraelykDomainClass[] domainClasses, Map<?, ?> domainMap) {

        // configure super/sub class relationships
        // and configure how domain class properties reference each other
        for (GraelykDomainClass domainClass : domainClasses) {
            if (!domainClass.isRoot()) {
                Class<?> superClass = domainClass.getClazz().getSuperclass();
                while (!superClass.equals(Object.class) && !superClass.equals(GroovyObject.class)) {
                    GraelykDomainClass gdc = (GraelykDomainClass) domainMap.get(superClass.getName());
                    if (gdc == null || gdc.getSubClasses() == null) {
                        break;
                    }

                    gdc.getSubClasses().add((GraelykDomainClass)domainClass);
                    superClass = superClass.getSuperclass();
                }
            }
            GraelykDomainClassProperty[] props = domainClass.getPersistentProperties();

            for (GraelykDomainClassProperty prop : props) {
                if (prop != null && prop.isAssociation()) {
                    GraelykDomainClass referencedGraelykDomainClass = (GraelykDomainClass) domainMap.get(prop.getReferencedPropertyType().getName());
                    prop.setReferencedDomainClass(referencedGraelykDomainClass);
                }
            }
        }

        // now configure so that the 'other side' of a property can be resolved by the property itself
        for (GraelykDomainClass domainClass1 : domainClasses) {
            GraelykDomainClass domainClass = domainClass1;
            GraelykDomainClassProperty[] props = domainClass.getPersistentProperties();

            for (GraelykDomainClassProperty prop : props) {
                if (prop != null && prop.isAssociation()) {

                    GraelykDomainClass referenced = prop.getReferencedDomainClass();
                    if (referenced != null) {
                        boolean isOwnedBy = referenced.isOwningClass(domainClass.getClazz());
                        prop.setOwningSide(isOwnedBy);
                        String refPropertyName = null;
                        try {
                            refPropertyName = prop.getReferencedPropertyName();
                        }
                        catch (UnsupportedOperationException e) {
                            // ignore (to support Hibernate entities)
                        }
                        if (!StringUtils.isBlank(refPropertyName)) {
                            GraelykDomainClassProperty otherSide = referenced.getPropertyByName(refPropertyName);
                            prop.setOtherSide(otherSide);
                            otherSide.setOtherSide(prop);
                        }
                        else {
                            GraelykDomainClassProperty[] referencedProperties = referenced.getPersistentProperties();
                            for (GraelykDomainClassProperty referencedProp : referencedProperties) {
                                // for bi-directional circular dependencies we don't want the other side
                                // to be equal to self
                                if (prop.equals(referencedProp) && prop.isBidirectional())
                                    continue;
                                if (isCandidateForOtherSide(domainClass, prop, referencedProp)) {
                                    prop.setOtherSide(referencedProp);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isCandidateForOtherSide(GraelykDomainClass domainClass, GraelykDomainClassProperty prop, GraelykDomainClassProperty referencedProp) {

        if (prop.equals(referencedProp)) return false;
        if (prop.isOneToMany() && referencedProp.isOneToMany()) return false;
        Class<?> referencedPropertyType = referencedProp.getReferencedPropertyType();
        if (referencedPropertyType == null || !referencedPropertyType.isAssignableFrom(domainClass.getClazz()))
            return false;
        Map<?, ?> mappedBy = domainClass.getMappedBy();

        Object propertyMapping = mappedBy.get(prop.getName());
        boolean mappedToDifferentProperty = propertyMapping != null && !propertyMapping.equals(referencedProp.getName());

        mappedBy = referencedProp.getDomainClass().getMappedBy();
        propertyMapping = mappedBy.get(referencedProp.getName());
        boolean mappedFromDifferentProperty = propertyMapping != null && !propertyMapping.equals(prop.getName());

        return !mappedToDifferentProperty && !mappedFromDifferentProperty;
    }

    /**
     * Returns the ORM frameworks mapping file name for the specified class name
     *
     * @param className The class name of the mapped file
     * @return The mapping file name
     */
    public static String getMappingFileName(String className) {
        return className.replaceAll("\\.", "/") + ".hbm.xml";
    }

    /**
     * Returns the association map for the specified domain class
     *
     * @param domainClass the domain class
     * @return The association map
     */
    public static Map<?, ?> getAssociationMap(Class domainClass) {
    	ClassPropertyFetcher cpf = ClassPropertyFetcher.forClass(domainClass);
    	
    	Map<?, ?> associationMap = cpf.getPropertyValue(GraelykDomainClassProperty.HAS_MANY, Map.class);
        if (associationMap == null) {
            associationMap = Collections.EMPTY_MAP;
        }
        return associationMap;
    }

    /**
     * Retrieves the mappedBy map for the specified class
     *
     * @param domainClass The domain class
     * @return The mappedBy map
     */
    public static Map<?, ?> getMappedByMap(Class<?> domainClass) {
    	ClassPropertyFetcher cpf = ClassPropertyFetcher.forClass(domainClass);
    	
        Map<?, ?> mappedByMap = cpf.getPropertyValue(GraelykDomainClassProperty.MAPPED_BY, Map.class);
        if (mappedByMap == null) {
            return Collections.EMPTY_MAP;
        }
        return mappedByMap;
    }

    /**
     * Establish whether its a basic type
     *
     * @param prop The domain class property
     * @return True if it is basic
     */
    public static boolean isBasicType(GraelykDomainClassProperty prop) {
        if (prop == null) return false;
        return isBasicType(prop.getType());
    }

    private static final Set<String> BASIC_TYPES;

    static {
        Set<String> basics = new HashSet<String>();
        basics.add(boolean.class.getName());
        basics.add(long.class.getName());
        basics.add(short.class.getName());
        basics.add(int.class.getName());
        basics.add(byte.class.getName());
        basics.add(float.class.getName());
        basics.add(double.class.getName());
        basics.add(char.class.getName());
        basics.add(Boolean.class.getName());
        basics.add(Long.class.getName());
        basics.add(Short.class.getName());
        basics.add(Integer.class.getName());
        basics.add(Byte.class.getName());
        basics.add(Float.class.getName());
        basics.add(Double.class.getName());
        basics.add(Character.class.getName());
        basics.add(String.class.getName());
        basics.add(java.util.Date.class.getName());
        basics.add(Time.class.getName());
        basics.add(Timestamp.class.getName());
        basics.add(java.sql.Date.class.getName());
        basics.add(BigDecimal.class.getName());
        basics.add(BigInteger.class.getName());
        basics.add(Locale.class.getName());
        basics.add(Calendar.class.getName());
        basics.add(GregorianCalendar.class.getName());
        basics.add(java.util.Currency.class.getName());
        basics.add(TimeZone.class.getName());
        basics.add(Object.class.getName());
        basics.add(Class.class.getName());
        basics.add(byte[].class.getName());
        basics.add(Byte[].class.getName());
        basics.add(char[].class.getName());
        basics.add(Character[].class.getName());
        basics.add(Blob.class.getName());
        basics.add(Clob.class.getName());
        basics.add(Serializable.class.getName());
        basics.add(URI.class.getName());
        basics.add(URL.class.getName());

        BASIC_TYPES = Collections.unmodifiableSet(basics);
    }

    public static boolean isBasicType(Class propType) {
        if(propType == null) return false;
        if (propType.isArray()) {
            return isBasicType(propType.getComponentType());
        }
        return BASIC_TYPES.contains(propType.getName());
    }

    /**
     * Checks whether is property is configurational
     *
     * @param descriptor The descriptor
     * @return True if it is configurational
     */
    public static boolean isNotConfigurational(PropertyDescriptor descriptor) {
        final String name = descriptor.getName();
        return !name.equals(GraelykDomainClassProperty.META_CLASS) &&
                !name.equals(GraelykDomainClassProperty.CLASS) &&
                !name.equals(GraelykDomainClassProperty.TRANSIENT) &&
                !name.equals(GraelykDomainClassProperty.RELATES_TO_MANY) &&
                !name.equals(GraelykDomainClassProperty.HAS_MANY) &&
                !name.equals(GraelykDomainClassProperty.EVANESCENT) &&
                !name.equals(GraelykDomainClassProperty.CONSTRAINTS) &&
                !name.equals(GraelykDomainClassProperty.MAPPING_STRATEGY) &&
                !name.equals(GraelykDomainClassProperty.MAPPED_BY) &&
                !name.equals(GraelykDomainClassProperty.BELONGS_TO);
    }

    /**
     * Evaluates the constraints closure to build the list of constraints
     *
     * @param instance   The instance to evaluate constraints for
     * @param properties The properties of the instance
     * @param defaultConstraints A map that defines the default constraints
     * 
     * @return A Map of constraints
     */
    public static Map<String, ConstrainedProperty> evaluateConstraints(Object instance, GraelykDomainClassProperty[] properties, Map<String, Object> defaultConstraints) {
        final Class<?> theClass = instance.getClass();
        return evaluateConstraints(theClass, properties, defaultConstraints);
    }

    /**
     * Evaluates the constraints closure to build the list of constraints
     *
     * @param theClass  The domain class to evaluate constraints for
     * @param properties The properties of the instance
     * @param defaultConstraints A map that defines the default constraints
     * 
     * @return A Map of constraints
     */    
	public static Map<String, ConstrainedProperty> evaluateConstraints(
														final Class<?> theClass, 
														GraelykDomainClassProperty[] properties,
														Map<String, Object> defaultConstraints) {
		boolean javaEntity = theClass.isAnnotationPresent(Entity.class);
        LinkedList<?> classChain = getSuperClassChain(theClass);
        Class<?> clazz;

        ConstrainedPropertyBuilder delegate = new ConstrainedPropertyBuilder(theClass);

        // Evaluate all the constraints closures in the inheritance chain
        for (Object aClassChain : classChain) {
            clazz = (Class<?>) aClassChain;
            Closure c = (Closure) GraelykClassUtils.getStaticPropertyValue(clazz, PROPERTY_NAME);
            if (c == null) {
                c = getConstraintsFromScript(theClass);
            }

            if (c != null) {
                c.setDelegate(delegate);
                c.call();
            }
            else {
                //LOG.debug("User-defined constraints not found on class [" + clazz + "], applying default constraints");
            }
        }

        Map<String, ConstrainedProperty> constrainedProperties = delegate.getConstrainedProperties();
        if(properties != null && !(constrainedProperties.isEmpty() && javaEntity)) {
            for (GraelykDomainClassProperty p : properties) {
				//Removed Hibernate mapping
				/*
            	PropertyConfig propertyConfig = GraelykDomainBinder.getPropertyConfig(p);
            	if(propertyConfig != null && propertyConfig.getFormula() != null) {
            		if(constrainedProperties.remove(p.getName()) != null) {
            			// constraint is registered but cannot be applied to a derived property
            			//LOG.warn("Derived properties may not be constrained. Property [" + p.getName() + "] of domain class " + theClass.getName() + " will not be checked during validation.");
            		}
            	} else {
				*/
            		final String propertyName = p.getName();
            		ConstrainedProperty cp = constrainedProperties.get(propertyName);
            		if (cp == null) {
            			cp = new ConstrainedProperty(p.getDomainClass().getClazz(), propertyName, p.getType());
            			cp.setOrder(constrainedProperties.size() + 1);
            			constrainedProperties.put(propertyName, cp);
            		}
            		// Make sure all fields are required by default, unless
            		// specified otherwise by the constraints
            		// If the field is a Java entity annotated with @Entity skip this
            		applyDefaultConstraints(propertyName, p, cp,
							defaultConstraints, delegate.getSharedConstraint(propertyName));
            	//}
            }
        }

        return constrainedProperties;
	}

    /**
     * Evaluates the constraints closure to build the list of constraints
     *
     * @param instance   The instance to evaluate constraints for
     * @param properties The properties of the instance
     * @return A Map of constraints
     *          When the bean cannot be introspected
     */
    public static Map<String, ConstrainedProperty> evaluateConstraints(Object instance, GraelykDomainClassProperty[] properties)  {
        return evaluateConstraints(instance, properties,null);
    }

    /**
     * Evaluates the constraints closure to build the list of constraints
     *
     * @param instance   The instance to evaluate constraints for
     * @return A Map of constraints
     *          When the bean cannot be introspected
     */
    public static Map<String, ConstrainedProperty> evaluateConstraints(Object instance)  {
        return evaluateConstraints(instance, null, null);
    }
    
    /**
     * Evaluates the constraints closure to build the list of constraints
     *
     * @param theClass  The class to evaluate constraints for
     * @return A Map of constraints
     *          When the bean cannot be introspected
     */
    public static Map<String, ConstrainedProperty> evaluateConstraints(Class theClass)  {
        return evaluateConstraints(theClass, null, null);
    }
    
    /**
     * Evaluates the constraints closure to build the list of constraints
     *
     * @param theClass  The class to evaluate constraints for
     * @return A Map of constraints
     *          When the bean cannot be introspected
     */
    public static Map<String, ConstrainedProperty> evaluateConstraints(Class theClass, GraelykDomainClassProperty[] properties)  {
        return evaluateConstraints(theClass, properties, null);
    }    
    

    private static void applyDefaultConstraints(String propertyName, GraelykDomainClassProperty p, ConstrainedProperty cp, Map<String, Object> defaultConstraints, String sharedConstraintReference) {
        if (defaultConstraints != null && !defaultConstraints.isEmpty()) {

            if (defaultConstraints.containsKey("*")) {
                final Object o = defaultConstraints.get("*");
                if (o instanceof Map) {
                    Map<String, Object> globalConstraints = (Map<String, Object>)o;
                    applyMapOfConstraints(globalConstraints, propertyName, p, cp);
                }
            }
            if(sharedConstraintReference!=null) {
                final Object o = defaultConstraints.get(sharedConstraintReference);
                if(o instanceof Map) {
                    applyMapOfConstraints((Map) o,propertyName, p, cp);
                }
                else {
                    throw new GraelykConfigurationException("Domain class property ["+p.getDomainClass().getFullName()+'.'+p.getName()+"] references shared constraint ["+sharedConstraintReference+":"+o+"], which doesn't exist!");
                }
            }            
        }
        

        if (canApplyNullableConstraint(propertyName, p, cp)) {
            cp.applyConstraint(ConstrainedProperty.NULLABLE_CONSTRAINT,
                    Collection.class.isAssignableFrom(p.getType()) ||
                    Map.class.isAssignableFrom(p.getType())
            );
        }
    }

    private static boolean canApplyNullableConstraint(String propertyName, GraelykDomainClassProperty property, ConstrainedProperty constrainedProperty) {
    	if(property == null || property.getType() == null) return false;
        final GraelykDomainClass domainClass = property.getDomainClass();
        // only apply default nullable to Groovy entities not legacy Java ones
        if(!GroovyObject.class.isAssignableFrom(domainClass.getClazz())) return false;
        final GraelykDomainClassProperty versionProperty = domainClass.getVersion();
        final boolean isVersion = versionProperty != null && versionProperty.equals(property);
        return !constrainedProperty.hasAppliedConstraint(ConstrainedProperty.NULLABLE_CONSTRAINT) &&
                isConstrainableProperty(property, propertyName) && !property.isIdentity() && !isVersion;
    }

    private static void applyMapOfConstraints(Map<String, Object> constraints, String propertyName, GraelykDomainClassProperty p, ConstrainedProperty cp) {
        for(Map.Entry<String, Object> entry : constraints.entrySet()) {
            String constraintName = entry.getKey();
            Object constrainingValue = entry.getValue();
            if(!cp.hasAppliedConstraint(constraintName) && cp.supportsContraint(constraintName)) {
                if(ConstrainedProperty.NULLABLE_CONSTRAINT.equals(constraintName)) {
                    if(isConstrainableProperty(p,propertyName))
                       cp.applyConstraint(constraintName, constrainingValue);
                }
                else {
                    cp.applyConstraint(constraintName,constrainingValue);
                }
            }
        }
    }

    private static boolean isConstrainableProperty(GraelykDomainClassProperty p, String propertyName) {
        return !propertyName.equals(GraelykDomainClassProperty.DATE_CREATED) &&
                !propertyName.equals(GraelykDomainClassProperty.LAST_UPDATED) &&
                !((p.isOneToOne() || p.isManyToOne()) && p.isCircular());
    }

    public static LinkedList<?> getSuperClassChain(Class<?> theClass) {
        LinkedList<Class<?>> classChain = new LinkedList<Class<?>>();
        Class<?> clazz = theClass;
        while (clazz != Object.class) {
            classChain.addFirst( clazz);
            clazz = clazz.getSuperclass();
        }
        return classChain;
    }

    private static Closure getConstraintsFromScript(Class theClass) {
        // Fallback to xxxxConstraints.groovy script for Java domain classes
        String className = theClass.getName();
        String constraintsScript = className.replaceAll("\\.","/") + CONSTRAINTS_GROOVY;
        InputStream stream = GraelykDomainConfigurationUtil.class.getClassLoader().getResourceAsStream(constraintsScript);

        if(stream!=null) {
            GroovyClassLoader gcl = new GroovyClassLoader();
            try {
                Class scriptClass = gcl.parseClass(DefaultGroovyMethods.getText(stream));
                Script script = (Script)scriptClass.newInstance();
                script.run();
                Binding binding = script.getBinding();
                if(binding.getVariables().containsKey(PROPERTY_NAME)) {
                    return (Closure)binding.getVariable(PROPERTY_NAME);
                }
                //LOG.warn("Unable to evaluate constraints from ["+constraintsScript+"], constraints closure not found!");
                return null;
            }
            catch (CompilationFailedException e) {
                //LOG.error("Compilation error evaluating constraints for class ["+theClass+"]: " + e.getMessage(),e );
                return null;
            } catch (InstantiationException e) {
                //LOG.error("Instantiation error evaluating constraints for class ["+theClass+"]: " + e.getMessage(),e );
                return null;
            } catch (IllegalAccessException e) {
                //LOG.error("Illegal access error evaluating constraints for class ["+theClass+"]: " + e.getMessage(),e );
                return null;
            } catch (IOException e) {
            	//LOG.error("IO error evaluating constraints for class ["+theClass+"]: " + e.getMessage(),e );
			}
        }
        return null;
    }
}
