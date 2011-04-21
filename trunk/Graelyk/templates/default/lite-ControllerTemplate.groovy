import ${domainClassFullName}

import com.googlecode.objectify.Key
import groovyx.gaelyk.graelyk.FileUpload

//This is here to handle multipart/form-data (i.e. file uploads) if they occur.
FileUpload.getFilesAndFields(request, params)

domainClassName = "${domainClassName}"
view = "index"
controllerName = "${domainClassPropertyName.toLowerCase()}" //Todo: Change this to the name you use in routes.groovy or web.xml
controllerURL = "/" + controllerName
il8nPrefix = "default" //Todo: Change this to the prefix you will use in the localization files for this domain class and its views, e.g. ${domainClassPropertyName}
controllerDefaultAction = "index"
flash = [:]
allowedActions = ["index", "list", "show"]
userActions = ["create", "save", "edit", "update", "delete", "deleteMultiple"]
adminActions = ["deleteAll"]
allowedMethods = [save: "POST", update: "POST", delete: "POST"]

//Todo: Redirect if the user is not logged in. Do things with the user object
  if(!user){redirect(users.createLoginURL(request.getRequestURI()))}


/*
Todo: Retrieve the user's locale preferences and set them. Otherwise they will be set to defaults in the GraelykCategory.processController() method.
      The application's available message locales and default locales can be set in the war/graelyk.properties file.
- userLocale is a List of Locales, e.g. [new Locale("ln", "CG"), new Locale("fr", "FR"), new Locale("en", "US")]

- userNumberLocale, userCurrencyLocale, and userDateLocale can be set separately from userLocale,
  so that localization message files can be made for even minority languages, but the same user that
  chooses to see localized messages in a minority language can also choose a locale that is actually 
  supported by Java for dates and numbers.
*/
//userLocale = this.getLocalesFromQueryOrCookie() ?: [new Locale("ln", "CG"), new Locale("fr", "FR"), new Locale("en", "US")]
//userNumberLocale = new Locale("en", "US")
//userCurrencyLocale = new Locale("en", "US")
//userDateLocale = new Locale("en", "US")

index = {
	list()
}


list = {
	view = "list"
		
	//Get all objects of class ${domainClassName} that match the criteria specified below
	def query = [:]
 	//Add automatic sorting using the sortableColumn from RenderTaglyk
 	if(params.sort){query["sort"] = [(params.order ?: "") + params.sort]}
	//Uncomment the following line to select only records where the domain class' "owner" property is equal to the currently logged in user. Change "owner" to the property you use to identify the user this record belongs to.
 	//query.putAll([filter:["owner==":user]])
	             
	//This query to gets all matching records (keys only) to get the record count for pagination purposes
 	def totalResults = ${domainClassName}.searchKeys(query).size()
 	
 	//This query gets the object-records for this page only
 	//Change perPage to change how many records are displayed per page
	def perPage = 16
	def offset = (params.offset ?: 0) as int
	query.putAll([limit:perPage, offset:offset])
 	def queryResults = ${domainClassName}.search(query)
 	
	//This list of variables will be made available to the view template by GraelykCategory.processController()
 	[${propertyName}List: queryResults, ${propertyName}Total:totalResults, ${propertyName}PerPage:perPage]		
}


//Create shows the form to create a new object. The new object is actually created & persisted to the datastore using the "save" action.
create = {
	view = "create"
	//Todo: verify user has authority to create objects of this type
	//Load params from URL or form variables into your domain class - but only ones that match the domain class' property names
	def ${propertyName} = new ${domainClassName}(this) << params
	
	//This list of variables will be made available to the view template by GraelykCategory.processController()
	[${propertyName}: ${propertyName}]
}


save = {
	//Load params from URL or form variables into your domain class - but only ones that match the domain class' property names
	def ${propertyName} = new ${domainClassName}(this) << params
	//Todo: verify user has authority to view this object

	//Attempt to validate and save.
	if(${propertyName}.store())
	{
		//Validation succeeds
		flash.message = "\${message(code: 'default.created.message', args: [message(code: '${domainClassPropertyName}.label', default: '${domainClassName}'), params.id])}"
		list()
	}
	else
	{
		//Validation fails
		//The create view will be re-displayed
		view = "edit"
		//This list of variables will be made available to the view template by GraelykCategory.processController()
		[${propertyName}: ${propertyName}]
	}
}


show = {
	view = "show"
	def ${propertyName} = ${domainClassName}.fetch(params.id)
	//Todo: verify user has authority to view this object

	if(!${propertyName})
	{
		flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClassPropertyName}.label', default: '${domainClassName}'), params.id])}"
		list()
		//redirect(controllerURL + "/list")
	}
	else
	{
		//This list of variables will be made available to the view template by GraelykCategory.processController()
		[${propertyName}: ${propertyName}]
	}
}


edit = {
	view = "edit"
	def ${propertyName} = ${domainClassName}.fetch(params.id)
	//Todo: verify user has authority to edit this object
	
	if(!${propertyName})
	{
		flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClassPropertyName}.label', default: '${domainClassName}'), params.id])}"
		list()
		//redirect(controllerURL + "/list")
	}
	else
	{
		//This list of variables will be made available to the view template by GraelykCategory.processController()
		[${propertyName}: ${propertyName}]
	}
}


update = {
	//Load the domain object from the datastore, then load params from URL or form variables into your domain class - but only ones that match the domain class' property names
	def ${propertyName} = ${domainClassName}.fetch(params.id) << params
	//Todo: verify user has authority to update this object

	//Attempt to validate and save.
	if(${propertyName}.store())
	{
		//Validation succeeds
		flash.message = "\${message(code: 'default.updated.message', args: [message(code: '${domainClassPropertyName}.label', default: '${domainClassName}'), params.id])}"
		list()
	}
	else
	{
		//Validation fails
		//The edit view will be re-displayed
		view = "edit"
		//This list of variables will be made available to the view template by GraelykCategory.processController()
		[${propertyName}: ${propertyName}]
	}
}


delete = {
	//Deletes an object from class ${domainClassName} with the id passed in on the URL
	//Todo: verify user has authority to delete this object
	${domainClassName}.destroy(params.id)
	list()
}

deleteMultiple = {
	def ${propertyName}List = ${domainClassName}.fetchList(params.id as List)
	${propertyName}List.each{${propertyName}->
		${propertyName}.destroy()
	}
	list()
}

//The deleteAll method is not available by default
/*
deleteAll = {
	def keyList = ${domainClassName}.queryKeys() //Gets the keys for all objects
	dao.delete(keyList) //Deletes all the objects using the list of keys
	list()
}
*/

//The processController method from GraelykCategory runs the appropriate actions and views
this.processController()
