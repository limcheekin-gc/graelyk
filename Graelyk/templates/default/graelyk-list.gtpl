[% import com.google.appengine.api.datastore.* %]
[% import ${domainClassFullName} %]
[% unwrapVariables() %]

[% entityName = "\${message(code: '${domainClassPropertyName}.label', default: '${domainClassName}')}" %]
[% headTitle = message(code:"default.list.label", args:[entityName]) %]
[% headIncludes = """
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script type="text/javascript" src="\${appProperties['jquery.url']}"></script>
@HeadIncludes@

""" %]

[% navIncludes = """ """ %]

[% subNavIncludes = """ """ %]

[% wrapVariables(["headTitle", "headIncludes", "navIncludes", "subNavIncludes"]) %]

[% include("/WEB-INF/includes/header.gtpl") %]
        <div class="body">
            <h1>[% g_message(code:"default.list.label", args:[entityName]) %]</h1>
            [% if(flash?.message){ %]
            <div class="message">\${flash.message}</div>
            [% } %]
			<form method="post" class="inline">
            <div class="list">
				<div class="nav">
					\${isActionAllowed("create") ? '<span class="menuButton">' + link(class:"create", action:"create"){message(code:"default.new.label", args:[entityName])} + '</span>' : ''}
				</div>
                <table>
                    <thead>
                        <tr>
						[% if(isActionAllowed("deleteMultiple")) { %]
							<th>
							<input type="checkbox" class="multi-checkbox" onclick="jQuery('.multi-checkbox').attr('checked', jQuery(this).attr('checked'));">
							</th>
						[% } %]
                        <%  
							//excludedProps = Event.allEvents.toList() << 'version'
							excludedProps = domainClass.excludedProperties << 'version'
                            props = domainClass.properties.findAll { !excludedProps.contains(it.name) /*&& it.type != Set.class*/ }
							Collections.sort(props, comparator)
                            props.eachWithIndex { p, i ->
                                if (i < 6) {
								if(p.isAssociation()) //not sortable
								{ %>
                            <th>[% g_message(code:"${domainClassPropertyName}.${p.name}.label", default:"${p.naturalName}") %]</th>
								<% } 
								else //sortable
								{ %>
                            [% g_sortableColumn(property:"${p.name}", title:"\${message(code: '${domainClassPropertyName}.${p.name}.label', default: '${p.naturalName}')}") %]
						<% }   }   } %>
                        </tr>
                    </thead>
                    <tbody>
                    [% ${propertyName}List.eachWithIndex{${propertyName}, i-> %]
                        <tr class="\${(i % 2) == 0 ? 'odd' : 'even'}">
						[% if(isActionAllowed("deleteMultiple")) { %]
							<td>
							<input type="checkbox" name="id" value="\${${propertyName}.id}" class="multi-checkbox">
							</td>
						[% } %]
                        <%  props.eachWithIndex { p, i ->
                                cp = domainClass.constrainedProperties[p.name]
                                if (i == 0) { %>
								<td valign="top" class="value">
									[% if(isActionAllowed("show")) { %]
										[% g_link(action:"show", id:"\${${propertyName}.id}"){%]${renderDisplay.call(p, headerList)}[% } %]
									[% } else { %]
										${renderDisplay.call(p, headerList)}
									[% } %]
								</td>
                        <%      } else if (i < 6) { %>
							<td valign="top" class="value">${renderDisplay.call(p, headerList)}</td>
                        <%  }  }  %>
                        </tr>
                    [% } %]
                    </tbody>
                </table>
            </div>
			[% if(isActionAllowed("deleteMultiple")) { %]
				<div class="buttons">
				<input type="checkbox" class="multi-checkbox" onclick="jQuery('.multi-checkbox').attr('checked', jQuery(this).attr('checked'));">
				<span class="button">[% g_actionSubmit class:"delete", action:"deleteMultiple", value:message(code: 'default.button.delete-selected.label', default: 'Delete Selected'), onclick:"return confirm('\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" %]</span>
				</div>
			[% } %]
			[% if(${propertyName}Total > ${propertyName}PerPage) { %]
            <div class="paginateButtons">
                [% g_paginate(total:"\${${propertyName}Total}", max:"\${${propertyName}PerPage}") %]
            </div>
			[% } %]
			</form>
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]