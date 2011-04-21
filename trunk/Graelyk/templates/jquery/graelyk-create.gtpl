<!-- JQUERY -->
[% import com.google.appengine.api.datastore.* %]
[% import ${domainClassFullName} %]
[% unwrapVariables() %]

[% entityName = "\${message(code: '${domainClassPropertyName}.label', default: '${domainClassName}')}" %]
[% headTitle = message(code:il8nPrefix + ".create.label", args:[entityName]) %]
[% headIncludes = """
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
@HeadIncludes@
""" %]

[% navIncludes = """ """ %]

[% subNavIncludes = """ """ %]

[% wrapVariables(["headTitle", "headIncludes", "navIncludes", "subNavIncludes"]) %]

[% include("/WEB-INF/includes/header.gtpl") %]
        <div class="body">
            <h1>[% g_message(code:il8nPrefix + ".create.label", args:[entityName]) %]</h1>
            [% if(flash.message)
			{ %]
            <div class="message">\${flash.message}</div>
            [% } %]
            [% g_hasErrors(bean:${propertyName})
			{ %]
            <div class="errors">
                [% g_renderErrors(bean:${propertyName}, as:"list") %]
            </div>
            [% } %]
            [% g_form(action:"save", method:"post" <%= multiPart ? ', enctype:"multipart/form-data"' : '' %>) 
			{ %]
                <div class="dialog">
					<div class="nav">
						\${isActionAllowed("create") ? '<span class="menuButton">' + link(class:"create", action:"create"){message(code:il8nPrefix + ".new.label", args:[entityName])} + '</span>' : ''}
					</div>
                    <table>
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
                                //if (!Collection.class.isAssignableFrom(p.type)) {
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
								{ %>
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
                <div class="buttons">
                    [% if(isActionAllowed("save")) { %]
						<span class="button">[% g_submitButton name:"create", class:"save", value:message(code: il8nPrefix + '.button.create.label', default: 'Create') %]</span>
					[% } %]
                </div>
				<%= hiddenFields.toString() %>
            [% } %]
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]
