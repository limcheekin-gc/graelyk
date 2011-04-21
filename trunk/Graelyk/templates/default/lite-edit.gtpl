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

[% subNavIncludes = """ """%]

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
			<!-- Todo: For forms that include file uploads, add: enctype:"multipart/form-data" -->
            [% g_form(method:"post") { %]
                [% g_hiddenField name:"id", value:${propertyName}?.id %]
                [% g_hiddenField name:"version", value:${propertyName}?.version %]
                <div class="dialog">
					<div class="nav">
						\${isActionAllowed("create") ? '<span class="menuButton">' + link(class:"create", action:"create"){message(code:il8nPrefix + ".new.label", args:[entityName])} + '</span>' : ''}
					</div>
					<!-- Todo: Use ${propertyName}.myProperty / ${domainClassPropertyName}.myProperty, etc. to create the form -->
                </div>
                <div class="buttons">
					[% if(isActionAllowed("update")) { %]
						<span class="button">[% g_actionSubmit class:"save", action:"update", value:message(code: il8nPrefix + '.button.update.label', default: 'Update') %]</span>
					[% } %]
					[% if(isActionAllowed("delete")) { %]
						<span class="button">[% g_actionSubmit class:"delete", action:"delete", value:message(code: il8nPrefix + '.button.delete.label', default: 'Delete'), onclick:"return confirm('\${message(code: il8nPrefix + '.button.delete.confirm.message', default: 'Are you sure?')}');" %]</span>
					[% } %]
                </div>
            [% } %]
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]
