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
//Original package org.codehaus.groovy.grails.scaffolding;
package groovyx.gaelyk.graelyk.domain

/*
import org.codehaus.groovy.grails.validation.ConstrainedProperty;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
*/

import groovyx.gaelyk.graelyk.validation.ConstrainedProperty;
import groovyx.gaelyk.graelyk.domain.GraelykDomainClass;
import groovyx.gaelyk.graelyk.domain.GraelykDomainClassProperty;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator that uses the domain class property constraints to establish order in sort methods and always
 * places the id first
 *
 * @author Graeme Rocher
 * @since 10-Feb-2006
 */
public class DomainClassPropertyComparator implements Comparator
{
    private Map constrainedProperties;
    private GraelykDomainClass domainClass;

    public DomainClassPropertyComparator(GraelykDomainClass domainClass)
	{
        if(domainClass == null)
            throw new IllegalArgumentException("Argument 'domainClass' is required!");

        this.constrainedProperties = domainClass.getConstrainedProperties();
        this.domainClass = domainClass;
    }

    public int compare(Object o1, Object o2)
	{
		/*
        if(o1.equals(domainClass.getIdentifier()))
            return -1;
        else if(o2.equals(domainClass.getIdentifier()))
            return 1;
		*/
		
        GraelykDomainClassProperty prop1 = (GraelykDomainClassProperty)o1;
        GraelykDomainClassProperty prop2 = (GraelykDomainClassProperty)o2;
		
		if(prop1.getName() == GraelykDomainClassProperty.IDENTITY)
			return -1;
		else if(prop2.getName() == GraelykDomainClassProperty.IDENTITY)
			return 1;

        ConstrainedProperty cp1 = (ConstrainedProperty)constrainedProperties.get(prop1.getName());
        ConstrainedProperty cp2 = (ConstrainedProperty)constrainedProperties.get(prop2.getName());

        if(cp1 == null & cp2 == null)
            return prop1.getName().compareTo(prop2.getName());
        if(cp1 == null)
            return 1;
        else if(cp2 == null)
            return -1;
        else 
		{
            if(cp1.getOrder() > cp2.getOrder())
                return 1;
            else if(cp1.getOrder() < cp2.getOrder())
                return -1;
            else
                return 0;
        }
		

		//Todo: reimplement the code above. The code below is a temporary placeholder.
		/*
		def cp1 = o1
		def cp2 = o2
		if(cp1 == null & cp2 == null)
			return 0;
		if(cp1 == null)
			return 1;
		else if(cp2 == null)
			return -1;
		else 
		{
			if(cp1.name == "id")
				return -1;
			else if(cp2.name == "id")
				return 1;
			else if(cp1.name > cp2.name)
				return 1;
			else if(cp1.name < cp2.name)
				return -1;
			else
				return 0;
		}
		*/
    }
}
