[% import com.google.appengine.api.datastore.* %]
[% import ${domainClassFullName} %]
[% unwrapVariables() %]

[% entityName = "\${message(code: '${domainClassPropertyName}.label', default: '${domainClassName}')}" %]
[% headTitle = message(code:il8nPrefix + ".edit.label", args:[entityName]) %]
[% headIncludes = """
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
@HeadIncludes@
""" %]

[% navIncludes = """ """ %]

[% subNavIncludes = """ """ %]

[% wrapVariables(["headTitle", "headIncludes", "navIncludes", "subNavIncludes"]) %]

[% include("/WEB-INF/includes/header.gtpl") %]
        <div class="body">
            <h1>[% g_message code:il8nPrefix + ".edit.label", args:[entityName] %]</h1>
            [% if(flash.message) { %]
            <div class="message">\${flash.message}</div>
            [% } %]
            [% g_hasErrors(bean:${propertyName}) { %]
            <div class="errors">
                [% g_renderErrors bean:${propertyName}, as:"list" %]
            </div>
            [% } %]
            [% g_form(method:"post", id:"mainForm", action:"" <%= multiPart ? ', enctype:"multipart/form-data"' : '' %>) { %]
				[% g_hiddenField name:"action", id:"action", value:"" %]
                [% g_hiddenField name:"id", value:${propertyName}?.id %]
                [% g_hiddenField name:"version", value:${propertyName}?.version %]
                <div class="dialog">
					<div class="ui-widget ui-widget-content ui-corner-all">
						[% if(isActionAllowed("list")) { %]
							[% g_jQueryButtonLink(action:"list", icon:"zoomout", text:message(code:il8nPrefix + ".list.label", args:[entityName])) %]
						[% } %]
						
						[% if(isActionAllowed("create")) { %]
							[% g_jQueryButtonLink(action:"create", icon:"plus", text:message(code:il8nPrefix + ".new.label", args:[entityName])) %]
						[% } %]
					</div>
                    <table class="ui-widget ui-widget-content ui-corner-all">
                        <tbody>
                        <%  
							//excludedProps = Event.allEvents.toList() << 'version' << 'id' << 'dateCreated' << 'lastUpdated'
							excludedProps = domainClass.excludedProperties
							excludedProps << 'id' << 'version'
							hiddenFields = new StringBuffer()
                            props = domainClass.properties.findAll { !excludedProps.contains(it.name) }
                            //Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
							Collections.sort(props, comparator)
                            props.each { p ->
                                cp = domainClass.constrainedProperties[p.name]
								display = cp?.display
								editable = cp?.editable
								if(display == null){display = true}
								if(editable == null){editable = true}
								if(display)
								{
								if(cp && cp.widget && cp.widget.toLowerCase() == "hidden")
								{
									hiddenFields << renderEditor.call(p, headerList) + "\r\n"
								}
								else
								{ 
							%>
									<tr class="prop">
										<td valign="top" class="name">
											<label for="${p.name}">[% g_message code:"${domainClassPropertyName}.${p.name}.label", default:"${p.naturalName}" %]</label>
										</td>
										<td valign="top" class="value \${hasErrors(bean: ${propertyName}, field: '${p.name}', 'errors')}">
											<%
											if(editable)
											{
											%>
											${renderEditor.call(p, headerList)}
											<%
											}
											else
											{
											%>
											${renderDisplay.call(p, headerList)}
											<% } %>
										</td>
									</tr>
						<%  }  }  }  %>
                        </tbody>
                    </table>
                </div>
                <div class="ui-widget ui-widget-content ui-corner-all">
					[% if(isActionAllowed("delete")) { %]
						[% g_jQueryActionSubmit(inputSelector:"#action", formSelector:"#mainForm", class:"right", icon:"trash", action:"delete", value:message(code: il8nPrefix + '.button.delete.label', default:'Delete'), onclick:"""if(!confirm("\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}")){return false;} """ ) %]
					[% } %]
					[% if(isActionAllowed("update")) { %]
						[% g_jQueryActionSubmit(inputSelector:"#action", formSelector:"#mainForm", icon:"disk", action:"update", value:message(code: il8nPrefix + '.button.update.label', default:'Update')) %]
					[% } %]
                </div>
				<%= hiddenFields.toString() %>
            [% } %]
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]
