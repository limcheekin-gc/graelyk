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

import java.io.Serializable;

import javax.persistence.Id;

/**
 * Acts as a convenience ancestor for domain classes to extend Obgaektifiable & GraelykDomainClass without needing to specify the Long id or import javax.persistence.Id
 * 
 * @author Jeremy Brown
 */
abstract class GraelykLongIdDomainClass extends GraelykDomainClass implements Serializable
{
	static idType = Long
	@Id Long id
	
	GraelykLongIdDomainClass(Object callingScript)
	{
		super(callingScript)
	}
}