/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx.gaelyk.graelyk.domain

import static java.util.Collections.unmodifiableMap
import static java.util.Collections.unmodifiableList

import groovyx.gaelyk.obgaektify.Obgaektifiable
import groovyx.gaelyk.obgaektify.ObgaektifyCategory
import com.googlecode.objectify.Key
import groovyx.gaelyk.GaelykCategory
import groovyx.gaelyk.graelyk.StaticResourceHolder
import groovyx.gaelyk.graelyk.annotation.GDC
import groovyx.gaelyk.graelyk.annotation.Contains
import groovyx.gaelyk.graelyk.cast.StructuredDateEditor
import groovyx.gaelyk.graelyk.cast.StructuredGeoPtEditor
import groovyx.gaelyk.graelyk.exception.InvalidPropertyException
import groovyx.gaelyk.graelyk.util.AnnotationUtils
import groovyx.gaelyk.graelyk.util.CastingUtils
import groovyx.gaelyk.graelyk.util.GraelykNameUtils
import groovyx.gaelyk.graelyk.util.GraelykClassUtils
import groovyx.gaelyk.graelyk.util.ObjectUtils;
import groovyx.gaelyk.graelyk.validation.GraelykDomainClassValidator
import groovyx.gaelyk.graelyk.validation.GraelykErrors
import groovyx.gaelyk.graelyk.validation.ConstrainedPropertyBuilder
import com.googlecode.objectify.annotation.*
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.text.NumberFormat
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.apache.commons.lang.ClassUtils

import com.google.appengine.api.datastore.Blob
import com.google.appengine.api.datastore.Category
import com.google.appengine.api.datastore.Email
import com.google.appengine.api.datastore.GeoPt
import com.google.appengine.api.datastore.IMHandle
//import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.Link
import com.google.appengine.api.datastore.PhoneNumber
import com.google.appengine.api.datastore.PostalAddress
import com.google.appengine.api.datastore.Rating
import com.google.appengine.api.datastore.ShortBlob
import com.google.appengine.api.datastore.Text
import com.google.appengine.api.users.User

import groovyx.gaelyk.graelyk.validation.ConstrainedProperty
import groovyx.gaelyk.graelyk.cast.CastingRegistry

abstract class GraelykDomainClass extends Obgaektifiable implements Serializable
{
	/*GraelykDomainClass is a semi-lightweight implementation of a domain class
	 * in a similar vein to that of Grails domain classes. It is based on Obgaektify
	 * and Objectify.
	 * 
	 * The ORM, validation, and constraint components are (as a group) lazily
	 * evaluated in the init() method. Any methods or properties that use these
	 * components are supposed to call init() if it has not already been called.
	 */
	
	Integer version = 0

	@NotSaved Class clazz
	@NotSaved Object callingScript
	@NotSaved boolean initialized = false
	@NotSaved ClassPropertyFetcher classPropertyFetcher;
	@NotSaved List<GraelykDomainClassProperty> properties = []
	@NotSaved List<GraelykDomainClassProperty> persistentProperties = []
	@NotSaved Map<String, GraelykDomainClassProperty> propertyMap = [:]
	@NotSaved Map relationshipMap = [:]
	@NotSaved Map hasOneMap = [:]
	@NotSaved Map mappedByMap = [:]
	@NotSaved List<Class> owners = []
	
	@NotSaved Map constrainedProperties
	@NotSaved def defaultConstraints
	@NotSaved GraelykDomainClassValidator validator
	@NotSaved GraelykErrors errors
	@NotSaved nestedStructMap = [:]
	
	@NotSaved String classFullName
	@NotSaved String classPackageName
	@NotSaved String classNaturalName
	@NotSaved String classShortName
	@NotSaved String className
	@NotSaved String propertyName
	
	@NotSaved CastingRegistry castingRegistry
	
	/*
	 * These static variables should be declared in extending classes:
	 * 
	 * static Closure constraints
	 * static Map transformOnReceive
	 * static Map transformOnDisplay
	 * 
	 * These static variables can be declared in extending classes
	 * 
	 * static optionals
	 */
	//Todo: optionals currently doesn't do anything
	
	
	/*
	excludedProperties needs to contain a list of all the "property" names 
	(including properties that are only exposed by a get method)
	that are not actual properties that should be persisted 
	or shown on web forms.
	*/
	@NotSaved List excludedProperties = ["class", "clazz", "metaClass", "classPropertyFetcher",
		"idType", "identifier",
		"clazz", "classFullName", "classPackageName", "classNaturalName", "classShortName", "className", "propertyName",
		"properties", "propertyMap", "persistentProperties",
		"relationshipMap", "hasOneMap", "mappedByMap", "belongsTo", "owners",
		"constraints", "defaultConstraints", "constrainedProperties", "optionals",
		"validator", "errors", "nestedStructMap", "callingScript", "initialized",
		"excludedProperties", "castingRegistry",
		"transformOnReceive", "transformOnDisplay"]

	public GraelykDomainClass(Object callingScript)
	{
		this(callingScript, false)
	}
	
	public GraelykDomainClass(Object callingScript, boolean init)
	{
		this.callingScript = callingScript
		this.clazz = this.getClass()
		this.classPropertyFetcher = ClassPropertyFetcher.forClass(this.clazz);
		
        this.classFullName = clazz.getName();
        this.classPackageName = ClassUtils.getPackageName(clazz);
        this.classNaturalName = GraelykNameUtils.getNaturalName(classFullName);
        this.classShortName = GraelykNameUtils.getShortName(clazz);        
        this.className = classShortName
		this.propertyName = GraelykNameUtils.getPropertyNameRepresentation(clazz)
		
		errors = new GraelykErrors(this, this.getClass().getName())
        
		if(init)
		{
			this.init()
		}
	}
	
	public GraelykDomainClass init()
	{
		this.initialized = true
        establishAssociationMap();
        this.mappedByMap = classPropertyFetcher.getStaticPropertyValue(GraelykDomainClassProperty.MAPPED_BY, Map.class);
        this.hasOneMap = classPropertyFetcher.getStaticPropertyValue(GraelykDomainClassProperty.HAS_ONE, Map.class);
        if(hasOneMap == null) hasOneMap = [:]
        if(mappedByMap == null) mappedByMap = [:]
        establishRelationshipOwners();
		establishProperties()
		establishPersistentProperties()
		getConstrainedProperties()
		establishCastingRegistry()
		errors.setMessageSource(StaticResourceHolder.getMessageBundle(getClazz().getName()))
		return this
	}
	
	public Object getIdentifier()
	{
		//By convention, the identifier is always the id property
		return id
	}
	
	/*
	 * @Override
	 */
	public boolean preSave()
	{
		//Override this method to perform operations before a save.
		//Return true to continue with the save, false to cancel the save.
		return true
	}
	
	public Key save()
	{
		//Persist the object to the datastore
		//There are two hooks in the save() method that developers can override: preSave() and postSave().
		def savedKey
		if(this.preSave())
		{
			savedKey = ObgaektifyCategory.store(this)
		}
		return this.postSave(savedKey)
	}
	
	/*
	 * @Override
	 */
	public Key postSave(Key savedKey)
	{
		//Override this method to perform operations after a save.
		//Normally you should return the Key of the saved object if it still exists.
		return savedKey
	}
	
	public Key validateAndSave()
	{
		//Validate first, and save only if valid
		def valid = this.validate()
		if(valid)
		{
			return this.save()
		}
		return null
	}
	
	/*
	 * @Override
	 */
	public boolean preValidate()
	{
		//Override this method to perform operations before validation.
		//Return true to continue with the validation, false to cancel the validation & return invalid.
		return true
	}
	
	public boolean validate()
	{
		//Return true if valid, false if invalid
		//There are two hooks in the validate() method that developers can override: preValidate() and postValidate().
		//If preValidate() returns false, cancel validation and return false from validate()
		//If postValidate() returns false, return false from validate()
		if(!initialized){init()}
		establishValidator()
		if(this.preValidate())
		{
			def valid = false
			validator.validate(this, this.errors, true)
			if(!errors.hasErrors())
			{
				valid = true
			}
			valid = postValidate(valid)
			return valid
		}
		return false
	}
	
	/*
	 * @Override
	 */
	public boolean postValidate(boolean valid)
	{
		//Override this method to perform operations after validation.
		//Your overridden method may change the validation status that was passed in.
		return valid
	}
	
	private void establishValidator()
	{
		//This method tries to retrieve a validator cached in the StaticResourceHolder.
		//If one doesn't exist yet, it creates one. The idea is to cut down on initialization
		//time and resources required, since the validator should be the same for all objects
		//of the same class.
		if(!initialized){init()}
		def domainClassName = getClazz().getName()
		def v = StaticResourceHolder.getValidator(domainClassName)
		if(!v)
		{
			v = new GraelykDomainClassValidator()
			v.setDomainClass(this)
			v.setMessageSource(StaticResourceHolder.getMessageBundle(domainClassName))
			StaticResourceHolder.setValidator(domainClassName, v)
		}
		this.validator = v
	}
	
	private void establishProperties()
	{
		def props = this.getMetaPropertyValues()
		//props.each{p->
		for(p in props)
		{
			//try
			//{
				if(!excludedProperties.contains(p.name))
				{
					def gdcp = new GraelykDomainClassProperty(this, p.name, p.type)
					this.properties << gdcp
					this.propertyMap[p.name] = gdcp
				}
			//}
			//catch(Exception e)
			//{
			//	System.err.println("Problem establishing property [${p.name}]. Should it be added to [excludedProperties]?")
			//}
		}
	}
	
	private void establishPersistentProperties()
	{
        for (GraelykDomainClassProperty currentProp in this.propertyMap.values())
		{
            if (currentProp.getType() != Object.class && currentProp.isPersistent() && !currentProp.isIdentity())
			{
                this.persistentProperties << currentProp
            }
        }
	}
	
    private Map establishAssociationMap()
	{
        if(this.relationshipMap == null || this.relationshipMap == [:]) 
		{
            this.relationshipMap = classPropertyFetcher.getStaticPropertyValue(GraelykDomainClassProperty.HAS_MANY, Map.class);
            if(this.relationshipMap == null)
                this.relationshipMap = new HashMap();

            Class theClass = getClazz();
            while(theClass != Object.class)
            {
                theClass = theClass.getSuperclass();
                ClassPropertyFetcher propertyFetcher = ClassPropertyFetcher.forClass(theClass);
                Map superRelationshipMap = propertyFetcher.getStaticPropertyValue(GraelykDomainClassProperty.HAS_MANY, Map.class);
                if(superRelationshipMap != null && !superRelationshipMap.equals(relationshipMap)) {
                    this.relationshipMap.putAll(superRelationshipMap);
                }
            }
        }
    }
	
	/**
	 * Evaluates the belongsTo property to find out who owns who
	 */
	private void establishRelationshipOwners()
	{
		Class belongsTo = classPropertyFetcher.getStaticPropertyValue(GraelykDomainClassProperty.BELONGS_TO, Class.class);
        if(belongsTo == null) {
            List ownersProp = classPropertyFetcher.getStaticPropertyValue(GraelykDomainClassProperty.BELONGS_TO, List.class);
            if(ownersProp != null) {
                this.owners = ownersProp;
            }
            else {
                Map ownersMap = classPropertyFetcher.getStaticPropertyValue(GraelykDomainClassProperty.BELONGS_TO, Map.class);
                if(ownersMap!=null) {
                    this.owners = new ArrayList(ownersMap.values());
                }
            }
        }
        else {
            this.owners = new ArrayList();
            this.owners.add(belongsTo);
        }
	}
	
	
	public ident()
	{
		return this.id
	}
	
	public List list()
	{
		//Query all objects of this type from the Google datastore
		//This is relying on the query method in groovyx.gaelyk.obgaektify.ObgaektifyCategory
		ObgaektifyCategory.search(this.clazz)
	}
	
	public GraelykDomainClassValidator getValidator()
	{
		if(!initialized){init()}
		return validator
	}
	
	public List<GraelykDomainClassProperty> getProperties()
	{
		if(!initialized){init()}
		return this.properties
	}
	
	public Map<String, GraelykDomainClassProperty> getPropertyMap()
	{
		if(!initialized){init()}
		return this.propertyMap
	}
	
    public List<GraelykDomainClassProperty> getPersistentProperties()
	{
		if(!initialized){init()}
        return this.persistentProperties;
    }
    
    public Map getRelationshipMap()
    {
		if(!initialized){init()}
		return this.relationshipMap
    }
    
    public Map getHasOneMap()
    {
		if(!initialized){init()}
		return this.hasOneMap
    }
    
    public Map getMappedByMap()
    {
		if(!initialized){init()}
		return this.mappedByMap
    }
    
    public Map getNestedStructMap()
    {
		if(!initialized){init()}
		return this.nestedStructMap
    }
    
    public List getOwners()
    {
		if(!initialized){init()}
		return this.owners
    }

    public getBelongsTo()
    {
		if(!initialized){init()}
		return this.belongsTo
    }
    
    public getOptionals()
    {
		if(!initialized){init()}
		return this.optionals
    }
    
    public getDefaultConstraints()
    {
		if(!initialized){init()}
		return this.defaultConstraints
    }
    
    public CastingRegistry getCastingRegistry()
    {
		if(!initialized){init()}
		return this.castingRegistry
    }
	
    public Class getRelatedClassType(String propertyName)
	{
		if(!initialized){init()}
        return (Class)this.relationshipMap.get(propertyName);
    }
    
    public GraelykDomainClassProperty getPropertyByName(String name)
	{
		if(!initialized){init()}
        if(this.propertyMap.containsKey(name))
		{
            return this.propertyMap.get(name);
        }
        else
		{
            throw new InvalidPropertyException("No property found for name ["+name+"] for class ["+getClazz()+"]");
        }
    }
	
    public String getFieldName(String propertyName)
	{
		if(!initialized){init()}
        return getPropertyByName(propertyName).getFieldName();
    }
	
    /*
    public String getClassName()
	{
        return ClassUtils.getShortClassName(super.getClassName());
    }
    */
	
	public Class getClazz()
	{
		return this.clazz
	}
	
	public void setClazz(Class c)
	{
		this.clazz = c
	}
	
    public String getPropertyName()
	{
        return propertyName
    }
	
	def Map getConstraints()
	{
		if(!initialized){init()}
		return getConstrainedProperties()
	}
	
	def Map getConstrainedProperties()
	{
		if(!initialized){init()}
		if(this.@constrainedProperties == null)
		{
			initializeConstraints()
		}
		return unmodifiableMap(this.@constrainedProperties)
	}
	
	private void initializeConstraints()
	{
		//Convert the constraints closure into a Map
		def Map constrainedProps
        def validationClosure = {}
		try
		{
			validationClosure = this.@constraints
		}
		catch(groovy.lang.MissingPropertyException mpe){}
		catch(groovy.lang.MissingFieldException mpe){}
        def validateable = this
        if (validationClosure)
        {
            def constrainedPropertyBuilder = new ConstrainedPropertyBuilder(this)
            validationClosure.setDelegate(constrainedPropertyBuilder)
            validationClosure()
            constrainedProps = constrainedPropertyBuilder.constrainedProperties
        }
        else
        {
            constrainedProps = [:]
        }
		
		//Add other constraints (for Google data types) that should be automatic
		//propertyMap.each{name, prop->
        for(pmEntry in propertyMap)
        {
        	def name = pmEntry.key
        	def prop = pmEntry.value
			ConstrainedProperty cp = constrainedProps[name]
			constrainedProps[name] = initializeAutomaticConstraints(name, prop, cp)
		}
		
		defaultConstraints = constrainedProps
		this.@constrainedProperties = constrainedProps
	}
	
	
	def ConstrainedProperty initializeAutomaticConstraints(name, prop, cp)
	{
		if(!cp){cp = new ConstrainedProperty(this.class, name, prop.type)}
		
		//The Category type has a max length of 500 characters
		if(prop.type == Category)
		{
			if(!cp.hasRegisteredConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT))
			{
				cp.applyConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT, 500)
			}
		}
		//The Email type should get the email constraint
		else if(prop.type == Email)
		{
			if(!cp.hasRegisteredConstraint(ConstrainedProperty.EMAIL_CONSTRAINT))
			{
				cp.applyConstraint(ConstrainedProperty.EMAIL_CONSTRAINT, new Boolean(true));
			}
		}
		//The Link type has a max length of 2038 characters
		else if(prop.type == Link)
		{
			if(!cp.hasRegisteredConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT))
			{
				cp.applyConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT, 500)
			}
		}
		//The PhoneNumber type has a max length of 500 characters
		else if(prop.type == PhoneNumber)
		{
			if(!cp.hasRegisteredConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT))
			{
				cp.applyConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT, 500)
			}
		}
		//The PostalAddress type has a max length of 500 characters
		else if(prop.type == PostalAddress)
		{
			if(!cp.hasRegisteredConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT))
			{
				cp.applyConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT, 500)
			}
		}
		//The Rating type has a max range of 0..100
		else if(prop.type == Rating)
		{
			if(!cp.hasRegisteredConstraint(ConstrainedProperty.RANGE_CONSTRAINT))
			{
				cp.applyConstraint(ConstrainedProperty.RANGE_CONSTRAINT, 0..100)
			}
		}
		//The String type has a max length of 500 characters
		else if(prop.type == String)
		{
			if(!cp.hasRegisteredConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT))
			{
				cp.applyConstraint(ConstrainedProperty.MAX_SIZE_CONSTRAINT, 500)
			}
		}
		
		return cp
	}

	
	private void establishCastingRegistry()
	{
		if(callingScript != null)
		{
			castingRegistry = CastingRegistry.createDefaultRegistry(callingScript.userLocale, callingScript.userNumberLocale, callingScript.userCurrencyLocale, callingScript.userDateLocale)
		}
		else
		{
			castingRegistry = CastingRegistry.createDefaultRegistry(Locale.getDefault())
		}
	}
	
	public List getExcludedProperties()
	{
		return this.@excludedProperties.clone()
	}
	
    public boolean hasPersistentProperty(String propertyName)
	{
		if(!initialized){init()}
        for (GraelykDomainClassProperty persistentProperty in persistentProperties)
		{
            if (persistentProperty.getName().equals(propertyName)) return true;
        }
        return false;
    }
	
	public Obgaektifiable setCallingScript(script)
	{
		callingScript = script
		return this
	}
	
	public Obgaektifiable leftShift(Map params)
	{
		if(!initialized){init()}
		
		//Get a list of any incoming fields that start with "_".
		//These are fields that identify the presence of a checkbox.
		def checkboxRemoveList = []
		def checkboxNullifyList = []
		//params.each{key, value->
		for(pEntry in params)
		{
    		def key = pEntry.key
       		def value = pEntry.value
			if(key.startsWith("_"))
			{
				//Add the names of all the _myCheckbox params to be removed after the completion of this loop
				checkboxRemoveList << key
				//Check to make sure a property exists for that checkbox
				def propName = key - "_"
				if(this.hasProperty(propName))
				{
					//If the incoming _myCheckbox field does not have a corresponding incoming myCheckbox field,
					//add the myCheckbox field to the params map with a value of null.
					//But really we add the propName to checkboxNullifyList, to be set after the end of the map loop, 
					//to avoid concurrent modification of the map
					if(!params[propName])
					{
						checkboxNullifyList << propName
					}
				}
			}
		}
		//Remove all the _myCheckbox params
		//checkboxRemoveList.each{key->
		for(key in checkboxRemoveList)
		{
			params.remove(key)
		}
		//Add the needed myCheckbox params with a value of null
		//checkboxNullifyList.each{key->
		for(key in checkboxNullifyList)
		{
			params[key] = false
		}
		
		//Load the persistable properties in the domain class with values from the HTTP request.
		//Only load properties that actually exist.
		//Special handling for Dates and GeoPts (which come in multiple pieces) and for multi-valued fields like checkboxes and selects
		//params.each{key, value->
		for(pEntry in params)
		{
			def key = pEntry.key
			def value = pEntry.value
			if(this.hasProperty(key))
			{
				def propertyType = this.getMetaPropertyValues().find{it.name == key}.type
				def componentType = propertyType
				
				Class componentGenericType = null
				def thisProperty = propertyMap[key]
				if(thisProperty.isArray || thisProperty.isCollection)
				{
					componentType = thisProperty.collectionOf
				}
				if(thisProperty.hasKey)
				{
					componentGenericType = thisProperty.keyOf
				}
				
				def assignValue = null
				def valList = []
				
				//Handle "STRUCT" params. e.g. a set of selects for a date (year, month, day, etc.)
				//this handles values from a form submission that ends in ".STRUCT"
				//The main example is date.STRUCT
				//but additional dataType.STRUCT options could be handled by adding a category class with methods like:
				//static evaluate[DataType]Param(script, params, key, index){...}
				if(value != null && ((value instanceof String && value.endsWith(".STRUCT")) || (ObjectUtils.isListOrArray(value) && value?.getAt(0)?.endsWith(".STRUCT"))))
				{
					//Always make the value be in a list/array, to make handling single value or list/array the same
					if(!ObjectUtils.isListOrArray(value))
					{
						value = [value]
					}
					
					//This is where we will store the list of transformed values
					nestedStructMap[key] = []
					                        
					//value.eachWithIndex{val, index->
					def index = -1
					for(val in value)
					{
						index++;
						//e.g. if the value contains date.STRUCT, this will call the method script.evaluateDateStruct(params, key)
						def prefix = val[0..-(".STRUCT".size()+1)]
						def newVal = this."evaluate${prefix[0].toUpperCase()}${prefix[1..-1]}Struct"(params, key, index)
						//Run the transformOnReceive closure if one has been defined
						if(thisProperty.transformOnReceive != null)
						{
							newVal = thisProperty.transformOnReceive(newVal)
						}
						nestedStructMap[key] << newVal
						valList << newVal
					}
				}
				//Handle normal params (single or multi value)
				else
				{
					def transformedValue = params[key]
					//Run the transformOnReceive closure if one has been defined
					if(thisProperty.transformOnReceive != null)
					{
						transformedValue = thisProperty.transformOnReceive(transformedValue)
					}

					Object[] valueArray
					if(ObjectUtils.isListOrArray(transformedValue))
					{
						valueArray = transformedValue as Object[]
					}
					else
					{
						valueArray = [transformedValue] as Object[]
					}
					
					if(valueArray == null)
					{
						valList = [];
					}
					else if(valueArray.size() == 1) 
					{
						valList = [castingRegistry.cast(valueArray[0], componentType, componentGenericType)]
					}
					else 
					{
						//valueArray.each{val->
						for(val in valueArray)
						{
							valList << castingRegistry.cast(val, componentType, componentGenericType)
						}
					}
				}
				
				//Take the valList and make the proper casts or extractions to assign the value of valList
				//depending on if the property in this object is a List, Array, or single value.
				//The domain class property is an array:
				if(thisProperty.isArray)
				{
					assignValue = valList as Object[]
				}
				//The domain class property is a List
				else if((List).isAssignableFrom(propertyType))
				{
					assignValue = valList
				}
				//The property is a Set
				else if((Set).isAssignableFrom(propertyType))
				{
					assignValue = (new java.util.HashSet())
					assignValue.addAll(valList)
				}
				//The domain class property is not an Array or List, it is just a single value
				else
				{
					if(valList.size() >= 1)
					{
						assignValue = valList[0]
					}
					else
					{
						assignValue = null
					}
				}
				
				//Only assign a value to the property if the value is non-null
				//Null values are probaby indicative of an error
				if(assignValue != null)
				{
					this[key] = assignValue
				}
			}
		}
		return this
		
	}
	
	
	//Params with a value of date.struct will be processed by this method. See leftShift(Map) above.
    Date evaluateDateStruct(Map params, String key, int index)
	{
		// parse date structs automatically
		def dateParams = [:]
		def prefix = key + "_"
		//params.each{paramName, value->
		for(pEntry in params)
		{
			def paramName = pEntry.key
			def value = pEntry.value
			if(paramName instanceof String && paramName.startsWith(prefix))
			{
				if(ObjectUtils.isListOrArray(value)){value = value[index]} //Get the correct value from an array or list, if applicable
				dateParams.put(paramName.substring(prefix.size(), paramName.size()), value)
			}
		}

        def dateFormat = new SimpleDateFormat(StaticResourceHolder.appProperties["defaultDateFormat"], callingScript.userDateLocale)
        def editor = new StructuredDateEditor(dateFormat,true)
		try {
	        return editor.assemble(Date.class,dateParams)
		}
		catch(IllegalArgumentException e) {
            return null
		}		
    }
    
    //Params with a value of geopt.struct will be processed by this method. See leftShift(Map) above.
    GeoPt evaluateGeoPtStruct(Map params, String key, int index)
    {
		// parse GeoPt structs automatically
		def geoptParams = [:]
		def prefix = key + "_"

		//params.each{paramName, value->
		for(pEntry in params)
		{
			def paramName = pEntry.key
			def value = pEntry.value
			if(paramName instanceof String && paramName.startsWith(prefix))
			{
				if(ObjectUtils.isListOrArray(value)){value = value[index]} //Get the correct value from an array or list, if applicable
				geoptParams.put(paramName.substring(prefix.size(), paramName.size()), value)
			}
		}
		
		def floatFormat = NumberFormat.getInstance(callingScript.userNumberLocale);
		def editor = new StructuredGeoPtEditor(floatFormat)
		try
		{
			def geopt = editor.assemble(GeoPt, geoptParams)
			return geopt
		}
		catch(IllegalArgumentException e)
		{
			return null
		}
    }
}