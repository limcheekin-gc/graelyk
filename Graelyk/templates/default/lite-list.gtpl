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
            <div class="list">
				<div class="nav">
					\${isActionAllowed("create") ? '<span class="menuButton">' + link(class:"create menuButton", action:"create"){message(code:"default.new.label", args:[entityName])} + "</span>": ''}
				</div>
                <table>
                    <thead>
                        <tr>
						[% if(isActionAllowed("deleteMultiple")) { %]
							<th>
							<input type="checkbox" class="multi-checkbox" onclick="jQuery('.multi-checkbox').attr('checked', jQuery(this).attr('checked'));">
							</th>
						[% } %]
							<!-- Todo: Add sortable or non-sortable column headers -->
                            <th>[% g_message(code:"${domainClassPropertyName}.SomeProperty.label", default:"Id NotSortable") %]</th>
                            [% g_sortableColumn(property:"myBoolean", title:"Boolean Sortable") %]
                            [% g_sortableColumn(property:"myDate", title:"Date Sortable") %]
                            [% g_sortableColumn(property:"myField", title:"FieldValue Sortable") %]
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
                            <td>[% g_link(action:"show", id:"\${${propertyName}.id}"){%]\${fieldValue(bean: ${propertyName}, field: "id")}[% } %]</td>
                            <td>[% g_formatBoolean(boolean:"\${${propertyName}.SomeProperty}") %]</td>
                            <td>[% g_formatDate(date:${propertyName}.SomeProperty) %]</td>
                            <td>\${fieldValue(bean: ${propertyName}, field: "SomeProperty")}</td>
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
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]