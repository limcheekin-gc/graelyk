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
            <h1>[% g_message(code:il8nPrefix + ".create.label", args:[entityName]) %]</h1>
            [% if(flash.message)
			{ %]
            <div class="message">\${flash.message}</div>
            [% } %]
			<!-- Todo: For forms that include file uploads, add: enctype:"multipart/form-data" -->
            [% g_form(action:"save", method:"post")
			{ %]
                <div class="dialog">
					<div class="nav">
						\${isActionAllowed("create") ? '<span class="menuButton">' + link(class:"create", action:"create"){message(code:il8nPrefix + ".new.label", args:[entityName])} + '</span>' : ''}
					</div>
					<!-- Todo: Use ${propertyName}.myProperty / ${domainClassPropertyName}.myProperty, etc. to create the form -->
                </div>
                <div class="buttons">
					[% if(isActionAllowed("save")) { %]
						<span class="button">[% g_submitButton name:"create", class:"save", value:message(code: il8nPrefix + '.button.create.label', default: 'Create') %]</span>
					[% } %]
                </div>
            [% } %]
        </div>
[% include("/WEB-INF/includes/footer.gtpl") %]
