[% import com.google.appengine.api.datastore.* %]
[% import ${domainClassFullName} %]
[% unwrapVariables() %]

[% entityName = "\${message(code: '${domainClassPropertyName}.label', default: '${domainClassName}')}" %]
[% headTitle = message(code:il8nPrefix + ".list.label", args:[entityName]) %]
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
				<div class="nav">
					\${isActionAllowed("list") ? '<span class="menuButton">' + link(class:"list", action:"list"){message(code:il8nPrefix + ".list.label", args:[entityName])} + '</span>' : ''}
					\${isActionAllowed("create") ? '<span class="menuButton">' + link(class:"create", action:"create"){message(code:il8nPrefix + ".new.label", args:[entityName])} + '</span>' : ''}
				</div>
				<!-- Todo: Show some of the data using ${propertyName}.SomeProperty -->
            </div>
            <div class="buttons">
                [% g_form([:]){ %]
                    [% g_hiddenField name:"id", value:${propertyName}?.id %]
					[% if(isAllowedAction("edit") { %]
						<span class="button">[% g_actionSubmit class:"edit", action:"edit", value:"\${message(code: il8nPrefix + '.button.edit.label', default: 'Edit')}" %]</span>
					[% } %]
					[% if(isAllowedAction("delete") { %]
						<span class="button">[% g_actionSubmit class:"delete", action:"delete", value:"\${message(code: il8nPrefix + '.button.delete.label', default: 'Delete')}", onclick:"return confirm('\${message(code: il8nPrefix + '.button.delete.confirm.message', default: 'Are you sure?')}');" %]</span>
					[% } %]
                [% } %]
            </div>
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]
