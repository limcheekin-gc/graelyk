[% import com.google.appengine.api.datastore.* %]
[% import ${domainClassFullName} %]
[% unwrapVariables() %]

[% entityName = "\${message(code: '${domainClassPropertyName}.label', default: '${domainClassName}')}" %]
[% headTitle = message(code:il8nPrefix + ".show.label", args:[entityName]) %]
[% headIncludes = """
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
@HeadIncludes@
""" %]

[% navIncludes = """ """ %]

[% subNavIncludes = """ """ %]

[% wrapVariables(["headTitle", "headIncludes", "navIncludes", "subNavIncludes"]) %]

[% include("/WEB-INF/includes/header.gtpl") %]
        <div class="body">
            <h1>[% g_message code:il8nPrefix + ".show.label", args:[entityName] %]</h1>
            [% if(flash.message) { %]
            <div class="message">\${flash.message}</div>
            [% } %]
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
						//excludedProps = Event.allEvents.toList() << 'version'
						excludedProps = domainClass.excludedProperties
						excludedProps << 'version'
						props = domainClass.properties.findAll { !excludedProps.contains(it.name) }
                        //Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))\
						Collections.sort(props, comparator)
                        props.each { p -> 
							cp = domainClass.constrainedProperties[p.name] 
							display = (cp?.display ?: true)
                            if (display) {
						%>
                        <tr class="prop">
                            <td valign="top" class="name">[% g_message code:"${domainClass.propertyName}.${p.name}.label", default:"${p.naturalName}" %]</td>
							<td valign="top" class="value">${renderDisplay.call(p, headerList)}</td>
                        </tr>
                    <% } } %>
                    </tbody>
                </table>
            </div>
            <div class="ui-widget ui-widget-content ui-corner-all">
                [% g_form(method:"post", id:"mainForm", action:""){ %]
					[% g_hiddenField name:"action", id:"action", value:"" %]
                    [% g_hiddenField name:"id", value:${propertyName}?.id %]
					
					[% if(isActionAllowed("delete")) { %]
						[% g_jQueryActionSubmit(inputSelector:"#action", formSelector:"#mainForm", class:"right", icon:"trash", action:"delete", value:message(code: il8nPrefix + '.button.delete.label', default:'Delete'), onclick:"""if(!confirm("\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}")){return false;} """ ) %]
					[% } %]
					[% if(isActionAllowed("edit")) { %]
						[% g_jQueryActionSubmit(inputSelector:"#action", formSelector:"#mainForm", icon:"disk", action:"edit", value:message(code: il8nPrefix + '.button.edit.label', default:'Edit')) %]
					[% } %]
                [% } %]
            </div>
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]
