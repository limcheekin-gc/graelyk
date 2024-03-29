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

import java.security.MessageDigest

abstract class DigestUtils {

    // Digest byte[], any list/array or string into a byte[]
    static def digest(String algorithm, data) {
        if (data == null) {
            return null
        } else {
            def md = MessageDigest.getInstance(algorithm)
            def src
            if ((data instanceof Byte[]) || (data instanceof byte[])) {
                src = data
            } else if ((data instanceof List) || data.class.isArray()) {
                src = new byte[data.size()]
                data.eachWithIndex { v, i -> src[i] = v }
            } else {
                src = data.toString().getBytes("UTF-8")
            }
            md.update(src) // This probably needs to use the thread's Locale encoding
            return md.digest()
        }
    }
}
