/*
 * Copyright 2004-2005 the original author or authors.
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

//Originally packaged with Grails as part of package org.codehaus.groovy.grails.plugins.codecs
package groovyx.gaelyk.graelyk.codecs

//import org.springframework.web.util.JavaScriptUtils
import groovyx.gaelyk.graelyk.util.JavaScriptUtils

/**
 * A codec that encodes strings to Javascript
 * 
 * @author Graeme Rocher
 * @since 0.5
 */
class JavaScriptCodec {
    static encode = {theTarget ->
        JavaScriptUtils.javaScriptEscape(theTarget.toString())
    }
}