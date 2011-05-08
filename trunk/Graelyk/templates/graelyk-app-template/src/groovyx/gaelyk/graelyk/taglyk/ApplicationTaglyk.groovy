package groovyx.gaelyk.graelyk.taglyk

import groovyx.gaelyk.graelyk.*


class ApplicationTaglyk
{
	static final SCOPES = [page:'pageScope',
						   application:'servletContext',
						   request:'request',
						   session:'session',
						   flash:'flash']
						   
	static boolean useJsessionId = false
	
	
	static void g_cookie(script, attrs){script.out << script.cookie(attrs)}
	static void g_header(script, attrs){script.out << script.header(attrs)}
	static void g_makeServerURL(script){script.out << script.makeServerURL()}
	static void g_resource(script, attrs){script.out << script.resource(attrs)}
	static void g_link(script, attrs, body){script.out << script.link(attrs, body)}
	static void g_createLink(script, attrs){script.out << script.createLink(attrs)}
	static void g_createURL(script, base, controller, action, params, frag){script.out << script.createURL(base, controller, action, params, frag)}
	static void g_withTag(script, attrs, body){script.out << script.withTag(attrs, body)}
	static void g_join(script, attrs){script.out << script.join(attrs)}
	static void g_meta(script, attrs){script.out << script.meta(attrs)}
	
	/**
	* Obtains the value of a cookie
	*/
	static cookie(script, attrs)
	{
		def cke = script.request.cookies.find {it.name == attrs['name']}
		if(cke){return cke.value}
	}

	static header(script, attrs)
	{
		if(attrs.name)
		{
			def hdr = script.request.getHeader(attrs.name)
			if(hdr){return hdr}
		}
	}
	
	/**
	* Sets a variable in the pageContext or the specified scope
	*/
	static set(script, attrs)
	{
		def scope = attrs.scope ? SCOPES[attrs.scope] : 'pageScope'
		def var = attrs.var
		def value = attrs.value
		def containsValue = attrs.containsKey('value')
		if(!scope) throw new TaglykException("Invalid [scope] attribute for taglyk <set>!")
		if(!var) throw new TaglykException("[var] attribute must be specified to for taglyk <set>!")

		script."$scope"."$var" = value
		null
	}
	
	/**
	* Get the URL of the server from the graelyk.properties file "appURL" property, or from the HTTP request
	*/
	static String makeServerURL(script)
	{
		def u = script.meta(name:"appURL")
		if(!u)
		{
			//Get the server URL from the HTTP request
			u = script.request.getRequestURL().toString().replaceAll(/([^\/]+\/\/[^\/]+).*/, "\$1")
		}
		return u
	}
	
	/**
	* Check for "absolute" attribute and render server URL if available from Config or deducible in non-production
	*/
	static private handleAbsolute(script, attrs)
	{
		def base = attrs.remove('base')
		if(base)
		{
			return base
		}
		else
		{
			def abs = attrs.remove("absolute")
			if (Boolean.valueOf(abs))
			{
				def u = script.makeServerURL()
				if(u)
				{
					return u
				}
				else
				{
					throw new TaglykException("Attribute absolute='true' specified but no appURL set in Config")
				}
			}
			else 
			{
				return script.request.getContextPath()
			}
		}
	}
	

	/**
	* Creates a link to a resource
	*
	* eg. <link type="text/css" href="${resource(dir:'css', file:'main.css')}" />
	*/
	static String resource (script, attrs)
	{
		def link = new StringBuffer()
		link << handleAbsolute(script, attrs)
		def dir = attrs['dir']
		if(attrs.contextPath != null) 
		{
			link << attrs.contextPath.toString()
		}
		if(dir) 
		{
			link << (dir.startsWith("/") ?  dir : "/${dir}")
		}
		def file = attrs['file']
		if(file) 
		{
			link << (file.startsWith("/") || dir?.endsWith('/') ?  file : "/${file}")
		}
		return link.toString()
	}
	
	
	/**
	*  General linking to controllers, actions etc. Examples:
	*
	*  ${link(action:"myaction", body:"link 1")}
	*  ${link(controller:"myctrl", action:"myaction"){"link 2"}}
	*/
	static link(script, attrs, body)
	{
		def link = new StringBuffer()
		def elementId = attrs.remove('elementId')
		link <<  "<a href=\"${script.createLink(attrs).encodeAs("HTML")}\""
		if(elementId) 
		{
			link << " id=\"${elementId}\""
		}

		link << "${attrs.collect {k, v -> " $k=\"$v\"" }.join('')}>"
		if(body)
		{
			link << script.captureOut(body)
		}
		link << "</a>"
		return link.toString()
	}
	
	
    /**
     * Creates a grails application link from a set of attributes. This
     * link can then be included in links, ajax calls etc. Generally used as a method call
     * rather than a tag eg.
     *
     *  <a href="${createLink(action:'list')}">List</a>
     */
    static createLink(script, attrs)
	{
    	def writer = new StringBuffer()

    	// prefer URI attribute
		if(attrs['uri'])
		{
			writer << handleAbsolute(script, attrs)
			writer << attrs.uri.toString()
		}
		else 
		{
			// prefer a URL attribute
			def urlAttrs = attrs
			if(attrs['url'] instanceof Map)
			{
				urlAttrs = attrs.remove('url').clone()
			}
			else if(attrs['url'])
			{
				urlAttrs = attrs.remove('url').toString()
			}

			if(urlAttrs instanceof String)
			{
				if(useJsessionId)
				{
					writer << script.response.encodeURL(urlAttrs)
				}
				else
				{
					writer << urlAttrs
				}
			}
			else
			{
				def controller = urlAttrs.containsKey("controller") ? urlAttrs.remove("controller")?.toString() : script.controllerName
				def action = urlAttrs.remove("action")?.toString()
				if(controller && !action && (action != ""))
				{
					action = script.controllerDefaultAction
				}
				def id = urlAttrs.remove("id")
				def frag = urlAttrs.remove('fragment')?.toString()
				def params = urlAttrs.params && urlAttrs.params instanceof Map ? urlAttrs.remove('params') : [:]
				//params.mappingName = urlAttrs.remove('mapping')
				if(script.request.getAttribute("flowExecutionKey"))
				{
					params."execution" = script.request.getAttribute("flowExecutionKey")
				}

				if(urlAttrs.event)
				{
					params."_eventId" = urlAttrs.remove('event')
				}
				def url
				if(id != null)
				{
					params.id = id
				}

				// cannot use jsessionid with absolute links
				if(useJsessionId && !attrs.absolute)
				{
					url = script.createURL("", controller, action, params, frag)
					def base = attrs.remove('base')
					if(base)
					{
						writer << base
					}
					writer << script.response.encodeURL(url)
				}
				else
				{
					url = script.createURL("", controller, action, params, frag)
					writer << handleAbsolute(script, attrs)
					writer << url
				}
			}
		}
		return writer.toString()
	}

	static String createURL(script, base, controller, action, params, frag)
	{
		def encoding = script.request.getCharacterEncoding()
		encoding = encoding ? encoding : "UTF-8"
		
		//params["action"] = action
		def queryParams = []
		//params.each{k,v->
		for(kvEntry in params)
		{
			def k = kvEntry.key
			def v = kvEntry.value
			queryParams << URLEncoder.encode(k.toString(), encoding) + "=" + URLEncoder.encode(v.toString(), encoding)
		}
		def query = queryParams.join("&")
		frag = frag ? URLEncoder.encode(frag, encoding) : ""
		def url = "/" + controller + (action ? "/" + action : "") + (query ? "?" + query : "") + (frag ? "#" + frag : "")
		return url
	}
	
	
	/**
	 * Helper method for creating tags called like:
	 *
	 * withTag(name:'script',attrs:[type:'text/javascript']) {
	 *
	 * }
	 */
	static String withTag(script, attrs, body)
	{
		def writer = new StringBuffer()
		writer << "<${attrs.name}"
		if(attrs.attrs) {
			//attrs.attrs.each{ k,v ->
			for(kvEntry in attrs.attrs)
			{
				def k = kvEntry.key
				def v = kvEntry.value
				if(v) {
					if(v instanceof Closure) {
						writer << " $k=\""
					    writer << script.captureOut(v)
						writer << '"'
					}
					else {
						writer << " $k=\"$v\""
					}
				}
			}
		}
		writer << '>'
		writer << script.captureOut(body)
		writer << "</${attrs.name}>"
		return writer.toString()
	}

	static String join(script, attrs)
	{
		def collection = attrs.'in'
		if(!collection)
		{
			throw new TaglykException('Taglyk ["join"] missing required attribute ["in"]')
		}
		def delimiter = attrs.delimiter ?: ', '

		return collection.join(delimiter)
	}

	/**
	 * Output application metadata that is loaded from application.properties
	 */
    static String meta(script, attrs)
	{
		return script.appProperties[attrs.name]
    }
}