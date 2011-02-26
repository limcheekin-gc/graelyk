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
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx.gaelyk.graelyk.taglyk

class UserTaglyk
{
	static void g_loginLogout(script){script.out << script.loginLogout()}
	static String loginLogout(script)
	{
		StringBuffer out = new StringBuffer()
		if(script.user)
		{
			def url = script.users.createLogoutURL("/")
			out << """<span class="menuButton"><a class="logout" href="${url}">${script.message(code:"default.logout.label", defaultMessage:"Log Out")}</a></span>"""
		}
		else
		{
			def thisURL = script.request.getRequestURI()
			def url = script.users.createLoginURL(thisURL)
			out << """<span class="menuButton"><a class="logout" href="${url}">${script.message(code:"default.login.label", defaultMessage:"Log In")}</a></span>"""
		}
		return out.toString()
	}
	
	//Return the list of locales in userLocale as a pipe separated String.
	//This way, the user's locale choices can be used as part of a String key for a HashMap, etc.
	static String userLocaleAsString(script)
	{
		return script.userLocale.join("|")
	}
}
