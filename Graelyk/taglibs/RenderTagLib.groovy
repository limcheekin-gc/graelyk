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
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.plugins.web.taglib

import org.springframework.web.servlet.support.RequestContextUtils as RCU

import com.opensymphony.module.sitemesh.Factory
import com.opensymphony.module.sitemesh.RequestConstants
import grails.util.GrailsNameUtils
import groovy.text.Template
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.ServletConfig
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.codehaus.groovy.grails.web.mapping.ForwardUrlMappingInfo
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods
import org.codehaus.groovy.grails.web.pages.GroovyPage
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.sitemesh.FactoryHolder
import org.codehaus.groovy.grails.web.sitemesh.GSPSitemeshPage
import org.codehaus.groovy.grails.web.sitemesh.GrailsPageFilter
import org.codehaus.groovy.grails.web.util.StreamCharBuffer
import org.codehaus.groovy.grails.web.util.WebUtils

/**
 * An tag library that contains tags to help rendering of views and layouts
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
class RenderTagLib implements com.opensymphony.module.sitemesh.RequestConstants {
	def out // to facilitate testing

    ServletConfig servletConfig
    GroovyPagesTemplateEngine groovyPagesTemplateEngine
    GrailsPluginManager pluginManager
    
    static Map TEMPLATE_CACHE = new ConcurrentHashMap()

    protected getPage() {
    	return request[PAGE]
    }

    /**
     * Includes another controller/action within the current response
     *
     * <g:include controller="foo" action="test"></g:include>
     *
     * @param controller The name of he controller
     * @param action The name of the action
     * @param id The identifier
     * @param params Any parameters
     * @param view The name of the view. Cannot be specified in combination with controller/action/id
     * @param model A model to pass onto the included controller in the request 
     */
	def include =  { attrs, body ->
		if(attrs.action && !attrs.controller) {
			def controller = request?.getAttribute(GrailsApplicationAttributes.CONTROLLER)
			def controllerName = controller?.getProperty(ControllerDynamicMethods.CONTROLLER_NAME_PROPERTY)
			attrs.controller = controllerName
		}
		if(attrs.controller || attrs.view){
			def mapping = new ForwardUrlMappingInfo(controller:attrs.controller,
													action:attrs.action,
													view:attrs.view,
													id:attrs.id,
                                                    params:attrs.params)

			out << WebUtils.includeForUrlMappingInfo(request,response,mapping, attrs.model ?: [:] )?.content
		}
	}

    /**
     * Apply a layout to a particular block of text or to the given view or template
     *
     * <g:applyLayout name="myLayout">some text</g:applyLayout>
     * <g:applyLayout name="myLayout" template="mytemplate" />
     * <g:applyLayout name="myLayout" url="http://www.google.com" />
     *
     * @param name The name of the layout
     * @param template Optional. The template to apply the layout to
     * @param url Optional. The URL to retrieve the content from and apply a layout to
     * @param contentType Optional. The content type to use, default is "text/html"
     * @param encoding Optional. The encoding to use
     * @param params Optiona. The params to pass onto the page object
     */
    def applyLayout = { attrs, body ->
        if(!groovyPagesTemplateEngine) throw new IllegalStateException("Property [groovyPagesTemplateEngine] must be set!")
        def oldPage = getPage()
        def contentType = attrs.contentType ? attrs.contentType : "text/html"

        def content = ""
        GSPSitemeshPage gspSiteMeshPage = null
        if(attrs.url) {
        	content = new URL(attrs.url).text
        } else {
        	def oldGspSiteMeshPage=request.getAttribute(GrailsPageFilter.GSP_SITEMESH_PAGE)
        	try {
        		gspSiteMeshPage = new GSPSitemeshPage()
        		request.setAttribute(GrailsPageFilter.GSP_SITEMESH_PAGE, gspSiteMeshPage)
		        if(attrs.view || attrs.template) {
		            content = render(attrs)
		        }
		        else {
		        	def bodyClosure = GroovyPage.createOutputCapturingClosure(this, body, webRequest, true)
		            content = bodyClosure()
		        }
        		if(content instanceof StreamCharBuffer) {
        			gspSiteMeshPage.setPageBuffer(content)
        		} else if (content != null) {
        			def buf=new StreamCharBuffer()
        			buf.writer.write(content)
        			gspSiteMeshPage.setPageBuffer(buf)
        		}
        	} finally {
       			 request.setAttribute(GrailsPageFilter.GSP_SITEMESH_PAGE, oldGspSiteMeshPage)
        	}
        }

        def page = null
        if(gspSiteMeshPage != null && gspSiteMeshPage.isUsed()) {
        	page = gspSiteMeshPage
        } else {
            def parser = getFactory().getPageParser(contentType)
        	page = parser.parse(content.toCharArray())
        }
        
        attrs.params?.each { k,v->
            page.addProperty(k,v?.toString())
        }
        def decoratorMapper = getFactory().getDecoratorMapper()

        if(decoratorMapper) {
            def d = decoratorMapper.getNamedDecorator(request, attrs.name)
            if(d && d.page) {
                try {
                    request[PAGE] = page
                  	def t = groovyPagesTemplateEngine.createTemplate(d.getPage())
                    def w = t.make()
                    w.writeTo(out)

                } finally {
                    request[PAGE] = oldPage
                }
            }
        }
    }

    private Factory getFactory() {
        return FactoryHolder.getFactory()
    }

    /**
     * Used to retrieve a property of the decorated page
     *
     * <g:pageProperty default="defaultValue" name="body.onload" />
     */
    def pageProperty = { attrs ->
            if(!attrs.name) {
	            throwTagError("Tag [pageProperty] is missing required attribute [name]")
            }

            def propertyName = attrs.name
            def htmlPage = getPage()
            def propertyValue = null
            
            if(htmlPage instanceof GSPSitemeshPage) {
            	// check if there is an component content buffer
            	propertyValue = htmlPage.getContentBuffer(propertyName)
            }

            if(!propertyValue)
            	propertyValue = htmlPage.getProperty(propertyName)

            if (!propertyValue)
                propertyValue = attrs.'default';

            if (propertyValue) {
                if (attrs.writeEntireProperty) {
                    out << ' '
                    out << propertyName.substring(propertyName.lastIndexOf('.') + 1)
                    out << "=\""
                    out << propertyValue
                    out << "\""
                }
                else {
                    out << propertyValue
                }
            }
    }

	/**
	 * Invokes the body of this tag if the page property exists:
	 *
	 * <g:ifPageProperty name="meta.index">body to invoke</g:ifPageProperty>
	 *
	 * of it equals a certain value:
	 *
	 *<g:ifPageProperty name="meta.index" equals="blah">body to invoke</g:ifPageProperty>
	 */
	def ifPageProperty = { attrs, body ->
		if(attrs.name) {
			def htmlPage = getPage()
			def names = ((attrs.name instanceof List) ? attrs.name : [attrs.name])


			def invokeBody = true
			for(i in 0..<names.size()) {
				String propertyValue = htmlPage.getProperty(names[i])
				if(propertyValue) {
					if(attrs.equals instanceof List) {
						invokeBody = (attrs.equals[i]==propertyValue)
					}
					else if(attrs.equals instanceof String) {
						invokeBody = (attrs.equals == propertyValue)
					}
				}
				else {
					invokeBody = false
					break
				}
			}
			if(invokeBody) {
				out << body()
			}
		}
	}
    /**
     * Used in layouts to render the page title from the SiteMesh page
     *
     * <g:layoutTitle default="The Default title" />
     */
	def layoutTitle = { attrs ->
        String title = page.title
        if (!title && attrs.'default') title = attrs.'default'
        if (title) out << title;
	}

    /**
     * Used in layouts to render the body of a SiteMesh layout
     *
     * <g:layoutBody />
     */
	def layoutBody = { attrs ->
		getPage().writeBody(out)
	}

    /**
     * Used in layouts to render the head of a SiteMesh layout
     *
     * <g:layoutHead />
     */
	def layoutHead = { attrs ->
		getPage().writeHead(out)
	}

	/**
	 * Creates next/previous links to support pagination for the current controller
	 *
	 * <g:paginate total="${Account.count()}" />
	 */
	def paginate = { attrs ->
		def writer = out
        if(attrs.total == null)
            throwTagError("Tag [paginate] is missing required attribute [total]")
                                    
		def messageSource = grailsAttributes.messageSource
		def locale = RCU.getLocale(request)

		def total = attrs.int('total') ?: 0
		def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))
		def offset = params.int('offset') ?: 0
		def max = params.int('max')
		def maxsteps = (attrs.int('maxsteps') ?: 10)

		if(!offset) offset = (attrs.int('offset') ?: 0)
		if(!max) max = (attrs.int('max') ?: 10)

		def linkParams = [:]
		if(attrs.params) linkParams.putAll(attrs.params)
        linkParams.offset = offset - max
        linkParams.max = max
		if(params.sort) linkParams.sort = params.sort
		if(params.order) linkParams.order = params.order

		def linkTagAttrs = [action:action]
		if(attrs.controller) {
			linkTagAttrs.controller = attrs.controller
		}
		if(attrs.id!=null) {
			linkTagAttrs.id = attrs.id
		}
		linkTagAttrs.params = linkParams

		// determine paging variables
		def steps = maxsteps > 0
		int currentstep = (offset / max) + 1
		int firststep = 1
		int laststep = Math.round(Math.ceil(total / max))

		// display previous link when not on firststep
		if(currentstep > firststep) {
			linkTagAttrs.class = 'prevLink'
            linkParams.offset = offset - max
            writer << link(linkTagAttrs.clone()) {
				(attrs.prev ? attrs.prev : messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
			 }
		}

		// display steps when steps are enabled and laststep is not firststep
		if(steps && laststep > firststep) {
			linkTagAttrs.class = 'step'

			// determine begin and endstep paging variables
			int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
			int endstep = currentstep + Math.round(maxsteps / 2) - 1

			if(beginstep < firststep) {
				beginstep = firststep
				endstep = maxsteps
			}
			if(endstep > laststep) {
				beginstep = laststep - maxsteps + 1
				if(beginstep < firststep) {
					beginstep = firststep
				}
				endstep = laststep
			}

			// display firststep link when beginstep is not firststep
			if(beginstep > firststep) {
				linkParams.offset = 0
				writer << link(linkTagAttrs.clone()) {firststep.toString()}
				writer << '<span class="step">..</span>'
			}

			// display paginate steps
			(beginstep..endstep).each { i ->
				if(currentstep == i) {
					writer << "<span class=\"currentStep\">${i}</span>"
				}
				else {
					linkParams.offset = (i - 1) * max
					writer << link(linkTagAttrs.clone()) {i.toString()}
				}
			}

			// display laststep link when endstep is not laststep
			if(endstep < laststep) {
				writer << '<span class="step">..</span>'
				linkParams.offset = (laststep -1) * max
				writer << link(linkTagAttrs.clone()) { laststep.toString() }
			}
		}

		// display next link when not on laststep
		if(currentstep < laststep) {
			linkTagAttrs.class = 'nextLink'
			linkParams.offset = offset + max
			writer << link(linkTagAttrs.clone()) {
				(attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
			}
		}

	}

	/**
	 * Renders a sortable column to support sorting in list views
	 *
	 * Attributes:
	 *
	 * property - name of the property relating to the field
	 * defaultOrder (optional) - default order for the property; choose between asc (default if not provided) and desc
	 * title (optional*) - title caption for the column
	 * titleKey (optional*) - title key to use for the column, resolved against the message source
	 * params (optional) - a map containing request parameters
	 * action (optional) - the name of the action to use in the link, if not specified the list action will be linked
	 *
	 * Attribute title or titleKey is required. When both attributes are specified then titleKey takes precedence,
	 * resulting in the title caption to be resolved against the message source. In case when the message could
	 * not be resolved, the title will be used as title caption.
	 *
	 * Examples:
	 *
	 * <g:sortableColumn property="title" title="Title" />
	 * <g:sortableColumn property="title" title="Title" style="width: 200px" />
	 * <g:sortableColumn property="title" titleKey="book.title" />
	 * <g:sortableColumn property="releaseDate" defaultOrder="desc" title="Release Date" />
	 * <g:sortableColumn property="releaseDate" defaultOrder="desc" title="Release Date" titleKey="book.releaseDate" />
	 */
	def sortableColumn = { attrs ->
		def writer = out
		if(!attrs.property)
			throwTagError("Tag [sortableColumn] is missing required attribute [property]")

		if(!attrs.title && !attrs.titleKey)
			throwTagError("Tag [sortableColumn] is missing required attribute [title] or [titleKey]")

		def property = attrs.remove("property")
		def action = attrs.action ? attrs.remove("action") : (actionName ?: "list")

		def defaultOrder = attrs.remove("defaultOrder")
		if(defaultOrder != "desc") defaultOrder = "asc"

		// current sorting property and order
		def sort = params.sort
		def order = params.order

		// add sorting property and params to link params
		def linkParams = [:]
		if(params.id) linkParams.put("id",params.id)
		if(attrs.params) linkParams.putAll(attrs.remove("params"))
		linkParams.sort = property

		// determine and add sorting order for this column to link params
		attrs.class = (attrs.class ? "${attrs.class} sortable" : "sortable")
		if(property == sort) {
			attrs.class = attrs.class + " sorted " + order
			if(order == "asc") {
				linkParams.order = "desc"
			}
			else {
				linkParams.order = "asc"
			}
		}
		else {
			linkParams.order = defaultOrder
		}

		// determine column title
		def title = attrs.remove("title")
		def titleKey = attrs.remove("titleKey")
		if(titleKey) {
			if(!title) title = titleKey
			def messageSource = grailsAttributes.messageSource
			def locale = RCU.getLocale(request)
			title = messageSource.getMessage(titleKey, null, title, locale)
		}

		writer << "<th "
		// process remaining attributes
		attrs.each { k, v ->
			writer << "${k}=\"${v.encodeAsHTML()}\" "
		}
		writer << ">${link(action:action, params:linkParams) { title }}</th>"
	}

    /**
     *  allows rendering of templates inside views for collections, models and beans. Examples:
     *
     *  <g:render template="atemplate" collection="${users}" />
     *  <g:render template="atemplate" model="[user:user,company:company]" />
     *  <g:render template="atemplate" bean="${user}" />
     */
    def render = { attrs, body ->
        if(!groovyPagesTemplateEngine) throw new IllegalStateException("Property [groovyPagesTemplateEngine] must be set!")
        if(!attrs.template)
            throwTagError("Tag [render] is missing required attribute [template]")

        def engine = groovyPagesTemplateEngine
        def uri = grailsAttributes.getTemplateUri(attrs.template,request)
        def var = attrs['var']

        Template t = TEMPLATE_CACHE[uri]

        if(t==null) {
            def contextPath = attrs.contextPath ? attrs.contextPath : null
            		                    
            def pluginName = attrs.plugin
            if(pluginName) {
                contextPath = pluginManager?.getPluginPath(pluginName) ?: ''
            }
            else if (contextPath == null) {
            	if(uri.startsWith("/plugins/")) contextPath = ""
            	else {
            		contextPath = pageScope.pluginContextPath ?: ""
            	}            
            }
            def templatePath = "${contextPath}${uri}"
            def templateResolveOrder
            if(pluginName) {
            	templateResolveOrder = [templatePath, "${contextPath}/grails-app/views/${uri}"]
            }
            else {
            	templateResolveOrder = [uri, templatePath, "${contextPath}/grails-app/views/${uri}"]
            }
        	
            t = engine.createTemplateForUri(templateResolveOrder as String[])
			if(!engine.isReloadEnabled() && t!=null) {
				  def prevt = TEMPLATE_CACHE.putIfAbsent(uri, t)
				  if(prevt != null) {
					  t = prevt
				  }
			}
        }

        if(!t) {
            throwTagError("Template not found for name [$attrs.template] and path [$uri]")            
        }
        else {
            if(attrs.containsKey('bean')) {
                def b = [body: body]
                if (attrs.model instanceof Map) {
                    b += attrs.model
                }
                if (var) {
                    b.put(var, attrs.bean)
                }
                else {
                    b.put('it', attrs.bean)
                }
                t.make(b).writeTo(out)
            }
            else if(attrs.containsKey('collection')) {
                def collection = attrs.collection
                def key = 'it'
                if(collection) {
                    def first = collection.iterator().next()
                    key = first ? GrailsNameUtils.getPropertyName(first.getClass()) : 'it'
                }
                collection.each {
                    def b = [body:body]
                    if (attrs.model instanceof Map) {
                        b += attrs.model
                    }
                    if (var) {
                        b.put(var, it)
                    }
                    else {
                        b.put('it', it)
                        b.put(key, it)
                    }
                    t.make(b).writeTo(out)
                }
            }
            else if(attrs.model instanceof Map) {
                t.make( [body:body] + attrs.model ).writeTo(out)
            }
            else if(attrs.template) {
                t.make([body:body]).writeTo(out)
            }
        }

    }

}
