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

package groovyx.gaelyk.graelyk.cast;

import org.apache.commons.lang.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.Locale;

/**
 * A Property editor for converting instances of java.util.Locale
 * 
 * @author Jeremy Brown
 */
public class LocaleEditor extends PropertyEditorSupport {
    public void setAsText(String text)
    {
        if(StringUtils.isBlank(text))
        {
            setValue(null);
        }
        try
        {
            setValue(new Locale(*(text.trim().split(/_/).toList())));
        }
        catch(Exception e)
        {
            // ignore and just set to null
            setValue(null);
        }
    }

    public String getAsText()
    {
        Locale locale = (Locale)getValue();
        if(locale == null)
        {
            return "";
        }
        else
        {
            return locale.toString();
        }
    }
}

