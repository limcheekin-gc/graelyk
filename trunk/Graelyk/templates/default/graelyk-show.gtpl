[% import com.google.appengine.api.datastore.* %]
[% import ${domainClassFullName} %]
[% unwrapVariables() %]

[% entityName = "\${message(code: '${domainClassPropertyName}.label', default: '${domainClassName}')}" %]
[% headTitle = message(code:"default.show.label", args:[entityName]) %]
[% headIncludes = """
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
@HeadIncludes@
""" %]

[% navIncludes = """ """ %]

[% subNavIncludes = """ """ %]

[% wrapVariables(["headTitle", "headIncludes", "navIncludes", "subNavIncludes"]) %]

[% include("/WEB-INF/includes/header.gtpl") %]
        <div class="body">
            <h1>[% g_message code:"default.show.label", args:[entityName] %]</h1>
            [% if(flash.message) { %]
            <div class="message">\${flash.message}</div>
            [% } %]
            <div class="dialog">
				<div class="nav">
					\${isActionAllowed("list") ? '<span class="menuButton">' + link(class:"list", action:"list"){message(code:"default.list.label", args:[entityName])} + '</span>' : ''}
					\${isActionAllowed("create") ? '<span class="menuButton">' + link(class:"create", action:"create"){message(code:"default.new.label", args:[entityName])} + '</span>' : ''}
				</div>
                <table>
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
            <div class="buttons">
                [% g_form([:]){ %]
                    [% g_hiddenField name:"id", value:${propertyName}?.id %]
                    [% if(isActionAllowed("edit")) { %]
						<span class="button">[% g_actionSubmit class:"edit", action:"edit", value:"\${message(code: 'default.button.edit.label', default: 'Edit')}" %]</span>
					[% } %]
                    [% if(isActionAllowed("delete")) { %]
						<span class="button">[% g_actionSubmit class:"delete", action:"delete", value:"\${message(code: 'default.button.delete.label', default: 'Delete')}", onclick:"return confirm('\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" %]</span>
					[% } %]
                [% } %]
            </div>
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]
