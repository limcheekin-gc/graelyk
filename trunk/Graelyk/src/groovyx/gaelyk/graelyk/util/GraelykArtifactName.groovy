package groovyx.gaelyk.graelyk.util

class GraelykArtifactName
{
	static String instanceSuffix = "Instance"
	static String controllerSuffix = "Controller"
	
	static String domainRoot = "src/graelyk-domains"
	static String viewRoot = "war/graelyk-views"
	static String controllerRoot = "war/WEB-INF/groovy/graelyk-controllers"
	
	def domainClassFullName
	def domainClassName
	def domainClassPackageName
	def domainClassPropertyName
	def domainClassPath
	def domainClassFileName
	
	def classFullName
	def className
	def classPropertyName
	def classPath
	def classFileName
	
	def viewPath
	
	public GraelykArtifactName(domainClassFullName)
	{
		this.domainClassFullName = domainClassFullName
		domainClassName = extractClassName(domainClassFullName)
		domainClassPackageName = extractPackageName(domainClassFullName)
		domainClassPropertyName = extractPropertyName(domainClassFullName)
		domainClassPath = domainRoot + "/" + domainClassPackageName.replaceAll(/\./, "/")
		domainClassFileName = domainClassName + ".groovy"
		
		classFullName = domainClassName + controllerSuffix
		className = domainClassName + controllerSuffix
		classPropertyName = domainClassPropertyName + instanceSuffix
		classPath = controllerRoot
		classFileName = className + ".groovy"
		
		viewPath = viewRoot + "/" + domainClassName
	}

	static String extractClassName(className)
	{
		className.split(/\./).toList().last()
	}
	
	static String extractPackageName(className)
	{
		def packageName = ""
		if(className.contains("."))
		{
			def splitup = className.split(/\./).toList()
			packageName = splitup[0..-2].join(".")
		}
		return packageName
	}
	
	static String extractPropertyName(className)
	{
		def propName = extractClassName(className)
		propName = propName[0].toLowerCase() + propName[1..-1]
	}
}