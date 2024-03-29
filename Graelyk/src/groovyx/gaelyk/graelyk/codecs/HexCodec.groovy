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

class HexCodec {
	static HEXDIGITS = '0123456789abcdef'
	
	// Expects an array/list of numbers
    static encode = {theTarget ->
		if (theTarget == null) {
			return null
		} else {
			def result = new StringBuffer()
            if (theTarget instanceof String) {
                theTarget = theTarget.bytes
            }
            theTarget.each() {
				result << HexCodec.HEXDIGITS[(it & 0xF0) >> 4]
				result << HexCodec.HEXDIGITS[it & 0x0F]
			}
			return result.toString()
		}
	}

	static decode = {theTarget ->
        if (!theTarget) return null
        
        def output = [];

        def str = theTarget.toString().toLowerCase()
        if (str.size() % 2) {
            throw new UnsupportedOperationException("Decode of hex strings requires strings of even length")
        }
        def currentByte
        str.eachWithIndex { val, idx ->
            if (!(idx % 2)) {
                currentByte = HEXDIGITS.indexOf(val) << 4
            } else {
                output << (currentByte | HEXDIGITS.indexOf(val))
                currentByte = 0
            }
        }
        def result = new byte[output.size()]
        output.eachWithIndex { v, i -> result[i] = v }
        return result
    }
}