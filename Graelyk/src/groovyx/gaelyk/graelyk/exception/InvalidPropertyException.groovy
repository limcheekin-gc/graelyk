/* Copyright 2010 the original author or authors.
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
//Originally package org.codehaus.groovy.grails.exceptions;
package groovyx.gaelyk.graelyk.exception

/**
 * 
 * Exception which is thrown when a property of a Grails class is invalidated.
 * 
 * @author Jeremy Brown
 * @since August 25, 2010
 * 
 */
public class InvalidPropertyException extends Exception
{

	public InvalidPropertyException()
	{
		super();
	}

	public InvalidPropertyException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	public InvalidPropertyException(String arg0)
	{
		super(arg0);
	}

	public InvalidPropertyException(Throwable arg0)
	{
		super(arg0);
	}

}
